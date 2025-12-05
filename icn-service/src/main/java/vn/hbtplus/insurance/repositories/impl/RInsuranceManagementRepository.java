package vn.hbtplus.insurance.repositories.impl;

import org.springframework.stereotype.Repository;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.insurance.constants.Constant;
import vn.hbtplus.insurance.models.InsuranceContributionsDto;
import vn.hbtplus.insurance.models.request.InsuranceContributionsRequest;
import vn.hbtplus.insurance.models.response.InsuranceContributionsResponse;
import vn.hbtplus.repositories.BaseRepository;
import vn.hbtplus.insurance.repositories.entity.InsuranceContributionsEntity;
import vn.hbtplus.utils.Utils;

import java.util.*;


@Repository
public class RInsuranceManagementRepository extends BaseRepository {

    //    @Value("${config.org.support.market:0}")
    private static final Long orgIdSupport = 9004552L;

    public List<Map<String, Object>> getListExportDSChiTiet(InsuranceContributionsRequest.ReportForm dto) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                	e.employee_code,
                	e.full_name,
                	mj.name job_name,
                	o.org_name_level_1 don_vi,
                	o.org_name_level_2 phong_ban,
                	o.org_name_level_3 to_nhom,
                	date_format(a.period_date,'%m/%Y') as ky_trich_nop,
                	'N/A' as phan_tru,
                	CASE WHEN odb.path_id like '%/:orgIdSupport/%' then odb.org_name_level_3 else odb.org_name_level_2 end don_vi_hach_toan,
                	a.reason ly_do,
                	a.insurance_factor as he_so_luong,
                	a.allowance_factor as he_so_pccv,
                	a.reserve_factor as he_so_clbl,
                	a.seniority_percent as tham_nien_vuot_khung,
                	a.pos_seniority_percent as tham_nien_nghe,
                	ifnull(a.insurance_timekeeping,0) + ifnull(a.leave_timekeeping,0) as cong_che_do,
                	a.insurance_timekeeping as cong_trich_nop,
                	a.maternity_timekeeping as cong_thai_san,
                	case when a.type in (:typeKoQuyLuong) then 0 else a.contract_salary end contract_salary,
                    case when a.type in (:typeKoQuyLuong) then 0 else a.reserve_salary end reserve_salary,
                    case when a.type in (:typeKoQuyLuong) then 0 else a.pos_allowance_salary end pos_allowance_salary,
                    case when a.type in (:typeKoQuyLuong) then 0 else a.seniority_salary end seniority_salary,
                    case when a.type in (:typeKoQuyLuong) then 0 else a.pos_seniority_salary end pos_seniority_salary,
                    case when a.type in (:typeKoQuyLuong) then 0 else a.total_salary end total_salary,
                    case when a.type = :typeBHYT then a.total_salary else 0 end salary_bhyt,
                	a.per_social_amount,
                	a.unit_social_amount,
                	a.per_medical_amount,
                	a.unit_medical_amount,
                	a.per_unemp_amount,
                	a.unit_unemp_amount,
                	a.unit_union_amount,
                	a.base_union_amount,
                	a.superior_union_amount,
                	a.mod_union_amount,
                	a.total_amount,
                	(select et.name from hr_emp_types et where et.code = a.emp_type_code) emp_type_code,
                	(select s.name from sys_categories s where s.value = a.labour_type and s.category_type = :labourTypeCode) loai_lao_dong,
                	(select s.name from sys_categories s where s.value = a.status and s.category_type = :statusType) status,
                	(select s.name from sys_categories s where s.value = a.type and s.category_type = :typeCateType) type,
                	a.reason,
                	a.note
                """);
        sql.append("""
                    FROM icn_insurance_contributions a
                    JOIN hr_employees e ON e.employee_id = a.employee_id
                    LEFT JOIN hr_jobs mj ON mj.job_id = a.job_id
                    JOIN hr_organizations o ON o.organization_id = a.org_id
                    LEFT JOIN hr_organizations odb ON odb.organization_id = a.debit_org_id
                    WHERE a.is_deleted = :activeStatus
                    and a.type IN (:types)
                """);
        Map<String, Object> params = new HashMap<>();
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        params.put("statusType", Constant.CATEGORY_TYPE.TRANG_THAI_THU_BHXH);
        params.put("typeCateType", Constant.CATEGORY_TYPE.LOAI_DS_TRICH_NOP);
        params.put("labourTypeCode", Constant.CATEGORY_TYPE.PHAN_LOAI_LAO_DONG);
        params.put("typeBHYT", InsuranceContributionsEntity.TYPES.TRUY_THU_BHYT);
        params.put("types", Arrays.asList(InsuranceContributionsEntity.TYPES.THU));
        params.put("typeKoQuyLuong", Arrays.asList(InsuranceContributionsEntity.TYPES.THAI_SAN, InsuranceContributionsEntity.TYPES.TRUY_THU_BHYT));
        params.put("orgIdSupport", orgIdSupport);
        CommonRepository.addFilter(sql, params, dto);
        sql.append("""
                
