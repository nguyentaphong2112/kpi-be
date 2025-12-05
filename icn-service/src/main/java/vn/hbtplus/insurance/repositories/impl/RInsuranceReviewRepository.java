package vn.hbtplus.insurance.repositories.impl;

import lombok.AllArgsConstructor;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.stereotype.Repository;
import vn.hbtplus.insurance.constants.Constant;
import vn.hbtplus.insurance.models.request.InsuranceContributionsRequest;
import vn.hbtplus.repositories.BaseRepository;
import vn.hbtplus.insurance.repositories.jpa.ContributionRateRepositoryJPA;
import vn.hbtplus.utils.Utils;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Báo cáo rà soát
 */

@Repository
@AllArgsConstructor
public class RInsuranceReviewRepository extends BaseRepository {
    private final ConfigParameterRepository configParameterRepository;
    private final ContributionRateRepositoryJPA contributionRateRepositoryJPA;
    private final String sqlSelect = """
            select e.employee_code ma_nv,
                		e.full_name ho_va_ten,
                		icn.emp_type_code as doi_tuong,
                		(select name from hr_jobs where job_id = icn.job_id) as chuc_danh,
                		org.org_name_level_2 as don_vi,
                		org.org_name_level_3 as phong_ban,
                		icn.contract_salary luong_cap_bac,
                		icn.reserve_salary luong_clbl,
                		icn.pos_allowance_salary pccv,
                		icn.seniority_salary pc_tnvk,
                		icn.pos_seniority_salary pc_tnnghe,
                		icn.total_salary tong_luong,
                		icn.per_social_amount as bhxh_ca_nhan,
                		icn.unit_social_amount as bhxh_don_vi,
                		icn.per_medical_amount as bhyt_ca_nhan,
                		icn.unit_medical_amount as bhyt_don_vi,
                		icn.per_unemp_amount as bhtn_ca_nhan,
                		icn.unit_unemp_amount as bhtn_don_vi,
                		icn.total_amount as tong_dong
            """;

    public List<Map<String, Object>> getListIncrease(Date periodDate) {
        String sql = sqlSelect + """                
                 from icn_insurance_contributions icn, hr_employees e , hr_organizations org
                where type = 'THU_BHXH'
                and icn.period_date = :periodDate
                and icn.is_deleted = 'N'
                and icn.org_id = org.organization_id
                and icn.employee_id = e.employee_id
                and not exists (
                	select 1 from icn_insurance_contributions icn1
                	where icn1.employee_id = icn.employee_id
                	and icn1.type in ('THU_BHXH', 'THAI_SAN')
                	and icn1.period_date = :prePeriodDate
                	and icn1.is_deleted = 'N'
                )
                """;
        Map<String, Object> params = new HashMap<>();
        params.put("periodDate", periodDate);
        params.put("prePeriodDate", Utils.getLastDay(DateUtils.addMonths(periodDate, -1)));
        List<Map<String, Object>> result = getListData(sql, params);
        if (result.isEmpty()) {
            result.add(getMapEmptyAliasColumns(sql));
        }
        return result;
    }

    public List<Map<String, Object>> getListDecrease(Date periodDate) {
        String sql = sqlSelect + """                
                 from icn_insurance_contributions icn, hr_employees e , hr_organizations org
                where type = 'THU_BHXH'
                and icn.period_date = :prePeriodDate
                and icn.is_deleted = 'N'
                and icn.org_id = org.organization_id
                and icn.employee_id = e.employee_id
                and not exists (
                	select 1 from icn_insurance_contributions icn1
                	where icn1.employee_id = icn.employee_id
                	and icn1.type in ('THU_BHXH', 'THAI_SAN')
                	and icn1.period_date = :periodDate
                	and icn1.is_deleted = 'N'
                )
                """;
        Map<String, Object> params = new HashMap<>();
        params.put("periodDate", periodDate);
        params.put("prePeriodDate", Utils.getLastDay(DateUtils.addMonths(periodDate, -1)));
        List<Map<String, Object>> result = getListData(sql, params);
        if (result.isEmpty()) {
            result.add(getMapEmptyAliasColumns(sql));
        }
        return result;
    }

