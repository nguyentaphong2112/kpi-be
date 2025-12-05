package vn.hbtplus.insurance.repositories.impl;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Repository;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.insurance.constants.Constant;
import vn.hbtplus.insurance.models.OrganizationDto;
import vn.hbtplus.insurance.models.request.InsuranceContributionsRequest;
import vn.hbtplus.insurance.models.response.InsuranceContributionsResponse;
import vn.hbtplus.insurance.repositories.entity.SysCategoryEntity;
import vn.hbtplus.repositories.BaseRepository;
import vn.hbtplus.insurance.repositories.entity.InsuranceContributionsEntity;
import vn.hbtplus.insurance.repositories.entity.InsuranceRetractionsEntity;
import vn.hbtplus.utils.Utils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
@AllArgsConstructor
public class RInsuranceContributionsRepository extends BaseRepository {

    public Map<String, InsuranceContributionsResponse> getTongHopTheoDoiTuong(InsuranceContributionsRequest.ReportForm dto, List<String> listType) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    COUNT(DISTINCT(a.employee_id)) countEmp,
                    a.emp_type_code,
                    SUM(IFNULL(case when a.type in (:typeKoQuyLuong) then 0 else a.contract_salary end, 0) ) contract_salary,
                    SUM(IFNULL(case when a.type in (:typeKoQuyLuong) then 0 else a.reserve_salary end, 0) ) reserve_salary,
                    SUM(IFNULL(case when a.type in (:typeKoQuyLuong) then 0 else a.pos_allowance_salary end, 0) ) pos_allowance_salary,
                    SUM(IFNULL(case when a.type in (:typeKoQuyLuong) then 0 else a.seniority_salary end, 0) ) seniority_salary,
                    SUM(IFNULL(case when a.type in (:typeKoQuyLuong) then 0 else a.pos_seniority_salary end, 0) ) pos_seniority_salary,
                    SUM(IFNULL(case when a.type in (:typeKoQuyLuong) then 0 else a.total_salary end, 0) ) total_salary,
                    
                    SUM(IFNULL(a.per_social_amount, 0) ) per_social_amount,
                    SUM(IFNULL(a.unit_social_amount, 0) ) unit_social_amount,
                    SUM(IFNULL(a.per_medical_amount, 0) ) per_medical_amount,
                    SUM(IFNULL(a.unit_medical_amount, 0) ) unit_medical_amount,
                    SUM(IFNULL(a.per_unemp_amount, 0) ) per_unemp_amount,
                    SUM(IFNULL(a.unit_unemp_amount, 0) ) unit_unemp_amount,
                    SUM(IFNULL(a.total_amount, 0) ) total_amount,
                    SUM(IFNULL(a.retirement_social_amount, 0) ) retirement_social_amount,
                    SUM(IFNULL(a.sickness_social_amount, 0) ) sickness_social_amount,
                    SUM(IFNULL(a.accident_social_amount, 0) ) accident_social_amount
                FROM icn_insurance_contributions a
                WHERE a.is_deleted = :activeStatus
                AND a.type IN (:listType)
                                
                """);

        Map<String, Object> params = new HashMap<>();
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        params.put("typeKoQuyLuong", Arrays.asList(InsuranceContributionsEntity.TYPES.THAI_SAN,InsuranceContributionsEntity.TYPES.TRUY_THU_BHYT));
        params.put("listType", listType);
        CommonRepository.addFilter(sql, params, dto);
        sql.append("""
                                
