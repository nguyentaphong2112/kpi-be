package vn.hbtplus.repositories.impl;

import org.springframework.stereotype.Repository;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.models.BaseDataTableDto;
import vn.hbtplus.models.dto.CategoryTypeDto;
import vn.hbtplus.models.request.CategoryRequest;
import vn.hbtplus.models.request.CategoryTypeRequest;
import vn.hbtplus.models.response.CategoryResponse;
import vn.hbtplus.models.response.CategoryTypeResponse;
import vn.hbtplus.repositories.BaseRepository;
import vn.hbtplus.utils.QueryUtils;
import vn.hbtplus.utils.Utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class CategoryTypeRepository extends BaseRepository {
    public List<CategoryTypeDto> getListCategoryType(String groupType) {
        StringBuilder sql = new StringBuilder("""
                     select category_type_id, name, code, is_auto_increase, attributes
                     from sys_category_types
                     where is_deleted = 'N'
                 """);
        Map<String, Object> map = new HashMap<>();
        QueryUtils.filter(groupType, sql, map, "group_type");
        sql.append("  order by order_number, name");
        return getListData(sql.toString(), map, CategoryTypeDto.class);
    }

    public CategoryTypeDto getCategoryType(String categoryType) {
        String sql = """
                 select category_type_id, name, code, is_auto_increase, attributes
                 from sys_category_types
                 where is_deleted = 'N'
                 and code = :categoryType
                 """;
        Map<String, Object> map = new HashMap<>();
        map.put("categoryType", categoryType);
        return getFirstData(sql, map, CategoryTypeDto.class);
    }

    public BaseDataTableDto<CategoryTypeResponse.SearchResult> searchData(CategoryTypeRequest.SearchForm dto) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    a.category_type_id,
                    a.code,
                    a.name,
                    a.order_number,
                    a.attributes,
                    CASE
                        WHEN a.is_auto_increase = 'Y'
                        THEN 'Có' ELSE 'Không'
                    END isAutoIncreaseName,
                    a.is_deleted,
                    a.created_by,
                    a.created_time,
                    a.modified_by,
                    a.modified_time,
                    a.last_update_time
                """);
        HashMap<String, Object> params = new HashMap<>();
        addCondition(sql, params, dto);
        return getListPagination(sql.toString(), params, dto, CategoryTypeResponse.SearchResult.class);
    }

    private void addCondition(StringBuilder sql, Map<String, Object> params, CategoryTypeRequest.SearchForm dto) {
        sql.append("""
                    FROM sys_category_types a
                    WHERE IFNULL(a.is_deleted, :activeStatus) = :activeStatus
                """);
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        if (!Utils.isNullOrEmpty(dto.getKeySearch())) {
            sql.append(" AND (lower(a.code) like :keySearch or lower(a.name) like :keySearch)");
            params.put("keySearch", "%" + dto.getKeySearch().toLowerCase() + "%");
        }
        params.put("maxInteger", Integer.MAX_VALUE);
        sql.append(" ORDER BY ifnull(a.order_number,:maxInteger)");
    }

    public List<Map<String, Object>> getListExport(CategoryTypeRequest.SearchForm dto) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    a.category_type_id,
                    a.code,
                    a.name,
                    a.order_number,
                    a.attributes,
                    CASE
                        WHEN a.is_auto_increase = 'Y'
                        THEN 'Có' ELSE 'Không'
                    END isAutoIncreaseName,
                    a.is_deleted,
                    a.created_by,
                    a.created_time,
                    a.modified_by,
                    a.modified_time,
                    a.last_update_time
                """);
        HashMap<String, Object> params = new HashMap<>();
        addCondition(sql, params, dto);
        return getListData(sql.toString(), params);
    }
}