    public List<Map<String, Object>> getListEndMaternity(Date periodDate) {
        String sql = sqlSelect + """
                 from icn_insurance_contributions icn, hr_employees e , hr_organizations org
                where type in ('THU_BHXH', 'KO_THU')
                and icn.period_date = :periodDate
                and icn.is_deleted = 'N'
                and icn.org_id = org.organization_id
                and exists (
                	select 1 from icn_insurance_contributions icn1
                	where icn1.employee_id = icn.employee_id
                	and icn1.type in ( 'THAI_SAN')
                	and icn1.period_date = :prePeriodDate
                	and icn1.is_deleted = 'N'
                )
                AND icn.employee_id = e.employee_id
                """;
        Map<String, Object> params = new HashMap<>();
        params.put("periodDate", periodDate);
        params.put("prePeriodDate", Utils.getLastDay(DateUtils.addMonths(periodDate, -1)));
        List<Map<String, Object>> result = getListData(sql, params);
        if (result.isEmpty()) {
            result.add(getMapEmptyAliasColumns(sql));
        }

        return result;
    }

    public List<Map<String, Object>> getListNewMaternity(Date periodDate) {
        String sql = sqlSelect + """
                 from icn_insurance_contributions icn, hr_employees e , hr_organizations org
                where type in ('THAI_SAN')
                and icn.period_date = :periodDate
                and icn.is_deleted = 'N'
                and icn.org_id = org.organization_id
                and not exists (
                	select 1 from icn_insurance_contributions icn1
                	where icn1.employee_id = icn.employee_id
                	and icn1.type in ('THAI_SAN')
                	and icn1.period_date = :prePeriodDate
                	and icn1.is_deleted = 'N'
                )
                AND icn.employee_id = e.employee_id
                """;
        Map<String, Object> params = new HashMap<>();
        params.put("periodDate", periodDate);
        params.put("prePeriodDate", Utils.getLastDay(DateUtils.addMonths(periodDate, -1)));
        List<Map<String, Object>> result = getListData(sql, params);
        if (result.isEmpty()) {
            result.add(getMapEmptyAliasColumns(sql));
        }
        return result;
    }

    public List<Map<String, Object>> getListChangeSalary(Date periodDate) {
        String sql = sqlSelect + """
                 from icn_insurance_contributions icn, hr_employees e , hr_organizations org
                where type in ('THU_BHXH')
                and icn.period_date = :periodDate
                and icn.is_deleted = 'N'
                and icn.org_id = org.organization_id
                and exists (
                	select 1 from icn_insurance_contributions icn1
                	where icn1.employee_id = icn.employee_id
                	and icn1.type in ('THU_BHXH')
                	and icn1.period_date = :prePeriodDate
                	and icn1.is_deleted = 'N'
                	and icn1.total_salary <> icn.total_salary
                )
                AND icn.employee_id = e.employee_id
                """;
        Map<String, Object> params = new HashMap<>();
        params.put("periodDate", periodDate);
        params.put("prePeriodDate", Utils.getLastDay(DateUtils.addMonths(periodDate, -1)));
        List<Map<String, Object>> result = getListData(sql, params);
        if (result.isEmpty()) {
            result.add(getMapEmptyAliasColumns(sql));
        }
        return result;
    }

