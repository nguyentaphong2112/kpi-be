/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.kpi.repositories.impl;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Repository;
import vn.kpi.constants.BaseConstants;
import vn.kpi.constants.Constant;
import vn.kpi.constants.Scope;
import vn.kpi.feigns.PermissionFeignClient;
import vn.kpi.models.BaseDataTableDto;
import vn.kpi.models.BaseSearchRequest;
import vn.kpi.models.PermissionDataDto;
import vn.kpi.models.request.AllowanceProcessRequest;
import vn.kpi.models.request.EmployeesRequest;
import vn.kpi.models.response.AllowanceProcessResponse;
import vn.kpi.repositories.BaseRepository;
import vn.kpi.services.AuthorizationService;
import vn.kpi.utils.QueryUtils;
import vn.kpi.utils.Utils;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Lop repository Impl ung voi bang hr_allowance_process
 *
 * @author tudd
 * @version 1.0
 * @since 1.0
 */

@Repository
@RequiredArgsConstructor
public class AllowanceProcessRepository extends BaseRepository {
    private final PermissionFeignClient permissionFeignClient;
    private final HttpServletRequest request;
    private final AuthorizationService authorizationService;

    public BaseDataTableDto<AllowanceProcessResponse.SearchResult> searchData(EmployeesRequest.SearchForm dto) {
        Pair<String, Map<String, Object>> pair = buildSql(dto);
        return getListPagination(pair.getLeft(), pair.getRight(), dto, AllowanceProcessResponse.SearchResult.class);
    }

