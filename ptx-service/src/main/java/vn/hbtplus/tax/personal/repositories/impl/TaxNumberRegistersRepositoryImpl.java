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
import vn.hbtplus.tax.personal.models.dto.EmployeeInfosDTO;
import vn.hbtplus.tax.personal.models.dto.PersonalIdentitiesDTO;
import vn.hbtplus.tax.personal.models.request.AdminSearchDTO;
import vn.hbtplus.tax.personal.models.request.TaxNumberRegistersDTO;
import vn.hbtplus.tax.personal.models.response.TaxNumberRegistersResponse;
import vn.hbtplus.tax.personal.repositories.entity.TaxNumberRegistersEntity;
import vn.hbtplus.utils.I18n;
import vn.hbtplus.utils.QueryUtils;
import vn.hbtplus.utils.Utils;

import java.util.*;

/**
 * Lop repository Impl ung voi bang PTX_TAX_NUMBER_REGISTERS
 *
 * @author tudd
 * @version 1.0
 * @since 1.0
 */
@Repository
@RequiredArgsConstructor
public class TaxNumberRegistersRepositoryImpl extends BaseRepository {

    private final AuthorizationService authorizationService;

    public BaseDataTableDto<TaxNumberRegistersResponse> searchAdminRegister(AdminSearchDTO dto) {
        StringBuilder sql = new StringBuilder("SELECT "
                + "    a.*,"
                + "    e.employee_code,"
                + "    e.full_name empName,"
                + "    IFNULL(mo.full_name, mo.name) orgName,"
                + "    (select sc.name from sys_categories sc where sc.code = e.emp_type_id and sc.code = :typeEmpTypeCode ) empTypeName,"
                + "    (select sc.name from sys_categories sc1 where sc1.code = a.id_type_code and sc1.code = :idTypeCode ) idTypeName,"
                + "    (select sc.name from sys_categories sc2 where sc2.code = a.old_id_type_code and sc2.code = :idTypeCode ) oldIdTypeName,"
                + "    (select mj.name from hr_jobs mj where mj.job_id = e.job_id) jobName,"
                + "    e.status empStatus");
        HashMap<String, Object> params = new HashMap<>();
        params.put("typeEmpTypeCode", Constant.CATEGORY_CODES.DOI_TUONG_CV);
        params.put("idTypeCode", Constant.CATEGORY_CODES.LOAI_GIAY_TO);
        addCondition(dto, sql, params);
        sql.append(" ORDER BY a.created_time ");
        return getListPagination(sql.toString(), params, dto, TaxNumberRegistersResponse.class);
    }

    public List<Map<String, Object>> getListNewRegister(AdminSearchDTO dto) {
        StringBuilder sql = new StringBuilder("SELECT "
                + "    a.*,"
                + "    sc.name id_type_name, "
                + "    f_get_label_status(a.status) status_name, "
                + "    DATE_FORMAT(a.created_time, '%d/%m/%Y') STR_CREATE_DATE,"
                + "    e.employee_code,"
                + "    e.full_name empName,"
                + "    IFNULL(mo.full_name, mo.name) orgName,"
                + "    (select sc.name from sys_categories sc where sc.code = e.emp_type_id and sc.code = :typeEmpTypeCode ) empTypeName,"
                + "    (select mj.job_name from hr_jobs mj where mj.job_id = e.job_id) jobName,"
                + "    CASE e.status WHEN 1 THEN 'Đang làm việc' WHEN 2 THEN 'Tạm hoãn HĐ' WHEN 3 THEN 'Đã nghỉ việc' END empStatusName,"
                + "    e.status empStatus");
        HashMap<String, Object> params = new HashMap<>();
        params.put("typeEmpTypeCode", Constant.CATEGORY_CODES.DOI_TUONG_CV);
        addCondition(dto, sql, params);
        sql.append(" ORDER BY a.created_time ");
        return getListData(sql.toString(), params);
    }

