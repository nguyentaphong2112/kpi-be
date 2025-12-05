/*
 * Copyright (C) 2022 EcoIT. All rights reserved.
 * EcoIT. Use is subject to license terms.
 */
package vn.hbtplus.tax.personal.repositories.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.constants.Scope;
import vn.hbtplus.models.BaseDataTableDto;
import vn.hbtplus.models.PermissionDataDto;
import vn.hbtplus.repositories.BaseRepository;
import vn.hbtplus.services.AuthorizationService;
import vn.hbtplus.tax.personal.constants.Constant;
import vn.hbtplus.tax.personal.models.request.AdminSearchDTO;
import vn.hbtplus.tax.personal.models.response.InvoiceRequestsResponse;
import vn.hbtplus.tax.personal.repositories.entity.HrEmployeesEntity;
import vn.hbtplus.tax.personal.repositories.entity.InvoiceRequestsEntity;
import vn.hbtplus.utils.QueryUtils;
import vn.hbtplus.utils.Utils;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Lop repository Impl ung voi bang PTX_INVOICE_REQUESTS
 *
 * @author tudd
 * @version 1.0
 * @since 1.0
 */

@Repository
@RequiredArgsConstructor
public class InvoiceRequestsRepositoryImpl extends BaseRepository {
    private final AuthorizationService authorizationService;

    public BaseDataTableDto<InvoiceRequestsResponse> searchData(AdminSearchDTO dto) {
        StringBuilder sql = new StringBuilder("""
            SELECT
             a.*,
             CONVERT(a.year, CHAR) STR_YEAR,
             a.created_time STR_CREATE_DATE,
             a.full_name empName,
             IFNULL(a.tax_no, e.tax_no) taxNo,
             mo.name orgManageName,
             IFNULL(o.full_name, o.name) orgName,
             (select sc.name from sys_categories sc where sc.code = e.emp_type_id and sc.category_type = :typeEmpTypeCode ) empTypeName,
             (select mj.name from hr_jobs mj where mj.job_id = e.job_id) jobName,
             CASE e.status WHEN 1 THEN 'Đang làm việc' WHEN 2 THEN 'Tạm hoãn HĐ' WHEN 3 THEN 'Đã nghỉ việc' END empStatusName,
             e.status empStatus
            """
        );
        HashMap<String, Object> params = new HashMap<>();
        params.put("typeEmpTypeCode", Constant.CATEGORY_CODES.DOI_TUONG_CV);
        addCondition(dto, sql, params);
        sql.append(" ORDER BY a.created_time DESC ");
        return getListPagination(sql.toString(), params, dto, InvoiceRequestsResponse.class);
    }

