/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.repositories.impl;

import org.springframework.stereotype.Repository;
import lombok.RequiredArgsConstructor;
import vn.hbtplus.models.BaseDataTableDto;
import vn.hbtplus.models.response.ConfigParameterResponse;
import vn.hbtplus.repositories.BaseRepository;
import vn.hbtplus.models.request.ConfigMappingsRequest;
import vn.hbtplus.models.response.ConfigMappingsResponse;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.repositories.entity.ConfigMappingsEntity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Lop repository Impl ung voi bang sys_config_mappings
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Repository
@RequiredArgsConstructor
public class ConfigMappingsRepository extends BaseRepository {

    public BaseDataTableDto searchData(ConfigMappingsRequest.SearchForm dto) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    a.config_mapping_id,
                    a.code,
                    a.name,
                    a.parameter_title,
                    a.value_title,
                    a.data_type,
                    a.is_deleted,
                    a.created_by,
                    a.created_time,
                    a.modified_by,
                    a.modified_time,
                    a.last_update_time
                """);
        HashMap<String, Object> params = new HashMap<>();
        addCondition(sql, params, dto);
        return getListPagination(sql.toString(), params, dto, ConfigMappingsResponse.class);
    }

    public List<Map<String, Object>> getListExport(ConfigMappingsRequest.SearchForm dto) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    a.config_mapping_id,
                    a.code,
                    a.name,
                    a.parameter_title,
                    a.value_title,
                    a.data_type,
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

    private void addCondition(StringBuilder sql, Map<String, Object> params, ConfigMappingsRequest.SearchForm dto) {
        sql.append("""
            FROM sys_config_mappings a
            
            
            WHERE IFNULL(a.is_deleted, :activeStatus) = :activeStatus
        """);
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        //QueryUtils.filterGe(dto.getFromDate(), sql, params, "a.from_date", "fromDate");
        //QueryUtils.filterLe(dto.getToDate(), sql, params, "a.from_date", "toDate");
//        sql.append(" ORDER BY mo.display_seq, mo.path_id");
    }

    public List<ConfigMappingsResponse> getListConfigByCodes(List<String> codes) {
        String sql = "select * from sys_config_mappings a" +
                     " where a.is_deleted = :activeStatus" +
                     " and a.code in (:codes)" +
                     " order by a.name";
        HashMap<String, Object> params = new HashMap<>();
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        params.put("codes", codes);
        return getListData(sql, params, ConfigMappingsResponse.class);
    }
}