    public List<Map<String, Object>> getListLackEmpTypeProcess(Date periodDate) {
        String sql = """
                 select
                    e.employee_code as ma_nv,
                    e.full_name as ho_va_ten,
                    T.org_name_level_2 as phong_ban,
                    null doi_tuong,
                    null ngay_ky_hdld,
                    T.chuc_danh,
                    T.from_date as tu_ngay,
                    T.to_date as den_ngay
                   from hr_employees e , (
                    select wp.employee_id,
                    org.org_name_level_2,
                    (select name from hr_jobs where job_id = wp.job_id) as chuc_danh,
                    min(cd.calendar_date) as from_date,
                    max(cd.calendar_date) as to_date
                    from hr_work_process wp, hr_organizations org, sys_calendars cd
                    where IFNULL(wp.end_date,:endDate) >= :startDate
                    and wp.start_date <= :endDate
                    and cd.calendar_date BETWEEN :startDate and :endDate
                    and cd.calendar_date BETWEEN wp.start_date and IFNULL(wp.end_date,:endDate)
                    and not exists (
                        select 1 from hr_contract_process etp
                        where wp.employee_id = etp.employee_id
                        and cd.calendar_date BETWEEN etp.start_date and IFNULL(etp.end_date,:endDate)
                        and etp.is_deleted = 'N'
                    )
                    and wp.organization_id = org.organization_id
                    and wp.document_type_id in (
                        select document_type_id from hr_document_types dt
                        where dt.type <> 'OUT'
                    )
                    and wp.is_deleted = 'N'
                    and org.path_id like :orgVCC
                    group by wp.employee_id
                   ) T
                   WHERE T.employee_id = e.employee_id
                """;
        Map<String, Object> params = new HashMap<>();
        params.put("startDate", Utils.getFirstDay(periodDate));
        params.put("endDate", periodDate);
        params.put("orgVCC", configParameterRepository.getConfigValue(Constant.CONFIG_PARAMETERS.ROOT_LEGACY_ID, periodDate, Long.class));

        List<Map<String, Object>> result = getListData(sql, params);
        if (result.isEmpty()) {
            result.add(getMapEmptyAliasColumns(sql));
        }
        return result;
    }

    public List<Map<String, Object>> getListLackInsuranceSalaryProcess(Date periodDate) {
        String sql = """
                 select 
                    e.employee_code as ma_nv,
                    e.full_name as ho_va_ten,
                    T.doi_tuong,
                    T.chuc_danh,
                    T.org_name_level_2 as phong_ban,
                    T.from_date as tu_ngay,
                    T.to_date as den_ngay,
                    T.start_contract_date as ngay_ky_hdld
                  from hr_employees e , (
                    select wp.employee_id,
                    org.org_name_level_2,
                    ets.code as doi_tuong,
                    (select name from hr_jobs where job_id = wp.job_id) as chuc_danh,
                    f_get_first_contract_date(wp.employee_id, :endDate) start_contract_date,
                    min(cd.calendar_date) as from_date,
                    max(cd.calendar_date) as to_date
                    from hr_work_process wp, hr_contract_process etp, hr_organizations org, sys_calendars cd, hr_emp_types ets
                    where IFNULL(wp.end_date,:endDate) >= :startDate
                    and wp.start_date <= :endDate
                    and cd.calendar_date BETWEEN :startDate and :endDate
                    and cd.calendar_date BETWEEN wp.start_date and IFNULL(wp.end_date,:endDate)
                    and cd.calendar_date BETWEEN etp.start_date and IFNULL(etp.end_date,:endDate)
                    and etp.start_date <= IFNULL(wp.end_date,:endDate)
                    and wp.start_date <= IFNULL(etp.end_date,:endDate)
                    and wp.employee_id = etp.employee_id
                    and not exists (
                        select 1 from hr_insurance_salary_process isp
                        where wp.employee_id = isp.employee_id
                        and cd.calendar_date BETWEEN isp.start_date and IFNULL(isp.end_date,:endDate)
                        and isp.is_deleted = 'N'
                    )
                    and ets.emp_type_id = etp.emp_type_id
                    and ets.code in (:empTypeCodes)
                    and wp.organization_id = org.organization_id
                    and wp.document_type_id in (
                        select document_type_id from hr_document_types dt
                        where dt.type <> 'OUT'
                    )
                    and wp.is_deleted = 'N'
                    and etp.is_deleted = 'N'
                    and org.path_id like :orgVCC
                    group by wp.employee_id
                  ) T
                  WHERE T.employee_id = e.employee_id
                  and T.start_contract_date <= :middleMonth 
                """;
        Map<String, Object> params = new HashMap<>();
        params.put("startDate", Utils.getFirstDay(periodDate));
        params.put("endDate", periodDate);
        params.put("middleMonth", Utils.stringToDate("15/"
                + Utils.formatDate(periodDate, "MM/yyyy"))
        );
        params.put("empTypeCodes", contributionRateRepositoryJPA.getListEmTypeCodes(periodDate));
        params.put("orgVCC", "%/" + configParameterRepository.getConfigValue(Constant.CONFIG_PARAMETERS.ROOT_LEGACY_ID, periodDate, Long.class) + "/%");

        List<Map<String, Object>> result = getListData(sql, params);
        if (result.isEmpty()) {
            result.add(getMapEmptyAliasColumns(sql));
        }
        return result;
    }

