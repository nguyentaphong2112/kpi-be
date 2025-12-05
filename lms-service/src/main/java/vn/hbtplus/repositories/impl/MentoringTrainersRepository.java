/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.repositories.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.constants.Constant;
import vn.hbtplus.models.BaseDataTableDto;
import vn.hbtplus.models.dto.CategoryDto;
import vn.hbtplus.models.dto.EmployeeDto;
import vn.hbtplus.models.request.MentoringTrainersRequest;
import vn.hbtplus.models.response.MentoringTrainersResponse;
import vn.hbtplus.repositories.BaseRepository;
import vn.hbtplus.utils.QueryUtils;
import vn.hbtplus.utils.Utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Lop repository Impl ung voi bang lms_mentoring_trainers
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Repository
@RequiredArgsConstructor
public class MentoringTrainersRepository extends BaseRepository {

    public BaseDataTableDto searchData(MentoringTrainersRequest.SearchForm dto) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    a.mentoring_trainer_id,
                    a.employee_id,
                    e.employee_code,
                    e.full_name,
                    a.start_date,
                    a.end_date,
                    a.major_id,
                    a.hospital_id,
                    o.name orgName,
                    (SELECT sc.name FROM sys_categories sc WHERE sc.category_type = :roleType and sc.value = a.role_id) roleName,
                    (SELECT sc.name FROM sys_categories sc WHERE sc.category_type = :majorType and sc.value = a.major_id) majorName,
                    (SELECT sc.name FROM sys_categories sc WHERE sc.category_type = :hospitalType and sc.value = a.hospital_id) hospitalName,
                    a.content,
                    a.class_name,
                    a.total_lessons,
                    a.total_classes,
                    a.total_students,
                    a.total_examinations,
                    a.total_surgeries,
                    a.total_tests,
                    a.role_id,
                    a.is_deleted,
                    a.created_by,
                    a.created_time,
                    a.modified_by,
                    a.modified_time,
                    a.last_update_time
                """);
        HashMap<String, Object> params = new HashMap<>();
        addCondition(sql, params, dto);
        return getListPagination(sql.toString(), params, dto, MentoringTrainersResponse.class);
    }

    public List<Map<String, Object>> getListExport(MentoringTrainersRequest.SearchForm dto) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    a.mentoring_trainer_id,
                    a.employee_id,
                    e.employee_code,
                    e.full_name,
                    a.role_id,
                    a.start_date,
                    a.end_date,
                    a.major_id,
                    a.hospital_id,
                    o.name orgName,
                    (SELECT sc.name FROM sys_categories sc WHERE sc.category_type = :roleType and sc.value = a.role_id) roleName,
                    (SELECT sc.name FROM sys_categories sc WHERE sc.category_type = :majorType and sc.value = a.major_id) majorName,
                    (SELECT sc.name FROM sys_categories sc WHERE sc.category_type = :hospitalType and sc.value = a.hospital_id) hospitalName,
                    a.content,
                    a.class_name,
                    a.total_lessons,
                    a.total_classes,
                    a.total_students,
                    a.total_examinations,
                    a.total_surgeries,
                    a.total_tests,
                    a.is_deleted,
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

    private void addCondition(StringBuilder sql, Map<String, Object> params, MentoringTrainersRequest.SearchForm dto) {
        sql.append("""
            FROM lms_mentoring_trainers a
            JOIN hr_employees e ON e.employee_id = a.employee_id
            LEFT JOIN hr_jobs mj ON mj.job_id = e.job_id
            JOIN hr_organizations o ON o.organization_id = e.organization_id
            WHERE IFNULL(a.is_deleted, :activeStatus) = :activeStatus
        """);
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        params.put("roleType", Constant.CATEGORY_CODES.CDT_VAI_TRO);
        params.put("majorType", Constant.CATEGORY_CODES.CDT_CHUYEN_MON);
        params.put("hospitalType", Constant.CATEGORY_CODES.CDT_BENH_VIEN);

        QueryUtils.filter(dto.getKeySearch(), sql, params, "a.full_name", "a.employee_code");

        if (!Utils.isNullObject(dto.getOrgId())) {
            sql.append(" and o.path_id LIKE :orgId");
            params.put("orgId", "%/" + dto.getOrgId() + "/%");
        }

        if (dto.getStartDate() != null && dto.getEndDate() != null) {
            if (dto.getStartDate().equals(dto.getEndDate())) {
                sql.append(" AND a.start_date = :startDate AND a.end_date = :endDate");
                params.put("startDate", dto.getStartDate());
                params.put("endDate", dto.getStartDate());
            } else {
                sql.append(" AND a.start_date >= :startDate AND a.end_date <= :endDate");
                params.put("startDate", dto.getStartDate());
                params.put("endDate", dto.getEndDate());
            }
        } else if (dto.getStartDate() != null) {
            sql.append(" AND a.start_date >= :startDate");
            params.put("startDate", dto.getStartDate());
        } else if (dto.getEndDate() != null) {
            sql.append(" AND a.end_date <= :endDate");
            params.put("endDate", dto.getEndDate());
        }

    }

    public List<CategoryDto> getListCategories(String categoryType) {
        String sql = """
                select value, name
                from sys_categories 
                where is_deleted = 'N'
                  and category_type = :categoryType
                  order by ifnull(order_number,:maxInteger), name
                  """;
        Map<String, Object> map = new HashMap<>();
        map.put("categoryType", categoryType);
        map.put("maxInteger", Integer.MAX_VALUE);
        return getListData(sql, map, CategoryDto.class);
    }

    public List<EmployeeDto> getListEmployee() {
        String sql = """
                select employee_id, employee_code, full_name as employeeName
                from hr_employees
                where is_deleted = 'N'
                """;
        Map<String, Object> map = new HashMap<>();
        return getListData(sql, map, EmployeeDto.class);
    }

    public List<EmployeeDto> findEmployeesByCodes(Set<String> employeeCodes){
        String sql = """
                select e.employee_id, e.employee_code, e.full_name as employeeName
                from hr_employees e
                where is_deleted = 'N'
                and e.employee_code IN (:employeeCodes)
                """;
        Map<String, Object> map = new HashMap<>();
        map.put("employeeCodes",employeeCodes);

        return getListData(sql, map, EmployeeDto.class);
    }

}