    public List<Map<String, Object>> getListChangeRegister(AdminSearchDTO dto) {
        StringBuilder sql = new StringBuilder("SELECT "
                + "    sc.name id_type_name, "
                + "    (select sc.name from sys_categories sc where sc.code = a.old_id_type_code and sc.code = :idTypeCode ) OLD_ID_TYPE_NAME, "
                + "    f_get_label_status(a.status) status_name, "
                + "    a.created_time,"
                + "    DATE_FORMAT(a.created_time, '%d/%m/%Y') STR_CREATE_DATE,"
                + "    a.ID_NO, a.OLD_ID_NO,"
                + "    a.ID_DATE, a.OLD_ID_DATE, "
                + "    a.ID_PLACE, a.OLD_ID_PLACE, "
                + "    e.employee_code,"
                + "    e.full_name empName,"
                + "    IFNULL(mo.full_name, mo.name) orgName,"
                + "    (select sc.name from sys_categories sc where sc.code = e.emp_type_id and sc.code = :typeEmpTypeCode ) empTypeName,"
                + "    (select mj.job_name from hr_jobs mj where mj.job_id = e.job_id) jobName,"
                + "    CASE e.status WHEN 1 THEN 'Đang làm việc' WHEN 2 THEN 'Tạm hoãn HĐ' WHEN 3 THEN 'Đã nghỉ việc' END empStatusName,"
                + "    e.status empStatus");
        HashMap<String, Object> params = new HashMap<>();
        params.put("idTypeCode", Constant.CATEGORY_CODES.LOAI_GIAY_TO);
        params.put("typeEmpTypeCode", Constant.CATEGORY_CODES.DOI_TUONG_CV);
        addCondition(dto, sql, params);
        sql.append(" ORDER BY a.created_time ");
        return getListData(sql.toString(), params);
    }

    public List<Long> getListIdApproveByForm(AdminSearchDTO dto, Integer status) {
        StringBuilder sql = new StringBuilder("SELECT a.tax_number_register_id"
                + "     FROM ptx_tax_number_registers a"
                + "     JOIN hr_employees e ON a.employee_id = e.employee_id"
                + "     JOIN mp_organizations mo ON mo.organization_id = e.organization_id"
                + "     WHERE IFNULL(a.is_deleted, :activeStatus) = :activeStatus");
        HashMap<String, Object> params = new HashMap<>();
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        dto.setStatus(status);
        String functionCode;
        if(Constant.REG_TYPE.TAX_CREATE.equalsIgnoreCase(dto.getRegType())){
            functionCode = Constant.OBJECT_ATTRIBUTES.FUNCTION_CODES.TAX_REGISTER;
        } else {
            functionCode = Constant.OBJECT_ATTRIBUTES.FUNCTION_CODES.TAX_CHANGE;
        }
        if (Utils.isNullObject(dto.getOrgId())) {
            List<PermissionDataDto> listOrgId = authorizationService.getPermissionData(Scope.APPROVE, functionCode, Utils.getUserNameLogin());
            QueryUtils.addConditionPermission(listOrgId, sql, params);
        } else {
            QueryUtils.filter("/" + dto.getOrgId() + "/", sql, params, "mo.path_id");
        }

        return getListData(sql.toString(), params, Long.class);
    }

    private void addCondition(AdminSearchDTO dto, StringBuilder sql, HashMap<String, Object> params) {
        sql.append("    FROM ptx_tax_number_registers a"
                + "     JOIN hr_employees e ON a.employee_id = e.employee_id"
                + "     JOIN hr_organizations mo ON mo.organization_id = e.organization_id"
                + "     LEFT JOIN sys_categories sc ON sc.code = a.id_type_code AND sc.code = :idTypeCode"
                + "     WHERE IFNULL(a.is_deleted, :activeStatus) = :activeStatus"
                + "     AND a.status > :statusDraft");
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        params.put("statusDraft", Constant.TAX_STATUS.DRAFT);
        params.put("idTypeCode", Constant.CATEGORY_CODES.LOAI_GIAY_TO);

//        String functionCode;
//        if(Constant.REG_TYPE.TAX_CREATE.equalsIgnoreCase(dto.getRegType())){
//            functionCode = Constant.OBJECT_ATTRIBUTES.FUNCTION_CODES.TAX_REGISTER;
//        } else {
//            functionCode = Constant.OBJECT_ATTRIBUTES.FUNCTION_CODES.TAX_CHANGE;
//        }

//        if (Utils.isNullObject(dto.getOrgId())) {
//            List<PermissionDataDto> listOrgId = authorizationService.getPermissionData(Scope.VIEW, functionCode, Utils.getUserNameLogin());
//            QueryUtils.addConditionPermission(listOrgId, sql, params);
//        } else {
//            authorizationService.hasPermissionWithOrg(dto.getOrgId(), Scope.VIEW, functionCode);
//            QueryUtils.filter("/" + dto.getOrgId() + "/", sql, params, "mo.path_id");
//        }


    }