    public List<Map<String, Object>> getListDuplicateWorkProcess(Date periodDate) {
        String sql = """
                 select 
                    e.employee_code as ma_nv, e.full_name as ho_va_ten,
                    null as doi_tuong,
                    (select name from hr_jobs where job_id = wp.job_id) as chuc_danh,
                    org.org_name_level_2 as phong_ban,
                    null ngay_ky_hdld,
                    wp.start_date as tu_ngay,
                    wp.end_date as den_ngay
                 from hr_work_process wp, hr_employees e, hr_organizations org,
                 hr_document_types dt 
                 where wp.start_date <= :endDate
                 and (wp.end_date is null or wp.end_date >= :startDate)
                 and wp.document_type_id = dt.document_type_id
                 and wp.employee_id = e.employee_id
                 and wp.organization_id = org.organization_id
                 and dt.type <> 'OUT'
                 and wp.is_deleted = 'N'
                 and org.path_id like :orgVCC
                 and exists (
                    select 1 from hr_work_process wp1
                    where wp1.employee_id = wp.employee_id
                    and wp1.start_date <= ifnull(wp.end_date,:endDate)
                    and wp.start_date <= ifnull(wp1.end_date,:endDate)
                    and  wp1.start_date <= :endDate
                    and wp1.is_deleted = 'N'
                    and (wp1.end_date is null or wp1.end_date >= :startDate)
                    and wp.work_process_id <> wp1.work_process_id
                 )
                """;
        Map<String, Object> params = new HashMap<>();
        params.put("startDate", Utils.getFirstDay(periodDate));
        params.put("endDate", periodDate);
        params.put("orgVCC", "%/" + configParameterRepository.getConfigValue(Constant.CONFIG_PARAMETERS.ROOT_LEGACY_ID, periodDate, Long.class) + "/%");

        List<Map<String, Object>> result = getListData(sql, params);
        if (result.isEmpty()) {
            result.add(getMapEmptyAliasColumns(sql));
        }
        return result;
    }

    public List<Map<String, Object>> getListDuplicateEmpTypeProcess(Date periodDate) {
        String sql = """
                 select e.employee_code as ma_nv, e.full_name as ho_va_ten,
                 	ets.code as doi_tuong,
                 	(select name from hr_jobs where job_id = e.job_id) as chuc_danh,
                 	org.org_name_level_2 as phong_ban,
                 	null ngay_ky_hdld,
                 	wp.start_date as tu_ngay,
                 	wp.end_date as den_ngay
                 from hr_contract_process wp, hr_employees e, hr_organizations org, hr_emp_types ets
                 where wp.start_date <= :endDate
                 and (wp.end_date is null or wp.end_date >= :startDate)
                 and wp.employee_id = e.employee_id
                 and e.organization_id = org.organization_id
                 and ets.emp_type_id = wp.emp_type_id
                 and wp.is_deleted = 'N'
                 and exists (
                 	select 1 from hr_contract_process wp1
                 	where wp1.employee_id = wp.employee_id
                 	and wp1.is_deleted = 'N'
                 	and wp1.start_date <= ifnull(wp.end_date, :endDate)
                 	and wp.start_date <= ifnull(wp1.end_date, :endDate)
                 	and  wp1.start_date <= :endDate
                 	and (wp1.end_date is null or wp1.end_date >= :startDate)
                 	and wp.contract_process_id != wp1.contract_process_id
                 )
                 and exists (
                 	select 1 from hr_work_process wp1 , hr_organizations org1
                 	where wp1.employee_id = wp.employee_id
                 	and wp1.is_deleted = 'N'
                 	and wp1.start_date <= ifnull(wp.end_date, :endDate)
                 	and wp.start_date <= ifnull(wp1.end_date, :endDate)
                 	and wp1.organization_id = org1.organization_id
                 	and org1.path_id like :orgVCC
                 )
                """;
        Map<String, Object> params = new HashMap<>();
        params.put("startDate", Utils.getFirstDay(periodDate));
        params.put("endDate", periodDate);
        params.put("orgVCC", "%/" + configParameterRepository.getConfigValue(Constant.CONFIG_PARAMETERS.ROOT_LEGACY_ID, periodDate, Long.class) + "/%");

        List<Map<String, Object>> result = getListData(sql, params);
        if (result.isEmpty()) {
            result.add(getMapEmptyAliasColumns(sql));
        }
        return result;
    }

