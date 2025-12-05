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
import vn.hbtplus.models.request.CustomerCertificatesRequest;
import vn.hbtplus.models.response.CustomerCertificatesResponse;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.utils.QueryUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Lop repository Impl ung voi bang crm_customer_certificates
 *
 * @author tudd
 * @version 1.0
 * @since 1.0
 */

@Repository
@RequiredArgsConstructor
public class CustomerCertificatesRepository extends BaseRepository {

    public BaseDataTableDto searchData(CustomerCertificatesRequest.SearchForm dto) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    a.customer_certificate_id,
                    c.full_name,
                    c.mobile_number,
                    a.issued_date,
                    a.status_id,
                    (select
                    	CONCAT(p.`name` , '#', p.unit_price) as productDetail
                    from crm_orders od,  crm_order_details ord , crm_products p
                    where od.order_id = ord.order_id
                    and ord.product_id = p.product_id
                    and od.customer_id = a.customer_id
                    and od.is_deleted = 'N'
                    and ord.is_deleted = 'N'
                    order by p.unit_price desc limit 1) as productDetail,
                    (SELECT sc.name FROM sys_categories sc WHERE sc.value = a.certificate_id AND sc.category_type = :certificateCode) certificateName,
                    a.created_by,
                    a.created_time,
                    a.modified_by,
                    a.modified_time,
                    a.approved_by,
                    a.approved_date
                """);
        HashMap<String, Object> params = new HashMap<>();
        addCondition(sql, params, dto);
        return getListPagination(sql.toString(), params, dto, CustomerCertificatesResponse.SearchResult.class);
    }

    public List<Map<String, Object>> getListExport(CustomerCertificatesRequest.SearchForm dto) {
        StringBuilder sql = new StringBuilder("""
                 SELECT
                    a.customer_certificate_id,
                    c.full_name,
                    c.mobile_number,
                    a.issued_date,
                    a.status_id,
                    od.productDetail,
                    ct.name certificateName,
                    (SELECT sc.name FROM sys_categories sc WHERE sc.code = a.status_id AND sc.category_type = :status) statusName,
                    a.created_by,
                    a.created_time,
                    a.modified_by,
                    a.modified_time,
                    a.approved_by,
                    a.approved_date
                """);
        Map<String, Object> params = new HashMap<>();
        addCondition(sql, params, dto);
        return getListData(sql.toString(), params);
    }

    private void addCondition(StringBuilder sql, Map<String, Object> params, CustomerCertificatesRequest.SearchForm dto) {
        sql.append("""
                    FROM crm_customer_certificates a
                    JOIN crm_customers c on c.customer_id = a.customer_id
                    JOIN sys_categories ct on ct.value = a.certificate_id AND ct.category_type = :certificateCode
                    LEFT JOIN (
                        SELECT
                            p.name as product_name,
                            p.unit_price as product_unit_price,
                            od.customer_id, CONCAT(p.`name`, '#', p.unit_price) AS productDetail,
                            ROW_NUMBER() OVER (PARTITION BY od.customer_id ORDER BY p.unit_price DESC) AS row_num
                            FROM crm_orders od
                            JOIN crm_order_details ord ON od.order_id = ord.order_id
                            JOIN crm_products p ON ord.product_id = p.product_id
                            WHERE od.is_deleted = 'N'
                              AND ord.is_deleted = 'N'
                    ) od on a.customer_id = od.customer_id and od.row_num = 1
                    WHERE IFNULL(a.is_deleted, :activeStatus) = :activeStatus
                """);
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        params.put("certificateCode", Constant.CATEGORY_TYPES.CRM_CHUNG_CHI);
        params.put("status", Constant.CATEGORY_TYPES.CRM_STATUS_CERTIFICATE);
        QueryUtils.filter(dto.getKeySearch(), sql, params, "c.full_name");
        QueryUtils.filter(dto.getStatusId(), sql, params, "a.status_id");
        QueryUtils.filter(dto.getCertificateId(), sql, params, "a.certificate_id");
        QueryUtils.filter(dto.getMobileNumber(), sql, params, "c.mobile_number");

        QueryUtils.filter(dto.getMobileNumberFilter(), sql, params, "c.mobile_number");
        QueryUtils.filter(dto.getFullNameFilter(), sql, params, "c.full_name");
        QueryUtils.filter(dto.getProductNameFilter(),sql, params, "od.product_name");
        QueryUtils.filter(dto.getCertificateNameFilter(),sql, params, "ct.name");


        if (dto.getStartDate() != null) {
            sql.append(" and IFNULL(a.issued_date, :startDate) >= :startDate");
            params.put("startDate", dto.getStartDate());
        }
        if (dto.getEndDate() != null) {
            sql.append(" and IFNULL(a.issued_date, :endDate) <= :endDate");
            params.put("endDate", dto.getEndDate());
        }
    }
}
