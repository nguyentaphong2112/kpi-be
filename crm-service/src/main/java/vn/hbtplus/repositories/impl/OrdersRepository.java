/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.repositories.impl;

import org.springframework.stereotype.Repository;
import lombok.RequiredArgsConstructor;
import vn.hbtplus.models.BaseDataTableDto;
import vn.hbtplus.repositories.BaseRepository;
import vn.hbtplus.models.request.OrdersRequest;
import vn.hbtplus.models.response.OrdersResponse;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.utils.QueryUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Lop repository Impl ung voi bang crm_orders
 *
 * @author tudd
 * @version 1.0
 * @since 1.0
 */

@Repository
@RequiredArgsConstructor
public class OrdersRepository extends BaseRepository {

    public BaseDataTableDto searchData(OrdersRequest.SearchForm dto) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    a.order_id,
                    a.order_no,
                    a.customer_id,
                    a.order_date,
                    a.total_amount,
                    a.discount_amount,
                    a.final_amount,
                    a.is_deleted,
                    a.created_by,
                    a.created_time,
                    a.modified_by,
                    a.modified_time,
                    a.last_update_time,
                    a.tax_amount,
                    a.tax_rate,
                    a.sale_staff_id,
                    a.province_id,
                    a.district_id,
                    a.ward_id,
                    a.village_address,
                    c.full_name,
                    c.mobile_number,
                    pm.referralFee,
                    pm.careFee,
                    pm.welfareFee,
                    IFNULL(cp.collectedAmount, 0) as collectedAmount
                """);
        HashMap<String, Object> params = new HashMap<>();
        params.put("phiDonHang", BaseConstants.CATEGORY_CODES.PHI_DON_HANG);
        addCondition(sql, params, dto);
        return getListPagination(sql.toString(), params, dto, OrdersResponse.class);
    }

    public List<Map<String, Object>> getListExport(OrdersRequest.SearchForm dto) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    a.order_id,
                    a.order_no,
                    a.customer_id,
                    a.order_date,
                    a.total_amount,
                    a.discount_amount,
                    a.final_amount,
                    a.is_deleted,
                    a.created_by,
                    a.created_time,
                    a.modified_by,
                    a.modified_time,
                    a.last_update_time,
                    a.tax_amount,
                    a.tax_rate,
                    a.sale_staff_id,
                    a.province_id,
                    a.district_id,
                    a.ward_id,
                    a.village_address,
                    c.full_name,
                    pm.referralFee,
                    pm.careFee,
                    pm.welfareFee,
                    c.mobile_number,
                    IFNULL(cp.collectedAmount, 0) as collectedAmount,
                    (IFNULL(a.final_amount, 0) - IFNULL(cp.collectedAmount, 0)) as remainingAmount
                """);
        Map<String, Object> params = new HashMap<>();
        params.put("phiDonHang", BaseConstants.CATEGORY_CODES.PHI_DON_HANG);
        addCondition(sql, params, dto);
        return getListData(sql.toString(), params);
    }

    private void addCondition(StringBuilder sql, Map<String, Object> params, OrdersRequest.SearchForm dto) {
        sql.append("""
                FROM crm_orders a
                JOIN crm_customers c on c.customer_id = a.customer_id
                LEFT JOIN
                    (SELECT p.order_id, SUM(p.amount) AS collectedAmount
                     FROM crm_payments p
                     WHERE IFNULL(p.is_deleted, :activeStatus) = :activeStatus
                     AND p.payment_type = :phiDonHang
                     GROUP BY p.order_id) cp ON a.order_id = cp.order_id
                left join(
                    select p.order_id, 
                    sum(p.referral_fee) referralFee,
                    sum(p.care_fee) careFee,
                    sum(p.welfare_fee) welfareFee
                    from crm_order_payables p
                    where p.is_deleted = :activeStatus
                    and p.status_id = 'PHE_DUYET'
                    group by p.order_id
                ) pm on a.order_id = pm.order_id
                WHERE IFNULL(a.is_deleted, :activeStatus) = :activeStatus
                """);
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        QueryUtils.filter(dto.getKeySearch(), sql, params, "a.order_no", "c.full_name", "c.mobile_number");
        QueryUtils.filter(dto.getCustomerId(), sql, params, "a.customer_id");
        QueryUtils.filter(dto.getSaleStaffId(), sql, params, "a.sale_staff_id");
        QueryUtils.filter(dto.getFullNameFilter(), sql, params, "c.full_name");
        QueryUtils.filter(dto.getMobileNumberFilter(), sql, params, "c.mobile_number");
        QueryUtils.filter(dto.getOrderNoFilter(), sql, params, "a.order_no");
        QueryUtils.filterExpression(dto.getFinalAmountFilter(), sql, params, "a.final_amount");
        QueryUtils.filterExpression(dto.getCollectedAmountFilter(), sql, params, "IFNULL(cp.collectedAmount, 0)");
        QueryUtils.filterExpression(dto.getReferralFeeFilter(), sql, params, "pm.referralFee");
        QueryUtils.filterExpression(dto.getCareFeeFilter(), sql, params, "pm.careFee");
        QueryUtils.filterExpression(dto.getWelfareFeeFilter(), sql, params, "pm.welfareFee");
        QueryUtils.filterExpression(dto.getRemainingAmountFilter(), sql, params, "(IFNULL(a.final_amount, 0) - IFNULL(cp.collectedAmount, 0))");
        sql.append(" ORDER BY a.created_time desc");
    }

    public String getMaxOrderNo(String prefix) {
        String sql = """
                SELECT order_no
                FROM crm_orders a
                ORDER BY CAST(SUBSTRING(order_no, :prefixLength) AS UNSIGNED) DESC
                LIMIT 1
                """;
        Map<String, Object> params = new HashMap<>();
        params.put("prefixLength", prefix.length() + 1);
        return queryForObject(sql, params, String.class);
    }
}