    public List<Map<String, Object>> getListDuplicateInsuranceSalaryProcess(Date periodDate) {
        String sql = """
                 select e.employee_code as ma_nv, e.full_name as ho_va_ten,
                 	null as doi_tuong,
                 	(select name from hr_jobs where job_id = e.job_id) as chuc_danh,
                 	org.org_name_level_2 as phong_ban,
                 	null ngay_ky_hdld,
                 	wp.start_date as tu_ngay,
                 	wp.end_date as den_ngay
                 from hr_insurance_salary_process wp, hr_employees e, hr_organizations org
                 where wp.start_date <= :endDate
                 and (wp.end_date is null or wp.end_date >= :startDate)
                 and wp.employee_id = e.employee_id
                 and e.organization_id = org.organization_id
                 and wp.is_deleted = 'N'
                 and exists (
                 	select 1 from hr_insurance_salary_process wp1
                 	where wp1.employee_id = wp.employee_id
                 	and wp1.is_deleted = 'N'
                 	and wp1.start_date <= ifnull(wp.end_date, :endDate)
                 	and wp.start_date <= ifnull(wp1.end_date, :endDate)
                 	and  wp1.start_date <= :endDate
                 	and (wp1.end_date is null or wp1.end_date >= :startDate)
                 	and wp.insurance_salary_process_id != wp1.insurance_salary_process_id
                 )
                 and exists (
                 	select 1 from hr_work_process wp1 , hr_organizations org1
                 	where wp1.employee_id = wp.employee_id
                 	and wp1.is_deleted = 'N'
                 	and wp1.start_date <= ifnull(wp.end_date, :endDate)
                 	and wp.start_date <= ifnull(wp1.end_date, :endDate)
                 	and wp1.organization_id = org1.organization_id
                 	and org1.path_id like :orgVCC
                 )
                """;
        Map<String, Object> params = new HashMap<>();
        params.put("startDate", Utils.getFirstDay(periodDate));
        params.put("endDate", periodDate);
        params.put("orgVCC", "%/" + configParameterRepository.getConfigValue(Constant.CONFIG_PARAMETERS.ROOT_LEGACY_ID, periodDate, Long.class) + "/%");

        List<Map<String, Object>> result = getListData(sql, params);
        if (result.isEmpty()) {
            result.add(getMapEmptyAliasColumns(sql));
        }
        return result;
    }

