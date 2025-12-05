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
import vn.hbtplus.models.request.MentoringTraineesRequest;
import vn.hbtplus.models.response.MentoringTraineesResponse;
import vn.hbtplus.repositories.BaseRepository;
import vn.hbtplus.utils.QueryUtils;
import vn.hbtplus.utils.Utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Lop repository Impl ung voi bang lms_mentoring_trainees
 *
 * @author tudd
 * @version 1.0
 * @since 1.0
 */

@Repository
@RequiredArgsConstructor
public class MentoringTraineesRepository extends BaseRepository {

    public BaseDataTableDto searchData(MentoringTraineesRequest.SearchForm dto) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    a.med_mentoring_trainee_id,
                    a.employee_id,
                    e.employee_code,
                    e.full_name as employeeName,
                    a.start_date,
                    a.end_date,
                    (select sc.name from sys_categories sc where sc.value = a.project_id and sc.category_type = :project) projectName,
                    (select sc.name from sys_categories sc where sc.value = a.hospital_id and sc.category_type = :hospital) hospitalName,
                    a.total_lessons,
                    a.content,
                    a.document_no,
                    a.is_deleted,
                    a.created_by,
                    a.created_time,
                    a.modified_by,
                    a.modified_time,
                    a.last_update_time
                """);
        HashMap<String, Object> params = new HashMap<>();
        addCondition(sql, params, dto);
        params.put("project", Constant.CATEGORY_CODES.CDT_CHUONG_TRINH_DAO_TAO);
        params.put("hospital", Constant.CATEGORY_CODES.CDT_DIA_DIEM_DAO_TAO);
        return getListPagination(sql.toString(), params, dto, MentoringTraineesResponse.SearchResult.class);
    }

    public List<Map<String, Object>> getListExport(MentoringTraineesRequest.SearchForm dto) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    a.employee_id,
                    e.employee_code,
                    e.full_name as employeeName,
                    a.start_date,
                    a.end_date,
                    (select sc.name from sys_categories sc where sc.value = a.project_id and sc.category_type = :project) projectName,
                    (select sc.name from sys_categories sc where sc.value = a.hospital_id and sc.category_type = :hospital) hospitalName,
                    a.total_lessons,
                    a.content,
                    a.document_no,
                    a.created_by,
                    a.created_time,
                    a.modified_by,
                    a.modified_time
                """);
        Map<String, Object> params = new HashMap<>();
        addCondition(sql, params, dto);
        params.put("project", Constant.CATEGORY_CODES.CDT_CHUONG_TRINH_DAO_TAO);
        params.put("hospital", Constant.CATEGORY_CODES.CDT_DIA_DIEM_DAO_TAO);
        return getListData(sql.toString(), params);
    }

    private void addCondition(StringBuilder sql, Map<String, Object> params, MentoringTraineesRequest.SearchForm dto) {
        sql.append("""
                    FROM lms_mentoring_trainees a
                    JOIN hr_employees e ON e.employee_id = a.employee_id
                    LEFT JOIN hr_jobs mj ON mj.job_id = e.job_id
                    JOIN hr_organizations o ON o.organization_id = e.organization_id
                    WHERE IFNULL(a.is_deleted, :activeStatus) = :activeStatus
                """);
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        QueryUtils.filter(dto.getKeySearch(), sql, params, "e.full_name", "e.employee_code");
        if (!Utils.isNullOrEmpty(dto.getOrganizationId())) {
            sql.append(" AND o.path_id LIKE :orgId ");
            params.put("orgId", "%/" + dto.getOrganizationId() + "/%");
        }
        if (!Utils.isNullObject(dto.getEmployeeId())) {
            sql.append(" AND a.employee_id = :employeeId ");
            params.put("employeeId", dto.getEmployeeId());
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

}
