/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.kpi.repositories.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import vn.kpi.constants.BaseConstants;
import vn.kpi.constants.Constant;
import vn.kpi.constants.Scope;
import vn.kpi.models.BaseDataTableDto;
import vn.kpi.models.PermissionDataDto;
import vn.kpi.models.dto.CategoryDto;
import vn.kpi.models.dto.EmployeeInfoDto;
import vn.kpi.models.dto.WardDto;
import vn.kpi.models.request.EmployeesRequest;
import vn.kpi.models.response.ContractProcessResponse;
import vn.kpi.models.response.EducationCertificatesResponse;
import vn.kpi.models.response.EmployeesResponse;
import vn.kpi.models.response.FamilyRelationshipsResponse;
import vn.kpi.repositories.BaseRepository;
import vn.kpi.repositories.entity.ContactAddressesEntity;
import vn.kpi.repositories.entity.EmployeesEntity;
import vn.kpi.repositories.entity.WorkedHistoriesEntity;
import vn.kpi.services.AuthorizationService;
import vn.kpi.utils.QueryUtils;
import vn.kpi.utils.Utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Lop repository Impl ung voi bang hr_employees
 *
 * @author tudd
 * @version 1.0
 * @since 1.0
 */

@Repository
@RequiredArgsConstructor
public class EmployeesRepository extends BaseRepository {
    private final AuthorizationService authorizationService;


    private void buildSql(StringBuilder sql, Map<String, Object> params, EmployeesRequest.SearchForm dto) {
        if (sql.isEmpty()) {

            sql.append("""
                    SELECT
                         e.employee_id,
                         e.employee_code,
                         e.full_name,
                         IFNULL(o.full_name, o.name) orgName,
                         e.date_of_birth dateOfBirth,
                         (select sc.name from sys_categories sc where sc.value = e.gender_id and sc.category_type = :genderTypeCode) genderName,
                         (select sc.name from sys_categories sc where sc.value = e.ethnic_id and sc.category_type = :ethnicTypeCode) ethnicName,
                         (select sc.name from sys_categories sc where sc.value = e.religion_id and sc.category_type = :religionTypeCode) religionName,
                         (select sc.name from sys_categories sc where sc.value = e.status and sc.category_type = :empStatus) empStatusName,
                         (select sc.name from sys_categories sc where sc.value = e.marital_status_id and sc.category_type = :maritalStatus) maritalStatusName,
                         (select sc.name from sys_categories sc where sc.value = e.education_level_id and sc.category_type = :educationTypeCode) educationLevelName,
                         e.email,
                         e.mobile_number,
                         e.place_of_birth,
                         e.original_address,
                         e.permanent_address,
                         e.current_address,
                         e.tax_no,
                         e.insurance_no,
                         e.created_by,
                         e.created_time,
                         e.modified_by,
                         e.modified_time,
                         hj.name jobName,
                         et.name empTypeName
                         FROM hr_employees e
                        LEFT JOIN hr_jobs hj ON hj.job_id = e.job_id
                        JOIN hr_organizations o ON o.organization_id = e.organization_id
                        LEFT JOIN hr_emp_types et ON et.emp_type_id = e.emp_type_id
                        WHERE e.is_deleted = :activeStatus
                    """);
        }
        this.addConditionSearchPersonalEmp(sql, params, dto);
    }

    public List<Map<String, Object>> getListExport(String sqlSelect, EmployeesRequest.SearchForm dto) {

        StringBuilder sql = Utils.isNullOrEmpty(sqlSelect) ? new StringBuilder() : new StringBuilder(sqlSelect);
        Map<String, Object> params = new HashMap<>();
        buildSql(sql, params, dto);
        List<Map<String, Object>> dataList = getListData(sql.toString(), params);
        if (Utils.isNullOrEmpty(dataList)) {
            dataList.add(getMapEmptyAliasColumns(sql.toString()));
        }
        return dataList;
    }

