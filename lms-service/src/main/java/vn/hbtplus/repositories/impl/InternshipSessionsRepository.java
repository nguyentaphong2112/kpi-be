/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.repositories.impl;

import org.springframework.stereotype.Repository;
import lombok.RequiredArgsConstructor;
import vn.hbtplus.constants.Constant;
import vn.hbtplus.models.BaseDataTableDto;
import vn.hbtplus.models.dto.CategoryDto;
import vn.hbtplus.models.dto.OrgDto;
import vn.hbtplus.repositories.BaseRepository;
import vn.hbtplus.models.request.InternshipSessionsRequest;
import vn.hbtplus.models.response.InternshipSessionsResponse;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.utils.QueryUtils;
import vn.hbtplus.utils.Utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Lop repository Impl ung voi bang lms_internship_sessions
 *
 * @author tudd
 * @version 1.0
 * @since 1.0
 */

@Repository
@RequiredArgsConstructor
public class InternshipSessionsRepository extends BaseRepository {

    public BaseDataTableDto searchData(InternshipSessionsRequest.SearchForm dto) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    a.internship_session_id,
                    a.university_id,
                    (select name from sys_categories sc where sc.category_type = :truongHoc and sc.value = a.university_id) as university_name, 
                    a.session_name,
                    a.start_date,
                    a.end_date,
                    a.total_students,
                    a.created_by,
                    a.created_time,
                    a.modified_by,
                    a.modified_time,
                    a.last_update_time
                """);
        HashMap<String, Object> params = new HashMap<>();
        addCondition(sql, params, dto);
        sql.append(" GROUP BY a.internship_session_id");
        sql.append(" ORDER BY a.start_date desc");
        params.put("truongHoc", Constant.CATEGORY_CODES.LMS_INTERN_TRUONG_DAO_TAO);
        return getListPagination(sql.toString(), params, dto, InternshipSessionsResponse.SearchResult.class);
    }

    public List<Map<String, Object>> getListExport(InternshipSessionsRequest.SearchForm dto) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    a.internship_session_id,
                    (select name from sys_categories sc where sc.category_type = :truongHoc and sc.value = a.university_id) as university_name,
                    a.university_id,
                    a.session_name,
                    a.start_date,
                    a.end_date,
                    a.total_students,
                    (select name from hr_organizations o where o.organization_id = b.organization_id) as organizationName,
                    (select name from sys_categories sc where sc.category_type = :chuyenNganh and sc.value = b.major_id) as major_name,
                    b.num_of_students
                """);
        Map<String, Object> params = new HashMap<>();
        addCondition(sql, params, dto);
        params.put("truongHoc", Constant.CATEGORY_CODES.LMS_INTERN_TRUONG_DAO_TAO);
        params.put("chuyenNganh", Constant.CATEGORY_CODES.LMS_INTERN_CHUYEN_NGANH);
        return getListData(sql.toString(), params);
    }

    private void addCondition(StringBuilder sql, Map<String, Object> params, InternshipSessionsRequest.SearchForm dto) {
        sql.append("""
                    FROM lms_internship_sessions a
                    LEFT JOIN lms_internship_session_details b ON (a.internship_session_id = b.internship_session_id and IFNULL(b.is_deleted, :activeStatus) = :activeStatus)
                    WHERE IFNULL(a.is_deleted, :activeStatus) = :activeStatus
                """);
        QueryUtils.filter(dto.getKeySearch(), sql, params, "(select name from sys_categories sc where sc.category_type = :truongHoc and sc.value = a.university_id)");
        QueryUtils.filter(dto.getUniversityId(), sql, params, "a.university_id");
        QueryUtils.filter(dto.getSessionName(), sql, params, "a.session_name");

        if (dto.getStartDate() != null) {
            sql.append(" and IFNULL(a.end_date, :startDate) >= :startDate");
            params.put("startDate", dto.getStartDate());
        }

        if (dto.getEndDate() != null) {
            sql.append(" and IFNULL(a.start_date, :endDate) <= :endDate");
            params.put("endDate", dto.getEndDate());
        }
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
    }

    public List<InternshipSessionsResponse.DetailResponse> getListDetail(Long id) {
        String sql = "select " +
                "  a.major_id," +
                "  a.organization_id," +
                "  (select org.name from hr_organizations org where org.organization_id = a.organization_id) as organization_name," +
                "  a.num_of_students," +
                "  (select name from sys_categories sc where sc.category_type = :chuyenNganh and sc.value = a.major_id) as major_name" +
                "  from lms_internship_session_details a " +
                "  where a.is_deleted = 'N'" +
                "  and a.internship_session_id = :id" +
                " order by a.internship_session_detail_id";
        Map<String, Object> params = new HashMap<>();
        params.put("id", id);
        params.put("chuyenNganh", Constant.CATEGORY_CODES.LMS_INTERN_CHUYEN_NGANH);
        return getListData(sql, params, InternshipSessionsResponse.DetailResponse.class);
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

    public List<OrgDto> getListOrg() {
        String sql = """
                select organization_id, name, full_name
                from hr_organizations
                where is_deleted = 'N'
                  order by path_order desc
                  """;
        Map<String, Object> map = new HashMap<>();
        map.put("maxInteger", Integer.MAX_VALUE);
        return getListData(sql, map, OrgDto.class);
    }
}
