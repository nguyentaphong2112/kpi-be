/*
 * Copyright (C) 2022 EcoIT. All rights reserved.
 * EcoIT. Use is subject to license terms.
 */
package vn.hbtplus.tax.personal.repositories.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.tax.personal.constants.Constant;
import vn.hbtplus.constants.Scope;
import vn.hbtplus.models.BaseDataTableDto;
import vn.hbtplus.models.PermissionDataDto;
import vn.hbtplus.tax.personal.models.request.AdminSearchDTO;
import vn.hbtplus.tax.personal.models.response.DeclarationRegistersResponse;
import vn.hbtplus.repositories.BaseRepository;
import vn.hbtplus.tax.personal.repositories.entity.DeclarationRegistersEntity;
import vn.hbtplus.services.AuthorizationService;
import vn.hbtplus.utils.QueryUtils;
import vn.hbtplus.utils.Utils;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Lop repository Impl ung voi bang PTX_DECLARATION_REGISTERS
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Repository
@RequiredArgsConstructor
public class DeclarationRegistersRepositoryImpl extends BaseRepository {
    private final AuthorizationService authorizationService;

    public BaseDataTableDto<DeclarationRegistersResponse> searchData(AdminSearchDTO dto) {
        StringBuilder sql = new StringBuilder("""
                    SELECT
                     a.declaration_register_id,
                     a.year,
                     CONVERT(a.year, CHAR) STR_YEAR,
                     a.employee_id,
                     a.method_code,
                     a.rev_invoice,
                     CASE a.rev_invoice WHEN 0 THEN 'Không' WHEN 1 THEN 'Có' END revInvoiceName,
                     a.status,
                     a.note,
                     a.created_by,
                     a.created_time,
                     a.created_time STR_CREATE_DATE,
                     e.employee_code,
                     e.email,
                     e.tax_no,
                     e.full_name empName,
                     IFNULL(mo.full_name, mo.name) orgName,
                     (select sc.name from sys_categories sc where sc.code = e.emp_type_id and sc.category_type = :typeEmpTypeCode ) empTypeName,
                     (select mj.name from hr_jobs mj where mj.job_id = e.job_id) jobName,
                     CASE e.status WHEN 1 THEN 'Đang làm việc' WHEN 2 THEN 'Tạm hoãn HĐ' WHEN 3 THEN 'Đã nghỉ việc' END empStatusName,
                     e.status empStatus
                """);
        HashMap<String, Object> params = new HashMap<>();
        params.put("typeEmpTypeCode", Constant.CATEGORY_CODES.DOI_TUONG_CV);
        addCondition(dto, sql, params);
        sql.append(" ORDER BY a.created_time DESC ");
        return getListPagination(sql.toString(), params, dto, DeclarationRegistersResponse.class);
    }

