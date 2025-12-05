/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.kpi.repositories.impl;

import org.springframework.stereotype.Repository;
import lombok.RequiredArgsConstructor;
import vn.kpi.constants.Constant;
import vn.kpi.models.BaseDataTableDto;
import vn.kpi.repositories.BaseRepository;
import vn.kpi.models.request.ConfigChartsRequest;
import vn.kpi.models.response.ConfigChartsResponse;
import vn.kpi.constants.BaseConstants;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Lop repository Impl ung voi bang sys_config_charts
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Repository
@RequiredArgsConstructor
public class ConfigChartsRepository extends BaseRepository {

    public BaseDataTableDto searchData(ConfigChartsRequest.SearchForm dto) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    a.config_chart_id,
                    a.code,
                    a.name,
                    a.type,
                    (select sc.name from sys_categories sc where sc.value = a.type and sc.category_type = :chartType) as typeName,
                    a.sql_query,
                    a.url,
                    a.service_name,
                    a.order_number,
                    a.is_deleted,
                    a.created_by,
                    a.created_time,
                    a.modified_by,
                    a.modified_time,
                    a.last_update_time
                """);
        HashMap<String, Object> params = new HashMap<>();
        params.put("chartType", Constant.CATEGORY_TYPE.SYS_LOAI_BIEU_DO);
        addCondition(sql, params, dto);
        return getListPagination(sql.toString(), params, dto, ConfigChartsResponse.class);
    }

    public List<Map<String, Object>> getListExport(ConfigChartsRequest.SearchForm dto) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    a.config_chart_id,
                    a.code,
                    a.name,
                    a.type,
                    a.sql_query,
                    a.url,
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

    private void addCondition(StringBuilder sql, Map<String, Object> params, ConfigChartsRequest.SearchForm dto) {
        sql.append("""
            FROM sys_config_charts a
            
            
            WHERE IFNULL(a.is_deleted, :activeStatus) = :activeStatus
        """);
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        //QueryUtils.filterGe(dto.getFromDate(), sql, params, "a.from_date", "fromDate");
        //QueryUtils.filterLe(dto.getToDate(), sql, params, "a.from_date", "toDate");
//        sql.append(" ORDER BY mo.display_seq, mo.path_id");
    }

    public List<ConfigChartsResponse> getListCharts() {
        String sql = """
                select a.config_chart_id, a.code, a.order_number, soa.attribute_value color,
                 a.name,
                 a.type,
                 a.url,
                 a.service_name
                 from sys_config_charts a
                 LEFT JOIN sys_object_attributes soa on a.config_chart_id = soa.object_id and soa.table_name = :tableName
                where a.is_deleted = 'N'
                order by a.order_number
                """;
        Map<String, Object> params = new HashMap<>();
        params.put("tableName", Constant.ATTACHMENT.TABLE_NAMES.CONFIG_CHARTS);
        return getListData(sql, params, ConfigChartsResponse.class);
    }
}
