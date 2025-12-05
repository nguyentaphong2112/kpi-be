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
import vn.hbtplus.models.dto.EmployeeDto;
import vn.hbtplus.models.request.TrainingProcessRequest;
import vn.hbtplus.models.response.TrainingProcessResponse;
import vn.hbtplus.repositories.BaseRepository;
import vn.hbtplus.utils.QueryUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Lop repository Impl ung voi bang lms_training_process
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Repository
@RequiredArgsConstructor
public class TrainingProcessRepository extends BaseRepository {

    public BaseDataTableDto searchData(TrainingProcessRequest.SearchForm dto) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    a.training_process_id,
                    a.employee_id,
                    e.employee_code,
                    e.full_name as employeeName,
                    mj.name as jobName,
                    o.full_name as orgName,
                    a.start_date,
                    a.end_date,
                    a.major_id,
                    a.training_place_id,
                    a.training_plan_id,
                    a.training_course_id,
                    (select name from sys_categories sc where sc.category_type = :noiDung and sc.value = a.major_id) as majorName, 
                    (select name from sys_categories sc where sc.category_type = :noiDaoTao and sc.value = a.training_place_id) as trainingPlaceName, 
                    (select name from sys_categories sc where sc.category_type = :keHoach and sc.value = a.training_plan_id) as trainingPlanName, 
                    (select name from sys_categories sc where sc.category_type = :khoa and sc.value = a.training_course_id) as trainingCourseName, 
                    a.document_no,
                    a.document_signed_date,
                    a.total_budget,
                    a.total_hours,
                    a.created_by,
                    a.created_time,
                    a.modified_by,
                    a.modified_time,
                    a.last_update_time
                """);
        HashMap<String, Object> params = new HashMap<>();
        addCondition(sql, params, dto);
        return getListPagination(sql.toString(), params, dto, TrainingProcessResponse.SearchResult.class);
    }

    public List<Map<String, Object>> getListExport(TrainingProcessRequest.SearchForm dto) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    a.training_process_id,
                    a.employee_id,
                    e.employee_code,
                    e.full_name as employeeName,
                    mj.name as jobName,
                    o.full_name as orgName,
                    a.start_date,
                    a.end_date,
                    a.major_id,
                    a.training_place_id,
                    a.training_plan_id,
                    a.training_course_id,
                    (select name from sys_categories sc where sc.category_type = :noiDung and sc.value = a.major_id) as majorName, 
                    (select name from sys_categories sc where sc.category_type = :noiDaoTao and sc.value = a.training_place_id) as trainingPlaceName, 
                    (select name from sys_categories sc where sc.category_type = :keHoach and sc.value = a.training_plan_id) as trainingPlanName, 
                    (select name from sys_categories sc where sc.category_type = :khoa and sc.value = a.training_course_id) as trainingCourseName, 
                    a.document_no,
                    a.document_signed_date,
                    a.total_budget,
                    a.total_hours,
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

    private void addCondition(StringBuilder sql, Map<String, Object> params, TrainingProcessRequest.SearchForm dto) {
        sql.append("""
            FROM lms_training_process a
            JOIN hr_employees e ON e.employee_id = a.employee_id
            LEFT JOIN hr_jobs mj ON mj.job_id = e.job_id
            JOIN hr_organizations o ON o.organization_id = e.organization_id
            WHERE IFNULL(a.is_deleted, :activeStatus) = :activeStatus
        """);
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        params.put("noiDung", Constant.CATEGORY_CODES.LMS_NOI_DUNG_DAO_TAO);
        params.put("noiDaoTao", Constant.CATEGORY_CODES.LMS_NOI_DAO_TAO);
        params.put("keHoach", Constant.CATEGORY_CODES.LMS_KE_HOACH_DAO_TAO);
        params.put("khoa", Constant.CATEGORY_CODES.LMS_KHOA_DAO_TAO);
        QueryUtils.filter(dto.getKeySearch(), sql, params, "e.full_name", "e.employee_code");
        QueryUtils.filter(dto.getEmployeeId(), sql, params, "a.employee_id");
        if (dto.getStartDate() != null) {
            sql.append(" and NVL(a.end_date, :startDate) >= :startDate");
            params.put("startDate", dto.getStartDate());
        }
        if (dto.getEndDate() != null) {
            sql.append(" and NVL(a.start_date, :endDate) <= :endDate");
            params.put("endDate", dto.getEndDate());
        }
    }

    public List<EmployeeDto> getListEmployee() {
        String sql = """
                select e.employee_id, e.employee_code, e.full_name as employeeName, p.identity_no identityNo
                from hr_employees e, hr_personal_identities p
                where p.is_deleted = 'N'
                and e.is_deleted = 'N'
                and e.employee_id = p.employee_id
                and p.is_main = 'Y'
                """;
        Map<String, Object> map = new HashMap<>();
        return getListData(sql, map, EmployeeDto.class);
    }
}
