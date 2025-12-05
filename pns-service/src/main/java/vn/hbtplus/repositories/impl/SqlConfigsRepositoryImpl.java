/*
 * Copyright (C) 2022 EcoIT. All rights reserved.
 * EcoIT. Use is subject to license terms.
 */
package vn.hbtplus.repositories.impl;

import org.springframework.stereotype.Repository;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.repositories.BaseRepository;
import vn.hbtplus.models.response.SqlConfigsResponse;


import java.util.HashMap;
import java.util.List;

/**
 * Lop repository Impl ung voi bang PNS_SQL_CONFIGS
 *
 * @author tudd
 * @version 1.0
 * @since 1.0
 */

@Repository
public class SqlConfigsRepositoryImpl extends BaseRepository {

    public List<SqlConfigsResponse> getListSqlConfig(Long contractTypeId, int type) {
        StringBuilder sql = new StringBuilder("""
                    SELECT
                        a.sql_config_id,
                        a.name,
                        a.contract_type_ids,
                        a.sql
                    FROM pns_sql_configs a
                    WHERE IFNULL(a.is_deleted, :flagStatus) = :flagStatus
                    AND a.type = :type
                """);
        HashMap<String, Object> params = new HashMap<>();
        if (contractTypeId == null) {
            sql.append(" AND a.contract_type_ids IS NULL");
        } else if (type == 1) { // neu xuat file rieng le
            sql.append(" AND (a.contract_type_ids LIKE :contractTypeId OR a.contract_type_ids IS NULL)");
            params.put("contractTypeId", "," + contractTypeId + ",");
        } else if (type == 2) { // neu xuat theo danh sach thi can phan biet contract_type_ids = null hay khong
            sql.append(" AND (a.contract_type_ids LIKE :contractTypeId)");
            params.put("contractTypeId", "," + contractTypeId + ",");
        }
        params.put("flagStatus", BaseConstants.STATUS.NOT_DELETED);
        params.put("type", type);
        return getListData(sql.toString(), params, SqlConfigsResponse.class);
    }
}
