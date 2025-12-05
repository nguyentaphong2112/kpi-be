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
import vn.hbtplus.models.request.OrderDetailsRequest;
import vn.hbtplus.models.response.OrderDetailsResponse;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.utils.Utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Lop repository Impl ung voi bang crm_order_details
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Repository
@RequiredArgsConstructor
public class OrderDetailsRepository extends BaseRepository {

    public BaseDataTableDto searchData(OrderDetailsRequest.SearchForm dto) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    a.order_detail_id,
                    a.order_id,
                    a.product_id,
                    a.quantity,
                    a.discount,
                    a.discount_type,
                    a.unit_price,
                    a.total_price,
                    a.is_deleted,
                    a.created_by,
                    a.created_time,
                    a.modified_by,
                    a.modified_time,
                    a.last_update_time
                """);
        HashMap<String, Object> params = new HashMap<>();
        addCondition(sql, params, dto);
        return getListPagination(sql.toString(), params, dto, OrderDetailsResponse.class);
    }

    public List<Map<String, Object>> getListExport(OrderDetailsRequest.SearchForm dto) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    a.order_detail_id,
                    a.order_id,
                    a.product_id,
                    a.quantity,
                    a.discount,
                    a.discount_type,
                    a.unit_price,
                    a.total_price,
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

    private void addCondition(StringBuilder sql, Map<String, Object> params, OrderDetailsRequest.SearchForm dto) {
        sql.append("""
            FROM crm_order_details a
            
            
            WHERE IFNULL(a.is_deleted, :activeStatus) = :activeStatus
        """);
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        //QueryUtils.filterGe(dto.getFromDate(), sql, params, "a.from_date", "fromDate");
        //QueryUtils.filterLe(dto.getToDate(), sql, params, "a.from_date", "toDate");
        sql.append(" ORDER BY mo.display_seq, mo.path_id");
    }

    public void deleteOrderDetails(List<Long> orderDetailIds, Long orderId) {
        String sql = """
                update crm_order_details a
                set a.is_deleted = 'Y', a.modified_by = :userName, a.modified_time = now()
                where a.is_deleted = 'N'
                and a.order_id = :orderId
                """;
        Map params = new HashMap();
        if (Utils.isNullOrEmpty(orderDetailIds)) {
            sql = sql + " and a.order_detail_id in (:ids)";
            params.put("ids", orderDetailIds);
        }
        params.put("userName", Utils.getUserNameLogin());
        params.put("orderId", orderId);
        executeSqlDatabase(sql, params);
    }

    public List<OrderDetailsResponse> getOrderDetailsByOrderId(Long id) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    a.order_detail_id,
                    a.order_id,
                    a.product_id,
                    a.quantity,
                    a.discount,
                    a.discount_type,
                    a.unit_price,
                    a.total_price,
                    a.is_deleted,
                    a.created_by,
                    a.created_time,
                    a.modified_by,
                    a.modified_time,
                    a.last_update_time,
                    (select sc.name from sys_categories sc where sc.value = p.unit_id and sc.category_type = :unitType) unitName
                FROM crm_order_details a
                JOIN crm_products p on p.product_id = a.product_id
                WHERE IFNULL(a.is_deleted, :activeStatus) = :activeStatus
                and a.order_id = :orderId
                """);
        HashMap<String, Object> params = new HashMap<>();
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        params.put("unitType", Constant.CATEGORY_TYPES.CRM_DVT_SAN_PHAM);
        params.put("orderId", id);
        return getListData(sql.toString(), params, OrderDetailsResponse.class);
    }
}