    public List<Map<String, Object>> getDataDeclarationRegister(AdminSearchDTO dto) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                 a.declaration_register_id,
                 a.year,
                 CONVERT(a.year, CHAR) STR_YEAR,
                 a.employee_id,
                 a.method_code,
                 a.rev_invoice,
                 CASE a.rev_invoice WHEN 0 THEN 'Không' WHEN 1 THEN 'Có' END revInvoiceName,
                 a.status,
                 a.note,
                 a.created_by,
                 a.created_time,
                 DATE_FORMAT(a.created_time, '%d/%m/%Y') STR_CREATE_DATE,
                 e.employee_code,
                 e.email,
                 e.tax_no,
                 e.full_name empName,
                 IFNULL(mo.full_name, mo.name) orgName,
                 (select sc.name from sys_categories sc where sc.code = e.emp_type_id and sc.category_type = :typeEmpTypeCode ) empTypeName,
                 (select mj.name from hr_jobs mj where mj.job_id = e.job_id) jobName,
                 CASE e.status WHEN 1 THEN 'Đang làm việc' WHEN 2 THEN 'Tạm hoãn HĐ' WHEN 3 THEN 'Đã nghỉ việc' END empStatusName,
                 e.status empStatus
                """);
        HashMap<String, Object> params = new HashMap<>();
        params.put("typeEmpTypeCode", Constant.CATEGORY_CODES.DOI_TUONG_CV);
        addCondition(dto, sql, params);
        sql.append(" ORDER BY a.created_time DESC ");
        return getListData(sql.toString(), params);
    }

    private void addCondition(AdminSearchDTO dto, StringBuilder sql, HashMap<String, Object> params) {
        sql.append("""
                FROM ptx_declaration_registers a
                 JOIN hr_employees e ON a.employee_id = e.employee_id
                 JOIN hr_organizations mo ON mo.organization_id = e.organization_id
                 WHERE IFNULL(a.is_deleted, :activeStatus) = :activeStatus
                """);
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        if (Utils.isNullObject(dto.getOrgId())) {
//            List<PermissionDataDto> listOrgId = authorizationService.getPermissionData(Scope.VIEW, Constant.OBJECT_ATTRIBUTES.FUNCTION_CODES.DECLARATION_REGISTERS,Utils.getUserNameLogin());
//            QueryUtils.addConditionPermission(listOrgId, sql, params);
        } else {
            authorizationService.hasPermissionWithOrg(dto.getOrgId(), Scope.VIEW, Constant.OBJECT_ATTRIBUTES.FUNCTION_CODES.DECLARATION_REGISTERS);
            QueryUtils.filter("/" + dto.getOrgId() + "/", sql, params, "mo.path_id");
        }
        QueryUtils.filter(dto.getYear(), sql, params, "a.year");

    }

    public Map<String, DeclarationRegistersEntity> getListRegisterByEmpCodes(List<String> listEmpCode) {
        Map<String, DeclarationRegistersEntity> mapResult = new HashMap<>();
        if (listEmpCode == null || listEmpCode.isEmpty()) {
            return mapResult;
        }
        StringBuilder sql = new StringBuilder("""
                SELECT a.*,
                  e.employee_code
                FROM ptx_declaration_registers a
                JOIN hr_employees e ON e.employee_id = a.employee_id
                WHERE IFNULL(a.is_deleted, :activeStatus) = :activeStatus
                AND IFNULL(e.is_deleted, :activeStatus) = :activeStatus
                AND e.employee_code IN (:empCodes)
                """
        );
        HashMap<String, Object> params = new HashMap<>();
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);

        List<List<String>> listPartition = Utils.partition(listEmpCode, Constant.SIZE_PARTITION);
        for (List<String> empCodes : listPartition) {
            params.put("empCodes", empCodes);
            List<DeclarationRegistersEntity> listRegister = getListData(sql.toString(), params, DeclarationRegistersEntity.class);
            if (listRegister != null && !listRegister.isEmpty()) {
                for (DeclarationRegistersEntity entity : listRegister) {
                    mapResult.put((entity.getEmployeeCode() + entity.getYear()).toLowerCase(), entity);
                }
            }
        }
        return mapResult;
    }

    public List<DeclarationRegistersEntity> findEmployeeAutoDeclarationRegister(Date dateReport, int yearRegister){
        String sql = "SELECT wp.employee_id"
                + "   FROM hr_work_process wp"
                + "   JOIN hr_document_types dt ON dt.document_type_id = wp.document_type_id"
                + "   WHERE IFNULL(wp.is_deleted, :activeStatus) = :activeStatus"
                + "   AND wp.to_date <= :dateReport"
                + "   AND wp.to_date >= :firstDate"
                + "   AND dt.type = 'OUT'"
                + "   AND wp.to_date = ("
                + "       select max(wp1.to_date) from hr_work_process wp1"
                + "       where wp1.employee_id = wp.employee_id "
                + "       and IFNULL(wp1.is_deleted, :activeStatus) = :activeStatus "
                + "   ) "
                + "   AND NOT EXISTS("
                + "       select 1 from ptx_declaration_registers pdr"
                + "       where pdr.employee_id = wp.employee_id"
                + "       and IFNULL(pdr.is_deleted, :activeStatus) = :activeStatus"
                + "       and pdr.year = :yearRegister"
                + "   ) "
                ;
        HashMap<String, Object> params = new HashMap<>();
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        params.put("dateReport", dateReport);
        params.put("yearRegister", yearRegister);
        params.put("firstDate", Utils.stringToDate("01/01/" + yearRegister));
        return getListData(sql, params, DeclarationRegistersEntity.class);
    }

    public List<DeclarationRegistersEntity> findUpdateDeclarationRegister(Date dateReport, int yearRegister){
        String sql = "SELECT pdr.*"
                + "   FROM ptx_declaration_registers pdr"
                + "   JOIN hr_work_process wp ON pdr.employee_id = wp.employee_id"
                + "   JOIN hr_document_types dt ON dt.document_type_id = wp.document_type_id"
                + "   WHERE IFNULL(pdr.is_deleted, :activeStatus) = :activeStatus"
                + "   AND pdr.year = :yearRegister"
                + "   AND pdr.method_code = :methodCode"
                + "   AND wp.to_date <= :dateReport"
                + "   AND wp.to_date >= :firstDate"
                + "   AND IFNULL(wp.is_deleted, :activeStatus) = :activeStatus"
                + "   AND dt.type = 'OUT'"
                + "   AND wp.to_date = ("
                + "       select max(wp1.to_date) from hr_work_process wp1"
                + "       where wp1.employee_id = wp.employee_id "
                + "       and IFNULL(wp1.is_deleted, :activeStatus) = :activeStatus "
                + "   ) "
                ;
        HashMap<String, Object> params = new HashMap<>();
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        params.put("dateReport", dateReport);
        params.put("yearRegister", yearRegister);
        params.put("firstDate", Utils.stringToDate("01/01/" + yearRegister));
        params.put("methodCode", Constant.METHOD_CODE.AUTHORITY);
        return getListData(sql, params, DeclarationRegistersEntity.class);
    }

}