    public BaseDataTableDto<EmployeesResponse.SearchResult> searchBasicInfoEmployee(EmployeesRequest.SearchForm dto) {
        StringBuilder sql = new StringBuilder();
        Map<String, Object> params = new HashMap<>();
        buildSql(sql, params, dto);
        return getListPagination(sql.toString(), params, dto, EmployeesResponse.SearchResult.class);
    }

    private void addConditionSearchPersonalEmp(StringBuilder sql, Map<String, Object> params, EmployeesRequest.SearchForm dto) {
        params.put("genderTypeCode", Constant.CATEGORY_CODES.GIOI_TINH);
        params.put("ethnicTypeCode", Constant.CATEGORY_CODES.DAN_TOC);
        params.put("religionTypeCode", Constant.CATEGORY_CODES.TON_GIAO);
        params.put("empStatus", Constant.CATEGORY_CODES.HR_TRANG_THAI_NHAN_VIEN);
        params.put("maritalStatus", Constant.CATEGORY_CODES.TINH_TRANG_HON_NHAN);
        params.put("educationTypeCode", Constant.CATEGORY_CODES.TRINH_DO_VAN_HOA);
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        UtilsEmployeeSearchRepository.setParamEmployeeSearch(dto, sql, params);
        List<PermissionDataDto> permissionDataDtos = authorizationService.getPermissionData(
                Scope.VIEW, Constant.RESOURCES.EMPLOYEE, Utils.getUserNameLogin()
        );
        QueryUtils.addConditionPermission(permissionDataDtos, sql, params);
        sql.append(" ORDER BY e.employee_id");
    }

    public BaseDataTableDto<EmployeesResponse.SearchResult> getEmpDataPicker(EmployeesRequest.SearchForm dto) {
        StringBuilder sql = new StringBuilder("""
                    SELECT
                        CASE
                            WHEN e.employee_id IN (:selectedValue) THEN 1
                            ELSE 0
                        END AS valueSelect,
                        e.employee_id,
                        e.employee_code,
                        e.full_name fullName,
                        concat(e.employee_code, ' - ', e.full_name) label,
                        e.email email,
                        e.position_id positionId,
                        e.mobile_number,
                        ho.organization_id orgId,
                        IFNULL(ho.full_name, ho.name) orgName,
                        hj.name jobName
                    FROM hr_employees e
                    LEFT JOIN hr_organizations ho on ho.organization_id = e.organization_id
                    LEFT JOIN hr_jobs hj ON hj.job_id = e.job_id
                    WHERE IFNULL(e.is_deleted, :activeStatus) = :activeStatus
                """);
        HashMap<String, Object> params = new HashMap<>();
        params.put("selectedValue", Utils.isNullOrEmpty(dto.getSelectedValue()) ? List.of(0) : dto.getSelectedValue());
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        addConditionSearchEmpDataPicker(dto, sql, params);
        return getListPagination(sql.toString(), params, dto, EmployeesResponse.SearchResult.class);
    }

