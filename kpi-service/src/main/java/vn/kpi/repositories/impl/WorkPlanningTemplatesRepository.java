/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.kpi.repositories.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import vn.kpi.constants.BaseConstants;
import vn.kpi.models.BaseDataTableDto;
import vn.kpi.models.request.WorkPlanningTemplatesRequest;
import vn.kpi.models.response.WorkPlanningTemplatesResponse;
import vn.kpi.repositories.BaseRepository;
import vn.kpi.repositories.entity.WorkPlanningTemplatesEntity;
import vn.kpi.utils.QueryUtils;
import vn.kpi.utils.Utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Lop repository Impl ung voi bang kpi_indicators
 *
 * @author tudd
 * @version 1.0
 * @since 1.0
 */

@Repository
@RequiredArgsConstructor
public class WorkPlanningTemplatesRepository extends BaseRepository {

    public BaseDataTableDto searchData(WorkPlanningTemplatesRequest.SearchForm dto) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    a.work_planning_template_id,
                    a.name,
                    a.created_by,
                    a.created_time,
                    a.modified_by,
                    a.modified_time
                """);
        HashMap<String, Object> params = new HashMap<>();
        addCondition(sql, params, dto);
        return getListPagination(sql.toString(), params, dto, WorkPlanningTemplatesResponse.SearchResult.class);
    }

    public WorkPlanningTemplatesResponse.DetailBean getListExportById(Long id) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    a.work_planning_template_id,
                    a.name,
                    a.content
                    FROM kpi_work_planning_templates a
                    WHERE IFNULL(a.is_deleted, :activeStatus) = :activeStatus
                    AND a.work_planning_template_id = :id
                """);
        Map<String, Object> params = new HashMap<>();
        params.put("id", id);
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        return getFirstData(sql.toString(), params, WorkPlanningTemplatesResponse.DetailBean.class);
    }

    private void addCondition(StringBuilder sql, Map<String, Object> params, WorkPlanningTemplatesRequest.SearchForm dto) {
        sql.append("""
                    FROM kpi_work_planning_templates a
                    WHERE IFNULL(a.is_deleted, :activeStatus) = :activeStatus
                """);
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        if (!Utils.isNullOrEmpty(dto.getKeySearch())) {
            sql.append(" AND lower(a.name) like :keySearch");
            params.put("keySearch", "%" + dto.getKeySearch().toLowerCase() + "%");
        }
    }

    public List<WorkPlanningTemplatesEntity> getListData(WorkPlanningTemplatesRequest.SearchForm dto) {
        StringBuilder sql = new StringBuilder("""
                SELECT a.*
                FROM kpi_work_planning_templates a
                WHERE IFNULL(a.is_deleted, :activeStatus) = :activeStatus
                """);
        Map<String, Object> params = new HashMap<>();
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        QueryUtils.filter(dto.getId(), sql, params, "a.work_planning_template_id");
        QueryUtils.filter(dto.getListId(), sql, params, "a.work_planning_template_id");
        QueryUtils.filterEq(dto.getType(), sql, params, "a.type");
        QueryUtils.filter(dto.getListCode(), sql, params, "a.code");
        return getListData(sql.toString(), params, WorkPlanningTemplatesEntity.class);
    }
}
