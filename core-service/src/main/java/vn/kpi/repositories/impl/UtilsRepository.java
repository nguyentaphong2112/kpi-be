package vn.kpi.repositories.impl;

import org.springframework.stereotype.Repository;
import vn.kpi.constants.BaseConstants;
import vn.kpi.models.AttachmentFileDto;
import vn.kpi.models.AttributeConfigDto;
import vn.kpi.models.beans.DbColumnBean;
import vn.kpi.models.response.ObjectAttributesResponse;
import vn.kpi.repositories.BaseRepository;
import vn.kpi.utils.Utils;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class UtilsRepository extends BaseRepository {

    public List<DbColumnBean> getColumns(List<String> tables) {
        String sql = """
                select 
                    a.table_name,
                	a.COLUMN_NAME ,
                	a.COLUMN_TYPE ,
                	a.COLUMN_KEY ,
                	a.COLUMN_COMMENT,
                	(select t.table_comment from information_schema.TABLES t 
                	    where t.TABLE_TYPE = 'BASE TABLE' 
                	    and a.TABLE_NAME = t.TABLE_NAME
                	) table_comment
                from information_schema.columns a
                where a.table_name in (:tables)
                order by a.TABLE_NAME , a.ORDINAL_POSITION
                """;
        Map mapParams = new HashMap();
        mapParams.put("tables", tables);
        return getListData(sql, mapParams, DbColumnBean.class);
    }

    public List<AttributeConfigDto> getAttributes(String tableName, String functionCode) {
        StringBuilder sql = new StringBuilder("""
                    SELECT
                        a.attributes
                    FROM sys_config_object_attributes a
                    WHERE IFNULL(a.is_deleted, :activeStatus) = :activeStatus
                    AND lower(a.table_name) = :tableName
                """);
        HashMap<String, Object> params = new HashMap<>();
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        params.put("tableName", tableName.toLowerCase());
        if (Utils.isNullOrEmpty(functionCode)) {
            sql.append(" AND ifnull(a.function_code,'') = '' ");
        } else {
            sql.append(" AND lower(a.function_code) = :functionCode");
            params.put("functionCode", functionCode.toLowerCase());
        }
        String attributes = queryForObject(sql.toString(), params, String.class);
        return Utils.fromJsonList(attributes, AttributeConfigDto.class);
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

    public List<AttachmentFileDto> getListObjects(String tableName, String idColumn, String columnName) {
        String sql = MessageFormat.format("select {0} as objectId, {1} as fileContent from {2}", idColumn, columnName, tableName);
        return getListData(sql, new HashMap<>(), AttachmentFileDto.class);
    }

    public boolean checkOrgContains(List<Long> orgPermissionIds, Long orgId) {
        String sql = """
                select 1 from hr_organizations org 
                where org.organization_id in (:orgPermissionIds)
                and org.is_deleted = 'N'
                and (select path_id from hr_organizations where organization_id = :orgId) like concat(org.path_id, '%')
                limit 1
                """;
        Map mapParams = new HashMap();
        mapParams.put("orgPermissionIds", orgPermissionIds);
        mapParams.put("orgId", orgId);
        return queryForObject(sql, mapParams, Integer.class) != null;
    }

    public List<String> getListTables(Connection conn) {
        String sql = """
                select a.table_name from information_schema.`TABLES` a where a.table_schema = DATABASE()
                and a.TABLE_TYPE = 'BASE TABLE'
                order by a.table_name;
                """;
        if (conn != null) {
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {
                List<String> tables = new ArrayList<>();
                while (rs.next()) {
                    tables.add(rs.getString("table_name"));
                }
                return tables;
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        } else {
            return getListData(sql, new HashMap<>(), String.class);
        }
    }

    public String getDdlCreateTable(String tableName, Connection conn) {

        String sql = "SHOW CREATE TABLE " + tableName;
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                return rs.getString(2);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    public List<DbColumnBean> getListTableColumn(String tableName, Connection conn) {
        String sql = """
                               SELECT
                               column_name columnName,
                               CONCAT('ALTER TABLE `', TABLE_NAME, '` MODIFY COLUMN `', COLUMN_NAME, '` ',
                                 COLUMN_TYPE,
                                 ' ', IF(IS_NULLABLE = 'NO', 'NOT NULL', 'NULL'),
                                 IF(COLUMN_DEFAULT <> 'NULL', CONCAT(' DEFAULT ', COLUMN_DEFAULT, ''), ''),
                                 IF(COLUMN_COMMENT != '', CONCAT(' COMMENT \\'', REPLACE(COLUMN_COMMENT, '\\'', '\\\\\\''), '\\''), ''),
                                 ';'
                               ) AS modifyStatement,                             
                               CONCAT('ALTER TABLE `', TABLE_NAME, '` ADD COLUMN `', COLUMN_NAME, '` ',
                                 COLUMN_TYPE,
                                 ' ', IF(IS_NULLABLE = 'NO', 'NOT NULL', 'NULL'),
                                 IF(COLUMN_DEFAULT <> 'NULL', CONCAT(' DEFAULT ', COLUMN_DEFAULT, ''), ''),
                                 IF(COLUMN_COMMENT != '', CONCAT(' COMMENT \\'', REPLACE(COLUMN_COMMENT, '\\'', '\\\\\\''), '\\''), ''),
                                 ';'
                               ) AS addStatement                             
                             FROM information_schema.columns 
                             WHERE table_schema = DATABASE()
                             and table_name =
                             """ + "'" + tableName + "' order by column_name";
        if (conn == null) {
            return getListData(sql, new HashMap<>(), DbColumnBean.class);
        } else {
            List listColumns = new ArrayList();
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {

                while (rs.next()) {
                    DbColumnBean bean = new DbColumnBean();
                    bean.setColumnName(rs.getString("columnName"));
                    bean.setModifyStatement(rs.getString("modifyStatement"));
                    bean.setAddStatement(rs.getString("addStatement"));
                    listColumns.add(bean);
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            return listColumns;
        }
    }
}