    private Pair<String, Map<String, Object>> buildSql(EmployeesRequest.SearchForm dto) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    a.allowance_process_id,
                    a.employee_id,
                    e.employee_code,
                    e.full_name,
                    a.start_date,
                    a.end_date,
                    a.allowance_type_id,
                    a.amount,
                    a.document_no,
                    a.document_signed_date,
                    a.created_by,
                    a.created_time,
                    a.modified_by,
                    a.modified_time,
                    a.last_update_time,
                    NVL(o.full_name, o.name) orgName,
                    mj.name jobName,
                    (select et.name from hr_emp_types et where et.emp_type_id = e.emp_type_id) empTypeName,
                    (select sc.name from sys_categories sc where sc.value = e.status and sc.category_type = :empStatus) empStatusName,
                    (select sc.name from sys_categories sc where sc.value = a.allowance_type_id and sc.category_type = :allowanceTypeCode) allowanceTypeName
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
                    FROM hr_allowance_process a
                    JOIN hr_employees e ON (e.employee_id = a.employee_id AND NVL(e.is_deleted, :activeStatus) = :activeStatus)
                    LEFT JOIN hr_jobs mj ON mj.job_id = e.job_id
                    JOIN hr_organizations o ON o.organization_id = e.organization_id
                    WHERE a.is_deleted = :activeStatus
                """);
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        params.put("empStatus", Constant.CATEGORY_CODES.HR_TRANG_THAI_NHAN_VIEN);
        params.put("allowanceTypeCode", Constant.CATEGORY_CODES.LOAI_PHU_CAP);
        UtilsEmployeeSearchRepository.setParamEmployeeSearch(dto, sql, params);
        QueryUtils.filter(dto.getDocumentNo(), sql, params, "a.document_no");
        QueryUtils.filter(dto.getListAllowanceType(), sql, params, "a.allowance_type_id");
        if (!Utils.isNullOrEmpty(dto.getListDocumentSignedDate())) {
            QueryUtils.filterGe(Utils.stringToDate(dto.getListDocumentSignedDate().get(0)), sql, params, "a.document_signed_date", "fromDocumentSignedDate");
            QueryUtils.filterLe(dto.getListDocumentSignedDate().size() > 1 ? Utils.stringToDate(dto.getListDocumentSignedDate().get(1)) : null, sql, params, "a.document_signed_date", "toDocumentSignedDate");
        }
        if (!Utils.isNullOrEmpty(dto.getListStartDate())) {
            QueryUtils.filterGe(Utils.stringToDate(dto.getListStartDate().get(0)), sql, params, "a.start_date", "startDate");
            QueryUtils.filterLe(dto.getListStartDate().size() > 1 ? Utils.stringToDate(dto.getListStartDate().get(1)) : null, sql, params, "a.start_date", "endDate");
        }
        List<PermissionDataDto> permissionDataDtos = authorizationService.getPermissionData(
                Scope.VIEW, Constant.RESOURCES.ALLOWANCE_PROCESS, Utils.getUserNameLogin()
        );
        QueryUtils.addConditionPermission(permissionDataDtos, sql, params);
        sql.append(" ORDER BY e.employee_id, a.start_date DESC");
    }

    public boolean isConflictProcess(AllowanceProcessRequest.SubmitForm inputDTO, Long employeeId, Long id) {
        StringBuilder sql = new StringBuilder("""
                    SELECT ap.start_date, ap.end_date
                    FROM hr_allowance_process ap
                    WHERE ap.employee_id = :employeeId
                    AND ap.allowance_process_id != :allowanceProcessId
                    AND ap.allowance_type_id = :allowanceTypeId
                    AND NVL(ap.is_deleted, :activeStatus) = :activeStatus
                    AND (ap.end_date IS NULL OR :startDate <= ap.end_date)
                """);
        HashMap<String, Object> params = new HashMap<>();
        if (inputDTO.getEndDate() != null) {
            sql.append(" AND ap.start_date <= :endDate");
            params.put("endDate", inputDTO.getEndDate());
        }

        params.put("employeeId", employeeId);
        params.put("allowanceTypeId", inputDTO.getAllowanceTypeId());
        params.put("allowanceProcessId", Utils.NVL(id, 0L));
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        params.put("startDate", inputDTO.getStartDate());

        List<AllowanceProcessResponse.DetailBean> listData = getListData(sql.toString(), params, AllowanceProcessResponse.DetailBean.class);
        if (Utils.isNullOrEmpty(listData)) {
            return false;
        } else if (listData.size() > 1) {
            return true;
        } else {
            AllowanceProcessResponse.DetailBean dto = listData.get(0);
            return dto.getEndDate() != null || !dto.getStartDate().before(inputDTO.getStartDate());
        }
    }

    public void autoUpdateToDate(Long employeeId, String allowanceTypeId) {
        String sql = """
                    update hr_allowance_process ap
                    set ap.end_date = (select min(date_add(ap1.start_date, interval -1 day))
                        from hr_allowance_process ap1
                       where ap1.start_date > ap.start_date
                       and ap1.employee_id = ap.employee_id
                       and ap1.allowance_type_id = ap.allowance_type_id
                       and (ap.end_date is null or ap.end_date >= ap1.start_date)
                       and ap1.is_deleted = :activeStatus
                    )
                    where ap.employee_id = :employeeId
                    and ap.is_deleted = :activeStatus
                    and ap.allowance_type_id = :allowanceTypeId
                    and exists (select ap1.start_date from hr_allowance_process ap1
                        where ap1.start_date > ap.start_date
                        and ap1.employee_id = ap.employee_id
                        and ap1.allowance_type_id = ap.allowance_type_id
                        and (ap.end_date is null or ap.end_date >= ap1.start_date)
                    )
                """;
        HashMap<String, Object> params = new HashMap<>();
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        params.put("allowanceTypeId", allowanceTypeId);
        params.put("employeeId", employeeId);
        executeSqlDatabase(sql, params);
    }

    public AllowanceProcessResponse.DetailBean getDataById(Long allowanceProcessId) {
        String sql = """
                    SELECT ap.*, e.employee_code, e.full_name
                    FROM hr_allowance_process ap
                    JOIN hr_employees e on e.employee_id = ap.employee_id
                    WHERE NVL(ap.is_deleted, :activeStatus) = :activeStatus
                    AND ap.allowance_process_id = :allowanceProcessId
                """;
        HashMap<String, Object> params = new HashMap<>();
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        params.put("allowanceProcessId", allowanceProcessId);
        return queryForObject(sql, params, AllowanceProcessResponse.DetailBean.class);
    }

    public BaseDataTableDto<AllowanceProcessResponse.SearchResult> getTableList(Long employeeId, BaseSearchRequest request) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    a.allowance_process_id,
                    a.employee_id,
                    a.start_date,
                    a.end_date,
                    a.allowance_type_id,
                    a.amount,
                    a.document_no,
                    a.document_signed_date,
                    a.created_by,
                    a.created_time,
                    a.modified_by,
                    a.modified_time,
                    (select sc.name from sys_categories sc where sc.value = a.allowance_type_id and sc.category_type = :allowanceTypeCode) allowanceTypeName
                    FROM hr_allowance_process a
                    WHERE a.is_deleted = :activeStatus
                    and a.employee_id = :employeeId
                    order by a.start_date DESC
                """);
        HashMap<String, Object> params = new HashMap<>();
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        params.put("employeeId", employeeId);
        params.put("allowanceTypeCode", Constant.CATEGORY_CODES.LOAI_PHU_CAP);
        return getListPagination(sql.toString(), params, request, AllowanceProcessResponse.SearchResult.class);

    }

    public List<AllowanceProcessResponse.DetailBean> getListCurrent(Long employeeId) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    a.allowance_process_id,
                    a.employee_id,
                    a.start_date,
                    a.end_date,
                    a.allowance_type_id,
                    a.amount,
                    sc.name allowanceTypeName
                    FROM hr_allowance_process a, sys_categories sc
                    WHERE a.is_deleted = :activeStatus
                    and a.employee_id = :employeeId
                    and DATE(now()) between a.start_date and ifnull(a.end_date, now())
                    and sc.value = a.allowance_type_id 
                    and sc.category_type = :allowanceTypeCode
                    and exists (
                        select 1 from sys_category_attributes st
                        where st.category_id = sc.category_id
                        and st.attribute_code = 'IS_SHOW_COMMON_INFO'
                        and st.attribute_value = 'Y'
                        and st.is_deleted = 'N' 
                    )
                    order by sc.order_number, sc.name
                """);
        HashMap<String, Object> params = new HashMap<>();
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        params.put("employeeId", employeeId);
        params.put("allowanceTypeCode", Constant.CATEGORY_CODES.LOAI_PHU_CAP);
        return getListData(sql.toString(), params, AllowanceProcessResponse.DetailBean.class);

    }
}
