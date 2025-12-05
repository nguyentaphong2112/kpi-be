package vn.hbtplus.repositories.impl;

import org.springframework.stereotype.Repository;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.constants.Constant;
import vn.hbtplus.models.BaseDataTableDto;
import vn.hbtplus.models.dto.ConfigObjectAttributeDto;
import vn.hbtplus.models.request.ConfigObjectAttributeRequest;
import vn.hbtplus.models.request.ConfigPageRequest;
import vn.hbtplus.models.response.ConfigPageResponse;
import vn.hbtplus.repositories.BaseRepository;
import vn.hbtplus.repositories.entity.ConfigPageEntity;
import vn.hbtplus.utils.Utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class ConfigPageRepository extends BaseRepository {
    public ConfigPageEntity getEntityByUrl(String url) {
        String sql = "select a.* from sys_config_pages a " +
                     " where a.url like :url" +
                     " and a.is_deleted = 'N'";
        Map map = new HashMap<>();
        map.put("url", url);
        return getFirstData(sql, map, ConfigPageEntity.class);
    }

    public BaseDataTableDto searchData(ConfigPageRequest.SearchForm dto) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    a.config_page_id,
                    a.url,
                    a.report_codes,
                    a.type,
                    a.created_by,
                    a.created_time,
                    a.modified_by,
                    a.modified_time
                """);
        HashMap<String, Object> params = new HashMap<>();
        addCondition(sql, params, dto);
        return getListPagination(sql.toString(), params, dto, ConfigPageResponse.SearchResult.class);
    }

    private void addCondition(StringBuilder sql, Map<String, Object> params, ConfigPageRequest.SearchForm dto) {
        sql.append("""
                    FROM sys_config_pages a
                    WHERE IFNULL(a.is_deleted, :activeStatus) = :activeStatus
                """);
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        if (!Utils.isNullOrEmpty(dto.getKeySearch())) {
            sql.append(" AND (lower(a.url) like :keySearch or lower(a.report_codes) like :keySearch)");
            params.put("keySearch", "%" + dto.getKeySearch().toLowerCase() + "%");
        }
//        sql.append(" ORDER BY a.order_number");
    }

    public List<Map<String, Object>> getListExport(ConfigPageRequest.SearchForm dto) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    a.config_page_id,
                    a.url,
                    a.report_codes,
                    a.created_by,
                    a.created_time,
                    a.modified_by,
                    a.modified_time
                """);
        Map<String, Object> params = new HashMap<>();
        addCondition(sql, params, dto);
        return getListData(sql.toString(), params);
    }
}
