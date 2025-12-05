/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.tax.income.repositories.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import vn.hbtplus.annotations.Parameter;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.exceptions.BaseAppException;
import vn.hbtplus.models.BaseDataTableDto;
import vn.hbtplus.tax.income.constants.Constant;
import vn.hbtplus.repositories.BaseRepository;
import vn.hbtplus.tax.income.models.request.TaxSettlementMastersRequest;
import vn.hbtplus.tax.income.models.response.TaxSettlementDetailsResponse;
import vn.hbtplus.tax.income.models.response.TaxSettlementMastersResponse;
import vn.hbtplus.tax.income.repositories.entity.TaxDeclareMastersEntity;
import vn.hbtplus.tax.income.repositories.entity.TaxSettlementDetailsEntity;
import vn.hbtplus.tax.income.repositories.entity.TaxSettlementMastersEntity;
import vn.hbtplus.utils.QueryUtils;
import vn.hbtplus.utils.Utils;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Lop repository Impl ung voi bang pit_tax_settlement_masters
 *
 * @author tudd
 * @version 1.0
 * @since 1.0
 */

@Repository
@RequiredArgsConstructor
public class TaxSettlementMastersRepository extends BaseRepository {

    public BaseDataTableDto searchData(TaxSettlementMastersRequest.SearchForm dto) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    a.tax_settlement_master_id,
                    a.year,
                    a.status,
                    (select s.name from sys_categories s where s.value = a.status and s.category_type = :statusType) statusName,
                    (select s.name from sys_categories s where s.value = a.input_type and s.category_type = :inputType) input_type,
                    a.total_taxpayers,
                    a.total_income_taxable,
                    a.total_insurance_deduction,
                    a.total_tax_collected,
                    a.is_deleted,
                    a.created_by,
                    a.created_time,
                    a.modified_by,
                    a.modified_time,
                    a.last_update_time
                """);
        HashMap<String, Object> params = new HashMap<>();
        addCondition(sql, params, dto);
        params.put("statusType", Constant.CATEGORY_TYPE.TRANG_THAI_KKT);
        params.put("inputType", Constant.CATEGORY_TYPE.ACTION_TYPE);
        return getListPagination(sql.toString(), params, dto, TaxSettlementMastersResponse.class);
    }

    public List<Map<String, Object>> getListExport(TaxSettlementMastersRequest.SearchForm dto) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    a.tax_settlement_master_id,
                    a.year,
                    (select s.name from sys_categories s where s.value = a.status and s.category_type = :statusType) statusName,
                    (select s.name from sys_categories s where s.value = a.input_type and s.category_type = :inputType) input_type,
                    a.total_taxpayers,
                    a.total_income_taxable,
                    a.total_insurance_deduction,
                    a.total_tax_collected,
                    a.is_deleted,
                    a.created_by,
                    a.created_time,
                    a.modified_by,
                    a.modified_time,
                    a.last_update_time
                """);
        Map<String, Object> params = new HashMap<>();
        params.put("statusType", Constant.CATEGORY_TYPE.TRANG_THAI_KKT);
        params.put("inputType", Constant.CATEGORY_TYPE.ACTION_TYPE);
        addCondition(sql, params, dto);
        List<Map<String, Object>>  resultList = getListData(sql.toString(), params);
        if (Utils.isNullOrEmpty(resultList)) {
            resultList.add(getMapEmptyAliasColumns(sql.toString()));
        }
        return resultList;
    }

    private void addCondition(StringBuilder sql, Map<String, Object> params, TaxSettlementMastersRequest.SearchForm dto) {
        sql.append("""
                    FROM pit_tax_settlement_masters a
                    WHERE IFNULL(a.is_deleted, :activeStatus) = :activeStatus
                """);
        QueryUtils.filter(dto.getYear(), sql, params, "a.year");
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        sql.append(" ORDER BY a.year desc, a.created_time desc");
    }

    public void updateDataMasterById(Long taxSettlementMasterId, Integer year) {
        String sql = """
                update pit_tax_settlement_masters a,
                	(
                		select count(*) as total_taxpayers,
                		sum(td.total_income_taxable) total_income_taxable,
                		sum(td.total_insurance_deduction) total_insurance_deduction,
                		sum(td.total_tax_collected) total_tax_collected
                		from  pit_tax_settlement_details td
                		where td.year =  :year
                		and td.tax_settlement_master_id  = :id
                		and td.is_deleted = 'N'
                	) T
                set a.total_taxpayers = T.total_taxpayers,
                	a.total_income_taxable = T.total_income_taxable,
                	a.total_insurance_deduction = T.total_insurance_deduction,
                	a.total_tax_collected = T.total_tax_collected
                where a.tax_settlement_master_id = :id
                """;
        Map<String, Object> mapParams = new HashMap<>();
        mapParams.put("id", taxSettlementMasterId);
        mapParams.put("year", year);
        executeSqlDatabase(sql, mapParams);
    }

    public List<Map<String, Object>> getListDetailsById(TaxSettlementMastersEntity mastersEntity) {
        String sql = """
                select
                	a.tax_settlement_detail_id,
                	null as hinh_thuc_quyet_toan,
                	a.report_form as bieu_mau,
                	null chiu_thue_thang_1, null chiu_thue_thang_2,null chiu_thue_thang_3,null chiu_thue_thang_4,null chiu_thue_thang_5,null chiu_thue_thang_6,
                	null chiu_thue_thang_7, null chiu_thue_thang_8,null chiu_thue_thang_9,null chiu_thue_thang_10,null chiu_thue_thang_11,null chiu_thue_thang_12,
                	null thue_tncn_thang_1, null thue_tncn_thang_2,null thue_tncn_thang_3,null thue_tncn_thang_4,null thue_tncn_thang_5,null thue_tncn_thang_6,
                    null thue_tncn_thang_7, null thue_tncn_thang_8,null thue_tncn_thang_9,null thue_tncn_thang_10,null thue_tncn_thang_11,null thue_tncn_thang_12,
                    null bao_hiem_thang_1, null bao_hiem_thang_2,null bao_hiem_thang_3,null bao_hiem_thang_4,null bao_hiem_thang_5,null bao_hiem_thang_6,
                    null bao_hiem_thang_7, null bao_hiem_thang_8,null bao_hiem_thang_9,null bao_hiem_thang_10,null bao_hiem_thang_11,null bao_hiem_thang_12,
                	(select name from sys_categories sc where sc.category_type = 'THUE_DIEN_DOI_TUONG' and sc.value = a.emp_type_code) as doi_tuong,
                	(select name from sys_categories sc where sc.category_type = 'THUE_TRANG_THAI_NV' AND sc.value = a.working_status) as trang_thai,
                	a.emp_code as ma_nhan_vien,
                	a.full_name as ten_nhan_vien,
                	a.tax_no as ma_so_thue,
                	a.personal_id_no as so_cccd,
                	a.created_by as nguoi_tao,
                	a.created_time as ngay_tao,
                	a.num_of_dependents as so_nguoi_phu_thuoc,
                	IFNULL(a.self_deduction,0) as giam_tru_ban_than,
                	IFNULL(a.dependent_deduction,0) as giam_tru_npt,
                	a.total_income_taxable as tong_chiu_thue_qt,
                	a.total_insurance_deduction as tong_bao_hiem_qt,
                	a.total_tax_collected as tong_thue_qt,
                	a.total_tax_payed as tong_thue_duoc_hoan,
                	a.note as ghi_chu,
                	a.total_income_tax as tong_thue,
                	(select org_name_level_2 from hr_organizations org where org.organization_id = a.declare_org_id) as don_vi
                from pit_tax_settlement_details a
                where a.year = :year
                and a.tax_settlement_master_id = :masterId
                and a.is_deleted = 'N'
                """;
        Map<String, Object> params = new HashMap<>();
        params.put("year", mastersEntity.getYear());
        params.put("masterId", mastersEntity.getTaxSettlementMasterId());
        List<Map<String, Object>>  resultList = getListData(sql, params);
        if (Utils.isNullOrEmpty(resultList)) {
            resultList.add(getMapEmptyAliasColumns(sql));
        }
        return resultList;
    }

    public List<TaxSettlementDetailsEntity> getTaxDeclareOfMonth(Date taxPeriodDate, String inputType) {
        String sql = """
                select a.emp_code, a.full_name, a.tax_no, a.personal_id_no, a.tax_method,
                a.income_taxable total_income_taxable, a.dependent_deduction,
                a.insurance_deduction total_insurance_deduction, a.self_deduction,
                a.income_tax as total_tax_collected,
                MONTH(:taxPeriodDate) as month , tdm.tax_declare_master_id ,
                a.declare_org_id, a.work_org_id
                from pit_tax_declare_details a, pit_tax_declare_masters tdm
                where a.tax_period_date = :taxPeriodDate
                  and tdm.input_type = :inputType
                  and a.tax_declare_master_id = tdm.tax_declare_master_id
                  and a.is_deleted = 'N'
                  """;
        Map<String, Object> params = new HashMap<>();
        params.put("taxPeriodDate", taxPeriodDate);
        params.put("inputType", inputType);
        return getListData(sql, params, TaxSettlementDetailsEntity.class);

    }

    public TaxSettlementMastersEntity getTaxSettlementMaster(Integer year, String inputType) {
        String sql = "select tsm.* from pit_tax_settlement_masters tsm " +
                " where tsm.year = :year" +
                " and tsm.input_type = :inputType" +
                " and tsm.is_deleted = 'N'";
        Map<String, Object> params = new HashMap<>();
        params.put("year", year);
        params.put("inputType", inputType);
        return getFirstData(sql, params, TaxSettlementMastersEntity.class);
    }

    public void deleteOldData(TaxSettlementMastersEntity oldMastersEntity) {
        if (oldMastersEntity != null) {
            String[] sql = new String[]{
                    """
                 DELETE a FROM pit_tax_settlement_details a where a.year = :year
                 and a.tax_settlement_master_id = :masterId
                """,
                    """
                 DELETE a FROM pit_tax_settlement_months a where a.year = :year
                 and a.tax_settlement_master_id = :masterId
                """,
                    """
                 DELETE a FROM pit_tax_settlement_masters a where a.year = :year
                 and a.tax_settlement_master_id = :masterId
                """,
            };
            Map<String, Object> params = new HashMap<>();
            params.put("year", oldMastersEntity.getYear());
            params.put("masterId", oldMastersEntity.getTaxSettlementMasterId());
            executeSqlDatabase(sql, params);

            //update trang thai cac ban ghi ke khai thue
            if (!Utils.isNullOrEmpty(oldMastersEntity.getTaxDeclareMasterIds())) {
                updateStatusTaxDeclare(Utils.stringToListLong(oldMastersEntity.getTaxDeclareMasterIds(), ","), TaxDeclareMastersEntity.STATUS.DA_CHOT);
            }
        }
    }

    public void updateStatusTaxDeclare(List<Long> taxDeclareMasterIds, String status) {
        String sql = "UPDATE pit_tax_declare_masters a" +
                "   set a.status = :status, a.modified_time = now(), a.modified_by = :userName" +
                "   where a.tax_declare_master_id in (:taxDeclareMasterIds)";
        Map<String, Object> params = new HashMap<>();
        params.put("taxDeclareMasterIds", taxDeclareMasterIds);
        params.put("status", status);
        params.put("userName", Utils.getUserNameLogin());
        executeSqlDatabase(sql, params);
    }

    public List<TaxSettlementDetailsResponse> getListExportByMasterId(Long masterId, List<String> doiTuongLuyTiens) {
        Map<String, Object> params = new HashMap<>();
        params.put("masterId", masterId);
        StringBuilder empTypeProgressiveTax = new StringBuilder(",");
        for (String str : doiTuongLuyTiens){
            empTypeProgressiveTax.append(str).append(",");
        }
        params.put("empTypeProgressiveTax", empTypeProgressiveTax);
        String sql = """
                SELECT
                     o.code,
                     o.org_name_level_2 org_name,
                     SUM(CASE WHEN :empTypeProgressiveTax LIKE CONCAT('%,', d.emp_type_code, ',%') THEN 1 ELSE 0 END) countEmpIn,
                     SUM(CASE WHEN :empTypeProgressiveTax NOT LIKE CONCAT('%,', d.emp_type_code, ',%') THEN 1 ELSE 0 END) countEmpOut,
                     SUM(d.income_taxable) sumIncomeTaxable,
                     SUM(d.tax_collected) sumTaxCollected,
                     SUM(d.insurance_deduction) sumInsuranceDeduction,
                     SUM(CASE WHEN :empTypeProgressiveTax LIKE CONCAT('%,', d.emp_type_code, ',%') THEN d.total_income_taxable ELSE 0 END) sumTotalIncomeTaxableIn,
                     SUM(CASE WHEN :empTypeProgressiveTax NOT LIKE CONCAT('%,', d.emp_type_code, ',%') THEN d.total_income_taxable ELSE 0 END) sumTotalIncomeTaxableOut,
                     COUNT(d.num_of_dependents) countNumOfDependents,
                     SUM(IFNULL(d.self_deduction, 0) + IFNULL(d.dependent_deduction, 0)) sumDeduction,
                     SUM(d.total_insurance_deduction) sumTotalInsuranceDeduction,
                     SUM(d.total_tax_collected) sumTotalTaxCollected,
                     SUM(d.total_income_tax) sumTotalIncomeTax,
                     SUM(CASE WHEN d.total_tax_payed < 0 THEN - d.total_tax_payed ELSE 0 END) sumTotalTaxPayedSubmitted,
                     SUM(CASE WHEN d.total_tax_payed > 0 THEN d.total_tax_payed ELSE 0 END) sumTotalTaxPayedNotSubmit
                FROM pit_tax_settlement_details d
                LEFT JOIN hr_organizations o ON o.organization_id = d.declare_org_id
                WHERE d.tax_settlement_master_id = :masterId
                AND d.is_deleted = 'N'
                GROUP BY o.org_name_level_2
                """;
        return getListData(sql, params, TaxSettlementDetailsResponse.class);
    }


    public List<Map<String, Object>> getTaxDeclareMasters(List<Long> taxDeclareMasterIds) {
        if (Utils.isNullOrEmpty(taxDeclareMasterIds)) {
            throw new BaseAppException("Chưa có dữ dữ liệu kê khai thuế TNCN");
        }
        String sql = """
                select
                	DATE_FORMAT(a.tax_period_date,'%m-%Y') as ky_ke_khai,
                	case
                		when a.input_type = 'IMPORT' then 'Dữ liệu import'
                		else 'Dữ liệu tính'
                	end as kieu_nhap_lieu,
                	a.total_income_taxable as tong_thu_nhap_chiu_thue,
                	ifnull(a.total_income_tax,0) + ifnull(a.total_month_retro_tax,0) as thue_ke_khai,
                	a.total_tax_collected as thue_da_thu,
                	a.total_insurance_deduction bhxh,
                	a.created_by as nguoi_nhap,
                	date_format(a.created_time,'%d-%m-%Y %H:%i:%s') as ngay_nhap
                from pit_tax_declare_masters a
                where a.tax_declare_master_id in (:taxDeclareMasterIds)
                order by a.tax_period_date
                """;
        Map<String, Object> params = new HashMap<>();
        params.put("taxDeclareMasterIds", taxDeclareMasterIds);

        List<Map<String, Object>>  resultList = getListData(sql, params);
        if (Utils.isNullOrEmpty(resultList)) {
            resultList.add(getMapEmptyAliasColumns(sql));
        }
        return resultList;
    }
}
