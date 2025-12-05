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
import vn.hbtplus.models.request.CustomerCareRecordsRequest;
import vn.hbtplus.models.response.CustomerCareRecordsResponse;
import vn.hbtplus.repositories.BaseRepository;
import vn.hbtplus.services.UtilsService;
import vn.hbtplus.utils.QueryUtils;
import vn.hbtplus.utils.Utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Lop repository Impl ung voi bang crm_customer_care_records
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Repository
@RequiredArgsConstructor
public class CustomerCareRecordsRepository extends BaseRepository {

    private final UtilsService utilsService;

    public BaseDataTableDto searchData(CustomerCareRecordsRequest.SearchForm dto) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                  a.customer_care_record_id,
                  a.type,
                  (SELECT sc.name FROM sys_categories sc WHERE a.type = sc.value and sc.category_type = :type) typeName,
                  a.customer_id,
                  a.full_name,
                  a.mobile_number,
                  a.date_of_birth,
                  a.request_date,
                  (SELECT e.full_name from crm_employees e WHERE a.requested_emp_id = e.employee_id) requestedEmpName,
                  a.caring_emp_id,
                  (SELECT e.full_name from crm_employees e WHERE a.caring_emp_id = e.employee_id) caringEmpName,
                  a.contact_date,
                  a.caring_status_id,
                  (SELECT sc.name FROM sys_categories sc WHERE a.caring_status_id = sc.value and sc.category_type = :caringStatusType) caringStatusName,
                  a.status_id,
                  (SELECT sc.name FROM sys_categories sc WHERE a.status_id = sc.value and sc.category_type = :statusType) statusName,
                  a.is_deleted,
                  a.created_by,
                  a.created_time,
                  a.modified_by,
                  a.modified_time,
                  a.last_update_time
                """);
        HashMap<String, Object> params = new HashMap<>();
        addCondition(sql, params, dto);
        return getListPagination(sql.toString(), params, dto, CustomerCareRecordsResponse.class);
    }

    public List<Map<String, Object>> getListExport(CustomerCareRecordsRequest.SearchForm dto) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                      a.customer_care_record_id,
                      a.type,
                      (SELECT sc.name FROM sys_categories sc WHERE a.type = sc.value and sc.category_type = :type) typeName,
                      a.customer_id,
                      a.full_name,
                      a.mobile_number,
                      a.date_of_birth,
                      a.request_date,
                      (SELECT e.full_name from crm_customers e WHERE a.requested_emp_id = e.customer_id) requestedEmpName,
                      a.caring_emp_id,
                      (SELECT e.full_name from crm_customers e WHERE a.requested_emp_id = e.customer_id) caringEmpName,
                      a.contact_date,
                      a.caring_status_id,
                      (SELECT sc.name FROM sys_categories sc WHERE a.caring_status_id = sc.value and sc.category_type = :caringStatusType) caringStatusName,
                      a.status_id,
                      (SELECT sc.name FROM sys_categories sc WHERE a.status_id = sc.value and sc.category_type = :statusType) statusName,
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

    private void addCondition(StringBuilder sql, Map<String, Object> params, CustomerCareRecordsRequest.SearchForm dto) {
        sql.append("""
            FROM crm_customer_care_records a
            JOIN crm_customers e ON e.customer_id = a.customer_id
            LEFT JOIN crm_customers ce ON ce.customer_id = e.introducer_id
            LEFT JOIN crm_customers ce1 ON ce1.customer_id = e.user_take_care_id
            LEFT JOIN crm_customers ce2 ON ce2.customer_id = e.receiver_id
            WHERE IFNULL(a.is_deleted, :activeStatus) = :activeStatus
        """);
        params.put("type", Constant.CATEGORY_TYPES.CRM_PHAN_LOAI);
        params.put("caringStatusType", Constant.CATEGORY_TYPES.CRM_TRANG_THAI_CHAM_SOC);
        params.put("statusType", Constant.CATEGORY_TYPES.CRM_TINH_TRANG);
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);

        QueryUtils.filter(dto.getKeySearch(), sql, params, "a.full_name", "a.mobile_number");
        QueryUtils.filter(dto.getType(), sql, params, "a.type");
        QueryUtils.filter(dto.getRequestedEmpId(), sql, params, "a.requested_emp_id");
        QueryUtils.filter(dto.getRequestedEmpId(), sql, params, "a.requested_emp_id");
        QueryUtils.filter(dto.getCaringEmpId(), sql, params, "a.caring_emp_id");
        //check phan quyen
        if(!utilsService.hasRole(Constant.Role.CRM_ADMIN)){
            //neu khong phai quyen admin thi chi cho tim kiem khach hang minh cham soc
            sql.append(" and (" +
                       "    e.created_by like :userLoginName" +
                       "    OR ce.login_name like :userLoginName" +
                       "    OR ce1.login_name like :userLoginName" +
                       "    OR ce2.login_name like :userLoginName" +
                       ")");
            params.put("userLoginName", Utils.getUserNameLogin());
        }
        sql.append(" GROUP BY a.customer_care_record_id ORDER BY a.created_time desc");
    }

}
