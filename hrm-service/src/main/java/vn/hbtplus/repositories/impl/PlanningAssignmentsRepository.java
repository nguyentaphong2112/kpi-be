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
import vn.hbtplus.models.response.PlanningAssignmentsResponse;
import vn.hbtplus.repositories.BaseRepository;
import vn.hbtplus.repositories.entity.PlanningAssignmentsEntity;
import vn.hbtplus.services.AuthorizationService;
import vn.hbtplus.utils.QueryUtils;
import vn.hbtplus.utils.Utils;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Lop repository Impl ung voi bang hr_planning_assignments
 *
 * @author tudd
 * @version 1.0
 * @since 1.0
 */

@Repository
@RequiredArgsConstructor
public class PlanningAssignmentsRepository extends BaseRepository {
    private final PermissionFeignClient permissionFeignClient;
    private final HttpServletRequest request;
    private final AuthorizationService authorizationService;

    public BaseDataTableDto searchData(EmployeesRequest.SearchForm dto) {
        Pair<String, Map<String, Object>> pair = buildSql(dto);
        return getListPagination(pair.getLeft(), pair.getRight(), dto, PlanningAssignmentsResponse.SearchResult.class);
    }

    private Pair<String, Map<String, Object>> buildSql(EmployeesRequest.SearchForm dto) {
        StringBuilder sql = new StringBuilder("""
                 SELECT
                    a.planning_assignment_id,
                    a.employee_id,
                    e.employee_code,
                    e.full_name,
                    a.start_date,
                    a.end_date,
                    a.document_no,
                    a.document_signed_date,
                    a.end_document_no,
                    a.end_document_signed_date,
                    mj.name jobName,
                    NVL(o.full_name, o.name) orgName,
                    (select et.name from hr_emp_types et where et.emp_type_id = e.emp_type_id) empTypeName,
                    (select sc.name from sys_categories sc where sc.value = e.status and sc.category_type = :empStatus) empStatusName,
                    (select sc.name from sys_categories sc where sc.value = a.planning_period_id and sc.category_type = :planningPeriod) planningPeriodName,
                    (select sc.name from sys_categories sc where sc.value = a.position_id and sc.category_type = :position) positionName,
                    (select sc.name from sys_categories sc where sc.value = a.end_reason_id and sc.category_type = :endReason) endReasonName,
                    a.created_by,
                    a.created_time,
                    a.modified_by,
                    a.modified_time,
                    a.last_update_time
                """);
        HashMap<String, Object> params = new HashMap<>();
        addCondition(sql, params, dto);
        return new MutablePair<>(sql.toString(), params);
    }

    public List<Map<String, Object>> getListExport(EmployeesRequest.SearchForm dto) {
        StringBuilder sql = new StringBuilder("""
                 SELECT
                    a.planning_assignment_id,
                    a.employee_id,
                    e.employee_code,
                    e.full_name,
                    a.start_date,
                    a.end_date,
                    a.document_no,
                    a.document_signed_date,
                    a.end_document_no,
                    a.end_document_signed_date,
                    mj.name jobName,
                    NVL(o.full_name, o.name) orgName,
                    (select et.name from hr_emp_types et where et.emp_type_id = e.emp_type_id) empTypeName,
                    (select sc.name from sys_categories sc where sc.value = e.status and sc.category_type = :empStatus) empStatusName,
                    (select sc.name from sys_categories sc where sc.value = a.planning_period_id and sc.category_type = :planningPeriod) planningPeriodName,
                    (select sc.name from sys_categories sc where sc.value = a.position_id and sc.category_type = :position) positionName,
                    (select sc.name from sys_categories sc where sc.value = a.end_reason_id and sc.category_type = :endReason) endReasonName,
                    a.created_by,
                    a.created_time,
                    a.modified_by,
                    a.modified_time,
                    a.last_update_time
                """);
        Map<String, Object> params = new HashMap<>();
        addCondition(sql, params, dto);
        return getListData(sql.toString(), params);
    }

    private void addCondition(StringBuilder sql, Map<String, Object> params, EmployeesRequest.SearchForm dto) {
        sql.append("""
                    FROM hr_planning_assignments a
                    JOIN hr_employees e ON e.employee_id = a.employee_id
                    LEFT JOIN hr_jobs mj ON mj.job_id = e.job_id
                    JOIN hr_organizations o ON o.organization_id = e.organization_id
                    WHERE IFNULL(a.is_deleted, :activeStatus) = :activeStatus
                """);
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        params.put("planningPeriod", Constant.CATEGORY_CODES.HR_GIAI_DOAN_QUY_HOACH);
        params.put("position", Constant.CATEGORY_CODES.HR_CHUC_VU_QUY_HOACH);
        params.put("endReason", Constant.CATEGORY_CODES.HR_LY_DO_RA_QUY_HOACH);
        params.put("empStatus", Constant.CATEGORY_CODES.HR_TRANG_THAI_NHAN_VIEN);
        UtilsEmployeeSearchRepository.setParamEmployeeSearch(dto, sql, params);
        List<PermissionDataDto> permissionDataDtos = authorizationService.getPermissionData(
                Scope.VIEW, Constant.RESOURCES.PLANNING_ASSIGNMENTS, Utils.getUserNameLogin()
        );
        QueryUtils.addConditionPermission(permissionDataDtos, sql, params);
        sql.append(" ORDER BY e.employee_id");
    }

    public BaseDataTableDto<PlanningAssignmentsResponse.SearchResult> getTableList(Long employeeId, BaseSearchRequest request) {
        StringBuilder sql = new StringBuilder("""
                    SELECT
                        a.planning_assignment_id,
                        a.employee_id,
                        a.start_date,
                        a.end_date,
                        a.document_no,
                        a.document_signed_date,
                        a.end_document_no,
                        a.end_document_signed_date,
                        (select sc.name from sys_categories sc where sc.value = a.planning_period_id and sc.category_type = :planningPeriod) planningPeriodName,
                        (select sc.name from sys_categories sc where sc.value = a.position_id and sc.category_type = :position) positionName,
                        (select sc.name from sys_categories sc where sc.value = a.end_reason_id and sc.category_type = :endReason) endReasonName,
                        a.created_by,
                        a.created_time,
                        a.modified_by,
                        a.modified_time,
                        a.last_update_time
                    FROM hr_planning_assignments a
                    WHERE a.is_deleted = :activeStatus
                    and a.employee_id = :employeeId
                    order by a.document_signed_date desc
                    """);
        HashMap<String, Object> params = new HashMap<>();
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        params.put("planningPeriod", Constant.CATEGORY_CODES.HR_GIAI_DOAN_QUY_HOACH);
        params.put("position", Constant.CATEGORY_CODES.HR_CHUC_VU_QUY_HOACH);
        params.put("endReason", Constant.CATEGORY_CODES.HR_LY_DO_RA_QUY_HOACH);
        params.put("employeeId", employeeId);
        return getListPagination(sql.toString(), params, request, PlanningAssignmentsResponse.SearchResult.class);
    }

    public List<PlanningAssignmentsEntity> getListPlanningAssignments(List<String> empCodeList) {
        String sql = """
                select a.* from hr_planning_assignments a, hr_employees e 
                where a.is_deleted = :activeStatus
                and a.employee_id = e.employee_id
                and e.employee_code in (:empCodeList)
                """;
        HashMap<String, Object> params = new HashMap<>();
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        params.put("empCodeList", empCodeList);
        return getListData(sql, params, PlanningAssignmentsEntity.class);
    }
}
