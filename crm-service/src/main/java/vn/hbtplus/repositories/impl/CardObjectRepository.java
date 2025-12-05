package vn.hbtplus.repositories.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import vn.hbtplus.constants.Constant;
import vn.hbtplus.models.BaseDataTableDto;
import vn.hbtplus.models.request.CardObjectRequest;
import vn.hbtplus.models.response.CardObjectResponse;
import vn.hbtplus.repositories.BaseRepository;
import vn.hbtplus.services.UtilsService;
import vn.hbtplus.utils.QueryUtils;
import vn.hbtplus.utils.Utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class CardObjectRepository extends BaseRepository {
    private final UtilsService utilsService;

    public BaseDataTableDto searchData(CardObjectRequest.SearchForm dto) {
        StringBuilder sql = new StringBuilder("""
                    SELECT
                        a.*,
                       COALESCE(
                           DATEDIFF(
                               IF(
                                   DATE_FORMAT(a.date_of_birth, '%m-%d') >= DATE_FORMAT(curdate(), '%m-%d'),
                                   STR_TO_DATE(CONCAT(YEAR(curdate()), '-', DATE_FORMAT(a.date_of_birth, '%m-%d')), '%Y-%m-%d'),
                                   STR_TO_DATE(CONCAT(YEAR(curdate()) + 1, '-', DATE_FORMAT(a.date_of_birth, '%m-%d')), '%Y-%m-%d')
                               ),
                               curdate()
                           ),
                           NULL
                       ) AS daysUntilBirthday,
                        (select
                    	CONCAT(p.`name` , '#', p.unit_price) as productDetail
                        from crm_orders od,  crm_order_details ord , crm_products p
                        where od.order_id = ord.order_id
                        and ord.product_id = p.product_id
                        and od.customer_id = a.obj_id
                        and od.is_deleted = 'N'
                        and ord.is_deleted = 'N'
                        order by p.unit_price desc limit 1) as  productDetail,
                        (select
                    	sum(od.final_amount) as total_order_amount
                        from crm_orders od,  crm_order_details ord 
                        where od.order_id = ord.order_id
                        and od.customer_id = a.obj_id
                        and od.is_deleted = 'N'
                        and ord.is_deleted = 'N') total_order_amount,
                        (select
                    	sum(cp.amount) as paid_amount
                        from crm_orders od,  crm_order_details ord, crm_payments cp
                        where od.order_id = ord.order_id
                        and od.customer_id = a.obj_id
                        and od.order_id = cp.order_id
                        and cp.payment_type = 'PHI_DON_HANG'
                        and cp.is_deleted = 'N'
                        and od.is_deleted = 'N'
                        and ord.is_deleted = 'N') paid_amount
                """);
        HashMap<String, Object> params = new HashMap<>();
        addCondition(sql, params, dto);
        return getListPagination(sql.toString(), params, dto, CardObjectResponse.SearchResult.class);
    }


    public List<Map<String, Object>> getListExport(CardObjectRequest.SearchForm dto) {
        StringBuilder sql = new StringBuilder("""
                    SELECT
                        a.*,
                       COALESCE(
                           DATEDIFF(
                               IF(
                                   DATE_FORMAT(a.date_of_birth, '%m-%d') >= DATE_FORMAT(curdate(), '%m-%d'),
                                   STR_TO_DATE(CONCAT(YEAR(curdate()), '-', DATE_FORMAT(a.date_of_birth, '%m-%d')), '%Y-%m-%d'),
                                   STR_TO_DATE(CONCAT(YEAR(curdate()) + 1, '-', DATE_FORMAT(a.date_of_birth, '%m-%d')), '%Y-%m-%d')
                               ),
                               curdate()
                           ),
                           NULL
                       ) AS daysUntilBirthday,
                        (select
                    	CONCAT(p.`name` , '#', p.unit_price) as productDetail
                        from crm_orders od,  crm_order_details ord , crm_products p
                        where od.order_id = ord.order_id
                        and ord.product_id = p.product_id
                        and od.customer_id = a.obj_id
                        and od.is_deleted = 'N'
                        and ord.is_deleted = 'N'
                        order by p.unit_price desc limit 1) as  productDetail,
                        (select
                    	sum(od.final_amount) as total_order_amount
                        from crm_orders od,  crm_order_details ord 
                        where od.order_id = ord.order_id
                        and od.customer_id = a.obj_id
                        and od.is_deleted = 'N'
                        and ord.is_deleted = 'N') total_order_amount,
                        (select
                    	sum(cp.amount) as paid_amount
                        from crm_orders od,  crm_order_details ord, crm_payments cp
                        where od.order_id = ord.order_id
                        and od.customer_id = a.obj_id
                        and od.order_id = cp.order_id
                        and cp.payment_type = 'PHI_DON_HANG'
                        and cp.is_deleted = 'N'
                        and od.is_deleted = 'N'
                        and ord.is_deleted = 'N') paid_amount
                """);
        HashMap<String, Object> params = new HashMap<>();
        addCondition(sql, params, dto);
        return getListData(sql.toString(), params);
    }

    private void addCondition(StringBuilder sql, HashMap<String, Object> params, CardObjectRequest.SearchForm dto) {
        sql.append("""
                    FROM v_card_objects a
                    WHERE 1 = 1
                """);
        QueryUtils.filter(dto.getKeySearch(), sql, params, "a.full_name", "a.mobile_number", "a.email");
        QueryUtils.filterEq(dto.getObjType(), sql, params, "a.obj_type");
        if(!utilsService.hasRole(Constant.Role.CRM_ADMIN)){
            sql.append("""
                     and a.customer_id in (
                        select c.customer_id from crm_customers c
                        LEFT JOIN crm_customers ce ON ce.customer_id = c.introducer_id
                        LEFT JOIN crm_customers ce1 ON ce1.customer_id = c.user_take_care_id
                        LEFT JOIN crm_customers ce2 ON ce2.customer_id = c.receiver_id
                        where ce.login_name like :userLoginName
                        OR ce1.login_name like :userLoginName
                        OR ce2.login_name like :userLoginName
                    )
                    """);
            params.put("userLoginName", Utils.getUserNameLogin());
        }
        sql.append(" ORDER BY IFNULL(daysUntilBirthday, 9999)");

        StringBuilder sqlFilter = new StringBuilder("select a.* from (" + sql + ") a where 1=1");

        QueryUtils.filter(dto.getFullNameFilter(), sqlFilter, params, "a.full_name");
        QueryUtils.filter(dto.getMobileNumberFilter(), sqlFilter, params, "a.mobile_number");
        QueryUtils.filterExpression(dto.getDaysUntilBirthdayFilter(), sqlFilter, params, "a.daysUntilBirthday");
        QueryUtils.filter(dto.getEmailFilter(), sqlFilter, params, "a.email");
        QueryUtils.filter(dto.getProductNameFilter(), sqlFilter, params, "a.productDetail");
        QueryUtils.filterExpression(dto.getOwedAmountFilter(), sqlFilter, params, "ifnull(a.total_order_amount,0) - ifnull(a.paid_amount,0)");
        QueryUtils.filter(dto.getCurrentAddressFilter(), sqlFilter, params, "a.current_address");
        QueryUtils.filter(dto.getParentNameFilter(), sqlFilter, params, "a.parent_name");
        QueryUtils.filter(dto.getRelationTypeNameFilter(), sqlFilter, params, "a.relation_type_name");
        QueryUtils.filter(dto.getParentMobileNumberFilter(), sqlFilter, params, "a.parent_mobile_number");
        sqlFilter.append(" order by IFNULL(a.daysUntilBirthday,999)");
        sql.setLength(0);
        sql.append(sqlFilter);

    }

    public List<CardObjectResponse.DetailBean> getListObject(String objType, Long objId, List<Long> listObjId) {
        StringBuilder sql = new StringBuilder("""
                    SELECT
                        a.*
                    FROM v_card_objects a
                    WHERE 1 = 1
                """);
        HashMap<String, Object> params = new HashMap<>();
        QueryUtils.filter(objId, sql, params, "a.obj_id");
        QueryUtils.filterEq(objType, sql, params, "a.obj_type");
        QueryUtils.filter(listObjId, sql, params, "a.obj_id");
        return getListData(sql.toString(), params, CardObjectResponse.DetailBean.class);
    }
}
