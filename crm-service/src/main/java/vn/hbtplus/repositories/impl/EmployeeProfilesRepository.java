/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.repositories.impl;

import org.springframework.stereotype.Repository;
import lombok.RequiredArgsConstructor;
import vn.hbtplus.constants.Constant;
import vn.hbtplus.models.BaseDataTableDto;
import vn.hbtplus.models.response.EmployeesResponse;
import vn.hbtplus.repositories.BaseRepository;
import vn.hbtplus.models.request.EmployeeProfilesRequest;
import vn.hbtplus.models.response.EmployeeProfilesResponse;
import vn.hbtplus.constants.BaseConstants;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Lop repository Impl ung voi bang crm_employee_profiles
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Repository
@RequiredArgsConstructor
public class EmployeeProfilesRepository extends BaseRepository {

    public BaseDataTableDto searchData(EmployeeProfilesRequest.SearchForm dto) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    a.employee_profile_id,
                    a.employee_id,
                    e.employee_code,
                    e.full_name,
                    a.attachment_type,
                    a.note,
                    a.is_deleted,
                    a.created_by,
                    a.created_time,
                    a.modified_by,
                    a.modified_time,
                    a.last_update_time
                """);
        HashMap<String, Object> params = new HashMap<>();
        addCondition(sql, params, dto);
        return getListPagination(sql.toString(), params, dto, EmployeeProfilesResponse.class);
    }

    public List<Map<String, Object>> getListExport(EmployeeProfilesRequest.SearchForm dto) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    a.employee_profile_id,
                    a.employee_id,
                    e.employee_code,
                    e.full_name,
                    a.attachment_type,
                    a.note,
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

    private void addCondition(StringBuilder sql, Map<String, Object> params, EmployeeProfilesRequest.SearchForm dto) {
        sql.append("""
            FROM crm_employee_profiles a
            JOIN hr_employees e ON e.employee_id = a.employee_id
            LEFT JOIN hr_jobs mj ON mj.job_id = e.job_id
            JOIN hr_organizations o ON o.organization_id = e.organization_id
            WHERE IFNULL(a.is_deleted, :activeStatus) = :activeStatus
        """);
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        //QueryUtils.filterGe(dto.getFromDate(), sql, params, "a.from_date", "fromDate");
        //QueryUtils.filterLe(dto.getToDate(), sql, params, "a.from_date", "toDate");
        sql.append(" ORDER BY mo.display_seq, mo.path_id");
    }

    public List<EmployeesResponse.ProfileAttachment> getListProfileAttachments(Long id) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    a.*,
                    (SELECT sc.name FROM sys_categories sc WHERE sc.value = a.attachment_type AND sc.category_type = :attachmentType) attachmentTypeName
                FROM crm_employee_profiles a
                WHERE IFNULL(a.is_deleted, :activeStatus) = :activeStatus and a.employee_id = :id
                """);
        HashMap<String, Object> params = new HashMap<>();
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        params.put("attachmentType", Constant.CATEGORY_TYPES.CRM_HO_SO_NHAN_VIEN);
        params.put("id", id);
        return getListData(sql.toString(), params, EmployeesResponse.ProfileAttachment.class);
    }
}