    private void addConditionSearchEmpDataPicker(EmployeesRequest.SearchForm employeesDTO, StringBuilder sql, HashMap<String, Object> params) {
        QueryUtils.filter(employeesDTO.getEmpTypeId(), sql, params, "e.emp_type_id");
        if (!Utils.isNullOrEmpty(employeesDTO.getKeySearch())) {
            sql.append(" and (");
            sql.append(" e.employee_code like :keyword");
            sql.append(" or upper(e.full_name) like :keyword");
            sql.append(" or upper(e.email) like :keyword");
            sql.append(" or exists(select 1 from hr_personal_identities hpi " +
                       "   where hpi.employee_id = e.employee_id " +
                       "   and upper(hpi.identity_no) like :keyword" +
                       "   and IFNULL(hpi.is_deleted, :activeStatus) = :activeStatus" +
                       " )");
            sql.append(" )");
            params.put("keyword", "%" + employeesDTO.getKeySearch().toUpperCase() + "%");
        }
        QueryUtils.filter(employeesDTO.getListPositionId(), sql, params, "e.job_id");
        if (!Utils.isNullObject(employeesDTO.getOrganizationId())) {
            QueryUtils.filter("/" + employeesDTO.getOrganizationId() + "/", sql, params, "ho.path_id");
        }
        if (!Utils.isNullOrEmpty(employeesDTO.getScope()) && !Utils.isNullOrEmpty(employeesDTO.getFunctionCode())) {
            List<PermissionDataDto> permissionDataDtos = authorizationService.getPermissionData(
                    employeesDTO.getScope(), employeesDTO.getFunctionCode(), Utils.getUserNameLogin()
            );
            QueryUtils.addConditionPermission(permissionDataDtos, sql, params, "ho.path_id", "e.emp_type_id");
        }

        if ("WORKING".equalsIgnoreCase(employeesDTO.getStatus())) {
            sql.append(" and e.status = :empWorking");
            params.put("empWorking", Constant.EMP_STATUS.WORK_IN);
        } else if ("OUT".equalsIgnoreCase(employeesDTO.getStatus())) {
            sql.append(" and e.status = :empOut");
            params.put("empOut", Constant.EMP_STATUS.WORK_OUT);
        } else if ("ACTIVE".equalsIgnoreCase(employeesDTO.getStatus())) {
            sql.append(" and e.status in (:listActiveStatus)");
            params.put("listActiveStatus", List.of(Constant.EMP_STATUS.WORK_IN, Constant.EMP_STATUS.PENDING));
        }
        sql.append(" ORDER BY valueSelect desc, ho.path_order, e.employee_code");
    }


    public EmployeeInfoDto getPersonalInformation(Long employeeId) {
        String sql = """
                SELECT
                	e.*,
                	sc1.name genderName,
                	sc2.name ethnicName,
                	sc3.name religionName,
                	sc4.name NationName,
                	sc5.name maritalStatusName,
                	sc6.name educationLevelName,
                	hp.name positionName,
                	(select name from hr_jobs where job_id = hp.job_id) as job_name,
                	(select sc.name from sys_categories sc
                	where sc.order_number = e.emp_type_id
                		and sc.category_type = :typeEmpTypeCode) empTypeName,
                	IFNULL(org.full_name, org.name) orgName
                FROM hr_employees e
                LEFT JOIN sys_categories sc1 ON
                	sc1.value = e.gender_id
                	AND sc1.category_type = :genderTypeCode
                LEFT JOIN sys_categories sc2 ON
                	sc2.value = e.ethnic_id
                	AND sc2.category_type = :ethnicTypeCode
                LEFT JOIN sys_categories sc3 ON
                	sc3.value = e.religion_id
                	AND sc3.category_type = :religionTypeCode
                LEFT JOIN sys_categories sc4 ON
                 	sc4.value = e.religion_id
                 	AND sc4.category_type = :nationTypeCode
                LEFT JOIN sys_categories sc5 ON
                	sc5.value = e.marital_status_id
                	AND sc5.category_type = :maritalStatusTypeCode
                LEFT JOIN sys_categories sc6 ON
                	sc6.value = e.education_level_id
                	AND sc6.category_type = :educationTypeCode
                LEFT JOIN hr_organizations org ON
                	org.organization_id = e.organization_id
                LEFT JOIN hr_positions hp ON
                	hp.position_id = e.position_id
                WHERE
                	e.employee_id = :employeeId
                	AND IFNULL(e.is_deleted , :idDeleted) = :idDeleted
                """;
        HashMap<String, Object> hashMapParams = new HashMap<>();
        hashMapParams.put("genderTypeCode", Constant.CATEGORY_CODES.GIOI_TINH);
        hashMapParams.put("ethnicTypeCode", Constant.CATEGORY_CODES.DAN_TOC);
        hashMapParams.put("religionTypeCode", Constant.CATEGORY_CODES.TON_GIAO);
        hashMapParams.put("nationTypeCode", Constant.CATEGORY_CODES.QUOC_GIA);
        hashMapParams.put("maritalStatusTypeCode", Constant.CATEGORY_CODES.TINH_TRANG_HON_NHAN);
        hashMapParams.put("educationTypeCode", Constant.CATEGORY_CODES.TRINH_DO_VAN_HOA);
        hashMapParams.put("employeeId", employeeId);
        hashMapParams.put("idDeleted", BaseConstants.STATUS.NOT_DELETED);
        hashMapParams.put("typeEmpTypeCode", Constant.CATEGORY_CODES.DIEN_DOI_TUONG);
        return getFirstData(sql, hashMapParams, EmployeeInfoDto.class);
    }