                GROUP BY a.emp_type_code
                """);
        List<InsuranceContributionsResponse> listData = getListData(sql.toString(), params, InsuranceContributionsResponse.class);
        Map<String, InsuranceContributionsResponse> mapResult = new HashMap<>();
        for (InsuranceContributionsResponse item : listData) {
            mapResult.put(item.getEmpTypeCode(), item);
        }
        return mapResult;
    }

    public List<Map<String, Object>> getDanhSachChiTiet(InsuranceContributionsRequest.ReportForm dto, List<String> listType) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                	e.employee_code ma_nv,
                	e.full_name ten_nv,
                	o.org_name_level_1 don_vi,
                	o.org_name_level_2 phong_ban,
                	o.org_name_level_3 to_nhom,
                	date_format(e.date_of_birth,'%Y') as nam_sinh,
                	(select et.name from hr_emp_types et where et.code = a.emp_type_code) doi_tuong,
                	a.reason ly_do,
                	mj.name chuc_danh,
                	null thang_td,
                	null nam_td,
                	null tong_so_thang_duoc_tinh_tnn,
                	null so_thang_ngung_tham_gia_bh,
                	null thang_tham_gia_bhxh,
                	null nam_tham_gia_bhxh,
                	null thang_nhan_bl_cuoi,
                	null nam_nhan_bl_cuoi,
                	a.seniority_percent tham_nien_vuot_khung,
                	a.insurance_factor as he_so_luong,
                	a.reserve_factor as he_so_clbl,
                	a.allowance_factor as he_so_pccv,
                	a.pos_seniority_percent as tham_nien_nghe,
                	case when a.type in (:typeKoQuyLuong) then 0 else a.contract_salary end luong_co_ban,
                	case when a.type in (:typeKoQuyLuong) then 0 else a.reserve_salary end luong_chenh_lech_bl,
                	case when a.type in (:typeKoQuyLuong) then 0 else a.pos_allowance_salary end luong_pccv,
                	case when a.type in (:typeKoQuyLuong) then 0 else a.seniority_salary end pc_tnvk,
                	case when a.type in (:typeKoQuyLuong) then 0 else a.pos_seniority_salary end pc_tnn,
                	case when a.type in (:typeKoQuyLuong) then 0 else a.total_salary end tong_luong,
                	a.per_social_amount bhxh_ca_nhan_huu_tri,
                	a.per_social_amount bhxh_ca_nhan,
                	a.retirement_social_amount bhxh_huu_tri,
                	a.sickness_social_amount bhxh_dau_om,
                	a.accident_social_amount bhxh_tai_nan,
                	a.unit_social_amount bhxh_don_vi,
                	a.per_medical_amount bhyt_ca_nhan,
                	a.unit_medical_amount bhyt_don_vi,
                	a.per_unemp_amount bhtn_ca_nhan,
                	a.unit_unemp_amount bhtn_don_vi,
                	
                	a.total_amount tong_dong,
                	a.unit_union_amount kpcd,
                	a.base_union_amount cd_so_so,
                	a.superior_union_amount cd_cap_tren,
                	a.mod_union_amount cd_bqp,
                	a.note ghi_chu,
                	date_format(a.period_date,'%m/%Y') ky_trich_nop
                FROM icn_insurance_contributions a
                JOIN hr_employees e ON e.employee_id = a.employee_id
                LEFT JOIN hr_jobs mj ON mj.job_id = a.job_id
                JOIN hr_organizations o ON o.organization_id = a.org_id
                WHERE a.is_deleted = :activeStatus
                AND a.type IN (:listType)
                                
                """);

        Map<String, Object> params = new HashMap<>();
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        params.put("listType", listType);
        params.put("typeKoQuyLuong", Arrays.asList(InsuranceContributionsEntity.TYPES.THAI_SAN,InsuranceContributionsEntity.TYPES.TRUY_THU_BHYT));
        CommonRepository.addFilter(sql, params, dto);
        sql.append("""
                                
                ORDER BY a.period_date, o.path_order, e.employee_code
                """);
        List<Map<String, Object>> resultList = getListData(sql.toString(), params);
        if (Utils.isNullOrEmpty(resultList)) {
            resultList.add(getMapEmptyAliasColumns(sql.toString()));
        }
        return resultList;
    }

    public Map<String, InsuranceContributionsResponse> getTongHopKPCDTheoDoiTuong(InsuranceContributionsRequest.ReportForm dto, List<String> listType) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    COUNT(DISTINCT(a.employee_id)) countEmp,
                    a.emp_type_code,
                    SUM(IFNULL(case when a.type in (:typeKoQuyLuong) then 0 else a.total_salary end, 0) ) contract_salary,
                    SUM(IFNULL(a.unit_union_amount, 0) ) unit_union_amount,
                    SUM(IFNULL(a.base_union_amount, 0) ) base_union_amount,
                    SUM(IFNULL(a.superior_union_amount, 0) ) superior_union_amount,
                    SUM(IFNULL(a.mod_union_amount, 0) ) mod_union_amount
                FROM icn_insurance_contributions a
                WHERE a.is_deleted = :activeStatus
                AND a.type IN (:listType)
                                
                """);

        Map<String, Object> params = new HashMap<>();
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        params.put("listType", listType);
        params.put("typeKoQuyLuong", Arrays.asList(InsuranceContributionsEntity.TYPES.THAI_SAN,InsuranceContributionsEntity.TYPES.TRUY_THU_BHYT));
        CommonRepository.addFilter(sql, params, dto);
        sql.append("""
                                
                GROUP BY a.emp_type_code
                """);
        List<InsuranceContributionsResponse> listData = getListData(sql.toString(), params, InsuranceContributionsResponse.class);
        Map<String, InsuranceContributionsResponse> mapResult = new HashMap<>();
        for (InsuranceContributionsResponse item : listData) {
            mapResult.put(item.getEmpTypeCode(), item);
        }
        return mapResult;
    }

    public List<Map<String, Object>> getListBangTruyThuLinh(InsuranceContributionsRequest.ReportForm dto) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    a.period_date,
                    e.employee_code,
                    e.full_name,
                    a.emp_type_code,
                    o.org_name_level_2 don_vi,
                    a.reason as ly_do,
                    date_format(a.retro_period_date, '%m') thang_truy_thu,
                    date_format(a.retro_period_date, '%Y') nam_truy_thu,
                    pre.seniority_percent pre_tnvk,
                    pre.insurance_factor pre_he_so_luong,
                    pre.reserve_factor pre_he_so_clbl,
                	pre.allowance_factor pre_he_so_pccv,
                	pre.pos_seniority_percent pre_tham_nien_nghe,
                	pre.contract_salary pre_luong_co_ban,
                	pre.reserve_salary pre_luong_chenh_lech_bl,
                	pre.pos_allowance_salary pre_luong_pccv,
                	pre.seniority_salary pre_pc_tnvk,
                	pre.pos_seniority_salary pre_pc_tnn,
                	pre.total_salary pre_tong_luong,
                	pre.per_social_amount pre_bhxh_ca_nhan,
                	pre.retirement_social_amount pre_bhxh_huu_tri,
                	pre.sickness_social_amount pre_bhxh_dau_om,
                	pre.accident_social_amount pre_bhxh_tai_nan,
                	pre.unit_social_amount pre_bhxh_don_vi,
                	pre.per_medical_amount pre_bhyt_ca_nhan,
                	pre.unit_medical_amount pre_bhyt_don_vi,
                	pre.per_unemp_amount pre_bhtn_ca_nhan,
                	pre.unit_unemp_amount pre_bhtn_don_vi,
                	pre.total_amount pre_tong_dong,
                	pre.unit_union_amount pre_kpcd,
                	pre.base_union_amount pre_cd_co_so,
                	pre.superior_union_amount pre_cd_cap_tren,
                	pre.mod_union_amount pre_cd_bqp,
                	
                	after.seniority_percent after_tnvk,
                	after.insurance_factor after_he_so_luong,
                    after.reserve_factor after_he_so_clbl,
                	after.allowance_factor after_he_so_pccv,
                	after.pos_seniority_percent after_tham_nien_nghe,
                	after.contract_salary after_luong_co_ban,
                	after.reserve_salary after_luong_chenh_lech_bl,
                	after.pos_allowance_salary after_luong_pccv,
                	after.seniority_salary after_pc_tnvk,
                	after.pos_seniority_salary after_pc_tnn,
                	after.total_salary after_tong_luong,
                	after.per_social_amount after_bhxh_ca_nhan,
                	after.retirement_social_amount after_bhxh_huu_tri,
                	after.sickness_social_amount after_bhxh_dau_om,
                	after.accident_social_amount after_bhxh_tai_nan,
                	after.unit_social_amount after_bhxh_don_vi,
                	after.per_medical_amount after_bhyt_ca_nhan,
                	after.unit_medical_amount after_bhyt_don_vi,
                	after.per_unemp_amount after_bhtn_ca_nhan,
                	after.unit_unemp_amount after_bhtn_don_vi,
                	after.total_amount after_tong_dong,
                	after.unit_union_amount after_kpcd,
                	after.base_union_amount after_cd_co_so,
                	after.superior_union_amount after_cd_cap_tren,
                	after.mod_union_amount after_cd_bqp,
                	
                	a.seniority_percent tnvk,
                	a.insurance_factor as he_so_luong,
                    a.reserve_factor as he_so_clbl,
                	a.allowance_factor as he_so_pccv,
                	a.pos_seniority_percent as tham_nien_nghe,
                	a.contract_salary luong_co_ban,
                	a.reserve_salary luong_chenh_lech_bl,
                	a.pos_allowance_salary luong_pccv,
                	a.seniority_salary pc_tnvk,
                	a.pos_seniority_salary pc_tnn,
                	a.total_salary tong_luong,
                	a.per_social_amount bhxh_ca_nhan,
                	a.retirement_social_amount bhxh_huu_tri,
                	a.sickness_social_amount bhxh_dau_om,
                	a.accident_social_amount bhxh_tai_nan,
                	a.unit_social_amount bhxh_don_vi,
                	a.per_medical_amount bhyt_ca_nhan,
                	a.unit_medical_amount bhyt_don_vi,
                	a.per_unemp_amount bhtn_ca_nhan,
                	a.unit_unemp_amount bhtn_don_vi,
                	a.total_amount tong_dong,
                	a.unit_union_amount kpcd,
                	a.base_union_amount cd_co_so,
                	a.superior_union_amount cd_cap_tren,
                	a.mod_union_amount cd_bqp
                FROM icn_insurance_retractions a
                JOIN hr_employees e ON e.employee_id = a.employee_id
                JOIN hr_organizations o ON o.organization_id = a.org_id 
                LEFT JOIN icn_insurance_retractions after ON a.insurance_retraction_id = after.base_id AND after.table_type = 'PHAI_THU'
                LEFT JOIN icn_insurance_retractions pre ON a.insurance_retraction_id = pre.base_id AND pre.table_type = 'DA_THU'
                WHERE a.is_deleted = :activeStatus
                AND a.table_type = :tableType
                """);
        Map<String, Object> params = new HashMap<>();
        params.put("tableType", InsuranceRetractionsEntity.TABLE_TYPES.CHENH_LECH);
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        if (!Utils.isNullOrEmpty(dto.getListLocationJoin())) {
            sql.append(" AND a.insurance_agency IN (:insuranceAgency) ");
            params.put("insuranceAgency", dto.getListLocationJoin());
        }

        if (dto.getPeriodType().equals("MONTH")) {
            sql.append(" AND a.retro_period_date BETWEEN :startMonth and :endMonth ");
            params.put("startMonth", Utils.getFirstDay(dto.getStartDate()));
            params.put("endMonth", Utils.getLastDay(dto.getEndDate()));
        } else if (dto.getPeriodType().equals("YEAR")) {
            sql.append(" AND a.retro_period_date BETWEEN :startMonth and :endMonth ");
            params.put("startMonth", Utils.stringToDate("01/01/" + dto.getStartYear()));
            params.put("endMonth", Utils.stringToDate("31/12/" + dto.getEndYear()));
        } else {
            sql.append(" AND a.retro_period_date BETWEEN :startMonth and :endMonth ");
            params.put("startMonth", Utils.stringToDate(("01/" + (dto.getQuarter() * 3 - 2) + "/" + dto.getYear())));
            params.put("endMonth", Utils.getLastDay(Utils.stringToDate(("01/" + (dto.getQuarter() * 3) + "/" + dto.getYear()))));
        }
        sql.append(" ORDER BY o.path_order, e.employee_code");
        List<Map<String, Object>> results = getListData(sql.toString(), params);
        if (results.isEmpty()) {
            results.add(getMapEmptyAliasColumns(sql.toString()));
        }
        return results;
    }

    public OrganizationDto getOrg(){
        StringBuilder sql = new StringBuilder("""
                select
                a.organization_id orgId,
                a.name orgName
                from hr_organizations a where a.parent_id is null
                """);
        Map map = new HashMap<>();
        return queryForObject(sql.toString(), map, OrganizationDto.class);
    }

    public List<SysCategoryEntity> getListEmpType() {
        String sql = """
                    SELECT a.*, concat(a.code, '') value
                    FROM hr_emp_types a
                    WHERE a.is_deleted = 'N'
                    ORDER BY a.order_number
                """;
        return getListData(sql, new HashMap<>(), SysCategoryEntity.class);
    }

}
