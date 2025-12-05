/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.repositories.impl;

import org.springframework.stereotype.Repository;
import lombok.RequiredArgsConstructor;
import vn.hbtplus.constants.Constant;
import vn.hbtplus.models.BaseDataTableDto;
import vn.hbtplus.repositories.BaseRepository;
import vn.hbtplus.models.request.ProductsRequest;
import vn.hbtplus.models.response.ProductsResponse;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.utils.QueryUtils;
import vn.hbtplus.utils.Utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Lop repository Impl ung voi bang crm_products
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Repository
@RequiredArgsConstructor
public class ProductsRepository extends BaseRepository {

    public BaseDataTableDto searchData(ProductsRequest.SearchForm dto) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    a.product_id,
                    a.name,
                    a.code,
                    a.unit_id,
                    (select sc.name from sys_categories sc where sc.value = a.unit_id and sc.category_type = :unitType) unitName,
                    a.unit_price,
                    a.status_id,
                    (select sc.name from sys_categories sc where sc.value = a.status_id and sc.category_type = :statusType) statusName,
                    a.category_id,
                    (select sc.name from sys_categories sc where sc.value = a.category_id and sc.category_type = :categoryType) categoryName,
                    a.is_deleted,
                    a.created_by,
                    a.created_time,
                    a.modified_by,
                    a.modified_time,
                    a.last_update_time
                """);
        HashMap<String, Object> params = new HashMap<>();
        addCondition(sql, params, dto);
        return getListPagination(sql.toString(), params, dto, ProductsResponse.class);
    }

    public List<Map<String, Object>> getListExport(ProductsRequest.SearchForm dto) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    a.product_id,
                    a.name,
                    a.code,
                    a.unit_id,
                    (select sc.name from sys_categories sc where sc.value = a.unit_id and sc.category_type = :unitType) unitName,
                    a.unit_price,
                    a.status_id,
                    (select sc.name from sys_categories sc where sc.value = a.status_id and sc.category_type = :statusType) statusName,
                    a.category_id,
                    (select sc.name from sys_categories sc where sc.value = a.category_id and sc.category_type = :categoryType) categoryName,
                    a.is_deleted,
                    a.created_by,
                    a.created_time,
                    a.modified_by,
                    a.modified_time,
                    a.last_update_time
                """);
        Map<String, Object> params = new HashMap<>();
        addCondition(sql, params, dto);
        return getListData(sql.toString(), params);
    }

    private void addCondition(StringBuilder sql, Map<String, Object> params, ProductsRequest.SearchForm dto) {
        sql.append("""
            FROM crm_products a
            WHERE IFNULL(a.is_deleted, :activeStatus) = :activeStatus
        """);
        params.put("unitType", Constant.CATEGORY_TYPES.CRM_DVT_SAN_PHAM);
        params.put("statusType", Constant.CATEGORY_TYPES.CRM_TRANG_THAI_SAN_PHAM);
        params.put("categoryType", Constant.CATEGORY_TYPES.CRM_NHOM_SAN_PHAM);
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);

        QueryUtils.filter(dto.getKeySearch(), sql, params, "a.code", "a.name");


        if(!Utils.isNullOrEmpty(dto.getCategoryId())){
            sql.append(" AND a.category_id = :categoryId");
            params.put("categoryId", dto.getCategoryId());
        }

        if(!Utils.isNullOrEmpty(dto.getStatusId())){
            sql.append(" AND a.status_id = :statusId");
            params.put("statusId", dto.getStatusId());
        }

        sql.append(" ORDER BY a.product_id DESC");
        if (!Utils.isNullOrEmpty(dto.getSelectedValue())) {
            String sqlValueSelect = ("""
                SELECT
                    CASE
                        WHEN a.product_id IN (:selectedValue) THEN 1
                        ELSE 0
                    END AS valueSelect, """);
            sql.replace(0, sql.length(), sql.toString().replaceFirst("SELECT", sqlValueSelect).replaceFirst("(?s)(.*)" + "ORDER BY" + "(?!.*" + "ORDER BY" + ")", "$1" + "ORDER BY valueSelect DESC,"));
            params.put("selectedValue", dto.getSelectedValue());
        }
    }
}
