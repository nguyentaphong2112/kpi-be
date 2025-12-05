package vn.kpi.repositories.impl;

import org.springframework.stereotype.Repository;
import vn.kpi.repositories.BaseRepository;
import vn.kpi.utils.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class ObjectRelationsRepository extends BaseRepository {
    public void inactiveReferIdNotIn(Long objectId, List objectIds, String tableName, String referTableName, String functionCode) {
        String sql = """
                update hr_object_relations a 
                set a.is_deleted = 'Y', a.modified_by = :userName, a.modified_time = now()
                where a.table_name = :tableName
                and a.object_id = :objectId
                and a.refer_table_name = :referTableName
                and a.function_code = :functionCode
                and a.refer_object_id not in (:objectIds)
                and a.is_deleted = 'N'
                """;
        Map params = new HashMap<>();
        params.put("tableName", tableName);
        params.put("referTableName", referTableName);
        params.put("functionCode", functionCode);
        params.put("objectId", objectId);
        params.put("objectIds", objectIds.isEmpty() ? Utils.castToList(-1L) : objectIds);
        params.put("userName", Utils.getUserNameLogin());
        executeSqlDatabase(sql, params);
    }

    public void activeRelations(Long objectId, List referObjectIds, String tableName, String referTableName, String functionCode) {
        String sql = """
                update hr_object_relations a 
                set a.is_deleted = 'N', a.modified_by = :userName, a.modified_time = now()
                where a.table_name = :tableName
                and a.object_id = :objectId
                and a.refer_table_name = :referTableName
                and a.function_code = :functionCode
                and a.refer_object_id in (:objectIds)
                and a.is_deleted = 'Y'
                """;
        Map params = new HashMap<>();
        params.put("tableName", tableName);
        params.put("referTableName", referTableName);
        params.put("functionCode", functionCode);
        params.put("objectId", objectId);
        params.put("objectIds", referObjectIds.isEmpty() ? Utils.castToList(-1) : referObjectIds);
        params.put("userName", Utils.getUserNameLogin());
        executeSqlDatabase(sql, params);
    }

    public void insertRelations(Long objectId, List referObjectIds, String tableName, String referTableName, String functionCode) {
        String sql = """
                insert into hr_object_relations (
                    table_name, refer_table_name,  function_code,object_id,refer_object_id, 
                    is_deleted, created_by, created_time)
                 select :tableName, :referTableName, :functionCode, :objectId, :referObjectId,
                    'N', :userName, now()
                 from dual 
                 where not exists (
                    select 1 from hr_object_relations a
                    where a.table_name = :tableName
                    and a.object_id = :objectId
                    and a.refer_table_name = :referTableName
                    and a.function_code = :functionCode
                    and a.refer_object_id = :referObjectId
                 )
                """;
        List<Map> listMaps = new ArrayList<>();
        String userName = Utils.getUserNameLogin();
        referObjectIds.forEach(id -> {
            Map params = new HashMap<>();
            params.put("tableName", tableName);
            params.put("referTableName", referTableName);
            params.put("functionCode", functionCode);
            params.put("objectId", objectId);
            params.put("referObjectId", id);
            params.put("userName", userName);
            listMaps.add(params);
        });
        executeBatch(sql, listMaps);
    }
}