                ORDER BY a.type, o.path_order, e.employee_id
                """);

        List<Map<String, Object>> result = getListData(sql.toString(), params);
//        if (result.isEmpty()) {
//            result.add(getMapEmptyAliasColumns(sql.toString()));
//        }
        return result;
    }

    public List<Map<String, Object>> getListQTThamGiaBHXH(InsuranceContributionsRequest.ReportForm dto) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    DATE_FORMAT(a.period_date, :shortFormat) period_date,
                    e.employee_code,
                    e.full_name,
                    (select et.name from hr_emp_types et where et.code = a.emp_type_code) emp_type_name,
                    o.org_name_level_3,
                    o.org_name_level_2,
                    o.org_name_level_1,
                    a.insurance_factor, 
                    a.reserve_factor,
                    a.allowance_factor,
                    a.seniority_percent,
                    a.pos_seniority_percent,
                    a.total_salary,
                    a.reason
                FROM icn_insurance_contributions a
                JOIN hr_employees e ON e.employee_id = a.employee_id
                JOIN hr_organizations o ON a.org_id = o.organization_id
                WHERE a.is_deleted = :activeStatus
                AND a.type IN (:types)
                """);
        Map<String, Object> params = new HashMap<>();
        params.put("shortFormat", BaseConstants.SQL_SHORT_DATE_FORMAT);
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        params.put("types", Arrays.asList(InsuranceContributionsEntity.TYPES.INSURANCE_PROCESS));
        CommonRepository.addFilter(sql, params, dto);
        sql.append("""
                
                ORDER BY a.period_date, o.path_order, e.employee_code
                """);
        List<Map<String, Object>> resultList = getListData(sql.toString(), params);
        return resultList;

    }

    public List<Map<String, Object>> getListKoThuBHXH(InsuranceContributionsRequest.ReportForm dto) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    DATE_FORMAT(a.period_date, :shortFormat) period_date,
                    e.employee_code,
                    e.full_name,
                    (select et.name from hr_emp_types et where et.code = a.emp_type_code) emp_type_name,
                    j.name job_name,
                    o.org_name_level_3,
                    o.org_name_level_2,
                    o.org_name_level_1,
                    a.insurance_factor, 
                    a.insurance_timekeeping,
                    IFNULL(a.insurance_timekeeping, 0) + IFNULL(a.leave_timekeeping, 0) base_timekeeping,
                    a.reason
                FROM icn_insurance_contributions a
                JOIN hr_employees e ON e.employee_id = a.employee_id
                JOIN hr_organizations o ON a.org_id = o.organization_id
                JOIN hr_jobs j ON j.job_id = a.job_id 
                WHERE a.is_deleted = :activeStatus
                AND a.type = :type
                """);
        Map<String, Object> params = new HashMap<>();
        params.put("shortFormat", BaseConstants.SQL_SHORT_DATE_FORMAT);
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        params.put("type", InsuranceContributionsEntity.TYPES.KO_THU);
        CommonRepository.addFilter(sql, params, dto);
        sql.append("""
                
                ORDER BY a.period_date, o.path_order, e.employee_code
                """);
        List<Map<String, Object>> resultList = getListData(sql.toString(), params);
        return resultList;

    }

    public List<InsuranceContributionsResponse> getDataTongHop(InsuranceContributionsRequest.ReportForm dto) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                     a.period_date,
                     SUM(IFNULL(a.unit_social_amount, 0) + IFNULL(a.unit_medical_amount, 0) + IFNULL(a.unit_unemp_amount, 0)) sumUnitTotal,
                     SUM(IFNULL(a.per_social_amount, 0) + IFNULL(a.per_medical_amount, 0) + IFNULL(a.per_unemp_amount, 0)) sumPerTotal,
                     SUM(a.unit_union_amount) sumUnitUnionAmount,
                     a.insurance_agency
                FROM icn_insurance_contributions a
                WHERE a.is_deleted = :activeStatus
                AND a.period_date BETWEEN :startMonth and :endMonth
                """);

        Map<String, Object> params = new HashMap<>();
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        params.put("startMonth", Utils.stringToDate("01/01/" + dto.getYear()));
        params.put("endMonth", Utils.stringToDate("31/12/" + dto.getYear()));

        if (!Utils.isNullOrEmpty(dto.getListLocationJoin())) {
            sql.append(" AND a.insurance_agency IN (:insuranceAgency) ");
            params.put("insuranceAgency", dto.getListLocationJoin());
        }
        if (!Utils.isNullOrEmpty(dto.getListStatus())) {
            sql.append(" AND a.status IN (:lstStatusFilter) ");
            params.put("lstStatusFilter", dto.getListStatus());
        }
        sql.append(" GROUP BY a.period_date, a.insurance_agency");
        return getListData(sql.toString(), params, InsuranceContributionsResponse.class);
    }

    public List<Map<String, Object>> getDataTongHopChiTiet(InsuranceContributionsRequest.ReportForm dto, List<String> types) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    DATE_FORMAT(a.period_date, :shortFormat) period_date,
                    DATE_FORMAT(e.date_of_birth, :yearFormat) year_of_birth,
                    (select s.name from sys_categories s where s.value = a.type and s.category_type = :typeCateType) type,
                    e.employee_code,
                    e.full_name,
                    (select et.name from hr_emp_types et where et.code = a.emp_type_code) emp_type_name,
                    j.name job_name,
                    o.org_name_level_3,
                    o.org_name_level_2,
                    o.org_name_level_1,
                    'N/A' phan_loai_lao_dong,
                	'N/A' phan_loai_tru,
                    (
                        IFNULL(a.unit_social_amount, 0) 
                        + IFNULL(a.unit_medical_amount, 0) 
                        + IFNULL(a.unit_unemp_amount, 0)
                        + IFNULL(a.unit_union_amount, 0)
                    ) sumUnitTotal,
                    case when a.type in (:typeKoQuyLuong) then 0 else a.total_salary end total_salary,
                    a.per_social_amount,
                    a.per_medical_amount,
                    a.per_unemp_amount,
                    a.unit_social_amount,
                    a.unit_medical_amount,
                    a.unit_unemp_amount
                FROM icn_insurance_contributions a
                JOIN hr_employees e ON e.employee_id = a.employee_id
                JOIN hr_organizations o ON a.org_id = o.organization_id
                JOIN hr_jobs j ON j.job_id = a.job_id 
                WHERE a.is_deleted = :activeStatus
                AND a.period_date BETWEEN :startMonth and :endMonth
                AND a.type IN (:typeList)
                """);
        Map<String, Object> params = new HashMap<>();
        params.put("shortFormat", BaseConstants.SQL_SHORT_DATE_FORMAT);
        params.put("yearFormat", BaseConstants.SQL_YEAR_FORMAT);
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        params.put("typeCateType", Constant.CATEGORY_TYPE.LOAI_DS_TRICH_NOP);
        params.put("startMonth", Utils.stringToDate("01/01/" + dto.getYear()));
        params.put("endMonth", Utils.stringToDate("31/12/" + dto.getYear()));
        params.put("typeList", types);
        params.put("typeKoQuyLuong", Arrays.asList(InsuranceContributionsEntity.TYPES.THAI_SAN, InsuranceContributionsEntity.TYPES.TRUY_THU_BHYT));
        CommonRepository.addFilter(sql, params, dto);
        sql.append("""
                
                ORDER BY a.type, a.period_date, o.path_order, e.employee_code
                """);
        List<Map<String, Object>> resultList = getListData(sql.toString(), params);
        if (Utils.isNullOrEmpty(resultList)) {
            resultList.add(getMapEmptyAliasColumns(sql.toString()));
        }
        return resultList;

    }

    public List<Map<String, Object>> getAllInsuranceSalaryProcess(InsuranceContributionsRequest.ReportForm dto) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                       	e.employee_code ma_nv,
                       	e.full_name ten_nhan_vien,
                       	DATE_FORMAT(isp.start_date, :formatDate) as tu_ngay,
                       	DATE_FORMAT(isp.end_date, :formatDate) as den_ngay,
                       	DATE_FORMAT(isp.end_date, :formatDate) as moc_nang_luong,
                       	CASE 
                       	    WHEN isp.start_date <= :endMonth AND (isp.end_date >= :endMonth OR isp.end_date IS NULL) THEN 'X'
                       	END qua_trinh_hien_tai,
                       	sr.name as dai_luong,
                       	sg.name as bac_luong,
                       	isp.insurance_factor as he_so,
                       	isp.reserve_factor as he_so_clbl,
                       	isp.seniority_percent as tham_nien_vuot_khung 	
                FROM hr_insurance_salary_process isp
                JOIN hr_employees e ON e.employee_id = isp.employee_id
                LEFT JOIN hr_salary_grades sg on isp.salary_grade_id = sg.salary_grade_id
                LEFT JOIN hr_salary_ranks sr on isp.salary_rank_id = sr.salary_rank_id
                WHERE 1 = 1
                ORDER BY e.employee_id, isp.start_date
                """);
        Map<String, Object> params = new HashMap<>();
        params.put("formatDate", BaseConstants.SQL_DATE_FORMAT);
        if (dto.getPeriodType().equals("MONTH")) {
            params.put("endMonth", Utils.getLastDay(dto.getEndDate()));
        } else if (dto.getPeriodType().equals("YEAR")) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(new Date());
            int currentYear = cal.get(Calendar.YEAR);
            Date endMonth;
            if (dto.getEndYear() < currentYear) {
                endMonth = Utils.stringToDate("31/12/" + dto.getEndYear());
            } else {
                endMonth = Utils.getLastDay(new Date());
            }
            params.put("endMonth", endMonth);
        } else {
            params.put("endMonth", Utils.getLastDay(new Date()));
        }

        List<Map<String, Object>> result = getListData(sql.toString(), params);
        return result;
    }

    public List<Map<String, Object>> getCurrentInsuranceSalaryProcess(InsuranceContributionsRequest.ReportForm dto) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                       	e.employee_code ma_nv,
                       	e.full_name ten_nhan_vien,
                       	DATE_FORMAT(isp.start_date, :formatDate) as tu_ngay,
                       	DATE_FORMAT(isp.end_date, :formatDate) as den_ngay,
                       	DATE_FORMAT(isp.end_date, :formatDate) as moc_nang_luong,
                       	'X' qua_trinh_hien_tai,
                       	sr.name as dai_luong,
                       	sg.name as bac_luong,
                       	isp.insurance_factor as he_so,
                       	isp.reserve_factor as he_so_clbl,
                       	isp.seniority_percent as tham_nien_vuot_khung
                FROM hr_insurance_salary_process isp
                JOIN hr_employees e ON e.employee_id = isp.employee_id
                LEFT JOIN hr_salary_grades sg on isp.salary_grade_id = sg.salary_grade_id
                LEFT JOIN hr_salary_ranks sr on isp.salary_rank_id = sr.salary_rank_id
                WHERE 1 = 1 and isp.is_deleted = 'N'
                """);
        Map<String, Object> params = new HashMap<>();
        params.put("formatDate", BaseConstants.SQL_DATE_FORMAT);
        if (dto.getPeriodType().equals("MONTH")) {
            sql.append(" AND isp.start_date <= :endMonth AND (isp.end_date >= :endMonth OR isp.end_date IS NULL) ");
            params.put("endMonth", Utils.getLastDay(dto.getEndDate()));
        } else if (dto.getPeriodType().equals("YEAR")) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(new Date());
            int currentYear = cal.get(Calendar.YEAR);
            Date endMonth;
            if (dto.getEndYear() < currentYear) {
                endMonth = Utils.stringToDate("31/12/" + dto.getEndYear());
            } else {
                endMonth = Utils.getLastDay(new Date());
            }
            sql.append(" AND isp.start_date <= :endMonth AND (isp.end_date >= :endMonth OR isp.end_date IS NULL) ");
            params.put("endMonth", endMonth);
        }
        sql.append("""
                
                ORDER BY e.employee_id
                """);
        List<Map<String, Object>> result = getListData(sql.toString(), params);
//        if (result.isEmpty()) {
//            result.add(getMapEmptyAliasColumns(sql.toString()));
//        }
        return result;
    }

    public List<InsuranceContributionsDto> getListSummaryForPositionGroup(InsuranceContributionsRequest.ReportForm dto, String groupType) {
        StringBuilder sql = new StringBuilder("""
                select
                	'N/A' labour_type ,
                	a.period_date,
                	a.unit_social_amount,
                	a.per_social_amount,
                	a.unit_medical_amount,
                	a.per_medical_amount,
                	a.unit_unemp_amount,
                	a.per_unemp_amount,
                	a.total_amount,
                	a.unit_union_amount
                from icn_insurance_contributions a
                where a.is_deleted = 'N'
                """);
        Map<String, Object> params = new HashMap<>();
        params.put("groupType", groupType);
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        CommonRepository.addFilter(sql, params, dto);
        String sqlSelect = """
                                       select T.labour_type,
                                           T.period_date,
                                           sum(T.unit_social_amount) unit_social_amount,
                                           sum(T.per_social_amount) per_social_amount,
                                           sum(T.unit_medical_amount) unit_medical_amount,
                                           sum(T.per_medical_amount) per_medical_amount,
                                           sum(T.unit_unemp_amount) unit_unemp_amount,
                                           sum(T.per_unemp_amount) per_unemp_amount,
                                           sum(T.total_amount) total_amount,
                                           sum(T.unit_union_amount) unit_union_amount
                                           From (
                                   """ + sql + ") T" +
                           " group by T.labour_type, T.period_date";
        return getListData(sqlSelect, params, InsuranceContributionsDto.class);
    }

    public List<Map<String, Object>> getListTruyThuTruyLinh(InsuranceContributionsRequest.ReportForm dto) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    a.period_date,
                    e.employee_code,
                    e.full_name,
                    a.contract_salary,
                    a.reserve_salary,
                    a.pos_allowance_salary,
                    a.seniority_salary,
                    a.pos_seniority_salary,
                    a.per_social_amount,
                    a.unit_social_amount,
                    a.per_medical_amount,
                    a.unit_medical_amount,
                    a.per_unemp_amount,
                    a.unit_unemp_amount,
                    a.total_amount,
                    after.contract_salary after_contract_salary,
                    after.reserve_salary after_reserve_salary,
                    after.pos_allowance_salary after_pos_allowance_salary,
                    after.seniority_salary after_seniority_salary,
                    after.pos_seniority_salary after_pos_seniority_salary,
                    after.per_social_amount after_per_social_amount,
                    after.unit_social_amount after_unit_social_amount,
                    after.per_medical_amount after_per_medical_amount,
                    after.unit_medical_amount after_unit_medical_amount,
                    after.per_unemp_amount after_per_unemp_amount,
                    after.unit_unemp_amount after_unit_unemp_amount,
                    after.total_amount after_total_amount,
                    pre.contract_salary pre_contract_salary,
                    pre.reserve_salary pre_reserve_salary,
                    pre.pos_allowance_salary pre_pos_allowance_salary,
                    pre.seniority_salary pre_seniority_salary,
                    pre.pos_seniority_salary pre_pos_seniority_salary,
                    pre.per_social_amount pre_per_social_amount,
                    pre.unit_social_amount pre_unit_social_amount,
                    pre.per_medical_amount pre_per_medical_amount,
                    pre.unit_medical_amount pre_unit_medical_amount,
                    pre.per_unemp_amount pre_per_unemp_amount,
                    pre.unit_unemp_amount pre_unit_unemp_amount,
                    pre.total_amount pre_total_amount,
                    (select et.name from hr_emp_types et where et.code = a.emp_type_code) empTypeName,
                    a.labour_type,
                    mj.name job_name,
                    a.reason as ly_do,
                    date_format(a.period_date, '%m/%Y') as ky_trich_nop,
                    date_format(a.retro_period_date, '%m/%Y') as ky_thuc_hien,
                    o.org_name_level_1 don_vi,
                    o.org_name_level_2 phong_ban
                    FROM icn_insurance_retractions a
                    JOIN hr_employees e ON e.employee_id = a.employee_id
                    LEFT JOIN hr_jobs mj ON mj.job_id = a.job_id
                    JOIN hr_organizations o ON o.organization_id = a.org_id    
                    LEFT JOIN icn_insurance_retractions after on a.insurance_retraction_id = after.base_id and after.table_type = 'PHAI_THU'
                    LEFT JOIN icn_insurance_retractions pre on a.insurance_retraction_id = pre.base_id and pre.table_type = 'DA_THU'
                    WHERE a.is_deleted = :activeStatus
                           and a.table_type = 'CHENH_LECH'
                """);

//        and (a.retro_period_date is null or a.retro_period_date = :endDate)
//        and a.period_date < :endDate
        Map<String, Object> params = new HashMap<>();
        params.put("shortFormat", BaseConstants.SQL_SHORT_DATE_FORMAT);
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        params.put("endDate", Utils.getLastDay(dto.getEndDate()));
//        UtilsRepository.addFilterTruyThu(sql, params, dto);
        CommonRepository.addFilter(sql, params, dto);
        sql.append("""      
                ORDER BY a.period_date, o.path_order, e.employee_id
                """);
        List<Map<String, Object>> resultList = getListData(sql.toString(), params);
        return resultList;
    }

}
