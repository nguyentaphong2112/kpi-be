/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.kpi.repositories.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import vn.kpi.constants.BaseConstants;
import vn.kpi.models.BaseDataTableDto;
import vn.kpi.models.request.ObjectAttributesRequest;
import vn.kpi.models.response.ObjectAttributesResponse;
import vn.kpi.repositories.BaseRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Lop repository Impl ung voi bang sys_object_attributes
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Repository
@RequiredArgsConstructor
public class ObjectAttributesRepository extends BaseRepository {

    public BaseDataTableDto searchData(ObjectAttributesRequest.SearchForm dto) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    a.object_attribute_id,
                    a.attribute_code,
                    a.attribute_value,
                    a.object_id,
                    a.table_name,
                    a.data_type
                """);
        HashMap<String, Object> params = new HashMap<>();
        addCondition(sql, params, dto);
        return getListPagination(sql.toString(), params, dto, ObjectAttributesResponse.class);
    }

    public List<Map<String, Object>> getListExport(ObjectAttributesRequest.SearchForm dto) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    a.object_attribute_id,
                    a.attribute_code,
                    a.attribute_value,
                    a.object_id,
                    a.table_name,
                    a.data_type
                """);
        Map<String, Object> params = new HashMap<>();
        addCondition(sql, params, dto);
        return getListData(sql.toString(), params);
    }

    private void addCondition(StringBuilder sql, Map<String, Object> params, ObjectAttributesRequest.SearchForm dto) {
        sql.append("""
            FROM sys_object_attributes a
            
            
            WHERE IFNULL(a.is_deleted, :activeStatus) = :activeStatus
        """);
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        //QueryUtils.filterGe(dto.getFromDate(), sql, params, "a.from_date", "fromDate");
        //QueryUtils.filterLe(dto.getToDate(), sql, params, "a.from_date", "toDate");
        sql.append(" ORDER BY mo.display_seq, mo.path_id");
    }

    public List<ObjectAttributesResponse> getListAttributes(Long objectId, String tableName) {
        String sql = """
                    select a.attribute_code, a.attribute_value, a.data_type
                    from sys_object_attributes a
                    where a.object_id = :objectId
                    and a.table_name = :tableName
                """;
        Map<String, Object> params = new HashMap<>();
        params.put("tableName", tableName);
        params.put("objectId", objectId);
        return getListData(sql, params, ObjectAttributesResponse.class);
    }
    public List<ObjectAttributesResponse> getListAttributes(List<Long> objectIds, String tableName) {
        String sql = """
                    select a.attribute_code, a.attribute_value, a.data_type, a.object_id
                    from sys_object_attributes a
                    where a.object_id in (:objectIds)
                    and a.table_name = :tableName
                """;
        Map<String, Object> params = new HashMap<>();
        params.put("tableName", tableName);
        params.put("objectIds", objectIds);
        return getListData(sql, params, ObjectAttributesResponse.class);
    }

    public void deleteAttributes(Long objectId, String tableName, List<String> codes) {
        String sql = """
                delete a from sys_object_attributes a 
                where a.object_id = :organizationId
                and a.table_name = :tableName
                and a.attribute_code in (:codes)             
                """;
        Map<String, Object> map = new HashMap<>();
        map.put("organizationId", objectId);
        map.put("tableName", tableName);
        map.put("codes", codes);
        executeSqlDatabase(sql, map);
    }
}
