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
import vn.hbtplus.tax.personal.models.response.DependentRegistersResponse;
import vn.hbtplus.repositories.BaseRepository;
import vn.hbtplus.services.AuthorizationService;
import vn.hbtplus.utils.QueryUtils;
import vn.hbtplus.utils.Utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Lop repository Impl ung voi bang PTX_DEPENDENT_REGISTERS
 *
 * @author tudd
 * @version 1.0
 * @since 1.0
 */

@Repository
@RequiredArgsConstructor
public class DependentReportRepositoryImpl extends BaseRepository {

    private final AuthorizationService authorizationService;

    public BaseDataTableDto<DependentRegistersResponse> searchData(AdminSearchDTO dto) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                 e.employee_code,
                 e.full_name empName,
                 e.tax_no empTaxNo,
                 e.status empStatus,
                 CASE e.status WHEN 1 THEN 'Đang làm việc' WHEN 2 THEN 'Tạm hoãn HĐ' WHEN 3 THEN 'Đã nghỉ việc' END empStatusName,
                 CONCAT(sc.name, ' ', hfr.full_name) dependentName,
                 hfr.date_of_birth fDateOfBith,
                 a.dependent_person_code dependentPersonCode,
                 IFNULL(a.tax_no, '') fTaxNo,
                 IFNULL(a.personal_id, passport_no) fPersonalId,
                 a.code_no,
                 a.book_no,
                 a.from_date fromMonth,
                 a.to_date toMonth,
                 IFNULL(mo.full_name, mo.name) orgName,
                 (select sc0.name from sys_categories sc0 where sc0.code = e.emp_type_id and sc0.category_type = :typeEmpTypeCode ) empTypeName,
                 (select mj.name from hr_jobs mj where mj.job_id = e.job_id) jobName,
                 a.note
                """);
        HashMap<String, Object> params = new HashMap<>();
        params.put("typeEmpTypeCode", Constant.CATEGORY_CODES.DOI_TUONG_CV);
        addCondition(dto, sql, params);
        sql.append(" ORDER BY e.full_name ");
        return getListPagination(sql.toString(), params, dto, DependentRegistersResponse.class);
    }

    public List<DependentRegistersResponse> exportData(AdminSearchDTO dto) {
        StringBuilder sql = new StringBuilder("SELECT "
                + "    e.employee_code,"
                + "    e.full_name empName,"
                + "    e.tax_no empTaxNo,"
                + "    CASE e.status WHEN 1 THEN 'Đang làm việc' WHEN 2 THEN 'Tạm hoãn HĐ' WHEN 3 THEN 'Đã nghỉ việc' END empStatusName,"
                + "    sc.name relationType,"
                + "    hfr.full_name dependentName,"
                + "    hfr.date_of_birth fDateOfBith,"
                + "    a.dependent_person_code dependentPersonCode,"
                + "    IFNULL(a.tax_no, '') fTaxNo,"
                + "    IFNULL(a.personal_id, passport_no) fPersonalId,"
                + "    a.code_no,"
                + "    a.book_no,"
                + "    a.from_date,"
                + "    a.to_date,"
                + "    (    select d.created_time " +
                "           from ptx_dependent_registers d" +
                "           where d.family_relationship_id = a.family_relationship_id" +
                "           and IFNULL(d.is_deleted, :activeStatus) = :activeStatus" +
                "           and d.reg_type = :regType" +
                "           and d.status = :statusRegister" +
                "           limit 1" +
                "       ) createdTime,"
                + "    IFNULL(mo.full_name, mo.name) orgName,"
                + "    (select sc0.name from sys_categories sc0 where sc0.code = e.emp_type_id and sc0.category_type = :typeEmpTypeCode ) empTypeName,"
                + "    (select mj.name from hr_jobs mj where mj.job_id = e.job_id) jobName,"
                + "    a.note");
        HashMap<String, Object> params = new HashMap<>();
        params.put("typeEmpTypeCode", Constant.CATEGORY_CODES.DOI_TUONG_CV);
        params.put("regType", Constant.REG_TYPE.DEPENDENT_CREATE);
        params.put("statusRegister", Constant.TAX_STATUS.TAX_APPROVAL);

        addCondition(dto, sql, params);
        sql.append(" ORDER BY e.full_name ");
        return getListData(sql.toString(), params, DependentRegistersResponse.class);
    }

    public List<Map<String, Object>> getReportGroupByMonth(AdminSearchDTO dto){
        StringBuilder sql = new StringBuilder("SELECT e.employee_code, "
                + " e.full_name,"
                + " CASE e.status WHEN 1 THEN 'Đang làm việc' WHEN 2 THEN 'Tạm hoãn HĐ' WHEN 3 THEN 'Đã nghỉ việc' END emp_status,"
                + " (   select count(1) " +
                "       from hr_dependent_persons a" +
                "       where a.employee_id = e.employee_id" +
                "       and IFNULL(a.is_deleted, :activeStatus) = :activeStatus" +
                (dto.getDateReport() != null ? " and a.from_date <= :fromDateReport" +
                "                            and (a.to_date >= :toDateReport or a.to_date is null)" : "") +
                "    ) count  "
                + " FROM hr_employees e "
                + " JOIN hr_organizations mo ON mo.organization_id = e.organization_id "
                + " WHERE IFNULL(e.is_deleted, :activeStatus) = :activeStatus "
                + " AND EXISTS(" +
                "               select 1 from hr_dependent_persons d " +
                "               where d.employee_id = e.employee_id " +
                "               and IFNULL(d.is_deleted, :activeStatus) = :activeStatus" +
                (dto.getDateReport() != null ? " and d.from_date <= :fromDateReport" +
                        "                            and (d.to_date >= :toDateReport or d.to_date is null)" : "") +
                "   ) ");
        HashMap<String, Object> params = new HashMap<>();
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        if (Utils.isNullObject(dto.getOrgId())) {
//            List<PermissionDataDto> listOrgId = authorizationService.getPermissionData(Scope.VIEW, Constant.OBJECT_ATTRIBUTES.FUNCTION_CODES.REPORT_DEPENDENT,Utils.getUserNameLogin());
//            QueryUtils.addConditionPermission(listOrgId, sql, params);
        } else {
            authorizationService.hasPermissionWithOrg(dto.getOrgId(), Scope.VIEW, Constant.OBJECT_ATTRIBUTES.FUNCTION_CODES.REPORT_DEPENDENT);
            QueryUtils.filter("/" + dto.getOrgId() + "/", sql, params, "mo.path_id");
        }
        if(dto.getDateReport() != null){
            params.put("fromDateReport", Utils.getLastDayOfMonth(dto.getDateReport()));
            params.put("toDateReport", Utils.getFirstDay(dto.getDateReport()));
        }
        QueryUtils.filter(dto.getListEmpStatus(), sql, params, "e.status");
        QueryUtils.filterOriginal(dto.getEmpCode(), sql, params, "e.employee_code");
        QueryUtils.filter(dto.getEmpName(), sql, params, "e.full_name");
        sql.append(" ORDER BY e.full_name ");
        return getListData(sql.toString(), params);
    }



    private void addCondition(AdminSearchDTO dto, StringBuilder sql, HashMap<String, Object> params) {
        sql.append("""
                  FROM hr_dependent_persons a
                   JOIN hr_employees e ON a.employee_id = e.employee_id
                   LEFT JOIN hr_organizations mo ON mo.organization_id = e.organization_id
                   JOIN hr_family_relationships hfr ON hfr.family_relationship_id = a.family_relationship_id
                   JOIN sys_categories sc ON sc.code = hfr.relation_type_id AND sc.category_type = :relationTypeCode
                   WHERE IFNULL(a.is_deleted, :activeStatus) = :activeStatus
                """);

        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        params.put("relationTypeCode", Constant.CATEGORY_CODES.MOI_QUAN_HE_TN);
        if (Utils.isNullObject(dto.getOrgId())) {
//            List<PermissionDataDto> listOrgId = authorizationService.getPermissionData(Scope.VIEW, Constant.OBJECT_ATTRIBUTES.FUNCTION_CODES.REPORT_DEPENDENT, Utils.getUserNameLogin());
//            QueryUtils.addConditionPermission(listOrgId, sql, params);
        } else {
            authorizationService.hasPermissionWithOrg(dto.getOrgId(), Scope.VIEW, Constant.OBJECT_ATTRIBUTES.FUNCTION_CODES.REPORT_DEPENDENT);
            QueryUtils.filter("/" + dto.getOrgId() + "/", sql, params, "mo.path_id");
        }

        if(dto.getDateReport() != null){
            sql.append(" AND a.from_date <= :fromDateReport");
            sql.append(" AND (a.to_date >= :toDateReport OR a.to_date is null)");
            params.put("fromDateReport", Utils.getLastDayOfMonth(dto.getDateReport()));
            params.put("toDateReport", Utils.getFirstDay(dto.getDateReport()));
        }
        QueryUtils.filter(dto.getListEmpStatus(), sql, params, "e.status");
        QueryUtils.filterOriginal(dto.getEmpCode(), sql, params, "e.employee_code");
        QueryUtils.filter(dto.getEmpName(), sql, params, "e.full_name");

    }


}
