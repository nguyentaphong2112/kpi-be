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
import vn.hbtplus.models.response.EducationCertificatesResponse;
import vn.hbtplus.models.response.EducationProcessResponse;
import vn.hbtplus.repositories.BaseRepository;
import vn.hbtplus.services.AuthorizationService;
import vn.hbtplus.utils.QueryUtils;
import vn.hbtplus.utils.Utils;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Lop repository Impl ung voi bang hr_education_process
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Repository
@RequiredArgsConstructor
public class EducationProcessRepository extends BaseRepository {
    private final PermissionFeignClient permissionFeignClient;
    private final HttpServletRequest request;
    private final AuthorizationService authorizationService;

    public BaseDataTableDto<EducationProcessResponse.SearchResult> searchData(EmployeesRequest.SearchForm dto) {
        Pair<String, Map<String, Object>> pair = buildSql(dto);
        return getListPagination(pair.getLeft(), pair.getRight(), dto, EducationProcessResponse.SearchResult.class);
    }

    private Pair<String, Map<String, Object>> buildSql(EmployeesRequest.SearchForm dto) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    a.education_process_id,
                    a.employee_id,
                    e.employee_code,
                    e.full_name,
                    a.course_name,
                    a.training_method_id,
                    a.course_content,
                    a.result,
                    a.created_by,
                    a.created_time,
                    a.modified_by,
                    a.modified_time,
                    a.last_update_time,
                    a.training_method_place,
                    DATE_FORMAT(a.start_date, '%m/%Y') startDate,
                    DATE_FORMAT(a.end_date, '%m/%Y') endDate,
                    NVL(o.full_name, o.name) orgName,
                    mj.name jobName,
                    (select et.name from hr_emp_types et where et.emp_type_id = e.emp_type_id) empTypeName,
                    (select sc.name from sys_categories sc where sc.value = e.status and sc.category_type = :empStatus) empStatusName,
                    (select sc.name from sys_categories sc where sc.value = a.training_method_id and sc.category_type = :trainingMethod) trainingMethodName
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
            FROM hr_education_process a
            JOIN hr_employees e ON (e.employee_id = a.employee_id AND NVL(e.is_deleted, :activeStatus) = :activeStatus)
            LEFT JOIN hr_jobs mj ON mj.job_id = e.job_id
            JOIN hr_organizations o ON o.organization_id = e.organization_id
            WHERE NVL(a.is_deleted, :activeStatus) = :activeStatus
        """);
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        params.put("empStatus", Constant.CATEGORY_CODES.HR_TRANG_THAI_NHAN_VIEN);
        params.put("trainingMethod", Constant.CATEGORY_CODES.HINH_THUC_DAO_TAO);
        UtilsEmployeeSearchRepository.setParamEmployeeSearch(dto, sql, params);
        QueryUtils.filter(dto.getListTrainingMethod(), sql, params, "a.training_method_id");
        QueryUtils.filter(dto.getCourseName(), sql, params, "a.course_name");
        QueryUtils.filter(dto.getResult(), sql, params, "a.result");
        List<PermissionDataDto> permissionDataDtos = authorizationService.getPermissionData(
                Scope.VIEW, Constant.RESOURCES.EDUCATION_PROCESS, Utils.getUserNameLogin()
        );
        QueryUtils.addConditionPermission(permissionDataDtos, sql, params);
        sql.append(" ORDER BY e.employee_id, a.start_date DESC");
    }

    public BaseDataTableDto<EducationProcessResponse.DetailBean> getTableList(Long employeeId, BaseSearchRequest request) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    a.education_process_id,
                    a.employee_id,
                    a.start_date,
                    a.end_date,
                    a.course_name,
                    a.training_method_id,
                    a.course_content,
                    a.result,
                    a.training_method_place,
                    a.created_by,
                    a.created_time,
                    a.modified_by,
                    a.modified_time,
                    (select sc.name from sys_categories sc where sc.value = a.training_method_id and sc.category_type = :trainingMethod) trainingMethodName
                    FROM hr_education_process a
                    WHERE a.is_deleted = :activeStatus
                    AND a.employee_id  = :employeeId
                    order by a.start_date
                """);
        HashMap<String, Object> params = new HashMap<>();
        params.put("trainingMethod", Constant.CATEGORY_CODES.HINH_THUC_DAO_TAO);
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        params.put("employeeId", employeeId);
        return getListPagination(sql.toString(), params, request, EducationProcessResponse.DetailBean.class);

    }
}
