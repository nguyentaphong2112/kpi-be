/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.repositories.impl;

import org.springframework.stereotype.Repository;
import lombok.RequiredArgsConstructor;
import vn.hbtplus.constants.Constant;
import vn.hbtplus.models.BaseDataTableDto;
import vn.hbtplus.models.dto.FeeRatio;
import vn.hbtplus.models.dto.OrderDetailDto;
import vn.hbtplus.repositories.BaseRepository;
import vn.hbtplus.models.request.OrderPayablesRequest;
import vn.hbtplus.models.response.OrderPayablesResponse;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.repositories.entity.OrderPayablesEntity;
import vn.hbtplus.utils.QueryUtils;
import vn.hbtplus.utils.Utils;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Lop repository Impl ung voi bang crm_order_payables
 *
 * @author tudd
 * @version 1.0
 * @since 1.0
 */

@Repository
@RequiredArgsConstructor
public class OrderPayablesRepository extends BaseRepository {

    public BaseDataTableDto searchData(OrderPayablesRequest.SearchForm dto) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    a.order_payable_id,
                    a.order_id,
                    a.product_id,
                    a.customer_id,
                    a.status_id,
                    a.period_date,
                    a.referral_fee,
                    a.care_fee,
                    a.order_amount,
                    a.welfare_fee,
                    c.mobile_number,
                    c.full_name,
                    o.order_no,
                    d.name productName,
                    a.payment_date payment_date,
                    c2.mobile_number receivedPhoneNumber,
                    c2.full_name receivedName,
                    a.created_by,
                    a.created_time,
                    a.modified_by,
                    a.modified_time
                """);
        HashMap<String, Object> params = new HashMap<>();
        addCondition(sql, params, dto);
        return getListPagination(sql.toString(), params, dto, OrderPayablesResponse.class);
    }

    public List<Map<String, Object>> getListExport(OrderPayablesRequest.SearchForm dto) {
        StringBuilder sql = new StringBuilder("""
               SELECT
                    a.order_payable_id,
                    a.order_id,
                    a.product_id,
                    a.customer_id,
                    a.status_id,
                    (SELECT sc.name FROM sys_categories sc WHERE sc.code = a.status_id AND sc.category_type = :status) statusName,
                    date_format(a.period_date, '%m/%Y') period_date,
                    a.referral_fee,
                    a.care_fee,
                    a.order_amount,
                    a.welfare_fee,
                    c.mobile_number,
                    c.full_name,
                    o.order_no,
                    d.name productName,
                    o.order_date,
                    c2.mobile_number receivedPhoneNumber,
                    c2.full_name receivedName,
                    a.created_by,
                    a.created_time,
                    a.modified_by,
                    a.modified_time
                """);
        Map<String, Object> params = new HashMap<>();
        addCondition(sql, params, dto);
        return getListData(sql.toString(), params);
    }

    public List<OrderPayablesEntity> getListData(OrderPayablesRequest.SubmitForm dto) {
        StringBuilder sql = new StringBuilder("""
               SELECT
                    a.*
               FROM crm_order_payables a
               WHERE IFNULL(a.is_deleted, :activeStatus) = :activeStatus
               AND a.period_date >= :startDate
               AND a.period_date <= :endDate
               AND a.status_id = :status
                """);
        Map<String, Object> params = new HashMap<>();
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        params.put("startDate", Utils.getFirstDay(dto.getPeriodDate()));
        params.put("endDate", Utils.getLastDayOfMonth(dto.getPeriodDate()));
        params.put("status", OrderPayablesEntity.STATUS.CHO_PHE_DUYET);
        return getListData(sql.toString(), params, OrderPayablesEntity.class);
    }

    private void addCondition(StringBuilder sql, Map<String, Object> params, OrderPayablesRequest.SearchForm dto) {
        sql.append("""
                    FROM crm_order_payables a
                    JOIN crm_customers c ON a.customer_id = c.customer_id
                    LEFT JOIN crm_customers c2 ON a.receiver_id = c2.customer_id
                    JOIN crm_orders o ON o.order_id = a.order_id
                    JOIN crm_products d ON d.product_id = a.product_id
                    WHERE a.is_deleted = :activeStatus
                    AND a.period_date >= :startDate
                    AND a.period_date <= :endDate
                """);

        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        params.put("startDate", Utils.getFirstDay(dto.getStartPeriodDate()));
        params.put("endDate", Utils.getLastDayOfMonth(dto.getEndPeriodDate()));
        params.put("status", Constant.CATEGORY_TYPES.CRM_ORDER_PAYABLES_STATUS);
        QueryUtils.filter(dto.getStatusId(), sql, params, "a.status_id");
        QueryUtils.filter(dto.getKeySearch(), sql, params, "c.full_name", "c.mobile_number","c2.full_name", "c2.mobile_number", "o.order_no");
        QueryUtils.filter(dto.getMobileNumberFilter(), sql, params,  "c.mobile_number");
        QueryUtils.filter(dto.getFullNameFilter(), sql, params,  "c.full_name");
        QueryUtils.filter(dto.getReceivedPhoneNumberFilter(), sql, params,  "c2.mobile_number");
        QueryUtils.filter(dto.getReceivedNameFilter(), sql, params,  "c2.full_name");
        QueryUtils.filter(dto.getOrderNoFilter(), sql, params,  "o.order_no");
        QueryUtils.filter(dto.getProductNameFilter(), sql, params,  "d.name");
        QueryUtils.filterExpression(dto.getOrderAmountFilter(), sql, params,  "a.order_amount");
        QueryUtils.filterExpression(dto.getReferralFeeFilter(), sql, params,  "a.referral_fee");
        QueryUtils.filterExpression(dto.getCareFeeFilter(), sql, params,  "a.care_fee");
        QueryUtils.filterExpression(dto.getWelfareFeeFilter(), sql, params,  "a.welfare_fee");
    }

    public List<OrderDetailDto> getListOrderEntities(Date periodDate) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    pm.customer_receive_id,
                    pm.introducer_amount introducer_amount,
                    pm.caregiver_amount caregiver_amount,
                    pm.order_amount order_amount,
                    pm.welfare_recipient_amount welfare_recipient_amount,
                    pm.payment_date payment_date,
                    c.introducer_id customerIntroducerId,
                    od.product_id,
                    a.customer_id,
                    od.order_id,
                    op.referral_fee referralFeePayed,
                    op.welfare_fee welfareFeePayed,
                    op.care_fee careFeePayed,
                    c.receiver_id
                FROM crm_order_details od
                JOIN crm_orders a ON od.order_id = a.order_id
                JOIN crm_customers c ON a.customer_id = c.customer_id
                join (
                    SELECT
                        pm.order_id,
                        sum(pm.amount) as order_amount
                    from crm_payments pm
                    where pm.is_deleted = 'N'
                    and pm.payment_type = 'PHI_DON_HANG'
                    and last_day(pm.payment_date) <= :endDate
                    GROUP BY pm.order_id
                ) pp on pp.order_id = a.order_id and pp.order_amount >= 5000000
                JOIN (
                    select
                    	T.order_id,
                    	T.customer_receive_id,
                    	T.payment_id,
                    	T.payment_date,
                    	T.order_amount,
                    	sum(T.introducer_amount) introducer_amount,
                    	sum(T.caregiver_amount) caregiver_amount,
                    	sum(T.welfare_recipient_amount) welfare_recipient_amount
                    from (
                    SELECT
                    		pm.order_id,
                    		pm.introducer_id as customer_receive_id,
                    		pm.payment_id,
                    		pm.payment_date,
                    		pm.amount as order_amount,
                    		100 * pm.amount /(100+sp.config_value) introducer_amount,
                    		0 caregiver_amount,
                    		0 welfare_recipient_amount
                    from crm_payments pm
                    join sys_parameters sp on sp.config_code = 'TYLE_THUE_VAT' 
                    and pm.payment_date between sp.start_date and IFNULL(sp.end_date,pm.payment_date)
                    where pm.is_deleted = 'N'
                    and pm.introducer_id is not null
                    and last_day(pm.payment_date) <= :endDate
                    union all
                    SELECT
                    		pm.order_id,
                    		pm.caregiver_id as customer_receive_id,
                    		pm.payment_id,
                    		pm.payment_date,                    		
                    		pm.amount as order_amount,
                    		0 introducer_amount,
                    		100 * pm.amount /(100+sp.config_value) caregiver_amount,
                    		0 welfare_recipient_amount
                    from crm_payments pm
                    join sys_parameters sp on sp.config_code = 'TYLE_THUE_VAT' 
                    and pm.payment_date between sp.start_date and IFNULL(sp.end_date,pm.payment_date)
                    where pm.is_deleted = 'N'
                    and pm.caregiver_id is not null
                    and last_day(pm.payment_date) <= :endDate
                    union all
                    SELECT
                    		pm.order_id,
                    		pm.welfare_recipient_id as customer_receive_id,
                    		pm.payment_id,
                    		pm.payment_date,                    		
                    		pm.amount as order_amount,
                    		0 introducer_amount,
                    		0 caregiver_amount,
                    		100 * pm.amount /(100+sp.config_value) welfare_recipient_amount
                    from crm_payments pm
                    join sys_parameters sp on sp.config_code = 'TYLE_THUE_VAT' and pm.payment_date between sp.start_date and IFNULL(sp.end_date,pm.payment_date)
                    where pm.is_deleted = 'N'
                    and pm.welfare_recipient_id is not null
                    and last_day(pm.payment_date) <= :endDate
                    ) T
                    group by T.order_id,
                    	T.customer_receive_id,
                    	T.payment_id,
                    	T.payment_date,                    		
                    	T.order_amount
                ) pm ON pm.order_id = a.order_id
                LEFT JOIN (
                    select op1.order_id, sum(op1.referral_fee) as referral_fee,
                    sum(op1.welfare_fee) as welfare_fee, sum(op1.care_fee) as care_fee
                     from crm_order_payables op1
                    where op1.is_deleted = :activeStatus
                    and op1.status_id in (:trangThaiPheDuyet)
                    group by op1.order_id
                ) op on op.order_id = a.order_id
                WHERE od.is_deleted = :activeStatus
                """);
        HashMap<String, Object> params = new HashMap<>();
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        params.put("startDate", Utils.getFirstDay(periodDate));
        params.put("endDate", Utils.getLastDayOfMonth(periodDate));
        params.put("trangThaiPheDuyet", Arrays.asList(OrderPayablesEntity.STATUS.PHE_DUYET));
        return getListData(sql.toString(), params, OrderDetailDto.class);
    }

    public void deleteOldData(Date periodDate) {
        String sql = "update crm_order_payables a" +
                     " set a.is_deleted = 'Y', a.modified_by = :userName, a.modified_time = now()" +
                     " where a.period_date = :periodDate" +
                     " and a.is_deleted = 'N'" +
                     " and a.status_id not in (:trangThaiPheDuyet)";
        HashMap<String, Object> params = new HashMap<>();
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        params.put("trangThaiPheDuyet", Arrays.asList(OrderPayablesEntity.STATUS.PHE_DUYET));
        params.put("periodDate", Utils.getLastDayOfMonth(periodDate));
        params.put("userName", Utils.getUserNameLogin());
        executeSqlDatabase(sql, params);
    }

    public List<String> getListPrePeriodNotClosed(Date periodDate) {
        String sql = "select date_format(a.period_date, '%m/%Y') as periodDate " +
                     " from crm_order_payables a" +
                     " where a.is_deleted = 'N'" +
                     " and a.status_id not in (:trangThaiPheDuyet)" +
                     " and a.period_date < :periodDate" +
                     " group by a.period_date";
        HashMap<String, Object> params = new HashMap<>();
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        params.put("trangThaiPheDuyet", Arrays.asList(OrderPayablesEntity.STATUS.PHE_DUYET, OrderPayablesEntity.STATUS.TU_CHOI));
        params.put("periodDate", Utils.getLastDayOfMonth(periodDate));
        return getListData(sql, params, String.class);
    }

    public List<FeeRatio> getRatioByCustomer(List<Long> customerIds) {
        String sql = """
                select a.customer_id AS id, 
                	(select attribute_value from sys_category_attributes sct
                	where sct.category_id = sc.category_id
                	and sct.is_deleted = 'N'
                	and sct.attribute_code = 'TLE_PHI_GIOI_THIEU') AS referralRatio,
                	(select attribute_value from sys_category_attributes sct
                	where sct.category_id = sc.category_id
                	and sct.is_deleted = 'N'
                	and sct.attribute_code = 'TLE_PHI_KEM_CAP') AS careRatio,
                	(select attribute_value from sys_category_attributes sct
                	where sct.category_id = sc.category_id
                	and sct.is_deleted = 'N'
                	and sct.attribute_code = 'TLE_PHI_PHUC_LOI') AS welfareRatio
                from crm_customer_certificates a, sys_categories sc
                where a.is_deleted = 'N'
                and a.status_id = 'PHE_DUYET'
                and sc.value = a.certificate_id
                and sc.category_type = 'CRM_CHUNG_CHI'
                and a.customer_id in (:customerIds)
                and not exists (
                	select 1 from crm_customer_certificates a1
                	where a1.is_deleted = 'N'
                	and a1.status_id = 'PHE_DUYET'
                	and a1.customer_id = a.customer_id
                	and a1.issued_date > a.issued_date
                )
                """;
        HashMap<String, Object> params = new HashMap<>();
        params.put("customerIds", customerIds);
        return getListData(sql, params, FeeRatio.class);
    }

    public List<FeeRatio> getRatioByProduct(List<Long> productIds) {
        String sql = """
                select p.product_id AS id, 
                	(select attribute_value from crm_object_attributes sct
                	where sct.object_id = p.product_id
                	and sct.table_name = 'crm_products'
                	and sct.is_deleted = 'N'
                	and sct.attribute_code = 'TLE_PHI_GIOI_THIEU') AS referralRatio,
                	(select attribute_value from crm_object_attributes sct
                	where sct.object_id = p.product_id
                	and sct.table_name = 'crm_products'
                	and sct.is_deleted = 'N'
                	and sct.attribute_code = 'TLE_PHI_KEM_CAP') AS careRatio,
                	(select attribute_value from crm_object_attributes sct
                	where sct.object_id = p.product_id
                	and sct.table_name = 'crm_products'
                	and sct.is_deleted = 'N'
                	and sct.attribute_code = 'TLE_PHI_PHUC_LOI') AS welfareRatio
                from crm_products p
                where p.is_deleted = 'N'
                and p.product_id in (:productIds)
                """;
        HashMap<String, Object> params = new HashMap<>();
        params.put("productIds", productIds);
        return getListData(sql, params, FeeRatio.class);
    }
}
