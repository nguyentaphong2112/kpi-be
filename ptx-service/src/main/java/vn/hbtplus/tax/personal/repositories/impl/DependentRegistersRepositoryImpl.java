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
import vn.hbtplus.tax.personal.models.dto.FamilyRelationshipsDTO;
import vn.hbtplus.tax.personal.models.request.AdminSearchDTO;
import vn.hbtplus.tax.personal.models.request.DependentRegistersDTO;
import vn.hbtplus.tax.personal.models.response.DependentRegistersResponse;
import vn.hbtplus.repositories.BaseRepository;
import vn.hbtplus.tax.personal.repositories.entity.DependentRegistersEntity;
import vn.hbtplus.tax.personal.repositories.entity.HrDependentPersonsEntity;
import vn.hbtplus.services.AuthorizationService;
import vn.hbtplus.tax.personal.repositories.entity.HrFamilyRelationshipsEntity;
import vn.hbtplus.utils.QueryUtils;
import vn.hbtplus.utils.Utils;

import java.util.*;

/**
 * Lop repository Impl ung voi bang PTX_DEPENDENT_REGISTERS
 *
 * @author tudd
 * @version 1.0
 * @since 1.0
 */

@Repository
@RequiredArgsConstructor
public class DependentRegistersRepositoryImpl extends BaseRepository {

    private final AuthorizationService authorizationService;

    public BaseDataTableDto<DependentRegistersResponse> searchData(AdminSearchDTO dto) {
        StringBuilder sql = new StringBuilder("""
                SELECT a.*,
                 e.employee_code,
                 e.full_name employeeName,
                 e.tax_no empTaxNo,
                 hfr.full_name dependentName,
                 IFNULL(mo.full_name, mo.name) orgName,
                 (select sc0.name from sys_categories sc0 where sc0.code = e.emp_type_id and sc0.code = :typeEmpTypeCode ) empTypeName,
                 (select sc1.name from sys_categories sc1 where sc1.code = a.nation_code and sc1.code = :nationCode ) nationName,
                 (select sc2.name from sys_categories sc2 where sc2.code = a.province_code and sc2.code = :provinceCode ) provinceName,
                 (select sc3.name from sys_categories sc3 where sc3.code = a.district_code and sc3.code = :districtCode ) districtName,
                 (select sc4.name from sys_categories sc4 where sc4.code = a.ward_code and sc4.code = :wardCode ) wardName,
                 sc.name relationTypeName,
                 (select mj.name from hr_jobs mj where mj.job_id = e.job_id) jobName,
                 CASE e.status WHEN 1 THEN 'Đang làm việc' WHEN 2 THEN 'Tạm hoãn HĐ' WHEN 3 THEN 'Đã nghỉ việc' END statusName,
                 CASE a.reg_type WHEN 'CREATE' THEN 'ĐK mới NPT' WHEN 'CANCEL' THEN 'ĐK giảm trừ NPT' END regTypeName,
                 DATE_FORMAT(a.from_date, '%m/%Y') STR_FROM_DATE,
                 DATE_FORMAT(a.to_date, '%m/%Y') STR_TO_DATE,
                 a.created_time STR_CREATE_DATE,
                 e.status empStatus
                """);
        HashMap<String, Object> params = new HashMap<>();
        params.put("typeEmpTypeCode", Constant.CATEGORY_CODES.DOI_TUONG_CV);
        params.put("nationCode", Constant.CATEGORY_CODES.QUOC_GIA);
        params.put("provinceCode", Constant.CATEGORY_CODES.TINH);
        params.put("districtCode", Constant.CATEGORY_CODES.HUYEN);
        params.put("wardCode", Constant.CATEGORY_CODES.XA);
        params.put("relationTypeCode", Constant.CATEGORY_CODES.MOI_QUAN_HE_TN);
        addCondition(dto, sql, params);
        sql.append(" ORDER BY a.created_time ");
        return getListPagination(sql.toString(), params, dto, DependentRegistersResponse.class);
    }

