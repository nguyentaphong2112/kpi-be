/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.repositories.impl;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Repository;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.constants.Constant;
import vn.hbtplus.models.BaseDataTableDto;
import vn.hbtplus.models.request.CustomersRequest;
import vn.hbtplus.models.response.CustomersResponse;
import vn.hbtplus.models.response.EmployeesResponse;
import vn.hbtplus.repositories.BaseRepository;
import vn.hbtplus.repositories.entity.FamilyRelationshipsEntity;
import vn.hbtplus.services.UtilsService;
import vn.hbtplus.utils.QueryUtils;
import vn.hbtplus.utils.Utils;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Lop repository Impl ung voi bang crm_customers
 *
 * @author tudd
 * @version 1.0
 * @since 1.0
 */

@Repository
@RequiredArgsConstructor
public class CustomersRepository extends BaseRepository {
    private final UtilsService utilsService;

    public BaseDataTableDto searchData(CustomersRequest.SearchForm dto) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    a.customer_id,
                    a.full_name,
                    a.mobile_number,
                    a.is_deleted,
                    a.created_by,
                    a.created_time,
                    a.modified_by,
                    a.modified_time,
                    a.last_update_time,
                    a.login_name,
                    a.gender_id,
                    a.date_of_birth,
                    a.email,
                    a.zalo_account,
                    a.introducer_id,
                    a.receiver_id,
                    a.user_take_care_id,
                    a.job,
                    a.department_name,
                    a.bank_account,
                    a.bank_name,
                    a.bank_branch,
                    a.status,
                    a.full_address,
                    P_MONTH.phi_gioi_thieu referral_fee,
                    P_MONTH.phi_cham_soc care_fee,
                    ifnull(T.welfare_fee,0) + ifnull(P_MONTH.phi_phuc_loi,0) as welfare_fee,
                    od.productDetail as  productDetail,
                    (select  
                    	sum(od.final_amount) as total_order_amount
                    from crm_orders od,  crm_order_details ord 
                    where od.order_id = ord.order_id
                    and od.customer_id = a.customer_id
                    and od.is_deleted = 'N'
                    and ord.is_deleted = 'N') total_order_amount,
                    case
                        when exists (select 1 from crm_family_relationships cfr
                        where cfr.object_type = :objectType
                        and cfr.is_deleted = 'N'
                        and cfr.object_id = a.customer_id
                        and cfr.relation_type_id in (3,4) ) then 'Có'
                        ELSE 'Không'
                    END AS isStatusChild,
                    (select  
                    	sum(cp.amount) as paid_amount
                    from crm_orders od,  crm_order_details ord, crm_payments cp 
                    where od.order_id = ord.order_id
                    and od.customer_id = a.customer_id
                    and od.order_id = cp.order_id
                    and cp.payment_type = 'PHI_DON_HANG'
                    and cp.is_deleted = 'N'
                    and od.is_deleted = 'N'
                    and ord.is_deleted = 'N') paid_amount,                    
                    concat(ce.login_name, ' - ', ce.full_name) introducerName,
                    concat(ce1.login_name, ' - ', ce1.full_name) userTakeCareName,
                    concat(ce2.login_name, ' - ', ce2.full_name) receiverName,
                    (SELECT sc.name FROM sys_categories sc WHERE sc.value = a.gender_id AND sc.category_type = :genderCode) genderName,
                    (SELECT sc.name FROM sys_categories sc WHERE sc.value = a.status AND sc.category_type = :crmStatus) statusName
                