    public List<Map<String, Object>> getListDataExport(AdminSearchDTO dto) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                 a.*,
                 CONVERT(a.year, CHAR) STR_YEAR,
                 DATE_FORMAT(a.created_time, '%d/%m/%Y') STR_CREATE_DATE,
                 a.full_name EMPNAME,
                 a.employee_code EMPLOYEE_CODE,
                 a.email EMAIL,
                 IFNULL(a.tax_no, e.tax_no) TAXNO,
                 mo.name ORGMANAGENAME,
                 IFNULL(o.full_name, o.name) ORGNAME,
                 (select sc.name from sys_categories sc where sc.code = e.emp_type_id and sc.category_type = :typeEmpTypeCode ) EMPTYPENAME,
                 (select mj.name from hr_jobs mj where mj.job_id = e.job_id) JOBNAME,
                 CASE e.status WHEN 1 THEN 'Đang làm việc' WHEN 2 THEN 'Tạm hoãn HĐ' WHEN 3 THEN 'Đã nghỉ việc' END EMPSTATUSNAME,
                 e.status EMPSTATUS
                """);
        HashMap<String, Object> params = new HashMap<>();
        params.put("typeEmpTypeCode", Constant.CATEGORY_CODES.DOI_TUONG_CV);
        addCondition(dto, sql, params);
        sql.append(" ORDER BY a.created_time DESC ");
        return getListData(sql.toString(), params);
    }

    private void addCondition(AdminSearchDTO dto, StringBuilder sql, HashMap<String, Object> params) {
        sql.append("""
                   FROM ptx_invoice_requests a
                     JOIN hr_organizations mo ON mo.organization_id = a.org_id
                     LEFT JOIN hr_employees e ON a.employee_id = e.employee_id
                     LEFT JOIN hr_organizations o ON o.organization_id = e.organization_id
                     WHERE IFNULL(a.is_deleted, :activeStatus) = :activeStatus
                """);
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);

        if (Utils.isNullObject(dto.getOrgId())) {
//            List<PermissionDataDto> listOrgId = authorizationService.getPermissionData(Scope.VIEW, Constant.OBJECT_ATTRIBUTES.FUNCTION_CODES.RECEIVE_INVOICE, Utils.getUserNameLogin());
//            QueryUtils.addConditionPermission(listOrgId, sql, params);
        } else {
            authorizationService.hasPermissionWithOrg(dto.getOrgId(), Scope.VIEW, Constant.OBJECT_ATTRIBUTES.FUNCTION_CODES.RECEIVE_INVOICE);
            QueryUtils.filter("/" + dto.getOrgId() + "/", sql, params, "mo.path_id");
        }

        QueryUtils.filter(dto.getYear(), sql, params, "a.year");
        QueryUtils.filter(dto.getListInvoiceStatus(), sql, params, "a.invoice_status");


    }

    public Map<String, InvoiceRequestsEntity> getListInvoiceByEmpCodes(List<String> listEmployeeCode) {
        Map<String, InvoiceRequestsEntity> mapResult = new HashMap<>();
        if (Utils.isNullOrEmpty(listEmployeeCode)) {
            return mapResult;
        }
        String sql = """
                 SELECT a.*
                   FROM ptx_invoice_requests a
                   WHERE a.employee_code IN (:empCodes)
                   AND IFNULL(a.is_deleted, :activeStatus) = :activeStatus
                """;
        HashMap<String, Object> params = new HashMap<>();
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        List<List<String>> listPartition = Utils.partition(listEmployeeCode, Constant.SIZE_PARTITION);
        for (List<String> empCodes : listPartition) {
            params.put("empCodes", empCodes);
            List<InvoiceRequestsEntity> listEntity = getListData(sql, params, InvoiceRequestsEntity.class);
            for (InvoiceRequestsEntity entity : listEntity) {
                mapResult.put(entity.getEmployeeCode() + entity.getYear(), entity);
            }
        }
        return mapResult;
    }

    public Map<String, InvoiceRequestsEntity> getListInvoiceByListIdNo(List<String> listIdNo) {
        Map<String, InvoiceRequestsEntity> mapResult = new HashMap<>();
        if (Utils.isNullOrEmpty(listIdNo)) {
            return mapResult;
        }
        String sql = """
                 SELECT a.*
                   FROM ptx_invoice_requests a
                   WHERE a.id_no IN (:listIdNo)
                   AND IFNULL(a.is_deleted, :activeStatus) = :activeStatus
                """;
        HashMap<String, Object> params = new HashMap<>();
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        List<List<String>> listPartition = Utils.partition(listIdNo, Constant.SIZE_PARTITION);
        for (List<String> listTaxNoPartition : listPartition) {
            params.put("listIdNo", listTaxNoPartition);
            List<InvoiceRequestsEntity> listEntity = getListData(sql, params, InvoiceRequestsEntity.class);
            for (InvoiceRequestsEntity entity : listEntity) {
                mapResult.put(entity.getIdNo() + entity.getYear(), entity);
            }
        }
        return mapResult;
    }

    public List<InvoiceRequestsEntity> findEmployeeAutoInvoiceRequests(Date dateReport, int yearRegister) {
        String sql = """
                SELECT wp.employee_id,
                      e.employee_code,
                      e.full_name,
                      e.tax_no,
                      e.personal_id,
                      e.personal_email email
                FROM hr_work_process wp
                JOIN hr_employees e ON e.employee_id = wp.employee_id
                JOIN hr_document_types dt ON dt.document_type_id = wp.document_type_id
                WHERE IFNULL(wp.is_deleted, :activeStatus) = :activeStatus
                AND wp.to_date <= :dateReport
                AND wp.to_date >= :firstDate
                AND dt.type = 'OUT'
                AND wp.to_date = (
                    select max(wp1.to_date) from hr_work_process wp1
                    where wp1.employee_id = wp.employee_id
                    and IFNULL(wp1.is_deleted, :activeStatus) = :activeStatus
                )
                AND NOT EXISTS(
                    select 1 from ptx_invoice_requests pir
                    where pir.employee_id = wp.employee_id
                    and IFNULL(pir.is_deleted, :activeStatus) = :activeStatus
                    and pir.year = :yearRegister
                )
                """;
        HashMap<String, Object> params = new HashMap<>();
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        params.put("dateReport", dateReport);
        params.put("yearRegister", yearRegister);
        params.put("firstDate", Utils.stringToDate("01/01/" + yearRegister));
        return getListData(sql, params, InvoiceRequestsEntity.class);
    }

    public List<String> getListOrgManage(List<PermissionDataDto> listOrgId){
        StringBuilder sql = new StringBuilder("""
                SELECT mo.name
                   FROM hr_organizations mo
                   WHERE IFNULL(mo.is_deleted, :activeStatus) = :activeStatus
                   AND mo.org_level_manage = 2
                """);
        Map<String, Object> params = new HashMap<>();
        QueryUtils.addConditionPermission(listOrgId, sql, params);
        sql.append(" ORDER BY mo.path_order");
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        return getListData(sql.toString(), params, String.class);
    }

    public void pushExportInvoice(InvoiceRequestsResponse dto, HrEmployeesEntity employeesEntity) {
        if(employeesEntity == null){
            employeesEntity = new HrEmployeesEntity();
        }
        String sql = "CALL rest_tax_api_pkg.put_tax_req(:empCode, :empName, :orgManageId, :orgId, :taxNo, :year, :reason, :email, :createdBy)";
        HashMap<String, Object> params = new HashMap<>();
        params.put("empCode", dto.getEmployeeCode());
        params.put("empName", dto.getFullName());
        params.put("orgManageId", dto.getOrgManageId());
        params.put("orgId", Utils.NVL(employeesEntity.getOrganizationId(), dto.getOrgId()));
        params.put("taxNo", Utils.NVL(dto.getTaxNo(), employeesEntity.getTaxNo()));
//        params.put("personalId", Utils.NVL(dto.getIdNo(), employeesEntity.getPersonalId()));
        params.put("year", dto.getYear());
        params.put("reason", Utils.NVL(dto.getReason()));
        params.put("email", Utils.NVL(dto.getEmail(), employeesEntity.getEmail()));
        params.put("createdBy", dto.getCreatedBy());
        executeSqlDatabase(sql, params);
    }

    public List<InvoiceRequestsResponse> getListDataByForm(AdminSearchDTO dto) {
        StringBuilder sql = new StringBuilder("""
                SELECT a.employee_code, a.full_name, a.invoice_request_id,
                     a.org_id orgManageId,
                     IFNULL(e.organization_id, a.org_id) orgId,
                     IFNULL(a.tax_no, e.tax_no) taxNo,
                     IFNULL(a.id_no, pid.identity_no),
                     a.year,
                     a.reason,
                     IFNULL(a.email, e.email) email,
                     a.created_by
                  FROM ptx_invoice_requests a
                  LEFT JOIN hr_employees e ON e.employee_id = a.employee_id
                  left join hr_personal_identities pid on pid.is_main = 'Y' and pid.employee_id = e.employee_id and pid.is_deleted = 'N'
                  JOIN hr_organizations mo ON mo.organization_id = a.org_id
                  WHERE IFNULL(a.is_deleted, :activeStatus) = :activeStatus
                  AND a.invoice_status = :invoiceStatus
                """);
        HashMap<String, Object> params = new HashMap<>();
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        params.put("invoiceStatus", Constant.INVOICE_STATUS.PROCESSING);
        if (Utils.isNullObject(dto.getOrgId())) {
//            List<PermissionDataDto> listOrgId = authorizationService.getPermissionData(Scope.VIEW, Constant.OBJECT_ATTRIBUTES.FUNCTION_CODES.RECEIVE_INVOICE, Utils.getUserNameLogin());
//            QueryUtils.addConditionPermission(listOrgId, sql, params);
        } else {
            authorizationService.hasPermissionWithOrg(dto.getOrgId(), Scope.VIEW, Constant.OBJECT_ATTRIBUTES.FUNCTION_CODES.RECEIVE_INVOICE);
            QueryUtils.filter("/" + dto.getOrgId() + "/", sql, params, "mo.path_id");
        }

        QueryUtils.filter(dto.getYear(), sql, params, "a.year");


        return getListData(sql.toString(), params, InvoiceRequestsResponse.class);
    }

    public Long getOrgManageId(Long orgId) {
        String sql = "SELECT mo.organization_id " +
                "   FROM hr_organizations mo" +
                "   WHERE IFNULL(mo.is_deleted, :activeStatus) = :activeStatus" +
                "   AND mo.name = " +
                "       (SELECT name_manage " +
                "       FROM hr_organizations WHERE organization_id = :orgId)";
        Map<String, Object> params = new HashMap<>();
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        params.put("orgId", orgId);
        return getFirstData(sql, params, Long.class);
    }

    public Map<String, Integer> getMapTaxPerIncomeByListEmpCode(List<String> listEmpCode, int year){
        String sql = "SELECT a.empcode employee_code," +
                "   a.req_status status " +
                "   FROM tax_per_income_doc_req a" +
                "   WHERE a.income_year = :year" +
                "   AND a.empcode IN (:empCodes)";
        HashMap<String, Object> params = new HashMap<>();
        params.put("year", year);
        List<List<String>> listPartition = Utils.partition(listEmpCode, Constant.SIZE_PARTITION);
        Map<String, Integer> mapResult = new HashMap<>();
        for (List<String> empCodes: listPartition){
            params.put("empCodes", empCodes);
            List<InvoiceRequestsResponse> listData = getListData(sql, params, InvoiceRequestsResponse.class);
            for (InvoiceRequestsResponse dto: listData){
                mapResult.put(dto.getEmployeeCode(), dto.getStatus());
            }
        }
        return mapResult;
    }

    public Map<String, Integer> getMapTaxPerIncomeByListTaxNo(List<String> listTaxNo, int year){
        String sql = "SELECT a.taxcode taxNo," +
                "   a.req_status status " +
                "   FROM tax_per_income_doc_req a" +
                "   WHERE a.income_year = :year" +
                "   AND a.taxcode IN (:taxNoes)";
        HashMap<String, Object> params = new HashMap<>();
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        params.put("year", year);
        List<List<String>> listPartition = Utils.partition(listTaxNo, Constant.SIZE_PARTITION);
        Map<String, Integer> mapResult = new HashMap<>();
        for (List<String> taxNoes: listPartition){
            params.put("taxNoes", taxNoes);
            List<InvoiceRequestsResponse> listData = getListData(sql, params, InvoiceRequestsResponse.class);
            for (InvoiceRequestsResponse dto: listData){
                mapResult.put(dto.getTaxNo(), dto.getStatus());
            }
        }
        return mapResult;
    }

    public void updateInvoiceStatus(List<Long> listId, int invoiceStatus){
        String sql = "UPDATE ptx_invoice_requests" +
                "   SET invoice_status = :invoiceStatus," +
                "   modified_by = :userName," +
                "   modified_time = curdate()" +
                "   WHERE invoice_request_id IN (:ids)";
        HashMap<String, Object> params = new HashMap<>();
        params.put("invoiceStatus", invoiceStatus);
        params.put("userName", Utils.getUserNameLogin());
        List<List<Long>> listPartition = Utils.partition(listId, Constant.SIZE_PARTITION);
        for (List<Long> ids: listPartition){
            params.put("ids", ids);
            executeSqlDatabase(sql, params);
        }
    }
}
