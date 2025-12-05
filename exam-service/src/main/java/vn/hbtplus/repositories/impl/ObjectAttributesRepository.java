/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.repositories.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.models.BaseDataTableDto;
import vn.hbtplus.models.response.ObjectAttributesResponse;
import vn.hbtplus.repositories.BaseRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Lop repository Impl ung voi bang hr_object_attributes
 *
 * @author tudd
 * @version 1.0
 * @since 1.0
 */

@Repository
@RequiredArgsConstructor
public class ObjectAttributesRepository extends BaseRepository {

    public List<ObjectAttributesResponse> getListAttributes(Long objectId, String tableName) {
        String sql = """
                    select a.attribute_code, a.attribute_value, a.data_type
                    from hr_object_attributes a
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
                    from hr_object_attributes a
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
                delete a from hr_object_attributes a 
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
