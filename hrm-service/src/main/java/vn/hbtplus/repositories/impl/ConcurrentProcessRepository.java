/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.repositories.impl;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Repository;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.constants.Constant;
import vn.hbtplus.constants.Scope;
import vn.hbtplus.feigns.PermissionFeignClient;
import vn.hbtplus.models.BaseDataTableDto;
import vn.hbtplus.models.BaseSearchRequest;
import vn.hbtplus.models.PermissionDataDto;
import vn.hbtplus.models.dto.ConcurrentProcessDto;
import vn.hbtplus.models.request.ConcurrentProcessRequest;
import vn.hbtplus.models.request.EmployeesRequest;
import vn.hbtplus.models.response.ConcurrentProcessResponse;
import vn.hbtplus.repositories.BaseRepository;
import vn.hbtplus.services.AuthorizationService;
import vn.hbtplus.utils.QueryUtils;
import vn.hbtplus.utils.Utils;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Lop repository Impl ung voi bang hr_concurrent_process
 *
 * @author tudd
 * @version 1.0
 * @since 1.0
 */

@Repository
@RequiredArgsConstructor
public class ConcurrentProcessRepository extends BaseRepository {
    private final PermissionFeignClient permissionFeignClient;
    private final HttpServletRequest request;
    private final AuthorizationService authorizationService;

    public BaseDataTableDto<ConcurrentProcessResponse.SearchResult> searchData(EmployeesRequest.SearchForm dto) {
        Pair<String, Map<String, Object>> pair = buildSql(dto);
        return getListPagination(pair.getLeft(), pair.getRight(), dto, ConcurrentProcessResponse.SearchResult.class);
    }

