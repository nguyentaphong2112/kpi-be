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
import vn.hbtplus.models.request.EmployeesRequest;
import vn.hbtplus.models.response.WorkedHistoriesResponse;
import vn.hbtplus.repositories.BaseRepository;
import vn.hbtplus.services.AuthorizationService;
import vn.hbtplus.utils.QueryUtils;
import vn.hbtplus.utils.Utils;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Lop repository Impl ung voi bang hr_worked_histories
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Repository
@RequiredArgsConstructor
public class WorkedHistoriesRepository extends BaseRepository {
    private final PermissionFeignClient permissionFeignClient;
    private final HttpServletRequest request;
    private final AuthorizationService authorizationService;

    public BaseDataTableDto<WorkedHistoriesResponse.SearchResult> searchData(EmployeesRequest.SearchForm dto) {
        Pair<String, Map<String, Object>> pair = buildSql(dto);
        return getListPagination(pair.getLeft(), pair.getRight(), dto, WorkedHistoriesResponse.SearchResult.class);
    }

    private Pair<String, Map<String, Object>> buildSql(EmployeesRequest.SearchForm dto) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    a.worked_history_id,
                    a.employee_id,
                    e.employee_code,
                    e.full_name,
                    a.start_date,
                    a.end_date,
                    date_format(a.start_date, '%m/%Y') from_month,
                    date_format(a.end_date, '%m/%Y') to_month,
                    a.job,
                    a.company_name,
                    a.reference_name,
                    a.reference_job,
                    a.created_by,
                    a.created_time,
                    a.modified_by,
                    a.modified_time,
                    a.last_update_time,
                    NVL(o.full_name, o.name) orgName,
                    mj.name jobName,
                    (select sc.name from sys_categories sc where sc.value = e.status and sc.category_type = :empStatus) empStatusName,
                    (select et.name from hr_emp_types et where et.emp_type_id = e.emp_type_id) empTypeName
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
            FROM hr_worked_histories a
            JOIN hr_employees e ON (e.employee_id = a.employee_id AND NVL(e.is_deleted, :activeStatus) = :activeStatus)
            LEFT JOIN hr_jobs mj ON mj.job_id = e.job_id
            JOIN hr_organizations o ON o.organization_id = e.organization_id
            WHERE NVL(a.is_deleted, :activeStatus) = :activeStatus
        """);
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        params.put("empStatus", Constant.CATEGORY_CODES.HR_TRANG_THAI_NHAN_VIEN);
        UtilsEmployeeSearchRepository.setParamEmployeeSearch(dto, sql, params);
        QueryUtils.filter(dto.getCompanyName(), sql, params, "a.company_name");
        QueryUtils.filter(dto.getJob(), sql, params, "a.job");
        List<PermissionDataDto> permissionDataDtos = authorizationService.getPermissionData(
                Scope.VIEW, Constant.RESOURCES.WORKED_HISTORIES, Utils.getUserNameLogin()
        );
        QueryUtils.addConditionPermission(permissionDataDtos, sql, params);
        sql.append(" ORDER BY e.employee_id, a.start_date DESC");
    }

    public WorkedHistoriesResponse.DetailBean getDataById(Long workedHistoryId) {
        String sql = """
                    SELECT wh.*, e.employee_code, e.full_name employeeName
                    FROM hr_worked_histories wh
                    JOIN hr_employees e on e.employee_id = wh.employee_id
                    WHERE NVL(wh.is_deleted, :activeStatus) = :activeStatus
                    AND wh.worked_history_id = :workedHistoryId
                """;
        HashMap<String, Object> params = new HashMap<>();
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        params.put("workedHistoryId", workedHistoryId);
        return queryForObject(sql, params, WorkedHistoriesResponse.DetailBean.class);
    }

    public BaseDataTableDto<WorkedHistoriesResponse.DetailBean> getTableList(Long employeeId, BaseSearchRequest request) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    a.worked_history_id,
                    a.employee_id ,
                    a.start_date  ,
                    a.end_date  ,
                    a.job,
                    a.company_name,
                    a.reference_name,
                    a.reference_job,
                    a.created_by,
                    a.created_time,
                    a.modified_by,
                    a.modified_time
                FROM hr_worked_histories a
                WHERE a.is_deleted = :activeStatus
                and a.employee_id = :employeeId
                order by a.start_date
                """);
        HashMap<String, Object> params = new HashMap<>();
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        params.put("empStatus", Constant.CATEGORY_CODES.HR_TRANG_THAI_NHAN_VIEN);
        params.put("employeeId", employeeId);
        return getListPagination(sql.toString(), params, request, WorkedHistoriesResponse.DetailBean.class);
    }
}