                """);
        HashMap<String, Object> params = new HashMap<>();
        addCondition(sql, params, dto);
        return getListPagination(sql.toString(), params, dto, CustomersResponse.SearchResult.class);
    }

    public List<Map<String, Object>> getListExport(CustomersRequest.SearchForm dto) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    a.customer_id,
                    a.full_name,
                    a.mobile_number,
                    a.is_deleted,
                    a.created_by,
                    a.created_time,
                    a.modified_by,
                    a.modified_time,
                    a.last_update_time,
                    a.login_name,
                    a.gender_id,
                    a.date_of_birth,
                    a.email,
                    a.zalo_account,
                    a.introducer_id,
                    a.user_take_care_id,
                    a.job,
                    a.department_name,
                    a.province_id,
                    a.district_id,
                    a.ward_id,
                    a.village_address,
                    a.bank_account,
                    a.bank_name,
                    a.bank_branch,
                    a.status,
                    a.full_address,
                    (select  
                    	CONCAT(p.`name` , '#', p.unit_price) as productDetail
                    from crm_orders od,  crm_order_details ord , crm_products p
                    where od.order_id = ord.order_id
                    and ord.product_id = p.product_id
                    and od.customer_id = a.customer_id
                    and od.is_deleted = 'N'
                    and ord.is_deleted = 'N'
                    order by p.unit_price desc limit 1) as  productDetail,
                    (select  
                    	sum(od.final_amount) as total_order_amount
                    from crm_orders od,  crm_order_details ord 
                    where od.order_id = ord.order_id
                    and od.customer_id = a.customer_id
                    and od.is_deleted = 'N'
                    and ord.is_deleted = 'N') total_order_amount,
                    case
                        when exists (select 1 from crm_family_relationships cfr
                        where cfr.object_type = :objectType
                        and cfr.is_deleted = 'N'
                        and cfr.object_id = a.customer_id
                        and cfr.relation_type_id in (3,4) ) then 'Có'
                        ELSE 'Không'
                    END AS isStatusChild,
                    (select  
                    	sum(cp.amount) as paid_amount
                    from crm_orders od,  crm_order_details ord, crm_payments cp 
                    where od.order_id = ord.order_id
                    and od.customer_id = a.customer_id
                    and od.order_id = cp.order_id
                    and cp.payment_type = 'PHI_DON_HANG'
                    and cp.is_deleted = 'N'
                    and od.is_deleted = 'N'
                    and ord.is_deleted = 'N') paid_amount,                    
                    concat(ce.login_name, ' - ', ce.full_name) introducerName,
                    concat(ce1.login_name, ' - ', ce1.full_name) userTakeCareName,
                    concat(ce2.login_name, ' - ', ce2.full_name) receiverName,
                    (SELECT sc.name FROM sys_categories sc WHERE sc.value = a.province_id AND sc.category_type = :provinceCode) provinceName,
                    (SELECT sc.name FROM sys_categories sc WHERE sc.value = a.district_id AND sc.category_type = :districtCode) districtName,
                    (SELECT sc.name FROM sys_categories sc WHERE sc.value = a.ward_id AND sc.category_type = :wardCode) wardName,
                    (SELECT sc.name FROM sys_categories sc WHERE sc.value = a.gender_id AND sc.category_type = :genderCode) genderName,
                    (SELECT sc.name FROM sys_categories sc WHERE sc.value = a.status AND sc.category_type = :crmStatus) statusName
                """);
        Map<String, Object> params = new HashMap<>();
        addCondition(sql, params, dto);
        return getListData(sql.toString(), params);
    }

    private void addCondition(StringBuilder sql, Map<String, Object> params, CustomersRequest.SearchForm dto) {
        sql.append("""
                    FROM crm_customers a
                    LEFT JOIN crm_customers ce ON ce.customer_id = a.introducer_id
                    LEFT JOIN crm_customers ce1 ON ce1.customer_id = a.user_take_care_id
                    LEFT JOIN crm_customers ce2 ON ce2.customer_id = a.receiver_id
                    LEFT JOIN (
                        select op.receiver_id customer_id,
                            sum(op.referral_fee) referral_fee,
                            sum(op.care_fee) care_fee,
                            sum(op.welfare_fee) welfare_fee
                        from crm_order_payables op
                        where op.status_id in ('PHE_DUYET')
                        and op.is_deleted = 'N'
                        group by op.receiver_id
                    ) T on T.customer_id = a.customer_id
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
                    LEFT JOIN (
                        	select T.caregiver_id as customer_id,
                                sum(T.amount * T.ty_le_phi_gioi_thieu/100) as phi_gioi_thieu,
                                sum(T.amount * T.ty_le_phi_cham_soc/100) as phi_cham_soc,
                                sum(T.amount * T.ty_le_phi_phuc_loi/100) as phi_phuc_loi
                            from (
                            select p1.caregiver_id, 
                                p1.amount,
                                (select at1.attribute_value from crm_object_attributes at1
                                where at1.table_name = 'crm_products'
                                and at1.is_deleted = 'N'
                                and at1.attribute_code = 'TLE_PHI_GIOI_THIEU'
                                and at1.object_id = od1.product_id
                                ) as ty_le_phi_gioi_thieu,
                                (select at1.attribute_value from crm_object_attributes at1
                                where at1.table_name = 'crm_products'
                                and at1.is_deleted = 'N'
                                and at1.attribute_code = 'TLE_PHI_KEM_CAP'
                                and at1.object_id = od1.product_id
                                ) as ty_le_phi_cham_soc,	
                                (select at1.attribute_value from crm_object_attributes at1
                                where at1.table_name = 'crm_products'
                                and at1.is_deleted = 'N'
                                and at1.attribute_code = 'TLE_PHI_PHUC_LOI'
                                and at1.object_id = od1.product_id
                                ) as ty_le_phi_phuc_loi	
                            from crm_payments p1 , crm_orders ord1 , crm_order_details od1 	
                            where p1.payment_type = 'PHI_DON_HANG'
                            and p1.is_deleted = 'N'
                            and p1.payment_date between :start_current_month and :end_current_month
                            and p1.order_id = ord1.order_id
                            and ord1.is_deleted = 'N'
                            and ord1.order_id = od1.order_id
                            and od1.is_deleted = 'N'
                        ) T
                        group by T.caregiver_id
                    ) P_MONTH on a.customer_id = P_MONTH.customer_id
                    WHERE IFNULL(a.is_deleted, :activeStatus) = :activeStatus
                """);
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        params.put("provinceCode", Constant.CATEGORY_TYPES.TINH);
        params.put("districtCode", Constant.CATEGORY_TYPES.HUYEN);
        params.put("wardCode", Constant.CATEGORY_TYPES.XA);
        params.put("genderCode", Constant.CATEGORY_TYPES.GIOI_TINH);
        params.put("crmStatus", Constant.CATEGORY_TYPES.CRM_TRANG_THAI_KHACH_HANG);
        params.put("objectType", FamilyRelationshipsEntity.OBJECT_TYPES.KHACH_HANG);
        params.put("start_current_month", Utils.getFirstDay(new Date()));
        params.put("end_current_month", Utils.getLastDay(new Date()));
        QueryUtils.filter(dto.getKeySearch(), sql, params, "a.full_name", "a.mobile_number", "a.email");
        QueryUtils.filter(dto.getIntroducerId(), sql, params, "a.introducer_id");
        QueryUtils.filter(dto.getUserTakeCareId(), sql, params, "a.user_take_care_id");
        if (dto.getDateOfBirth() != null) {
            Date startDate = Utils.getFirstDay(dto.getDateOfBirth());
            Date endDate = Utils.getLastDay(dto.getDateOfBirth());
            sql.append(" AND a.date_of_birth >= :startDate AND a.date_of_birth <= :endDate");
            params.put("endDate", endDate);
            params.put("startDate", startDate);
        }
        QueryUtils.filter(dto.getStatus(), sql, params, "a.status");
        //check phan quyen
        if (!utilsService.hasRole(Constant.Role.CRM_ADMIN)) {
            //neu khong phai quyen admin thi chi cho tim kiem khach hang minh cham soc
            sql.append(" and (" +
                       "    a.created_by like :userLoginName" +
                       "    OR a.login_name like :userLoginName" +
                       "    OR ce.login_name like :userLoginName" +
                       "    OR ce1.login_name like :userLoginName" +
                       "    OR ce2.login_name like :userLoginName" +
                       ")");
            params.put("userLoginName", Utils.getUserNameLogin());
        }

        //them các điều kiện tìm kiếm ở table
        QueryUtils.filter(dto.getFullNameFilter(), sql, params, "a.full_name");
        QueryUtils.filter(dto.getMobileNumberFilter(), sql, params, "a.mobile_number");
        QueryUtils.filter(dto.getProductNameFilter(), sql, params, "od.product_name");
        QueryUtils.filter(dto.getEmailFilter(), sql, params, "a.email");

        QueryUtils.filterExpression(dto.getProductPriceFilter(), sql, params, "od.product_unit_price");
        QueryUtils.filterExpression(dto.getReferralFeeFilter(), sql, params, "P_MONTH.phi_gioi_thieu");
        QueryUtils.filterExpression(dto.getCareFeeFilter(), sql, params, "P_MONTH.phi_cham_soc");
        QueryUtils.filterExpression(dto.getCareFeeFilter(), sql, params, "P_MONTH.phi_cham_soc");
        QueryUtils.filterExpression(dto.getWelfareFeeFilter(), sql, params, "ifnull(T.welfare_fee,0) + ifnull(P_MONTH.phi_phuc_loi,0)");
        QueryUtils.filter(dto.getIntroducerNameFilter(), sql, params, "ce.login_name", "ce.full_name");
        QueryUtils.filter(dto.getUserTakeCareNameFilter(), sql, params, "ce1.login_name", "ce1.full_name");
        QueryUtils.filter(dto.getReceiverNameFilter(), sql, params, "ce2.login_name", "ce2.full_name");
        QueryUtils.filter(dto.getEmailFilter(), sql, params, "a.email");
        QueryUtils.filter(dto.getLoginNameFilter(), sql, params, "a.login_name");


        sql.append(" ORDER BY a.created_time desc");
        if (!Utils.isNullOrEmpty(dto.getSelectedValue())) {
            String sqlValueSelect = ("""
                    SELECT
                        CASE
                            WHEN a.customer_id IN (:selectedValue) THEN 1
                            ELSE 0
                        END AS valueSelect, """);
            sql.replace(0, sql.length(), sql.toString().replaceFirst("SELECT", sqlValueSelect).replaceFirst("(?s)(.*)" + "ORDER BY" + "(?!.*" + "ORDER BY" + ")", "$1" + "ORDER BY valueSelect DESC,"));
            params.put("selectedValue", dto.getSelectedValue());
        }
    }

    public Map<String, CustomersResponse.DetailBean> getMapCustomerByMobileNumber(List<String> listMobileNumbers) {
        Map<String, CustomersResponse.DetailBean> mapResults = new HashMap<>();
        if (Utils.isNullOrEmpty(listMobileNumbers)) {
            return mapResults;
        }
        String sql = """
                    SELECT a.*
                    FROM crm_customers a
                    WHERE a.is_deleted = 'N'
                    AND a.mobile_number IN (:listMobileNumbers)
                """;
        Map<String, Object> params = new HashMap<>();
        params.put("listMobileNumbers", listMobileNumbers);
        List<CustomersResponse.DetailBean> listData = getListData(sql, params, CustomersResponse.DetailBean.class);
        if (!Utils.isNullOrEmpty(listData)) {
            listData.forEach(item -> mapResults.put(item.getMobileNumber(), item));
        }
        return mapResults;
    }

    public Map<String, EmployeesResponse.DetailBean> getMapEmpByMobileNumber(List<String> listMobileNumbers) {
        Map<String, EmployeesResponse.DetailBean> mapResults = new HashMap<>();
        if (Utils.isNullOrEmpty(listMobileNumbers)) {
            return mapResults;
        }
        String sql = """
                    SELECT a.*
                    FROM crm_employees a
                    WHERE a.is_deleted = 'N'
                    AND a.mobile_number IN (:listMobileNumbers)
                """;
        Map<String, Object> params = new HashMap<>();
        params.put("listMobileNumbers", listMobileNumbers);
        List<EmployeesResponse.DetailBean> listData = getListData(sql, params, EmployeesResponse.DetailBean.class);
        if (!Utils.isNullOrEmpty(listData)) {
            listData.forEach(item -> mapResults.put(item.getMobileNumber(), item));
        }
        return mapResults;
    }


    public List<CustomersResponse.DataSelected> getListData(List<String> mobileNumbers) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    a.customer_id,
                    CONCAT(a.full_name, ' - ', a.mobile_number) as fullName,
                    a.mobile_number,
                    a.date_of_birth
                FROM crm_customers a
                WHERE IFNULL(a.is_deleted, :activeStatus) = :activeStatus
                """);
        HashMap<String, Object> params = new HashMap<>();
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        if (!Utils.isNullOrEmpty(mobileNumbers)) {
            sql.append(" AND a.mobile_number in (:mobileNumbers)");
            params.put("mobileNumbers", mobileNumbers);
        }
        return getListData(sql.toString(), params, CustomersResponse.DataSelected.class);
    }

    public void updateUserTakeCare(Long customerId, Long caringEmpId, String userName, Date curDate) {
        String sql = """
                    UPDATE crm_customers
                    SET
                        user_take_care_id = :caringEmpId,
                        modified_by = :userName,
                        modified_time = :curDate
                    WHERE customer_id = :customerId
                    AND is_deleted = 'N'
                """;
        HashMap<String, Object> params = new HashMap<>();
        params.put("caringEmpId", caringEmpId);
        params.put("userName", userName);
        params.put("curDate", curDate);
        params.put("customerId", customerId);
        executeSqlDatabase(sql, params);
    }

    public Map<String, Long> getMapFullNameAndMobile(List<String> mobileList) {
        Map<String, Long> customerMap = new HashMap<>();
        if (Utils.isNullOrEmpty(mobileList)) {
            return customerMap;
        }

        String sql = """
                select * from crm_customers c
                WHERE IFNULL(c.is_deleted, :isDeleted) = :isDeleted
                    AND c.mobile_number IN (:mobileList)
                """;
        Map<String, Object> params = new HashMap<>();
        params.put("isDeleted", BaseConstants.STATUS.NOT_DELETED);
        params.put("mobileList", mobileList);

        List<CustomersResponse.DataSelected> customerList = getListData(sql, params, CustomersResponse.DataSelected.class);
        customerList.forEach(item -> {
            String key = StringUtils.trimToEmpty(item.getMobileNumber()) + "#" + StringUtils.trimToEmpty(item.getFullName());
            customerMap.put(StringUtils.lowerCase(key), item.getCustomerId());
        });

        return customerMap;
    }

    public BaseDataTableDto<CustomersResponse.SearchResult> getListPageable(CustomersRequest.SearchForm dto) {
        StringBuilder sql = new StringBuilder("""
                    SELECT a.customer_id,
                    a.mobile_number,
                    a.full_name
                    FROM crm_customers a
                    LEFT JOIN crm_customers ce ON ce.customer_id = a.introducer_id
                    LEFT JOIN crm_customers ce1 ON ce1.customer_id = a.user_take_care_id
                    LEFT JOIN crm_customers ce2 ON ce2.customer_id = a.receiver_id
                    WHERE a.is_deleted = :activeStatus
                """);
        Map<String, Object> params = new HashMap<>();
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        QueryUtils.filter(dto.getKeySearch(), sql, params, "a.full_name", "a.mobile_number", "a.email");
        //check phan quyen
        if (!utilsService.hasRole(Constant.Role.CRM_ADMIN)) {
            //neu khong phai quyen admin thi chi cho tim kiem khach hang minh cham soc
            sql.append(" and (" +
                       "    a.created_by like :userLoginName" +
                       "    OR a.login_name like :userLoginName" +
                       "    OR ce.login_name like :userLoginName" +
                       "    OR ce1.login_name like :userLoginName" +
                       "    OR ce2.login_name like :userLoginName" +
                       ")");
            params.put("userLoginName", Utils.getUserNameLogin());
        }
        sql.append(" ORDER BY a.full_name desc");
        if (!Utils.isNullOrEmpty(dto.getSelectedValue())) {
            String sqlValueSelect = ("""
                    SELECT
                        CASE
                            WHEN a.customer_id IN (:selectedValue) THEN 1
                            ELSE 0
                        END AS valueSelect, """);
            sql.replace(0, sql.length(), sql.toString().replaceFirst("SELECT", sqlValueSelect).replaceFirst("(?s)(.*)" + "ORDER BY" + "(?!.*" + "ORDER BY" + ")", "$1" + "ORDER BY valueSelect DESC,"));
            params.put("selectedValue", dto.getSelectedValue());
        }
        return getListPagination(sql.toString(), params, dto, CustomersResponse.SearchResult.class);
    }
}