    public boolean checkDuplicateTaxNoWithEmp(String taxNo, Long empId) {
        if (Utils.isNullOrEmpty(taxNo)) {
            return false;
        }
        String sql = """
                    SELECT count(1)
                    FROM hr_employees e
                    WHERE e.tax_no = :taxNo
                    AND e.status <> :statusOut
                    AND IFNULL(e.is_deleted, :activeStatus) = :activeStatus
                    AND e.employee_id != :empId
                """;
        HashMap<String, Object> params = new HashMap<>();
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        params.put("taxNo", taxNo);
        params.put("empId", Utils.NVL(empId));
        params.put("statusOut", BaseConstants.EMP_STATUS.OUT);
        return queryForObject(sql, params, Integer.class) > 0;
    }

    public EmployeeInfoDto getEmployeeInfo(Long employeeId) {
        String sql = """
                select 
                	e.employee_id,
                	e.employee_code,
                	e.full_name,
                	e.email,
                	e.mobile_number,
                	e.date_of_birth,
                	org.full_name organization_name,
                    case 
                        when jb.job_type = 'CONG_VIEC'
                        then jb.name 
                        else (
                            select mj1.name from hr_concurrent_process cp1, hr_jobs mj1
                            where cp1.job_id = mj1.job_id 
                            and cp1.employee_id = e.employee_id
                            and DATE(now()) between cp1.start_date and ifnull(cp1.end_date, now())
                            and mj1.job_type = 'CONG_VIEC'
                            and cp1.is_deleted = 'N'
                            limit 1
                        )
                    end jobName,
                    case 
                        when jb.job_type = 'CHUC_VU'
                        then jb.name 
                    end positionName,
                    (select GROUP_CONCAT(CONCAT_WS(' - ', mj1.name, o1.full_name) SEPARATOR ', ')
                        from hr_concurrent_process cp1, hr_jobs mj1, hr_organizations o1
                            where cp1.job_id = mj1.job_id 
                            and cp1.employee_id = e.employee_id
                            and DATE(now()) between cp1.start_date and ifnull(cp1.end_date, now())
                            and o1.organization_id = cp1.organization_id
                            and mj1.job_type = 'CHUC_VU'
                            and cp1.is_deleted = 'N'
                    ) as otherPositionName,
                	(select name from sys_categories sc where sc.category_type = 'GIOI_TINH' and sc.`value` = e.gender_id) as gender_name,
                	(select name from sys_categories sc where sc.category_type = 'QUOC_TICH' and sc.`value` = e.nation_id) as nation_name,
                	(select name from sys_categories sc where sc.category_type = 'DAN_TOC' and sc.`value` = e.ethnic_id) as ethnic_name,
                	(select name from sys_categories sc where sc.category_type = 'TON_GIAO' and sc.`value` = e.religion_id) as religion_name,	
                	(select name from sys_categories sc where sc.category_type = 'TINH_TRANG_HON_NHAN' and sc.`value` = e.marital_status_id) as marital_status_name,
                	(select name from sys_categories sc where sc.category_type = 'TRINH_DO_VAN_HOA' and sc.`value` = e.education_level_id) as education_level_name,
                	e.status,
                	(select name from sys_categories sc where sc.category_type = 'HR_TRANG_THAI_NHAN_VIEN' and sc.`value` = e.status) as status_name,	
                	IFNULL((select name from sys_categories sc where sc.category_type = 'LOAI_GIAY_TO' and sc.`value` = pid.identity_type_id), 'CMT/CCCD')
                		as identity_type,
                	pid.identity_no,
                	pid.identity_issue_date,
                	pid.identity_issue_place,
                	e.tax_no,
                	e.insurance_no,
                	e.permanent_address,
                	e.place_of_birth,
                	e.current_address,
                	e.original_address,
                	IFNULL((select name from sys_categories sc where sc.category_type = :trinhDoDaoTao and sc.`value` = ed.major_level_id),
                		ed.major_level_name) as major_level_name,	
                	IFNULL((select name from sys_categories sc where sc.category_type = :chuyenNganhDaoTao and sc.`value` = ed.major_id),
                		ed.major_name) as major_name,	
                	IFNULL((select name from sys_categories sc where sc.category_type = 'TRUONG_DAO_TAO' and sc.`value` = ed.training_school_id),
                		ed.training_school_name) as training_school_name,	
                	scep.name as promotion_rank_name,
                	scep.code as promotion_rank_code,
                	ep.issued_year as promotion_rank_year,
                	e.party_number,
                	e.party_date,
                	e.party_official_date,
                	e.party_place,
                	f_get_seniority(e.employee_id, now()) as seniority,
                	(select name from sys_categories sc where sc.category_type = 'THANH_PHAN_GIA_DINH' and sc.`value` = e.family_policy_id) as family_policy_name,
                	(select name from sys_categories sc where sc.category_type = 'THANH_PHAN_BAN_THAN' and sc.`value` = e.self_policy_id) as self_policy_name
                from hr_employees e
                left join hr_positions p on e.position_id = p.position_id
                left join hr_jobs jb on e.job_id = jb.job_id
                left join hr_organizations org on e.organization_id = org.organization_id
                left join hr_personal_identities pid on pid.is_main = 'Y' and pid.employee_id = e.employee_id and pid.is_deleted = 'N'
                left join hr_education_degrees ed on ed.is_highest = 'Y' and ed.employee_id = e.employee_id and ed.is_deleted = 'N'
                left join hr_education_promotions ep on ep.employee_id = e.employee_id and ep.is_deleted = 'N'
                left join sys_categories scep on scep.category_type = :categoryTypeHocHam and ep.promotion_rank_id = scep.value
                where e.employee_id = :employeeId
                and not exists (
                    select 1 from hr_education_promotions ep1, sys_categories scep1
                    where ep1.employee_id = :employeeId
                    and ep1.is_deleted = 'N'
                    and ep1.promotion_rank_id = scep1.value
                    and scep1.category_type = :categoryTypeHocHam
                    and scep1.order_number < scep.order_number
                )
                """;
        Map mapParams = new HashMap();
        mapParams.put("employeeId", employeeId);
        mapParams.put("categoryTypeHocHam", Constant.CATEGORY_CODES.HOC_HAM);
        mapParams.put("trinhDoDaoTao", Constant.CATEGORY_CODES.TRINH_DO_DAO_TAO);
        mapParams.put("chuyenNganhDaoTao", Constant.CATEGORY_CODES.CHUYEN_NGANH_DAO_TAO);
        return getFirstData(sql, mapParams, EmployeeInfoDto.class);
    }



