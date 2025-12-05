/*
 * Copyright (C) 2022 EcoIT. All rights reserved.
 * EcoIT. Use is subject to license terms.
 */
package vn.hbtplus.tax.personal.repositories.impl;

import org.springframework.stereotype.Repository;
import vn.hbtplus.models.BaseDataTableDto;
import vn.hbtplus.tax.personal.models.request.LogActionsDTO;
import vn.hbtplus.tax.personal.models.response.LogActionsResponse;
import vn.hbtplus.repositories.BaseRepository;
import vn.hbtplus.utils.QueryUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Lop repository Impl ung voi bang PTX_LOG_ACTIONS
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Repository
public class LogActionsRepositoryImpl extends BaseRepository {

    public BaseDataTableDto<LogActionsResponse> searchData(LogActionsDTO dto) {
        StringBuilder sql = new StringBuilder("SELECT "
                + "    a.log_action_id,"
                + "    a.object_id,"
                + "    a.object_type,"
                + "    a.content,"
                + "    a.created_by,"
                + "    a.create_date"
                + "    FROM ptx_log_actions a"
                + "    WHERE 1 = 1");
        HashMap<String, Object> params = new HashMap<>();

        QueryUtils.filter(dto.getObjectId(), sql, params, "a.object_id");
        QueryUtils.filter(dto.getObjectType(), sql, params, "a.object_type");
        sql.append(" ORDER BY a.create_date DESC");
        return getListPagination(sql.toString(), params, dto, LogActionsResponse.class);
    }

    public String getPrimaryKeyColumn(String tableName){
//        String sql = "  SELECT column_name FROM all_cons_columns " +
//                "       WHERE constraint_name = (" +
//                "           SELECT constraint_name FROM user_constraints " +
//                "           WHERE UPPER(table_name) = :tableName " +
//                "           AND CONSTRAINT_TYPE = 'P'" +
//                "       )";
        String sql = "SELECT COLUMN_NAME" +
                "   FROM INFORMATION_SCHEMA.COLUMNS" +
                "   WHERE LOWER(TABLE_NAME) = :tableName" +
                "   AND COLUMN_KEY = 'PRI'";
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("tableName", tableName.toLowerCase());
        return queryForObject(sql, paramMap, String.class);
    }

    public Long getEmpIdByObjectId(String objectType, Long objectId){
        String sql = "SELECT employee_id FROM " + objectType.toLowerCase()
                + " WHERE " + getPrimaryKeyColumn(objectType) + " = :idValue";
        Map<String, Object> params = new HashMap<>();
        params.put("idValue", objectId);
        return queryForObject(sql, params, Long.class);
    }
}