    public void updateStatusByListId(List<Long> listId, Integer status, Integer statusCondition) {
        String sql = "UPDATE ptx_tax_number_registers " +
                "   SET status = :status," +
                "       last_update_date = :modifiedTime," +
                "       last_updated_by = :modifiedBy" +
                "   WHERE tax_number_register_id IN (:ids)" +
                "   AND status = :statusCondition";
        List<List<Long>> listPartition = Utils.partition(listId, Constant.SIZE_PARTITION);
        HashMap<String, Object> params = new HashMap<>();
        params.put("status", status);
        params.put("statusCondition", statusCondition);
        params.put("modifiedTime", new Date());
        params.put("modifiedBy", Utils.getUserNameLogin());
        for (List<Long> ids : listPartition) {
            params.put("ids", ids);
            executeSqlDatabase(sql, params);
        }
    }

    public BaseDataTableDto<TaxNumberRegistersResponse> searchPersonalRegister(TaxNumberRegistersDTO dto) {
        String sql = """
                SELECT * FROM(
                        SELECT
                        a.tax_number_register_id registerId,
                        a.created_time,
                        a.status,
                        CASE a.reg_type  WHEN 'CREATE' THEN  1  WHEN 'CHANGE' THEN  2 END reg_type,
                        CASE WHEN a.id_no IS NULL THEN NULL
                            ELSE CONCAT(sc1.name, ': ', a.id_no)
                            END registerInformation,
                        CASE WHEN a.old_id_no IS NULL THEN NULL
                            ELSE CONCAT(sc2.name, ': ', a.old_id_no)
                            END oldRegisterInformation,
                            null to_date
                FROM ptx_tax_number_registers a
                LEFT JOIN sys_categories sc1 ON sc1.code = a.id_type_code AND sc1.category_type = :idTypeCode
                LEFT JOIN sys_categories sc2 ON sc2.code = a.old_id_type_code AND sc2.category_type = :idTypeCode
                WHERE IFNULL(a.is_deleted, :activeStatus) = :activeStatus
                AND a.employee_id = :employeeId
                
                UNION ALL
                
                SELECT  drs.dependent_register_id registerId,
                        drs.created_time,
                        drs.status,
                        CASE drs.reg_type  WHEN 'CREATE' THEN  3  WHEN 'CANCEL' THEN  4 END reg_type,
                        CONCAT(sc.name, ' ', hfr.full_name, ', ', :fromPeriodLabel, ' ', DATE_FORMAT(drs.from_date, '%m/%Y')) registerInformation,
                        '' oldRegisterInformation,
                        drs.to_date
                FROM ptx_dependent_registers drs
                JOIN hr_family_relationships hfr ON hfr.family_relationship_id = drs.family_relationship_id
                JOIN sys_categories sc ON sc.code = hfr.relation_type_id AND sc.category_type = :relationTypeCode
                WHERE IFNULL(drs.is_deleted, :activeStatus) = :activeStatus
                AND drs.employee_id = :employeeId
                UNION ALL
                SELECT
                dr.declaration_register_id registerId,
                dr.created_time,
                        dr.status,
                        5 reg_type,
                        CONCAT(:yearLabel, ' ', dr.year, ', ' , :methodLabel, ': ', dr.method_code) registerInformation,
                        '' oldRegisterInformation,
                        null to_date
                FROM ptx_declaration_registers dr
                WHERE IFNULL(dr.is_deleted, :activeStatus) = :activeStatus
                AND dr.employee_id = :employeeId
                
                UNION ALL
                
                SELECT  dr.invoice_request_id registerId,
                        dr.created_time,
                        dr.status,
                        6 reg_type,
                        CONCAT(:yearLabel, ' ', dr.year) registerInformation,
                        '' oldRegisterInformation,
                        null to_date
                FROM ptx_invoice_requests dr
                WHERE IFNULL(dr.is_deleted, :activeStatus) = :activeStatus
                AND dr.employee_id = :employeeId
                ) s
                WHERE 1 = 1
                """;

        //                + " UNION ALL"
        //                + "    SELECT "
        //                + "         crs.confirm_register_id registerId, "
        //                + "         crs.created_time, "
        //                + "         crs.status, "
        //                + "         7 reg_type, "
        //                + "         CASE crs.is_accepted  WHEN 1 THEN  :yesLabel  ELSE :noLabel END registerInformation, "
        //                + "         '' oldRegisterInformation, "
        //                + "         null to_date "
        //                + "    FROM ptx_confirm_registers crs"
        //                + "    WHERE IFNULL(crs.is_deleted, :activeStatus) = :activeStatus"
        //                + "    AND crs.employee_id = :employeeId"

        StringBuilder condition = new StringBuilder();
        HashMap<String, Object> params = new HashMap<>();
        params.put("idTypeCode", Constant.CATEGORY_CODES.LOAI_GIAY_TO);
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        params.put("fromPeriodLabel", I18n.getMessage("global.fromPeriod"));
        params.put("relationTypeCode", Constant.CATEGORY_CODES.MOI_QUAN_HE_TN);
        params.put("yearLabel", I18n.getMessage("global.year"));
        params.put("methodLabel", I18n.getMessage("global.method"));
        params.put("yesLabel", I18n.getMessage("global.accept"));
        params.put("noLabel", I18n.getMessage("global.reject"));
        params.put("employeeId", dto.getEmployeeId());
        QueryUtils.filterEq(dto.getRegType(), condition, params, "s.reg_type");
        QueryUtils.filter(dto.getStatus(), condition, params, "s.status");
        QueryUtils.filterGe(dto.getFromDate(), condition, params, "s.created_time", "fromDate");
        QueryUtils.filterLe(dto.getToDate(), condition, params, "s.create_date", "toDate");
        condition.append(" ORDER By s.created_time DESC");
        return getListPagination(sql + condition, params, dto, TaxNumberRegistersResponse.class);
    }

