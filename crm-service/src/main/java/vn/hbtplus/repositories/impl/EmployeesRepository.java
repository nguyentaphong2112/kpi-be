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
import vn.hbtplus.models.request.EmployeesRequest;
import vn.hbtplus.models.response.EmployeesResponse;
import vn.hbtplus.repositories.BaseRepository;
import vn.hbtplus.utils.QueryUtils;
import vn.hbtplus.utils.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Lop repository Impl ung voi bang crm_employees
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Repository
@RequiredArgsConstructor
public class EmployeesRepository extends BaseRepository {

    public BaseDataTableDto searchData(EmployeesRequest.SearchForm dto) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    a.employee_id,
                    a.full_name,
                    a.date_of_birth,
                    a.mobile_number,
                    a.login_name,
                    a.gender_id,
                    a.email,
                    a.zalo_account,
                    a.position_title_id,
                    a.department_id,
                    a.manager_id,
                    a.job_rank_id,
                    a.province_id,
                    a.district_id,
                    a.ward_id,
                    a.village_address,
                    a.bank_account,
                    a.bank_name,
                    a.bank_branch,
                    a.status,
                    a.personal_id_no,
                    a.tax_no,
                    a.insurance_no,
                    (SELECT sc.name FROM sys_categories sc WHERE sc.value = a.position_title_id AND sc.category_type = :positionTitle) positionTitleName,
                    (SELECT sc.name FROM sys_categories sc WHERE sc.value = a.job_rank_id AND sc.category_type = :jobRank) jobRankName,
                    (SELECT sc.name FROM sys_categories sc WHERE sc.value = a.department_id AND sc.category_type = :department) departmentName,
                    e.full_name managerName
                """);
        HashMap<String, Object> params = new HashMap<>();
        addCondition(sql, params, dto);
        return getListPagination(sql.toString(), params, dto, EmployeesResponse.SearchResult.class);
    }

    public List<Map<String, Object>> getListExport(EmployeesRequest.SearchForm dto) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    a.employee_id,
                    a.full_name,
                    a.date_of_birth,
                    a.mobile_number,
                    a.login_name,
                    a.gender_id,
                    a.email,
                    a.zalo_account,
                    a.position_title_id,
                    a.department_id,
                    a.manager_id,
                    a.job_rank_id,
                    a.province_id,
                    a.district_id,
                    a.ward_id,
                    a.village_address,
                    a.bank_account,
                    a.bank_name,
                    a.bank_branch,
                    a.status,
                    a.personal_id_no,
                    a.tax_no,
                    a.insurance_no,
                    (SELECT sc.name FROM sys_categories sc WHERE sc.value = a.position_title_id AND sc.category_type = :positionTitle) positionTitleName,
                    (SELECT sc.name FROM sys_categories sc WHERE sc.value = a.job_rank_id AND sc.category_type = :jobRank) jobRankName,
                    (SELECT sc.name FROM sys_categories sc WHERE sc.value = a.department_id AND sc.category_type = :department) departmentName,
                    e.full_name managerName
                """);
        Map<String, Object> params = new HashMap<>();
        addCondition(sql, params, dto);
        return getListData(sql.toString(), params);
    }

    private void addCondition(StringBuilder sql, Map<String, Object> params, EmployeesRequest.SearchForm dto) {
        sql.append("""
            FROM crm_employees a
            LEFT JOIN crm_employees e ON e.employee_id = a.manager_id
            WHERE IFNULL(a.is_deleted, :activeStatus) = :activeStatus
        """);
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        params.put("positionTitle", Constant.CATEGORY_TYPES.CHUC_VU);
        params.put("jobRank", Constant.CATEGORY_TYPES.VAI_TRO);
        params.put("department", Constant.CATEGORY_TYPES.DON_VI);
        QueryUtils.filter(dto.getFullName(), sql, params, "a.full_name");
        QueryUtils.filter(dto.getDateOfBirth(), sql, params, "a.date_of_birth");
        QueryUtils.filter(dto.getMobileNumber(), sql, params, "a.mobile_number");
        sql.append(" ORDER BY a.created_time desc");
        if (!Utils.isNullOrEmpty(dto.getSelectedValue())) {
            String sqlValueSelect = ("""
                SELECT
                    CASE
                        WHEN a.employee_id IN (:selectedValue) THEN 1
                        ELSE 0
                    END AS valueSelect, """);
            sql.replace(0, sql.length(), sql.toString().replaceFirst("SELECT", sqlValueSelect).replaceFirst("(?s)(.*)" + "ORDER BY" + "(?!.*" + "ORDER BY" + ")", "$1" + "ORDER BY valueSelect DESC,"));
            params.put("selectedValue", dto.getSelectedValue());
        }
    }

    public List<EmployeesResponse.SearchResult> getListEmployee(String keySearch) {
        StringBuilder sql = new StringBuilder("""
                        SELECT 
                            a.*
                        FROM crm_employees a
                        WHERE a.is_deleted = 'N'
                    """);
        Map<String, Object> params = new HashMap<>();
        QueryUtils.filter(keySearch, sql, params, "a.full_name", "a.mobile_number", "a.email", "a.login_name");
        return getListData(sql.toString(), params, EmployeesResponse.SearchResult.class);
    }
}