    public String getJobBeforeRecruitment(Long employeeId) {
        String sql = "select a.* from hr_worked_histories a " +
                     " where a.is_deleted = 'N'" +
                     "   and a.employee_id = :employeeId" +
                     "   order by a.end_date desc" +
                     "   limit 1";
        Map mapParams = new HashMap();
        mapParams.put("employeeId", employeeId);
        WorkedHistoriesEntity workedHistories = getFirstData(sql, mapParams, WorkedHistoriesEntity.class);
        return workedHistories == null ? null : Utils.join(", ", workedHistories.getJob(), workedHistories.getCompanyName());
    }

    public List<FamilyRelationshipsResponse.DetailBean> getListFamilyRelationship(Long employeeId) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    a.family_relationship_id,
                    a.employee_id,
                    a.full_name familyRelationshipName,
                    a.date_of_birth,
                    a.job,
                    a.organization_address,
                    a.current_address,
                    a.personal_id_no,
                    a.mobile_number,
                    sct.name relationTypeName,
                    (select sc.name from sys_categories sc where sc.value = a.relation_status_id and sc.category_type = :relationStatus) relationStatusName,
                    (select sc.name from sys_categories sc where sc.value = a.policy_type_id and sc.category_type = :policyType) policyTypeName
                FROM hr_family_relationships a
                join sys_categories sct on sct.value = a.relation_type_id and sct.category_type = :relationType
                WHERE a.is_deleted = :activeStatus
                and a.employee_id = :employeeId
                and exists (
                    select 1 from sys_category_attributes st
                    where st.category_id = sct.category_id
                    and st.attribute_code = 'IS_SHOW_COMMON_INFO'
                    and st.attribute_value = 'Y'
                    and st.is_deleted = 'N' 
                )
                order by sct.order_number, a.date_of_birth
                """);
        HashMap<String, Object> params = new HashMap<>();
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        params.put("empStatus", Constant.CATEGORY_CODES.HR_TRANG_THAI_NHAN_VIEN);
        params.put("relationType", Constant.CATEGORY_CODES.MOI_QUAN_HE_TN);
        params.put("relationStatus", Constant.CATEGORY_CODES.TINH_TRANG_TN);
        params.put("policyType", Constant.CATEGORY_CODES.DOI_TUONG_CHINH_SACH);
        params.put("employeeId", employeeId);
        return getListData(sql.toString(), params, FamilyRelationshipsResponse.DetailBean.class);
    }

    public List<EducationCertificatesResponse.DetailBean> getListCertificates(Long employeeId) {
        String sql = """
                select 
                	sc.`name` certificateTypeName,
                	ifnull(ec.certificate_name,cc.`name`) certificateName,
                	ec.result
                from hr_education_certificates ec
                left join sys_categories sc on ec.certificate_type_id = sc.value and sc.category_type = 'LOAI_CHUNG_CHI'
                left join sys_categories cc on ec.certificate_id = cc.value and cc.category_type = 'TEN_CHUNG_CHI'
                where ec.is_deleted = 'N'
                and not exists (
                	select 1 from hr_education_certificates ec1
                	where ec1.employee_id = ec.employee_id
                	and ec1.certificate_type_id = ec.certificate_type_id
                	and ec1.is_deleted = 'N'
                	and (
                		ec1.issued_date > ec.issued_date
                		or (ec1.issued_date > ec.issued_date and ec1.education_certificate_id > ec.education_certificate_id)
                	)	
                )
                and ec.employee_id = :employeeId
                group by ec.certificate_type_id, ec.certificate_id, ec.certificate_name
                order by ifnull(sc.order_number,1000000)
                """;
        HashMap<String, Object> params = new HashMap<>();
        params.put("employeeId", employeeId);
        return getListData(sql.toString(), params, EducationCertificatesResponse.DetailBean.class);
    }

    public ContractProcessResponse.DetailBean getContractInfo(Long employeeId) {
        String sql = """
                select
                    et.name as empTypeName,
                    ct.name as contractTypeName,
                    cp.end_date as endDate 
                from hr_contract_process cp
                left join hr_emp_types et on cp.emp_type_id = et.emp_type_id
                left join hr_contract_types ct on cp.contract_type_id = ct.contract_type_id
                where cp.employee_id = :employeeId
                and cp.classify_code = :hopDong
                and cp.is_deleted = 'N'
                and cp.start_date <= now() 
                order by cp.start_date desc, cp.created_time desc 
                """;
        HashMap<String, Object> params = new HashMap<>();
        params.put("employeeId", employeeId);
        params.put("hopDong", Constant.CLASSIFY_CONTRACT.HOP_DONG);
        return getFirstData(sql.toString(), params, ContractProcessResponse.DetailBean.class);
    }

    public List<String> getAwardTitle(Long employeeId) {
        String sql = """
                select CONCAT(sc.name,'(', group_concat(a.award_year order by a.award_year separator ', ') , ')') award_form_name
                from hr_award_process a,
                	sys_categories sc
                where a.award_form_id = sc.`value`
                and sc.category_type = 'HINH_THUC_KHEN_THUONG'
                and not exists (
                	select 1 from hr_award_process a1,
                		sys_categories sc1
                	where a1.award_form_id = sc1.`value`
                	and sc1.category_type = 'HINH_THUC_KHEN_THUONG'
                	and a1.employee_id = a.employee_id
                	and sc1.order_number < sc.order_number
                	and a1.is_deleted = 'N'
                )
                and a.employee_id = :employeeId
                and a.is_deleted = 'N'
                group by sc.name
                """;
        HashMap<String, Object> params = new HashMap<>();
        params.put("employeeId", employeeId);
        return getListData(sql.toString(), params, String.class);
    }

    public List<String> getDisciplineTitle(Long employeeId) {
        String sql = """
                select CONCAT(sc.name,' (', 
                    group_concat(DATE_FORMAT(a.start_date,'%m/%Y') order by a.start_date separator ', ') , ')') award_form_name
                from hr_discipline_process a,
                    sys_categories sc
                where a.discipline_form_id = sc.`value`
                and sc.category_type = 'HINH_THUC_KY_LUAT'
                and a.is_deleted = 'N'
                and a.employee_id = :employeeId
                group by sc.name, sc.order_number
                order by sc.order_number
                """;
        HashMap<String, Object> params = new HashMap<>();
        params.put("employeeId", employeeId);
        return getListData(sql.toString(), params, String.class);
    }


    public boolean isEmptyRequiredField(Long employeeId) {
        String sql = """
                select 1 from hr_employees e 
                where e.employee_id = :employeeId
                 and (e.mobile_number is null
                     or e.ethnic_id is null
                     or e.religion_id is null
                     or e.marital_status_id is null
                 )                 
                """;
        HashMap<String, Object> params = new HashMap<>();
        params.put("employeeId", employeeId);
        return queryForObject(sql, params, Integer.class) != null;
    }


    public String getMaxEmpCode(String prefixEmpCode) {
        String sql = "select max(employee_code) from hr_employees a" +
                     " where a.employee_code like :prefixEmpCode";
        HashMap<String, Object> params = new HashMap<>();
        params.put("prefixEmpCode", prefixEmpCode + "%");
        return getFirstData(sql, params, String.class);
    }

    public BaseDataTableDto<EmployeesResponse.SearchResult> searchEmployeeDirectory(EmployeesRequest.SearchForm dto) {
        StringBuilder sql = new StringBuilder("""
                    SELECT
                         e.employee_id,
                         e.employee_code,
                         e.full_name,
                         IFNULL(o.full_name, o.name) orgName,
                         e.date_of_birth dateOfBirth,
                         e.email,
                         e.mobile_number,
                         hj.name jobName,
                         YEAR(date_of_birth) yearOfBirth,
                         (select sc.name from sys_categories sc where sc.value = e.gender_id and sc.category_type = :genderTypeCode) genderName,
                         (select sc.name from sys_categories sc where sc.value = e.status and sc.category_type = :empStatus) empStatusName
                    FROM hr_employees e
                    JOIN hr_organizations o ON o.organization_id = e.organization_id
                    LEFT JOIN hr_jobs hj ON hj.job_id = e.job_id
                    WHERE e.is_deleted = :activeStatus
                """);
        Map<String, Object> params = new HashMap<>();
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        params.put("empStatus", Constant.CATEGORY_CODES.HR_TRANG_THAI_NHAN_VIEN);
        params.put("genderTypeCode", Constant.CATEGORY_CODES.GIOI_TINH);
        UtilsEmployeeSearchRepository.setParamEmployeeSearch(dto, sql, params);
//        List<PermissionDataDto> permissionDataDtos = authorizationService.getPermissionData(
//                Scope.VIEW, Constant.RESOURCES.EMPLOYEE_DIRECTORY, Utils.getUserNameLogin()
//        );
//        QueryUtils.addConditionPermission(permissionDataDtos, sql, params);
        sql.append(" ORDER BY e.status, o.path_order, hj.order_number, e.employee_code");
        return getListPagination(sql.toString(), params, dto, EmployeesResponse.SearchResult.class);
    }

    public Map<String, EmployeesResponse.BasicInfo> getMapEmpByCode(List<String> empCodeList) {
        Map<String, EmployeesResponse.BasicInfo> mapEmp = new HashMap<>();
        if (Utils.isNullOrEmpty(empCodeList)) {
            return mapEmp;
        }

        String sql = """
                select employee_id, employee_code, full_name
                from hr_employees e
                where ifnull(e.is_deleted, :isDeleted) = :isDeleted
                    and e.employee_code in (:empCodeList)
                """;
        Map<String, Object> params = new HashMap<>();
        params.put("isDeleted", BaseConstants.STATUS.NOT_DELETED);
        params.put("empCodeList", empCodeList);

        List<EmployeesResponse.BasicInfo> empResponseList = getListData(sql, params, EmployeesResponse.BasicInfo.class);
        empResponseList.forEach(item -> mapEmp.put(item.getEmployeeCode().toLowerCase(), item));

        return mapEmp;
    }

    public Map<String, EmployeesEntity> getMapEmpEntityByCode(List<String> empCodeList) {
        Map<String, EmployeesEntity> mapEmp = new HashMap<>();
        if (Utils.isNullOrEmpty(empCodeList)) {
            return mapEmp;
        }

        String sql = """
                select e.*
                from hr_employees e
                where ifnull(e.is_deleted, :isDeleted) = :isDeleted
                    and e.employee_code in (:empCodeList)
                """;
        Map<String, Object> params = new HashMap<>();
        params.put("isDeleted", BaseConstants.STATUS.NOT_DELETED);
        params.put("empCodeList", empCodeList);

        List<EmployeesEntity> empResponseList = getListData(sql, params, EmployeesEntity.class);
        empResponseList.forEach(item -> mapEmp.put(item.getEmployeeCode().toLowerCase(), item));

        return mapEmp;
    }


    public List<CategoryDto> getListCategories(String categoryType) {
        String sql = """
                select value, name, code
                from sys_categories
                where is_deleted = 'N'
                  and category_type = :categoryType
                  order by ifnull(order_number,:maxInteger), name
                """;
        Map<String, Object> map = new HashMap<>();
        map.put("categoryType", categoryType);
        map.put("maxInteger", Integer.MAX_VALUE);
        return getListData(sql, map, CategoryDto.class);
    }


    public Long getOrgByOrgLevelManage(String employeeCode, Long orgLevelManage) {
        String sql = """
                select op.organization_id
                from hr_organizations org
                join hr_employees e on e.employee_code = :employeeCode 
                    and e.organization_id = org.organization_id
                join hr_organizations op on op.org_level_manage = :orgLevelManage 
                    and org.path_id like concat(op.path_id, '%')
                limit 1
                """;
        Map<String, Object> params = new HashMap<>();
        params.put("employeeCode", employeeCode);
        params.put("orgLevelManage", orgLevelManage);
        return queryForObject(sql, params, Long.class);
    }

    public List<WardDto> getListWards() {
        String sql = """
                select 
                	sc.`value`,
                	sc.`code`,
                	sc.`name`,
                	ct.`value` as province_id,
                	ct.`name` as province_name
                from sys_categories sc
                	join sys_category_attributes sct on sc.category_id = sct.category_id and sct.is_deleted = 'N' and sct.attribute_code = 'MA_TINH'
                	join sys_categories ct on ct.category_type = 'TINH' and ct.`value` = sct.attribute_value
                where sc.category_type = 'XA'
                and sc.is_deleted = 'N'
                """;
        return getListData(sql, new HashMap<>(), WardDto.class);
    }

    public Map<String,ContactAddressesEntity> getMapContactAddress(List<String> empCodeList) {
        String sql = """
                select a.* from hr_contact_addresses a
                    join hr_employees e on e.employee_id = a.employee_id
                    where a.is_deleted = 'N'
                    and e.employee_code in (:empCodeList) 
                """;
        Map<String, Object> params = new HashMap<>();
        params.put("empCodeList", empCodeList);
        List<ContactAddressesEntity> contactAddresses = getListData(sql, params, ContactAddressesEntity.class);
        Map<String,ContactAddressesEntity> map = new HashMap<>();
        contactAddresses.forEach(item -> {
            map.put(item.getEmployeeId() + "-" + item.getAddressType(), item);
        });
        return map;
    }
}