    public int countRecordRegister(Long taxNumberRegisterId, Long empId, String regType, List<Integer> lstStatus) {
        StringBuilder sql = new StringBuilder("SELECT count(1)"
                + "   FROM ptx_tax_number_registers"
                + "   WHERE employee_id = :employeeId"
                + "   AND IFNULL(is_deleted, :activeStatus) = :activeStatus"
                + "   AND reg_type = :regType");
        HashMap<String, Object> params = new HashMap<>();
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        params.put("employeeId", empId);
        params.put("regType", regType);
        if (taxNumberRegisterId != null && taxNumberRegisterId > 0L) {
            sql.append(" AND tax_number_register_id != :taxNumberRegisterId");
            params.put("taxNumberRegisterId", taxNumberRegisterId);
        }
        if (!Utils.isNullOrEmpty(lstStatus)) {
            sql.append(" AND status IN (:lstStatus)");
            params.put("lstStatus", lstStatus);
        }
        return queryForObject(sql.toString(), params, Integer.class);
    }

    public Map<String, TaxNumberRegistersEntity> getListRegisterByEmpCodes(String regType, List<String> listEmpCode, List<Integer> lstStatus) {
        Map<String, TaxNumberRegistersEntity> mapResult = new HashMap<>();
        if (listEmpCode == null || listEmpCode.isEmpty()) {
            return mapResult;
        }
        StringBuilder sql = new StringBuilder("SELECT ptr.*, e.full_name, e.employee_code, e.email email_company"
                + "   FROM ptx_tax_number_registers ptr"
                + "   JOIN hr_employees e ON e.employee_id = ptr.employee_id"
                + "   WHERE IFNULL(ptr.is_deleted, :activeStatus) = :activeStatus"
                + "   AND ptr.reg_type = :regType"
                + "   AND e.employee_code IN (:empCodes)"
        );
        HashMap<String, Object> params = new HashMap<>();
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        params.put("regType", regType);
        if (!Utils.isNullOrEmpty(lstStatus)) {
            sql.append(" AND ptr.status IN (:lstStatus)");
            params.put("lstStatus", lstStatus);
        }

        List<List<String>> listPartition = Utils.partition(listEmpCode, Constant.SIZE_PARTITION);
        for (List<String> empCodes : listPartition) {
            params.put("empCodes", empCodes);
            List<TaxNumberRegistersEntity> listRegister = getListData(sql.toString(), params, TaxNumberRegistersEntity.class);
            if (listRegister != null && !listRegister.isEmpty()) {
                for (TaxNumberRegistersEntity entity : listRegister) {
                    mapResult.put(entity.getEmployeeCode().toLowerCase() + entity.getFullName().toLowerCase(), entity);
                }
            }
        }
        return mapResult;
    }

