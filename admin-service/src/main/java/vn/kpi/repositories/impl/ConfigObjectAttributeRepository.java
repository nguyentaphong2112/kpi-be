package vn.kpi.repositories.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import vn.kpi.constants.BaseConstants;
import vn.kpi.models.BaseDataTableDto;
import vn.kpi.models.dto.ConfigObjectAttributeDto;
import vn.kpi.models.request.ConfigObjectAttributeRequest;
import vn.kpi.models.response.ConfigObjectAttributeResponse;
import vn.kpi.repositories.BaseRepository;
import vn.kpi.utils.Utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class ConfigObjectAttributeRepository extends BaseRepository {


    public BaseDataTableDto searchData(ConfigObjectAttributeRequest.SearchForm dto) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    a.config_object_attribute_id,
                    a.table_name,
                    a.function_code,
                    a.name,
                    a.attributes,
                    a.is_deleted,
                    a.created_by,
                    a.created_time,
                    a.modified_by,
                    a.modified_time
                """);
        HashMap<String, Object> params = new HashMap<>();
        addCondition(sql, params, dto);
        return getListPagination(sql.toString(), params, dto, ConfigObjectAttributeDto.class);
    }


    private void addCondition(StringBuilder sql, Map<String, Object> params, ConfigObjectAttributeRequest.SearchForm dto) {
        sql.append("""
                    FROM sys_config_object_attributes a
                    WHERE IFNULL(a.is_deleted, :activeStatus) = :activeStatus
                """);
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        if (!Utils.isNullOrEmpty(dto.getKeySearch())) {
            sql.append(" AND (lower(a.table_name) like :keySearch or lower(a.name) like :keySearch)");
            params.put("keySearch", "%" + dto.getKeySearch().toLowerCase() + "%");
        }
//        sql.append(" ORDER BY a.order_number");
    }

    public List<ConfigObjectAttributeResponse.ListTableName> getTableName() {
        String sql = "select TABLE_NAME from information_schema.`TABLES`\n" +
                "where table_schema not in ('information_schema','mysql','performance_schema','sys')";
        return getListData(sql, new HashMap<>(), ConfigObjectAttributeResponse.ListTableName.class);
    }

    public ConfigObjectAttributeResponse.SearchByTableName searchDataByTableName(String tableName, String functionCode) {
        String sql = """
                    SELECT
                        a.table_name,
                        a.function_code,
                        a.name,
                        a.attributes
                    FROM sys_config_object_attributes a
                    WHERE IFNULL(a.is_deleted, :activeStatus) = :activeStatus
                    AND lower(a.table_name) = :tableName
                    AND lower(a.function_code) = :functionCode
                """;
        HashMap<String, Object> params = new HashMap<>();
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        params.put("tableName", tableName.toLowerCase());
        params.put("functionCode", functionCode.toLowerCase());
        return queryForObject(sql, params, ConfigObjectAttributeResponse.SearchByTableName.class);
    }

    public List<ConfigObjectAttributeResponse> getListConfigByCodes(List<String> codes) {
        String sql = "select * from sys_config_object_attributes a" +
                     " where a.is_deleted = :activeStatus" +
                     " and a.table_name in (:codes)" +
                     " order by a.name";
        HashMap<String, Object> params = new HashMap<>();
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        params.put("codes", codes);
        return getListData(sql, params, ConfigObjectAttributeResponse.class);
    }
}
