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
import vn.hbtplus.models.request.ResearchProjectsRequest;
import vn.hbtplus.models.response.ResearchProjectsResponse;
import vn.hbtplus.repositories.BaseRepository;
import vn.hbtplus.utils.QueryUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Lop repository Impl ung voi bang lms_research_projects
 *
 * @author tudd
 * @version 1.0
 * @since 1.0
 */

@Repository
@RequiredArgsConstructor
public class ResearchProjectsRepository extends BaseRepository {

    public BaseDataTableDto searchData(ResearchProjectsRequest.SearchForm dto) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    a.research_project_id,
                    a.title,
                    a.content,
                    a.target,
                    (select sc.name from sys_categories sc where sc.value = a.status_id and sc.category_type = :status) status,
                    (
                       SELECT GROUP_CONCAT(e1.full_name SEPARATOR ', ')
                       FROM lms_research_project_members rpm1
                       JOIN hr_employees e1 ON e1.employee_id = rpm1.employee_id
                       WHERE rpm1.research_project_id = a.research_project_id
                       AND rpm1.type = 'THAM_GIA_DE_TAI'
                       AND rpm1.role_id = 1
                       AND IFNULL(rpm1.is_deleted, :activeStatus) = :activeStatus
                    ) AS projectManager,
                    a.project_type_id,
                    a.organization_id,
                    (
                       SELECT GROUP_CONCAT(e1.full_name SEPARATOR ', ')
                       FROM lms_research_project_members rpm1
                       JOIN hr_employees e1 ON e1.employee_id = rpm1.employee_id
                       WHERE rpm1.research_project_id = a.research_project_id
                       AND rpm1.type = 'THAM_GIA_DE_TAI'
                       AND rpm1.role_id != 1
                       AND IFNULL(rpm1.is_deleted, :activeStatus) = :activeStatus
                    ) as memberName,
                    rpm.research_project_member_id,
                    o.name as organization,
                    a.research_level_id,
                    a.research_topic_id,
                    a.duration,
                    a.estimated_budget,
                    a.created_by,
                    a.created_time,
                    a.modified_by,
                    a.modified_time,
                    a.last_update_time
                """);
        HashMap<String, Object> params = new HashMap<>();
        addCondition(sql, params, dto);
        return getListPagination(sql.toString(), params, dto, ResearchProjectsResponse.SearchResult.class);
    }

    public List<Map<String, Object>> getListExport(ResearchProjectsRequest.SearchForm dto) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    a.research_project_id,
                    a.title,
                    a.content,
                    a.target,
                    a.project_type_id,
                    a.organization_id,
                    o.org_name,
                    a.research_level_id,
                    a.research_topic_id,
                    a.duration,
                    a.estimated_budget,
                    a.status_id,
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

    private void addCondition(StringBuilder sql, Map<String, Object> params, ResearchProjectsRequest.SearchForm dto) {
        sql.append("""
                    FROM lms_research_projects a
                    JOIN hr_organizations o ON o.organization_id = a.organization_id
                    JOIN lms_research_project_members rpm ON rpm.research_project_id = a.research_project_id AND rpm.type = 'THAM_GIA_DE_TAI' AND IFNULL(rpm.is_deleted, :activeStatus) = :activeStatus
                    JOIN hr_employees e ON e.employee_id = rpm.employee_id
                    WHERE IFNULL(a.is_deleted, :activeStatus) = :activeStatus
                """);
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        params.put("status", Constant.CATEGORY_CODES.NCKH_TRANG_THAI);
        QueryUtils.filter(dto.getKeySearch(), sql, params, "a.title");
        sql.append(" GROUP BY a.research_project_id");
    }

    public List<ResearchProjectsResponse.Member> getListMembers(Long id, String type) {
        String sql = """
                select e.employee_id, e.full_name as employeeName, e.employee_code,
                a.role_id, a.note
                from lms_research_project_members a, hr_employees e
                where a.employee_id = e.employee_id
                and a.is_deleted = 'N'
                and a.research_project_id = :researchProjectId
                and a.type = :type
                order by a.order_number
                """;
        Map<String, Object> params = new HashMap<>();
        params.put("researchProjectId", id);
        params.put("type", type);
        return getListData(sql, params, ResearchProjectsResponse.Member.class);
    }
}