    public List<Map<String, Object>> getListTaxRegisterExps(AdminSearchDTO searchDTO) {
        StringBuilder sql = new StringBuilder("""
                SELECT e.employee_code ma_nhan_vien,
                    e.full_name ho_ten,
                    a.tax_no ma_so_thue,
                    e.date_of_birth ngay_sinh,
                    ( SELECT sc.code FROM sys_categories sc WHERE sc.code = e.gender_id AND sc.category_type = :genderCode) ma_gioi_tinh,
                    ( SELECT sc.name FROM sys_categories sc WHERE sc.code = e.gender_id AND sc.category_type = :genderCode) gioi_tinh,
                    ( SELECT sc.name FROM sys_categories sc WHERE sc.code = e.nation_id AND sc.category_type = :nationCode ) quoc_tich,
                    a.old_id_no so_giay_to_cu,
                    ( SELECT sc.name FROM sys_categories sc WHERE sc.code = e.nation_id AND sc.category_type = 'MA_COQUANTHUE' ) ma_loai_giay_to,
                    ( SELECT sc.name FROM sys_categories sc WHERE sc.code = e.nation_id AND sc.category_type = 'TEN_COQUANTHUE' ) loai_giay_to,
                    a.id_no so_giay_to,
                    a.id_date ngay_cap,
                    ( SELECT sc.code FROM sys_categories sc WHERE sc.code = a.id_place_code AND sc.category_type = 'TX') ma_noi_cap,
                    a.id_place noi_cap,
                    a.permanent_detail so_nha_duong_pho,
                    REPLACE(a.permanent_ward_code, 'TX','') ma_phuong_xa,
                    ( SELECT sc.name FROM sys_categories sc WHERE sc.code = a.permanent_ward_code AND sc.category_type = :wardCode )  phuong_xa,
                    REPLACE(a.permanent_district_code , 'TX','') ma_quan_huyen,
                    ( SELECT sc.name
                      FROM sys_categories sc
                      WHERE sc.code = a.permanent_district_code AND sc.category_type = :districtCode
                     ) quan_huyen,
                    REPLACE(a.permanent_province_code, 'TX','') ma_tinh_thanh_pho,
                    ( SELECT sc.name
                      FROM sys_categories sc
                      WHERE sc.code = a.permanent_province_code AND sc.category_type = :provinceCode
                     ) tinh_thanh_pho,
                    'VN' ma_quoc_gia,
                    ( SELECT sc.name
                      FROM sys_categories sc
                      WHERE sc.code = a.permanent_nation_code AND sc.category_type = :nationCode
                     ) quoc_gia,
                    a.current_detail curr_so_nha_duong_pho,
                    REPLACE(a.current_ward_code, 'TX','')  curr_ma_phuong_xa,
                    ( SELECT sc.name
                      FROM sys_categories sc
                      WHERE sc.code = a.current_ward_code AND sc.category_type = :wardCode 
                     ) curr_phuong_xa,
                    REPLACE(a.current_district_code, 'TX','') curr_ma_quan_huyen,
                    ( SELECT sc.name
                      FROM sys_categories sc
                      WHERE sc.code = a.current_district_code AND sc.category_type = :districtCode 
                     ) curr_quan_huyen,
                    REPLACE(a.current_province_code, 'TX','') curr_ma_tinh_thanh_pho,
                    ( SELECT sc.name
                      FROM sys_categories sc
                      WHERE sc.code = a.current_province_code AND sc.category_type = :provinceCode 
                     ) curr_tinh_thanh_pho,
                    a.mobile_number dien_thoai,
                    a.email email
                """);
        HashMap<String, Object> params = new HashMap<>();
        params.put("nationCode", Constant.CATEGORY_CODES.QUOC_GIA);
        params.put("provinceCode", Constant.CATEGORY_CODES.TINH);
        params.put("districtCode", Constant.CATEGORY_CODES.HUYEN);
        params.put("wardCode", Constant.CATEGORY_CODES.XA);
        params.put("genderCode", Constant.CATEGORY_CODES.GIOI_TINH);
        addCondition(searchDTO, sql, params);
//        sql.append(" AND a.status IN (:listStatusExport)");
//        List<Integer> listStatusExport = new ArrayList<>();
//        listStatusExport.add(Constant.TAX_STATUS.ACCOUNTANT_RECEIVED);
//        listStatusExport.add(Constant.TAX_STATUS.ACCOUNTANT_PROCESSING);
//        params.put("listStatusExport", listStatusExport);
        sql.append(" ORDER BY a.created_time ");
        return getListData(sql.toString(), params);
    }

