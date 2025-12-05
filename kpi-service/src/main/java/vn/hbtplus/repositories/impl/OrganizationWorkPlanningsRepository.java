/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.repositories.impl;

import org.springframework.stereotype.Repository;
import lombok.RequiredArgsConstructor;
import vn.hbtplus.models.BaseDataTableDto;
import vn.hbtplus.repositories.BaseRepository;
import vn.hbtplus.models.request.OrganizationWorkPlanningsRequest;
import vn.hbtplus.models.response.OrganizationWorkPlanningsResponse;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.repositories.entity.EmployeeWorkPlanningsEntity;
import vn.hbtplus.repositories.entity.OrganizationWorkPlanningsEntity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Lop repository Impl ung voi bang kpi_organization_work_plannings
 *
 * @author tudd
 * @version 1.0
 * @since 1.0
 */

@Repository
@RequiredArgsConstructor
public class OrganizationWorkPlanningsRepository extends BaseRepository {

    public BaseDataTableDto searchData(OrganizationWorkPlanningsRequest.SearchForm dto) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    a.organization_work_planning_id,
                    a.organization_evaluation_id,
                    a.content,
                    a.created_by,
                    a.created_time,
                    a.modified_by,
                    a.modified_time,
                    a.last_update_time
                """);
        HashMap<String, Object> params = new HashMap<>();
        addCondition(sql, params, dto);
        return getListPagination(sql.toString(), params, dto, OrganizationWorkPlanningsResponse.class);
    }

    public List<Map<String, Object>> getListExport(OrganizationWorkPlanningsRequest.SearchForm dto) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    a.organization_work_planning_id,
                    a.organization_evaluation_id,
                    a.content,
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

    private void addCondition(StringBuilder sql, Map<String, Object> params, OrganizationWorkPlanningsRequest.SearchForm dto) {
        sql.append("""
                    FROM kpi_organization_work_plannings a
                
                
                    WHERE IFNULL(a.is_deleted, :activeStatus) = :activeStatus
                """);
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        //QueryUtils.filterGe(dto.getFromDate(), sql, params, "a.from_date", "fromDate");
        //QueryUtils.filterLe(dto.getToDate(), sql, params, "a.from_date", "toDate");
        sql.append(" ORDER BY mo.display_seq, mo.path_id");
    }

    public List<Long> getListId(Long organizationEvaluationId) {
        StringBuilder sql = new StringBuilder("""
                select
                    a.organization_work_planning_id
                FROM kpi_organization_work_plannings a
                where a.is_deleted = :activeStatus
                and a.organization_evaluation_id = :organizationEvaluationId
                """);
        Map<String, Object> params = new HashMap<>();
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        params.put("organizationEvaluationId", organizationEvaluationId);
        return getListData(sql.toString(), params, Long.class);
    }


    public List<OrganizationWorkPlanningsEntity> getListOrganizationWorkPlanning(Long id) {
        String sql = """
                SELECT
                    a.*,
                    b.adjust_reason
                    from kpi_organization_work_plannings a
                    JOIN kpi_organization_evaluations b ON a.organization_evaluation_id = b.organization_evaluation_id
                    WHERE IFNULL(a.is_deleted, :activeStatus) = :activeStatus
                    and a.organization_evaluation_id = :id
                """;
        HashMap<String, Object> params = new HashMap<>();
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        params.put("id", id);
        return getListData(sql, params, OrganizationWorkPlanningsEntity.class);
    }

    public List<OrganizationWorkPlanningsEntity> getListOrganizationWorkPlanning(Long periodId, List<Long> organizationIds) {
        String sql = """
                SELECT
                    a.content,
                    b.organization_id, b.status
                    from kpi_organization_work_plannings a
                    JOIN kpi_organization_evaluations b ON a.organization_evaluation_id = b.organization_evaluation_id
                    WHERE a.is_deleted = :activeStatus
                    and b.evaluation_period_id = :periodId
                    and b.organization_id in (:organizationIds)
                """;
        HashMap<String, Object> params = new HashMap<>();
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        params.put("periodId", periodId);
        params.put("organizationIds", organizationIds);
        return getListData(sql, params, OrganizationWorkPlanningsEntity.class);
    }
}
