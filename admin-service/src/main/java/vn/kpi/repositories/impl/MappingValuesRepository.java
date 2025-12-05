/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.kpi.repositories.impl;

import org.apache.commons.lang3.time.DateUtils;
import org.springframework.stereotype.Repository;
import lombok.RequiredArgsConstructor;
import vn.kpi.models.BaseDataTableDto;
import vn.kpi.repositories.BaseRepository;
import vn.kpi.models.request.MappingValuesRequest;
import vn.kpi.models.response.MappingValuesResponse;
import vn.kpi.constants.BaseConstants;
import vn.kpi.utils.QueryUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Lop repository Impl ung voi bang sys_mapping_values
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Repository
@RequiredArgsConstructor
public class MappingValuesRepository extends BaseRepository {

    public BaseDataTableDto searchData(MappingValuesRequest.SearchForm dto, String configMappingCode) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    a.mapping_value_id,
                    a.parameter,
                    a.value,
                    a.start_date,
                    a.end_date,
                    a.config_mapping_code,
                    scm.data_type,
                    a.is_deleted,
                    a.created_by,
                    a.created_time,
                    a.modified_by,
                    a.modified_time,
                    a.last_update_time
                """);
        HashMap<String, Object> params = new HashMap<>();
        params.put("configMappingCode", configMappingCode);
        addCondition(sql, params, dto,configMappingCode);
        return getListPagination(sql.toString(), params, dto, MappingValuesResponse.class);
    }

    public List<Map<String, Object>> getListExport(MappingValuesRequest.SearchForm dto) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    a.mapping_value_id,
                    a.parameter,
                    a.value,
                    a.start_date,
                    a.end_date,
                    a.config_mapping_code,
                    a.is_deleted,
                    a.created_by,
                    a.created_time,
                    a.modified_by,
                    a.modified_time,
                    a.last_update_time
                """);
        Map<String, Object> params = new HashMap<>();
        addCondition(sql, params, dto, "");
        return getListData(sql.toString(), params);
    }

    public List<Map<String, Object>> getListMappingValueByCode(String configMappingCode) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    a.mapping_value_id,
                    a.parameter,
                    a.value,
                    a.start_date,
                    a.end_date,
                    a.config_mapping_code,
                    a.is_deleted,
                    a.created_by,
                    a.created_time,
                    a.modified_by,
                    a.modified_time,
                    a.last_update_time
                    FROM sys_mapping_values a
                    WHERE IFNULL(a.is_deleted, :activeStatus) = :activeStatus
                    AND a.config_mapping_code = :configMappingCode
                """);
        Map<String, Object> params = new HashMap<>();
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        params.put("configMappingCode", configMappingCode);
        return getListData(sql.toString(), params);
    }

    private void addCondition(StringBuilder sql, Map<String, Object> params, MappingValuesRequest.SearchForm dto, String configMappingCode) {
        sql.append("""
            FROM sys_mapping_values a
            JOIN sys_config_mappings scm ON scm.code = a.config_mapping_code
            
            
            WHERE IFNULL(a.is_deleted, :activeStatus) = :activeStatus
        """);
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        if(params.get("configMappingCode") != null) {
            sql.append(" AND a.config_mapping_code = :configMappingCode");
        }
        QueryUtils.filter(dto.getKeySearch(), sql, params, "parameter","value");

    }

    public void updateEndDate(String configMappingCode, List<String> parameters, Date startDate) {
        String sql = """
                update sys_mapping_values a
                set a.end_date = :endDate
                where a.config_mapping_code in (:configMappingCode)
                and a.parameter in (:parameters)
                and a.start_date < :startDate
                and (a.end_date is null or a.end_date >= :startDate)
                and a.is_deleted = :isDeleted
                """;
        Map<String, Object> params = new HashMap<>();

        params.put("configMappingCode", configMappingCode);
        params.put("parameters", parameters);
        params.put("startDate", startDate);
        params.put("isDeleted", BaseConstants.STATUS.NOT_DELETED);
        params.put("endDate", DateUtils.addDays(startDate, -1));
        executeSqlDatabase(sql, params);
    }
}