    private Pair<String, Map<String, Object>> buildSql(EmployeesRequest.SearchForm dto) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    a.concurrent_process_id,
                    a.employee_id,
                    e.employee_code,
                    e.full_name,
                    a.start_date,
                    a.end_date,
                    a.job_id,
                    a.position_id,
                    a.organization_id,
                    a.document_no,
                    a.document_signed_date,
                    a.created_by,
                    a.created_time,
                    a.modified_by,
                    a.modified_time,
                    a.last_update_time,
                    NVL(o.full_name, o.name) orgName,
                    hj.name jobName,
                    NVL(ho.full_name, o.name) concurrentOrg,
                    mj.name concurrentJob,
                    (select et.name from hr_emp_types et where et.emp_type_id = e.emp_type_id) empTypeName,
                    (select sc.name from sys_categories sc where sc.value = e.status and sc.category_type = :empStatus) empStatusName
                """);
        HashMap<String, Object> params = new HashMap<>();
        addCondition(sql, params, dto);

        return new MutablePair<>(sql.toString(), params);
    }

    public List<Map<String, Object>> getListExport(EmployeesRequest.SearchForm dto) {
        Pair<String, Map<String, Object>> pair = buildSql(dto);
        List<Map<String, Object>> dataList = getListData(pair.getLeft(), pair.getRight());
        if (Utils.isNullOrEmpty(dataList)) {
            dataList.add(getMapEmptyAliasColumns(pair.getLeft()));
        }
        return dataList;
    }

    private void addCondition(StringBuilder sql, Map<String, Object> params, EmployeesRequest.SearchForm dto) {
        sql.append("""
                    FROM hr_concurrent_process a
                    JOIN hr_employees e ON (e.employee_id = a.employee_id AND NVL(e.is_deleted, :activeStatus) = :activeStatus)
                    LEFT JOIN hr_jobs mj ON mj.job_id = a.job_id
                    LEFT JOIN hr_jobs hj ON hj.job_id = e.job_id
                    JOIN hr_organizations o ON o.organization_id = e.organization_id
                    JOIN hr_organizations ho ON ho.organization_id = a.organization_id
                    WHERE NVL(a.is_deleted, :activeStatus) = :activeStatus
                """);
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        params.put("empStatus", Constant.CATEGORY_CODES.HR_TRANG_THAI_NHAN_VIEN);
        UtilsEmployeeSearchRepository.setParamEmployeeSearch(dto, sql, params);
        List<PermissionDataDto> permissionDataDtos = authorizationService.getPermissionData(
                Scope.VIEW, Constant.RESOURCES.CONCURRENT_PROCESS, Utils.getUserNameLogin()
        );

        if (!Utils.isNullOrEmpty(dto.getListStartDate())) {
            QueryUtils.filterGe(Utils.stringToDate(dto.getListStartDate().get(0)), sql, params, "a.start_date", "startDate");
            QueryUtils.filterLe(dto.getListStartDate().size() > 1 ? Utils.stringToDate(dto.getListStartDate().get(1)) : null, sql, params, "a.start_date", "endDate");
        }

        QueryUtils.addConditionPermission(permissionDataDtos, sql, params);
        sql.append(" ORDER BY e.employee_id, a.start_date DESC");
    }

    public boolean checkDuplicate(ConcurrentProcessRequest.SubmitForm dto, Long employeeId, Long id) {
        Map<String, Object> mapParams = new HashMap<>();
        StringBuilder sql = new StringBuilder("""
                    SELECT COUNT(1)
                    FROM hr_concurrent_process cp
                        WHERE NVL(cp.is_deleted, :activeStatus) = :activeStatus
                        AND cp.employee_id = :employeeId
                        AND NVL(cp.end_date,:startDate) >= :startDate
                        AND cp.end_date <= NVL(:endDate, cp.end_date)
                        AND cp.concurrent_process_id != :concurrentProcessId
                """);
        QueryUtils.filter(dto.getOrganizationId(), sql, mapParams, "cp.organization_id");
        QueryUtils.filter(dto.getPositionId(), sql, mapParams, "cp.position_id");
        mapParams.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        mapParams.put("employeeId", employeeId);
        mapParams.put("startDate", dto.getStartDate());
        mapParams.put("endDate", dto.getEndDate());
        mapParams.put("concurrentProcessId", Utils.NVL(id));
        return queryForObject(sql.toString(), mapParams, Integer.class) > 0;
    }

    public BaseDataTableDto<ConcurrentProcessResponse.DetailBean> getTableList(Long employeeId, BaseSearchRequest request) {
        String sql = """
                SELECT
                    a.concurrent_process_id,
                    a.employee_id,
                    a.start_date,
                    a.end_date,
                    a.job_id,
                    a.position_id,
                    a.organization_id,
                    a.document_no,
                    a.document_signed_date,
                    a.created_by,
                    a.created_time,
                    a.modified_by,
                    a.modified_time,
                    a.last_update_time,
                    NVL(ho.full_name, ho.name) AS concurrentOrg,
                    mj.name AS concurrentJob,
                    NVL(o.full_name, o.name) AS orgName,
                    (select sc.name from sys_categories sc where sc.value = e.status and sc.category_type = :empStatus) empStatusName,
                    hj.name AS jobName
                FROM
                    hr_concurrent_process a
                    JOIN hr_employees e ON e.employee_id = a.employee_id
                    LEFT JOIN hr_jobs mj ON mj.job_id = a.job_id
                    LEFT JOIN hr_jobs hj ON hj.job_id = e.job_id
                    JOIN hr_organizations o ON o.organization_id = e.organization_id
                    JOIN hr_organizations ho ON ho.organization_id = a.organization_id
                WHERE
                    a.is_deleted = :activeStatus
                    AND a.employee_id = :employeeId
                ORDER BY
                    a.start_date
                """;
        HashMap<String, Object> params = new HashMap<>();
        params.put("employeeId", employeeId);
        params.put("empStatus", Constant.CATEGORY_CODES.HR_TRANG_THAI_NHAN_VIEN);
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        return getListPagination(sql, params, request, ConcurrentProcessResponse.DetailBean.class);
    }

    public List<ConcurrentProcessDto> getListProcess(Long employeeId, Date date) {
        String sql = """
                    select mj.name as jobName,
                        ho.name as organizationName
                    FROM hr_concurrent_process a
                    LEFT JOIN hr_jobs mj ON mj.job_id = a.job_id
                    JOIN hr_organizations ho ON ho.organization_id = a.organization_id
                WHERE a.is_deleted = :activeStatus
                    and a.employee_id = :employeeId
                    and :date between a.start_date and ifnull(a.end_date,:date)
                    order by mj.order_number
                    """;
        HashMap<String, Object> params = new HashMap<>();
        params.put("date", Utils.truncDate(date));
        params.put("employeeId", employeeId);
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        return getListData(sql, params, ConcurrentProcessDto.class);
    }
}