    public List<Map<String, Object>> getPositionNotConfig(InsuranceContributionsRequest.ReportForm dto) {
        String sqlBuilder = """
                SELECT
                	jb.name chuc_danh,
                	org.org_name_level1 don_vi_cap_1,
                	org.org_name_level_2 don_vi_cap_2,
                	org.org_name_level3 don_vi_cap_3,
                	org.org_name_level4 don_vi_cap_4,
                	org.org_name_level5 don_vi_cap_5,
                	B.phan_loai_lao_dong,
                	B.phan_loai_tru
                 FROM (
                	SELECT
                		T.org_id,
                		T.job_id,
                		'N/A' phan_loai_lao_dong,
                		'N/A' phan_loai_tru
                	FROM (
                		select
                			wp.organization_id,
                			wp.job_id
                		from hr_work_process wp , hr_organizations org, hr_contract_process etp, hr_emp_types ets
                		where (wp.end_date is null or wp.end_date >= :startDate)
                		and wp.start_date <= :endDate
                		and wp.document_type_id in (
                			select document_type_id from hr_document_types dt where dt.type <> 'OUT'
                		)
                		and wp.employee_id = etp.employee_id
                		and wp.start_date <= ifnull(etp.end_date, :endDate)
                		and etp.start_date <= ifnull(wp.end_date, :endDate)
                		and org.organization_id = wp.organization_id
                		and ets.emp_type_id = etp.emp_type_id
                        and ets.code in (:empTypeCodes)
                		and org.path_id like :orgVCC
                		group by wp.organization_id, wp.job_id
                	) T
                ) B , hr_jobs jb, hr_organizations org
                WHERE B.org_id = org.organization_id
                and B.job_id = jb.job_id
                and (B.phan_loai_lao_dong IS NULL OR B.phan_loai_tru IS NULL )
                order by org.path_order, jb.name
                """;
        Map<String, Object> params = new HashMap<>();
        params.put("startDate", dto.getStartDate());
        params.put("endDate", dto.getEndDate());
        List<String> empTypeCodes = contributionRateRepositoryJPA.getListEmTypeCodes(Utils.getLastDay(dto.getEndDate()));
        params.put("orgVCC", "%/" + configParameterRepository.getConfigValue(Constant.CONFIG_PARAMETERS.ROOT_LEGACY_ID, dto.getEndDate(), Long.class) + "/%");
        params.put("empTypeCodes", empTypeCodes);
        return getListData(sqlBuilder, params);
    }

    public List<Map<String, Object>> getEmployeeNotConfig(InsuranceContributionsRequest.ReportForm dto) {
        String sqlBuilder = """
                SELECT
                    e.employee_code as ma_nhan_vien,
                    e.full_name as ten_nhan_vien,
                	jb.name chuc_danh,
                	org.org_name_level1 don_vi_cap_1,
                	org.org_name_level_2 don_vi_cap_2,
                	org.org_name_level3 don_vi_cap_3,
                	org.org_name_level4 don_vi_cap_4,
                	org.org_name_level5 don_vi_cap_5,
                	B.phan_loai_lao_dong,
                	B.phan_loai_tru
                 FROM (
                	SELECT
                	    T.employee_id,
                		T.org_id,
                		T.job_id,
                		'N/A' phan_loai_lao_dong,
                		'N/A' phan_loai_tru
                	FROM (
                		select
                			wp.organization_id,
                			wp.job_id,
                			wp.employee_id
                		from hr_work_process wp , hr_organizations org, hr_contract_process etp, hr_emp_types ets
                		where (wp.end_date is null or wp.end_date >= :startDate)
                		and wp.start_date <= :endDate
                		and wp.document_type_id in (
                			select document_type_id from hr_document_types dt where dt.type <> 'OUT'
                		)
                		and wp.employee_id = etp.employee_id
                		and wp.start_date <= ifnull(etp.end_date, :endDate)
                		and etp.start_date <= ifnull(wp.end_date, :endDate)
                		and ets.emp_type_id = etp.emp_type_id
                        and ets.code in (:empTypeCodes)
                		and org.organization_id = wp.organization_id
                		and org.path_id like :orgVCC
                		group by wp.organization_id, wp.job_id,wp.employee_id
                	) T
                ) B , hr_jobs jb, hr_organizations org, hr_employees e 
                WHERE B.org_id = org.organization_id
                and B.job_id = jb.job_id
                and (B.phan_loai_lao_dong IS NULL OR B.phan_loai_tru IS NULL )
                and B.employee_id = e.employee_id
                order by org.path_order, jb.name, e.employee_id
                """;
        Map<String, Object> params = new HashMap<>();
        params.put("startDate", Utils.getFirstDay(dto.getStartDate()));
        List<String> empTypeCodes = contributionRateRepositoryJPA.getListEmTypeCodes(Utils.getLastDay(dto.getEndDate()));
        params.put("orgVCC", "%/" + configParameterRepository.getConfigValue(Constant.CONFIG_PARAMETERS.ROOT_LEGACY_ID, dto.getEndDate(), Long.class) + "/%");
        params.put("empTypeCodes", empTypeCodes);
        params.put("endDate", Utils.getLastDay(dto.getEndDate()));
        return getListData(sqlBuilder, params);
    }
}