    public List<TaxNumberRegistersResponse> getListEmpNotRegister(){
        String sql = "SELECT e.employee_code, e.employee_id, e.full_name, e.email" +
                "   FROM hr_employees e" +
                "   WHERE e.status = 1" +
                "   AND IFNULL(e.is_deleted, :activeStatus) = :activeStatus" +
                "   AND e.tax_no IS NULL" +
                "   AND NOT EXISTS (" +
                "       select 1 from ptx_tax_number_registers t" +
                "       where t.employee_id = e.employee_id" +
                "       and t.reg_type = :regType" +
                "       and IFNULL(t.is_deleted, :activeStatus) = :activeStatus" +
                "   )";
        HashMap<String, Object> params = new HashMap<>();
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        params.put("regType", Constant.REG_TYPE.TAX_CREATE);
        return getListData(sql, params, TaxNumberRegistersResponse.class);
    }

    public EmployeeInfosDTO getContactInfo(Long employeeId) {
        String sql = """
                 SELECT  e.employee_id employeeId,
                      e.place_of_birth placeOfBirth,
                      e.permanent_address pernamentAddress,
                      e.current_address currentAddress,
                      e.original_address originalAddress,
                      sc1.code pernamentProvinceCode,
                      sc1.name pernamentProvinceName,
                      sc2.code pernamentDistrictCode,
                      sc2.name pernamentDistrictName,
                      sc3.code pernamentWardCode,
                      sc3.name pernamentWardName,
                      hca.village_address pernamentDetail,
                      sc4.code currentProvinceCode,
                      sc4.name currentProvinceName ,
                      sc5.code currentDistrictCode,
                      sc5.name currentDistrictName,
                      sc6.code currentWardCode,
                      sc6.name currentWardName,
                      hca2.village_address currentDetail
                   FROM hr_employees e
                   LEFT JOIN hr_contact_addresses hca on e.employee_id  = hca.employee_id and hca.address_type = :thuongTru
                   LEFT JOIN hr_contact_addresses hca2 on e.employee_id  = hca2.employee_id and hca2.address_type = :hienTai
                   LEFT JOIN sys_categories sc1 ON sc1.value = hca.province_id AND sc1.category_type = :provinceCode
                   LEFT JOIN sys_categories sc2 ON sc2.value = hca.district_id AND sc2.category_type = :districtCode
                   LEFT JOIN sys_categories sc3 ON sc3.value = hca.ward_id AND sc3.category_type = :wardCode
                   LEFT JOIN sys_categories sc4 ON sc4.value = hca2.province_id AND sc4.category_type = :provinceCode
                   LEFT JOIN sys_categories sc5 ON sc5.value = hca2.district_id AND sc5.category_type = :districtCode
                   LEFT JOIN sys_categories sc6 ON sc6.value = hca2.ward_id AND sc6.category_type = :wardCode
                 WHERE e.employee_id = :employeeId
                """;
        HashMap<String, Object> hashMapParams = new HashMap<>();
        hashMapParams.put("employeeId", employeeId);
        hashMapParams.put("provinceCode", Constant.CATEGORY_TYPE.TINH);
        hashMapParams.put("districtCode", Constant.CATEGORY_TYPE.HUYEN);
        hashMapParams.put("wardCode", Constant.CATEGORY_TYPE.XA);
        hashMapParams.put("thuongTru", Constant.CATEGORY_TYPE.THUONG_TRU);
        hashMapParams.put("hienTai", Constant.CATEGORY_TYPE.HIEN_TAI);
        return getFirstData(sql, hashMapParams, EmployeeInfosDTO.class);
    }

    public List<PersonalIdentitiesDTO> getPersonalIdentities(Long employeeId) {
        String sql = """
                  SELECT
                      hpi.personal_identity_id as personalIdentityId,
                      hpi.identity_type_id as idTypeCode,
                      hpi.employee_id as employeeId,
                      v.name as idTypeName,
                      hpi.identity_no as idNo,
                      hpi.identity_issue_date as idIssueDate,
                      hpi.identity_issue_place as idIssuePlace,
                      hpi.is_main as isMain
                  FROM hr_personal_identities hpi
                  LEFT JOIN sys_categories v ON v.code = hpi.identity_type_id AND v.category_type = :typeCode
                  WHERE hpi.employee_id = :employeeId
                  AND IFNULL(hpi.is_deleted, :activeStatus) = :activeStatus
                  ORDER BY hpi.personal_identity_id
                """;
        HashMap<String, Object> hashMapParams = new HashMap<>();
        hashMapParams.put("employeeId", employeeId);
        hashMapParams.put("typeCode", Constant.CATEGORY_CODES.LOAI_GIAY_TO);
        hashMapParams.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        return getListData(sql, hashMapParams, PersonalIdentitiesDTO.class);
    }

}