    public List<Map<String, Object>> getDataDependentRegister(AdminSearchDTO dto) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                 a.*,
                 e.employee_code,
                 e.full_name empName,
                 e.tax_no empTaxNo,
                 hfr.full_name dependentName,
                 IFNULL(mo.full_name, mo.name) orgName,
                 (select sc0.name from sys_categories sc0 where sc0.code = e.emp_type_id and sc0.code = :typeEmpTypeCode ) empTypeName,
                 (select sc1.name from sys_categories sc1 where sc1.code = a.nation_code and sc1.code = :nationCode ) nationName,
                 (select sc2.name from sys_categories sc2 where sc2.code = a.province_code and sc2.code = :provinceCode ) provinceName,
                 (select sc3.name from sys_categories sc3 where sc3.code = a.district_code and sc3.code = :districtCode ) districtName,
                 (select sc4.name from sys_categories sc4 where sc4.code = a.ward_code and sc4.code = :wardCode ) wardName,
                 sc.name relationTypeName,
                 (select mj.name from hr_jobs mj where mj.job_id = e.job_id) jobName,
                 CASE e.status WHEN 1 THEN 'Đang làm việc' WHEN 2 THEN 'Tạm hoãn HĐ' WHEN 3 THEN 'Đã nghỉ việc' END empStatusName,
                 CASE a.reg_type WHEN 'CREATE' THEN 'ĐK mới NPT' WHEN 'CANCEL' THEN 'ĐK giảm trừ NPT' END regTypeName,
                 DATE_FORMAT(a.from_date, '%m/%Y') STR_FROM_DATE,
                 DATE_FORMAT(a.to_date, '%m/%Y') STR_TO_DATE,
                 DATE_FORMAT(a.created_time, '%d/%m/%Y') STR_CREATE_DATE,
                 e.status empStatus
                """);
        HashMap<String, Object> params = new HashMap<>();
        params.put("typeEmpTypeCode", Constant.CATEGORY_CODES.DOI_TUONG_CV);
        params.put("nationCode", Constant.CATEGORY_CODES.QUOC_GIA);
        params.put("provinceCode", Constant.CATEGORY_CODES.TINH);
        params.put("districtCode", Constant.CATEGORY_CODES.HUYEN);
        params.put("wardCode", Constant.CATEGORY_CODES.XA);
        params.put("relationTypeCode", Constant.CATEGORY_CODES.MOI_QUAN_HE_TN);
        addCondition(dto, sql, params);
        sql.append(" ORDER BY a.created_time DESC ");
        return getListData(sql.toString(), params);
    }

    public int countRecordRegister(Long dependentRegisterId, Long empId, String regType, Long familyRelationshipId, List<Integer> lstStatus) {
        StringBuilder sql = new StringBuilder("SELECT count(1)"
                + "   FROM ptx_dependent_registers"
                + "   WHERE employee_id = :employeeId"
                + "   AND IFNULL(is_deleted, :activeStatus) = :activeStatus"
                + "   AND reg_type = :regType"
                + "   AND family_relationship_id = :familyRelationshipId");
        HashMap<String, Object> params = new HashMap<>();
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        params.put("employeeId", empId);
        params.put("regType", regType);
        params.put("familyRelationshipId", familyRelationshipId);
        if (dependentRegisterId != null && dependentRegisterId > 0L) {
            sql.append(" AND dependent_register_id != :dependentRegisterId");
            params.put("dependentRegisterId", dependentRegisterId);
        }
        if (!Utils.isNullOrEmpty(lstStatus)) {
            sql.append(" AND status IN (:lstStatus)");
            params.put("lstStatus", lstStatus);
        }
        return queryForObject(sql.toString(), params, Integer.class);
    }

    public List<DependentRegistersEntity> getRecordRegister(Long empId, String regType, Long familyRelationshipId, List<Integer> lstStatus) {
        StringBuilder sql = new StringBuilder("SELECT *"
                + "   FROM ptx_dependent_registers"
                + "   WHERE employee_id = :employeeId"
                + "   AND IFNULL(is_deleted, :activeStatus) = :activeStatus"
                + "   AND reg_type = :regType"
                + "   AND family_relationship_id = :familyRelationshipId");
        HashMap<String, Object> params = new HashMap<>();
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        params.put("employeeId", empId);
        params.put("regType", regType);
        params.put("familyRelationshipId", familyRelationshipId);
        if (!Utils.isNullOrEmpty(lstStatus)) {
            sql.append(" AND status IN (:lstStatus)");
            params.put("lstStatus", lstStatus);
        }
        return getListData(sql.toString(), params, DependentRegistersEntity.class);
    }

    public List<Long> getListIdApproveByForm(AdminSearchDTO dto, Integer status) {
        StringBuilder sql = new StringBuilder("SELECT a.dependent_register_id"
                + "     FROM ptx_dependent_registers a"
                + "     JOIN hr_employees e ON a.employee_id = e.employee_id"
                + "     JOIN hr_organizations mo ON mo.organization_id = e.organization_id"
                + "     WHERE IFNULL(a.is_deleted, :activeStatus) = :activeStatus");
        HashMap<String, Object> params = new HashMap<>();
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        dto.setStatus(status);
        if (Utils.isNullObject(dto.getOrgId())) {
            List<PermissionDataDto> listOrgId = authorizationService.getPermissionData(Scope.APPROVE, Constant.OBJECT_ATTRIBUTES.FUNCTION_CODES.DEPENDENT_REGISTERS,Utils.getUserNameLogin());
            QueryUtils.addConditionPermission(listOrgId, sql, params);
        } else {
            QueryUtils.filter("/" + dto.getOrgId() + "/", sql, params, "mo.path_id");
        }

        return getListData(sql.toString(), params, Long.class);
    }

    public void updateStatusByListId(List<Long> listId, Integer status, Integer statusCondition) {
        String sql = "UPDATE ptx_dependent_registers " +
                "   SET status = :status," +
                "       last_update_date = :modifiedTime," +
                "       last_updated_by = :modifiedBy" +
                "   WHERE dependent_register_id IN (:ids)" +
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

    public Map<String, DependentRegistersEntity> getListRegisterByDependentCodes(String regType, List<String> listDependentCode, List<Integer> lstStatus) {
        Map<String, DependentRegistersEntity> mapResult = new HashMap<>();
        if (listDependentCode == null || listDependentCode.isEmpty()) {
            return mapResult;
        }
        StringBuilder sql = new StringBuilder("SELECT pdr.*,"
                + "   e.employee_code,"
                + "   e.email"
                + "   FROM ptx_dependent_registers pdr"
                + "   JOIN hr_employees e ON e.employee_id = pdr.employee_id"
                + "   WHERE IFNULL(pdr.is_deleted, :activeStatus) = :activeStatus"
                + "   AND UPPER(pdr.dependent_person_code) IN (:listDependentCode)"
        );
        HashMap<String, Object> params = new HashMap<>();
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        QueryUtils.filterEq(regType, sql, params, "pdr.reg_type");
        QueryUtils.filter(lstStatus, sql, params, "pdr.status");

        List<List<String>> listPartition = Utils.partition(listDependentCode, Constant.SIZE_PARTITION);
        for (List<String> dependentCodes : listPartition) {
            params.put("listDependentCode", dependentCodes);
            List<DependentRegistersEntity> listRegister = getListData(sql.toString(), params, DependentRegistersEntity.class);
            if (listRegister != null && !listRegister.isEmpty()) {
                for (DependentRegistersEntity entity : listRegister) {
                    mapResult.put((entity.getDependentPersonCode()).toLowerCase(), entity);
                }
            }
        }
        return mapResult;
    }

    public Map<String, HrDependentPersonsEntity> getListDependentPersonsByEmpCodes(List<String> listEmpCode) {
        Map<String, HrDependentPersonsEntity> mapResult = new HashMap<>();
        if (listEmpCode == null || listEmpCode.isEmpty()) {
            return mapResult;
        }
        StringBuilder sql = new StringBuilder("SELECT hdp.*,"
                + "   e.employee_code"
                + "   FROM hr_dependent_persons hdp"
                + "   JOIN hr_employees e ON e.employee_id = hdp.employee_id"
                + "   WHERE IFNULL(hdp.is_deleted, :activeStatus) = :activeStatus"
                + "   AND e.employee_code IN (:empCodes)"
        );
        HashMap<String, Object> params = new HashMap<>();
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);

        List<List<String>> listPartition = Utils.partition(listEmpCode, Constant.SIZE_PARTITION);
        for (List<String> empCodes : listPartition) {
            params.put("empCodes", empCodes);
            List<HrDependentPersonsEntity> listRegister = getListData(sql.toString(), params, HrDependentPersonsEntity.class);
            if (listRegister != null && !listRegister.isEmpty()) {
                for (HrDependentPersonsEntity entity : listRegister) {
                    mapResult.put((entity.getEmployeeCode() + entity.getFamilyRelationshipId()), entity);
                }
            }
        }
        return mapResult;
    }

    public boolean isConflictProcess(DependentRegistersDTO dto) {
        String sql = "  SELECT sp.from_date fromDate, sp.to_date toDate "
                + " FROM hr_dependent_persons sp "
                + " WHERE sp.employee_id = :employeeId "
                + " AND IFNULL(sp.is_deleted, :activeStatus) = :activeStatus"
//                + " AND sp.dependent_person_id != :dependentPersonId"
                + " AND sp.family_relationship_id = :familyRelationshipsId "
                + " AND (sp.to_date IS NULL OR :fromDate <= sp.to_date) "
                + (dto.getToDate() == null ? "" : " AND sp.from_date <= :toDate");

        HashMap<String, Object> params = new HashMap<>();
        params.put("employeeId", dto.getEmployeeId());
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
//        params.put("dependentPersonId", Utils.IFNULL(dto.getDependentPersonId(), 0L));
        params.put("familyRelationshipsId", dto.getFamilyRelationshipId());
        params.put("fromDate", dto.getFromDate());
        if (dto.getToDate() != null) {
            params.put("toDate", dto.getToDate());
        }
        List<DependentRegistersDTO> lst = getListData(sql, params, DependentRegistersDTO.class);
        if (lst == null || lst.isEmpty()) {
            return false;
        } else if (lst.size() > 1) {
            return true;
        } else {
            DependentRegistersDTO dependentPersonsDTO = lst.get(0);
            return dependentPersonsDTO.getToDate() == null || dependentPersonsDTO.getFromDate().before(dto.getFromDate());
        }
    }

    public int countDependentPersons(DependentRegistersDTO dto) {
        String sql = "  SELECT COUNT(1) "
                + " FROM hr_dependent_persons sp "
                + " WHERE sp.employee_id = :employeeId "
                + " AND IFNULL(sp.is_deleted, :activeStatus) = :activeStatus"
                + " AND sp.family_relationship_id = :familyRelationshipsId "
                + " AND sp.from_date <= :toDateInput "
                + " AND (sp.to_date IS NULL OR :toDateInput <= sp.to_date) ";

        HashMap<String, Object> params = new HashMap<>();
        params.put("employeeId", dto.getEmployeeId());
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        params.put("familyRelationshipsId", dto.getFamilyRelationshipId());
        params.put("toDateInput", dto.getToDate());
        return queryForObject(sql, params, Integer.class);
    }

    public List<Map<String, Object>> getListDPExpsAccordingTaxAuthority(AdminSearchDTO dto) {
        StringBuilder sql;
        if (dto.getIsTaxNoOfDependentPerson() != null && dto.getIsTaxNoOfDependentPerson() == 1) { // has tax no of dp
            sql = new StringBuilder("SELECT e.employee_code                                                   ma_nv1, "
                    + "       e.full_name                                                                     ho_va_ten_nguoi_nop_thue1, "
                    + "       e.tax_no                                                                        mst_cua_nguoi_nop_thue1, "
                    + "       hfr.full_name                                                                   ho_va_ten_nguoi_phu_thuoc1, "
                    + "       a.date_of_birth                                                                 ngay_sinh_nguoi_phu_thuoc1, "
                    + "       a.tax_no                                                                        mst_cua_nguoi_phu_thuoc1, "
                    + "       '01' ma_qt_cua_nguoi_phu_thuoc1, "
                    + "       'Việt Nam' quoc_tich_cua_nguoi_phu_thuoc1, "
                    + "       a.id_no                                                                         cmnd_ho_chieu_nguoi_phu_thuoc1, "
                    + "       CASE "
                    + "             WHEN lower(sc.name) LIKE '%bố%' or lower(sc.name) LIKE '%mẹ%' THEN 'Cha/mẹ' "
                    + "             WHEN lower(sc.name) LIKE '%con%' THEN 'Con' "
                    + "             WHEN lower(sc.name) LIKE '%vợ%' or lower(sc.name) LIKE '%chồng%' THEN 'Vợ/Chồng' "
                    + "             ELSE 'Khác' "
                    + "       END as quan_he_voi_nguoi_nop_thue1, "
                    + "       CASE "
                    + "             WHEN lower(sc.name) LIKE '%bố%' or lower(sc.name) LIKE '%mẹ%' THEN 3 "
                    + "             WHEN lower(sc.name) LIKE '%con%' THEN 1 "
                    + "             WHEN lower(sc.name) LIKE '%vợ%' or lower(sc.name) LIKE '%chồng%' THEN 2 "
                    + "             ELSE 4 "
                    + "       END as ma_quan_he_voi_nguoi_nop_thue1, "
                    + "       a.code_no                                                                       so1, "
                    + "       a.book_no                                                                       quyen_so1, "
                    + "       '01' ma_quoc_gia1, "
                    + "       'Việt Nam' quoc_gia1, "
                    + "       REPLACE(a.province_code, 'TX') ma_tinh_thanh_pho1, "
                    + "       ( SELECT sc.name "
                    + "         FROM sys_categories sc "
                    + "         WHERE sc.code = a.province_code AND sc.type_code = :provinceCode )            tinh_thanh_pho1, "
                    + "       REPLACE(a.district_code, 'TX') ma_quan_huyen1, "
                    + "       ( SELECT sc.name "
                    + "         FROM sys_categories sc "
                    + "         WHERE sc.code = a.district_code AND sc.type_code = :districtCode )            quan_huyen1, "
                    + "       REPLACE(a.ward_code, 'TX') ma_phuong_xa1, "
                    + "       ( SELECT sc.name "
                    + "         FROM sys_categories sc "
                    + "         WHERE sc.code = a.ward_code AND sc.type_code = :wardCode )                    phuong_xa1, "
                    + "       DATE_FORMAT(a.from_date, '%m/%Y')                                                 tu_thang1,  "
                    + "       DATE_FORMAT(a.to_date, '%m/%Y')                                                   den_thang1,  "
                    + "       a.dependent_person_code ma_npt1,  "
                    + "       a.note                                                                          ghi_chu1 ");
        } else {
            sql = new StringBuilder("SELECT e.employee_code                                                   ma_nv,  "
                    + "       e.full_name                                                                     ho_va_ten_nguoi_nop_thue,  "
                    + "       e.tax_no                                                                        mst_cua_nguoi_nop_thue,  "
                    + "       hfr.full_name                                                                   ho_va_ten_nguoi_phu_thuoc,  "
                    + "       a.date_of_birth                                                                 ngay_sinh_nguoi_phu_thuoc,  "
                    + "       a.tax_no                                                                        mst_cua_nguoi_phu_thuoc,  "
                    + "       '01'                                                                            ma_qt_cua_nguoi_phu_thuoc,  "
                    + "       'Việt Nam'                                                                      quoc_tich_cua_nguoi_phu_thuoc,  "
                    + "       a.id_no                                                                         cmnd_ho_chieu_nguoi_phu_thuoc,  "
                    + "       CASE "
                    + "             WHEN lower(sc.name) LIKE '%bố%' or lower(sc.name) LIKE '%mẹ%' THEN 3 "
                    + "             WHEN lower(sc.name) LIKE '%con%' THEN 1 "
                    + "             WHEN lower(sc.name) LIKE '%vợ%' or lower(sc.name) LIKE '%chồng%' THEN 2 "
                    + "             ELSE 4 "
                    + "       END as ma_quan_he_voi_nguoi_nop_thue, "
                    + "       CASE "
                    + "             WHEN lower(sc.name) LIKE '%bố%' or lower(sc.name) LIKE '%mẹ%' THEN 'Cha/mẹ' "
                    + "             WHEN lower(sc.name) LIKE '%con%' THEN 'Con' "
                    + "             WHEN lower(sc.name) LIKE '%vợ%' or lower(sc.name) LIKE '%chồng%' THEN 'Vợ/Chồng' "
                    + "             ELSE 'Khác' "
                    + "       END as quan_he_voi_nguoi_nop_thue, "
                    + "       a.code_no                                                                       so,  "
                    + "       a.book_no                                                                       quyen_so,  "
                    + "       '01'                                                                            ma_quoc_gia,  "
                    + "       'Việt Nam'                                                                      quoc_gia,  "
                    + "       REPLACE(a.province_code, 'TX') ma_tinh_thanh_pho,  "
                    + "       ( SELECT sc.name  "
                    + "         FROM sys_categories sc  "
                    + "         WHERE sc.code = a.province_code AND sc.type_code = :provinceCode )            tinh_thanh_pho,  "
                    + "       REPLACE(a.district_code, 'TX') ma_quan_huyen, "
                    + "       ( SELECT sc.name  "
                    + "         FROM sys_categories sc  "
                    + "         WHERE sc.code = a.district_code AND sc.type_code = :districtCode )            quan_huyen,  "
                    + "       REPLACE(a.ward_code, 'TX') ma_phuong_xa, "
                    + "       ( SELECT sc.name  "
                    + "         FROM sys_categories sc  "
                    + "         WHERE sc.code = a.ward_code AND sc.type_code = :wardCode )                    phuong_xa,  "
                    + "       DATE_FORMAT(a.from_date, '%m/%Y')                                                 tu_thang,  "
                    + "       DATE_FORMAT(a.to_date, '%m/%Y')                                                   den_thang,  "
                    + "       a.dependent_person_code ma_npt,"
                    + "       a.note                                                                          ghi_chu ");
        }
        HashMap<String, Object> params = new HashMap<>();
        params.put("nationCode", Constant.CATEGORY_CODES.QUOC_GIA);
        params.put("relationTypeCode", Constant.CATEGORY_CODES.MOI_QUAN_HE_TN);
        params.put("provinceCode", Constant.CATEGORY_CODES.TINH);
        params.put("districtCode", Constant.CATEGORY_CODES.HUYEN);
        params.put("wardCode", Constant.CATEGORY_CODES.XA);
        addCondition(dto, sql, params);
        sql.append(" AND a.status IN (:listStatusExport)");
        List<Integer> listStatusExport = new ArrayList<>();
        listStatusExport.add(Constant.TAX_STATUS.ACCOUNTANT_RECEIVED);
        listStatusExport.add(Constant.TAX_STATUS.ACCOUNTANT_PROCESSING);
        params.put("listStatusExport", listStatusExport);
        sql.append(" ORDER BY a.created_time ");
        return getListData(sql.toString(), params);
    }

    private void addCondition(AdminSearchDTO dto, StringBuilder sql, HashMap<String, Object> params) {
        sql.append(" FROM ptx_dependent_registers a "
                + "  JOIN hr_employees e ON a.employee_id = e.employee_id "
                + "  LEFT JOIN hr_organizations mo ON mo.organization_id = e.organization_id "
                + "  JOIN hr_family_relationships hfr ON hfr.family_relationship_id = a.family_relationship_id "
                + "  JOIN sys_categories sc ON sc.value = hfr.relation_type_id AND sc.category_type = :relationTypeCode "
                + "  WHERE IFNULL(a.is_deleted, :activeStatus) = :activeStatus AND a.status > :statusDraft ");

        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        params.put("statusDraft", Constant.TAX_STATUS.DRAFT);

        if (Utils.isNullObject(dto.getOrgId())) {
//            List<PermissionDataDto> listOrgId = authorizationService.getPermissionData(Scope.VIEW, Constant.OBJECT_ATTRIBUTES.FUNCTION_CODES.DEPENDENT_REGISTERS,Utils.getUserNameLogin());
//            QueryUtils.addConditionPermission(listOrgId, sql, params);
        } else {
            authorizationService.hasPermissionWithOrg(dto.getOrgId(), Scope.VIEW, Constant.OBJECT_ATTRIBUTES.FUNCTION_CODES.DEPENDENT_REGISTERS);
            QueryUtils.filter("/" + dto.getOrgId() + "/", sql, params, "mo.path_id");
        }

        // mst của người phụ thuộc
        if (dto.getIsTaxNoOfDependentPerson() != null) {
            if (dto.getIsTaxNoOfDependentPerson() == 1) {
                sql.append(" AND a.tax_no IS NOT NULL ");
            } else {
                sql.append(" AND a.tax_no IS NULL ");
            }
        }
        QueryUtils.filter(dto.getDependentTaxNo(), sql, params, "a.tax_no");
        QueryUtils.filter(dto.getDependentPersonCode(), sql, params, "a.dependent_person_code");


    }

    public Long getDependentCodeSequenceValue() {
//        String sql = "SELECT hr_dependent_person_code_seq.nextval FROM dual";
        String sql = "SELECT count(1) hr_dependent_persons";
        return queryForObject(sql, new HashMap<>(), Long.class);
    }


    public List<HrFamilyRelationshipsEntity> getListDataByEmpId(FamilyRelationshipsDTO familyRelationshipsDTO) {
        String sql = """
                SELECT hfr.*,
                     sc1.name relationTypeName,
                     sc2.name relationStatusName,
                     sc3.name policyTypeName
                  FROM hr_family_relationships hfr
                  LEFT JOIN sys_categories sc1 ON sc1.code = hfr.relation_type_id AND sc1.category_type = :typeCode
                  LEFT JOIN sys_categories sc2 ON sc2.code = hfr.relation_status_id AND sc2.category_type = :typeCodeStatus
                  LEFT JOIN sys_categories sc3 ON sc3.code = hfr.policy_type_id AND sc3.category_type = :policyTypeCode
                  WHERE hfr.employee_id = :employeeId
                  AND IFNULL(hfr.is_deleted, :activeStatus) = :activeStatus
                  ORDER BY hfr.date_of_birth DESC
                """;
        HashMap<String, Object> params = new HashMap<>();
        params.put("employeeId", familyRelationshipsDTO.getEmployeeId());
        params.put("typeCode", Constant.CATEGORY_CODES.MOI_QUAN_HE_TN);
        params.put("typeCodeStatus", Constant.CATEGORY_CODES.TINH_TRANG_NT);
        params.put("policyTypeCode", Constant.CATEGORY_CODES.DOITUONG_CHINHSACH);
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        return getListData(sql.toString(), params, HrFamilyRelationshipsEntity.class);
    }

    public List<HrDependentPersonsEntity> getListDependentPersonAndFamily(Long employeeId, Long familyRelationshipId) {
        String sql = """
                SELECT * FROM hr_dependent_persons
                WHERE employee_id = :employeeId
                AND family_relationship_id = :familyRelationshipId
                AND IFNULL(is_deleted, :activeStatus) = :activeStatus
                """;
        HashMap<String, Object> params = new HashMap<>();
        params.put("employeeId", employeeId);
        params.put("familyRelationshipId", familyRelationshipId);
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        return getListData(sql, params, HrDependentPersonsEntity.class);
    }

}
