/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.repositories.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.models.BaseDataTableDto;
import vn.hbtplus.models.request.LogActionsRequest;
import vn.hbtplus.models.response.LogActionsResponse;
import vn.hbtplus.repositories.BaseRepository;
import vn.hbtplus.utils.QueryUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Lop repository Impl ung voi bang crm_log_actions
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Repository
@RequiredArgsConstructor
public class LogActionsRepository extends BaseRepository {

    public BaseDataTableDto searchData(LogActionsRequest.SearchForm dto) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    a.log_action_id,
                    a.action,
                    a.action_name,
                    a.obj_type,
                    a.obj_id,
                    a.obj_name,
                    a.data_before,
                    a.data_after,
                    a.is_deleted,
                    a.created_by,
                    a.created_time,
                    a.modified_by,
                    a.modified_time,
                    e.full_name createdByName
                """);
        HashMap<String, Object> params = new HashMap<>();
        addCondition(sql, params, dto);
        return getListPagination(sql.toString(), params, dto, LogActionsResponse.class);
    }

    private void addCondition(StringBuilder sql, Map<String, Object> params, LogActionsRequest.SearchForm dto) {
        sql.append("""
            FROM crm_log_actions a
            LEFT JOIN crm_employees e ON e.login_name = a.created_by
            WHERE IFNULL(a.is_deleted, :activeStatus) = :activeStatus
        """);
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        QueryUtils.filter(dto.getObjName(), sql, params, "a.obj_name");
        QueryUtils.filterEq(dto.getLoginName(), sql, params, "a.created_by");
        QueryUtils.filterEq(dto.getObjType(), sql, params, "a.obj_type");
        QueryUtils.filter(dto.getListObjType(), sql, params, "a.obj_type");
        QueryUtils.filter(dto.getActionName(), sql, params, "a.action_name");
        QueryUtils.filter(dto.getObjId(), sql, params, "a.obj_id");
        QueryUtils.filterGe(dto.getStartDate(), sql, params, "a.created_time", "startDate");
        QueryUtils.filterLe(dto.getEndDate(), sql, params, "a.created_time", "endDate");
        sql.append(" ORDER BY a.created_time desc");
    }
}
