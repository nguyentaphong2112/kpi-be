/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.kpi.repositories.impl;

import org.springframework.stereotype.Repository;
import lombok.RequiredArgsConstructor;
import vn.kpi.models.BaseDataTableDto;
import vn.kpi.repositories.BaseRepository;
import vn.kpi.models.request.WarningConfigsRequest;
import vn.kpi.models.response.WarningConfigsResponse;
import vn.kpi.constants.BaseConstants;
import vn.kpi.utils.Utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Lop repository Impl ung voi bang sys_warning_configs
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Repository
@RequiredArgsConstructor
public class WarningConfigsRepository extends BaseRepository {

    public BaseDataTableDto searchData(WarningConfigsRequest.SearchForm dto) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    a.warning_config_id,
                    a.title,
                    a.resource,
                    a.background_color,
                    a.icon,
                    a.api_uri,
                    a.url_view_detail,
                    a.sql_query,
                    CASE WHEN a.is_must_positive = 'Y' THEN 'Có'
                         WHEN a.is_must_positive = 'N' THEN 'Không'
                         ELSE NULL
                    END AS is_must_positive,
                    a.order_number,
                    a.is_deleted,
                    a.created_by,
                    a.created_time,
                    a.modified_by,
                    a.modified_time,
                    a.last_update_time
                """);
        HashMap<String, Object> params = new HashMap<>();
        addCondition(sql, params, dto);
        return getListPagination(sql.toString(), params, dto, WarningConfigsResponse.class);
    }

    public List<Map<String, Object>> getListExport(WarningConfigsRequest.SearchForm dto) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    a.warning_config_id,
                    a.title,
                    a.resource,
                    a.background_color,
                    a.icon,
                    a.api_uri,
                    a.url_view_detail,
                    a.sql_query,
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

    private void addCondition(StringBuilder sql, Map<String, Object> params, WarningConfigsRequest.SearchForm dto) {
        sql.append("""
            FROM sys_warning_configs a
            WHERE IFNULL(a.is_deleted, :activeStatus) = :activeStatus
        """);
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);

        if (!Utils.isNullOrEmpty(dto.getKeySearch())) {
            sql.append(" AND (lower(a.title) like :keySearch or lower(a.resource) like :keySearch)");
            params.put("keySearch", "%" + dto.getKeySearch().trim() + "%");
        }
    }

    public List<WarningConfigsResponse> getListWarning() {
        String sql = """
                select a.*
                 from sys_warning_configs a
                where a.is_deleted = 'N'
                order by a.order_number, a.warning_config_id
                """;
        return getListData(sql, new HashMap<>(), WarningConfigsResponse.class);
    }
}
