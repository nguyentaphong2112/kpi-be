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
import vn.hbtplus.models.request.PaymentsRequest;
import vn.hbtplus.models.response.PaymentsResponse;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.utils.Utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Lop repository Impl ung voi bang crm_payments
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Repository
@RequiredArgsConstructor
public class PaymentsRepository extends BaseRepository {

    public BaseDataTableDto searchData(PaymentsRequest.SearchForm dto) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    a.payment_id,
                    a.order_id,
                    a.payment_date,
                    a.amount,
                    a.payment_method,
                    a.payment_type,
                    a.account_no,
                    a.bank_name,
                    a.bank_fee,
                    a.note,
                    a.is_deleted,
                    a.created_by,
                    a.created_time,
                    a.modified_by,
                    a.modified_time,
                    a.last_update_time
                """);
        HashMap<String, Object> params = new HashMap<>();
        addCondition(sql, params, dto);
        return getListPagination(sql.toString(), params, dto, PaymentsResponse.class);
    }

    public List<Map<String, Object>> getListExport(PaymentsRequest.SearchForm dto) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    a.payment_id,
                    a.order_id,
                    a.payment_date,
                    a.amount,
                    a.payment_method,
                    a.payment_type,
                    a.account_no,
                    a.bank_name,
                    a.bank_fee,
                    a.note,
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

    private void addCondition(StringBuilder sql, Map<String, Object> params, PaymentsRequest.SearchForm dto) {
        sql.append("""
            FROM crm_payments a
            
            
            WHERE IFNULL(a.is_deleted, :activeStatus) = :activeStatus
        """);
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        //QueryUtils.filterGe(dto.getFromDate(), sql, params, "a.from_date", "fromDate");
        //QueryUtils.filterLe(dto.getToDate(), sql, params, "a.from_date", "toDate");
        sql.append(" ORDER BY mo.display_seq, mo.path_id");
    }

    public void deletePayments(List<Long> paymentIds, Long orderId) {
        String sql = """
                update crm_payments a
                set a.is_deleted = 'Y', a.modified_by = :userName, a.modified_time = now()
                where a.is_deleted = 'N'
                and a.order_id = :orderId
                """;
        Map params = new HashMap();
        if (Utils.isNullOrEmpty(paymentIds)) {
            sql = sql + " and a.payment_id in (:ids)";
            params.put("ids", paymentIds);
        }
        params.put("userName", Utils.getUserNameLogin());
        params.put("orderId", orderId);
        executeSqlDatabase(sql, params);
    }

    public List<PaymentsResponse> getPayments(Long id) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    a.payment_id,
                    a.order_id,
                    a.payment_date,
                    a.caregiver_id,
                    a.welfare_recipient_id,
                    a.introducer_id,
                    c1.full_name caregiver_name,
                    c2.full_name welfare_recipient_name,
                    c.full_name as introducer_name,
                    a.amount,
                    a.payment_method,
                    a.payment_type,
                    a.account_no,
                    a.bank_name,
                    a.bank_fee,
                    a.note,
                    a.is_deleted,
                    a.created_by,
                    a.created_time,
                    a.modified_by,
                    a.modified_time,
                    a.last_update_time,
                    (select sc.name from sys_categories sc where sc.value = a.payment_type and sc.category_type = :paymentType) paymentTypeName
                FROM crm_payments a
                LEFT JOIN crm_customers c1 on c1.customer_id = a.caregiver_id
                LEFT JOIN crm_customers c2 on c2.customer_id = a.welfare_recipient_id
                LEFT JOIN crm_customers c on c.customer_id = a.introducer_id
                WHERE IFNULL(a.is_deleted, :activeStatus) = :activeStatus
                and a.order_id = :orderId
                """);
        HashMap<String, Object> params = new HashMap<>();
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        params.put("paymentType", Constant.CATEGORY_TYPES.CRM_LOAI_THANH_TOAN);
        params.put("orderId", id);
        return getListData(sql.toString(), params, PaymentsResponse.class);
    }
}
