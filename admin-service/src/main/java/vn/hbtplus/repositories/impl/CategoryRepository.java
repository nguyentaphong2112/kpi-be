/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.repositories.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.constants.Constant;
import vn.hbtplus.models.BaseDataTableDto;
import vn.hbtplus.models.dto.CategoryDto;
import vn.hbtplus.models.request.CategoryRequest;
import vn.hbtplus.models.response.CategoryResponse;
import vn.hbtplus.repositories.BaseRepository;
import vn.hbtplus.repositories.entity.CategoryAttributeEntity;
import vn.hbtplus.utils.Utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Lop repository Impl ung voi bang sys_categories
 *
 * @author tudd
 * @version 1.0
 * @since 1.0
 */

@Repository
@RequiredArgsConstructor
public class CategoryRepository extends BaseRepository {

    public BaseDataTableDto searchData(String categoryType, CategoryRequest.SearchForm dto) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    a.category_id,
                    a.category_type,
                    a.code,
                    a.name,
                    a.value,
                    a.order_number,
                    a.note,
                    a.is_deleted,
                    a.created_by,
                    a.created_time,
                    a.modified_by,
                    a.modified_time,
                    a.last_update_time
                """);
        HashMap<String, Object> params = new HashMap<>();
        addCondition(sql, params, categoryType, dto);
        return getListPagination(sql.toString(), params, dto, CategoryResponse.SearchResult.class);
    }

    public List<Map<String, Object>> getListExport(String categoryType, CategoryRequest.SearchForm dto) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    a.category_id,
                    a.category_type,
                    a.code,
                    a.name,
                    a.value,
                    a.order_number,
                    a.note,
                    a.is_deleted,
                    a.created_by,
                    a.created_time,
                    a.modified_by,
                    a.modified_time,
                    a.last_update_time
                """);
        Map<String, Object> params = new HashMap<>();
        addCondition(sql, params, categoryType, dto);
        return getListData(sql.toString(), params);
    }

    private void addCondition(StringBuilder sql, Map<String, Object> params, String categoryType, CategoryRequest.SearchForm dto) {
        sql.append("""
                    FROM sys_categories a
                    WHERE IFNULL(a.is_deleted, :activeStatus) = :activeStatus
                    and a.category_type = :categoryType
                """);
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        params.put("categoryType", categoryType);
        if (!Utils.isNullOrEmpty(dto.getKeySearch())) {
            sql.append(" AND (lower(a.code) like :keySearch or lower(a.name) like :keySearch)");
            params.put("keySearch", "%" + dto.getKeySearch().toLowerCase() + "%");
        }
        params.put("maxInteger", Integer.MAX_VALUE);
        sql.append(" ORDER BY ifnull(a.order_number,:maxInteger)");
    }

    public List<CategoryDto> getListCategories(String categoryType) {
        return getListCategories(categoryType, false);
    }

    public List<CategoryDto> getListCategories(String categoryType, boolean isActive) {
        String sql = """
                select sc.value, sc.name, sc.code, sc.order_number, sc.category_id
                from sys_categories sc 
                where sc.is_deleted = 'N'
                  and sc.category_type = :categoryType
                """;
        if (isActive) {
            sql += " and not exists (" +
                   "    select 1 from sys_category_attributes sct1" +
                   "    where sct1.category_id = sc.category_id" +
                   "    and sct1.attribute_code = 'NGAY_HET_HIEU_LUC'" +
                   "    and sct1.is_deleted = 'N'" +
                   "    and str_to_date(sct1.attribute_value,'%d/%m/%Y') <= DATE(now())" +
                   ")";
        }
        Map<String, Object> map = new HashMap<>();
        map.put("categoryType", categoryType);
        map.put("maxInteger", Integer.MAX_VALUE);
        sql += "order by ifnull(sc.order_number,:maxInteger), name";
        return getListData(sql, map, CategoryDto.class);
    }

    public List<CategoryDto> getListCategories(String categoryType, List<String> ids) {
        StringBuilder sql = new StringBuilder("""
                select value, name, code, order_number, category_id
                from sys_categories
                where is_deleted = 'N'
                  and category_type = :categoryType                   
                """);
        Map<String, Object> map = new HashMap<>();
        map.put("categoryType", categoryType);
        if (!Utils.isNullOrEmpty(ids)) {
            sql.append(" and value in (:ids)");
            map.put("ids", ids);
        }
        sql.append(" order by ifnull(order_number,:maxInteger), name, category_id");
        map.put("maxInteger", Integer.MAX_VALUE);
        return getListData(sql.toString(), map, CategoryDto.class);
    }

    public List<CategoryAttributeEntity> getAllAttributeByCategoryType(String categoryType) {
        String sql = """
                select attribute_code, attribute_value, data_type, category_id
                from  sys_category_attributes a
                where a.category_id in (
                    select category_id from sys_categories sc 
                    where sc.category_type = :categoryType
                )
                """;
        Map<String, Object> map = new HashMap<>();
        map.put("categoryType", categoryType);
        return getListData(sql, map, CategoryAttributeEntity.class);
    }

    public List<CategoryResponse.AttributeDto> getAttributeOfCategory(Long categoryId) {
        String sql = """
                select attribute_code, attribute_value, data_type, category_id
                from  sys_category_attributes a
                where a.category_id = :categoryId
                """;
        Map<String, Object> map = new HashMap<>();
        map.put("categoryId", categoryId);
        return getListData(sql, map, CategoryResponse.AttributeDto.class);

    }

    public boolean checkDuplicateValueCategory(String categoryType, String value, Long categoryId) {
        StringBuilder sql = new StringBuilder("""
                    select count(1) 
                    from sys_categories
                    where category_type = :categoryType
                    and value = :value
                    and is_deleted = 'N'
                """);
        Map<String, Object> map = new HashMap<>();
        map.put("categoryType", categoryType);
        map.put("value", value);
        if (categoryId != null && categoryId > 0L) {
            sql.append(" and category_id != :categoryId");
            map.put("categoryId", categoryId);
        }
        return queryForObject(sql.toString(), map, Integer.class) > 0;
    }

    public void deleteAttributes(Long categoryId) {
        String sql = """
                delete a from sys_category_attributes a 
                where a.category_id = :categoryId                
                """;
        Map<String, Object> map = new HashMap<>();
        map.put("categoryId", categoryId);
        executeSqlDatabase(sql, map);
    }

    public Long getNextValueCategory(String categoryType) {
        String sql = """
                    select
                    max(cast(value as INTEGER)) nextValue
                    from sys_categories
                    where category_type = :categoryType
                """;
        Map<String, Object> map = new HashMap<>();
        map.put("categoryType", categoryType);
        return Utils.NVL(queryForObject(sql, map, Long.class)) + 1;
    }

    public BaseDataTableDto<CategoryDto> getPageable(String categoryType, CategoryRequest.PageableRequest request) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    a.category_id,
                    a.code,
                    a.name,
                    a.value,
                    a.order_number
                from sys_categories a
                where a.category_type = :categoryType
                """);
        HashMap<String, Object> params = new HashMap<>();
        params.put("categoryType", categoryType);
        if (!Utils.isNullOrEmpty(request.getKeySearch())) {
            sql.append(" and (lower(a.code) like :keySearch or lower(a.name) like :keySearch)");
            params.put("keySearch", "%" + request.getKeySearch().trim().toLowerCase() + "%");
        }
        sql.append(" order by ifnull(a.order_number,:maxInteger), a.name, a.category_id");
        params.put("maxInteger", Integer.MAX_VALUE);
        return getListPagination(sql.toString(), params, request, CategoryDto.class);
    }


    public BaseDataTableDto<CategoryDto> getPageableKeyCheck(String categoryType, CategoryRequest.PageableRequest request) {
        HashMap<String, Object> params = new HashMap<>();
        StringBuilder sql = new StringBuilder("""
                SELECT * FROM (
                    SELECT
                """);
        if (!Utils.isNullOrEmpty(request.getSelectedValue())) {
            sql.append("""
                    CASE
                        WHEN a.value IN (:selectedValue) THEN 1
                        ELSE 0
                    END AS valueSelect,""");
            params.put("selectedValue", request.getSelectedValue());
        }
        sql.append("""
                            a.category_id,
                            a.code,
                            a.name,
                            a.value,
                            a.order_number
                    FROM sys_categories a
                WHERE a.category_type = :categoryType) b
                """);
        params.put("categoryType", categoryType);
        if (!Utils.isNullOrEmpty(request.getKeySearch())) {
            sql.append(" WHERE (lower(b.code) like :keySearch or lower(b.name) like :keySearch)");
            params.put("keySearch", "%" + request.getKeySearch().trim().toLowerCase() + "%");
        }
        if (!Utils.isNullOrEmpty(request.getSelectedValue())) {
            sql.append(" ORDER BY b.valueSelect DESC");
        }
        return getListPagination(sql.toString(), params, request, CategoryDto.class);
    }

    public List<CategoryDto> getListDistrict() {
        String sql = """
                select a.value, CONCAT(a.name, ' - ', p.name) as name
                from sys_categories a, sys_categories p
                where a.category_type = :districtTypeCode
                and p.category_type = :provinceTypeCode
                and p.value = (
                    select ct.attribute_value from sys_category_attributes ct
                    where ct.category_id = a.category_id
                    and ct.attribute_code = :provinceAttributeCode
                    and ct.is_deleted = 'N'
                ) 
                and a.is_deleted = 'N'
                and p.is_deleted = 'N'
                order by p.name, a.name
                """;
        Map mapParams = new HashMap();
        mapParams.put("provinceTypeCode", Constant.CATEGORY_TYPE.TINH);
        mapParams.put("districtTypeCode", Constant.CATEGORY_TYPE.HUYEN);
        mapParams.put("wardTypeCode", Constant.CATEGORY_TYPE.XA);
        mapParams.put("provinceAttributeCode", Constant.ATTRIBUTE_CODES.MA_TINH);

        return getListData(sql, mapParams, CategoryDto.class);
    }

    public List<CategoryDto> getListDistrictByProvince(String provinceId) {
        String sql = """
                select a.value, a.name as name
                from sys_categories a, sys_categories p
                where a.category_type = :districtTypeCode
                and p.category_type = :provinceTypeCode
                and p.value = (
                    select ct.attribute_value from sys_category_attributes ct
                    where ct.category_id = a.category_id
                    and ct.attribute_code = :provinceAttributeCode
                    and ct.is_deleted = 'N'
                ) 
                and p.value = :provinceId
                and a.is_deleted = 'N'
                and p.is_deleted = 'N'
                order by a.name
                """;
        Map mapParams = new HashMap();
        mapParams.put("provinceId", provinceId);
        mapParams.put("provinceTypeCode", Constant.CATEGORY_TYPE.TINH);
        mapParams.put("districtTypeCode", Constant.CATEGORY_TYPE.HUYEN);
        mapParams.put("wardTypeCode", Constant.CATEGORY_TYPE.XA);
        mapParams.put("provinceAttributeCode", Constant.ATTRIBUTE_CODES.MA_TINH);
        return getListData(sql, mapParams, CategoryDto.class);
    }

    public List<CategoryDto> getListWardByDistrict(String districtId) {
        String sql = """
                select a.value, a.name as name
                from sys_categories a, sys_categories p
                where a.category_type = :wardTypeCode
                and p.category_type = :districtTypeCode
                and p.value = (
                    select ct.attribute_value from sys_category_attributes ct
                    where ct.category_id = a.category_id
                    and ct.attribute_code = :districtAttributeCode
                    and ct.is_deleted = 'N'
                ) 
                and p.value = :districtId
                and a.is_deleted = 'N'
                and p.is_deleted = 'N'
                order by a.name
                """;
        Map mapParams = new HashMap();
        mapParams.put("districtId", districtId);
        mapParams.put("provinceTypeCode", Constant.CATEGORY_TYPE.TINH);
        mapParams.put("districtTypeCode", Constant.CATEGORY_TYPE.HUYEN);
        mapParams.put("wardTypeCode", Constant.CATEGORY_TYPE.XA);
        mapParams.put("districtAttributeCode", Constant.ATTRIBUTE_CODES.MA_HUYEN);
        return getListData(sql, mapParams, CategoryDto.class);
    }

    public List<CategoryDto> getListCategoriesByParent(String categoryType, String parentTypeCode, String parentValue) {
        String sql = """
                     select sc.value, sc.name, sc.code, sc.category_id
                     from sys_categories sc
                     where sc.is_deleted = 'N'
                     and sc.category_type = :categoryType
                     and exists (
                        select 1 from sys_category_attributes ct
                        where ct.category_id = sc.category_id
                        and ct.attribute_code = :parentTypeCode
                        and ct.is_deleted = 'N'
                        and ct.attribute_value = :parentValue
                     )
                     order by ifnull(sc.order_number,:maxInteger), name
                """;
        Map<String, Object> map = new HashMap<>();
        map.put("categoryType", categoryType);
        map.put("parentTypeCode", parentTypeCode);
        map.put("parentValue", parentValue);
        map.put("maxInteger", Integer.MAX_VALUE);
        return getListData(sql, map, CategoryDto.class);
    }

    public List<CategoryDto> getListWardByProvince(String provinceId, boolean isActive) {
        String sql = """
                select a.value, a.name as name
                from sys_categories a, sys_categories p
                where a.category_type = :wardTypeCode
                and p.category_type = :provinceTypeCode
                and p.value = (
                    select ct.attribute_value from sys_category_attributes ct
                    where ct.category_id = a.category_id
                    and ct.attribute_code = :provinceAttributeCode
                    and ct.is_deleted = 'N'
                ) 
                and p.value = :provinceId
                and a.is_deleted = 'N'
                and p.is_deleted = 'N'
                """;
        if (isActive) {
            sql += " and not exists (" +
                   "    select 1 from sys_category_attributes sct1" +
                   "    where sct1.category_id = a.category_id" +
                   "    and sct1.attribute_code = 'NGAY_HET_HIEU_LUC'" +
                   "    and sct1.is_deleted = 'N'" +
                   "    and str_to_date(sct1.attribute_value,'%d/%m/%Y') <= DATE(now())" +
                   ")";
        }

        sql += " order by a.name COLLATE utf8mb4_vietnamese_ci";
        Map mapParams = new HashMap();
        mapParams.put("provinceId", provinceId);
        mapParams.put("provinceTypeCode", Constant.CATEGORY_TYPE.TINH);
        mapParams.put("districtTypeCode", Constant.CATEGORY_TYPE.HUYEN);
        mapParams.put("wardTypeCode", Constant.CATEGORY_TYPE.XA);
        mapParams.put("provinceAttributeCode", Constant.ATTRIBUTE_CODES.MA_TINH);
        return getListData(sql, mapParams, CategoryDto.class);
    }
}
