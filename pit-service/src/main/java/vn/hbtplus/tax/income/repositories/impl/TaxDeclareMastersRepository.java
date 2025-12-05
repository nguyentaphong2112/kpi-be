/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.tax.income.repositories.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.models.BaseDataTableDto;
import vn.hbtplus.tax.income.constants.Constant;
import vn.hbtplus.tax.income.models.bean.CategoryBean;
import vn.hbtplus.repositories.BaseRepository;
import vn.hbtplus.tax.income.models.EmpTaxInfoDto;
import vn.hbtplus.tax.income.models.request.TaxDeclareMastersRequest;
import vn.hbtplus.tax.income.models.response.TaxDeclareDetailsResponse;
import vn.hbtplus.tax.income.models.response.TaxDeclareMastersResponse;
import vn.hbtplus.tax.income.repositories.entity.IncomeItemMastersEntity;
import vn.hbtplus.tax.income.repositories.entity.TaxDeclareMastersEntity;
import vn.hbtplus.utils.QueryUtils;
import vn.hbtplus.utils.Utils;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Lop repository Impl ung voi bang pit_tax_declare_masters
 *
 * @author tudd
 * @version 1.0
 * @since 1.0
 */

@Repository
@RequiredArgsConstructor
public class TaxDeclareMastersRepository extends BaseRepository {
    private final CategoryRepository categoryRepository;
    private final ConfigParameterRepository configParameterRepository;

    public BaseDataTableDto searchData(TaxDeclareMastersRequest.SearchForm dto) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    a.tax_declare_master_id,
                    a.tax_period_date,
                    a.status,
                    (select s.name from sys_categories s where s.value = a.status and s.category_type = :statusType) statusName,
                    (select s.name from sys_categories s where s.value = a.input_type and s.category_type = :inputType) input_type,
                    a.total_income_taxable,
                    a.total_income_free_tax,
                    (ifnull(a.total_insurance_deduction,0) + ifnull(a.total_other_deduction,0)) total_deduction,
                    a.total_income_tax,
                    a.total_insurance_deduction,
                    a.total_tax_collected,
                    a.total_tax_payable,
                    a.total_month_retro_tax,
                    a.created_by,
                    a.created_time,
                    a.modified_by,
                    a.modified_time
                """);
        HashMap<String, Object> params = new HashMap<>();
        addCondition(sql, params, dto);
        return getListPagination(sql.toString(), params, dto, TaxDeclareMastersResponse.class);
    }

    public List<Map<String, Object>> getListExport(TaxDeclareMastersRequest.SearchForm dto) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    a.tax_declare_master_id,
                    date_format(a.tax_period_date, '%m/%Y') strTaxPeriodDate,
                    (select s.name from sys_categories s where s.value = a.status and s.category_type = :statusType) status,
                    (select s.name from sys_categories s where s.value = a.input_type and s.category_type = :inputType) input_type,
                    a.total_income_taxable,
                    a.total_income_free_tax,
                    (ifnull(a.total_insurance_deduction,0) + ifnull(a.total_other_deduction,0)) total_deduction,
                    a.total_income_tax,
                    a.total_tax_collected,
                    a.total_tax_payable,
                    a.total_month_retro_tax,
                    a.created_by,
                    date_format(a.created_time, '%d/%m/%Y %H:%i:%s') strCreatedTime,
                    a.modified_by,
                    a.modified_time
                """);
        Map<String, Object> params = new HashMap<>();
        addCondition(sql, params, dto);
        List<Map<String, Object>> resultList = getListData(sql.toString(), params);
        if (Utils.isNullOrEmpty(resultList)) {
            resultList.add(getMapEmptyAliasColumns(sql.toString()));
        }
        return resultList;
    }

    private void addCondition(StringBuilder sql, Map<String, Object> params, TaxDeclareMastersRequest.SearchForm dto) {
        sql.append("""
                    FROM pit_tax_declare_masters a
                    WHERE IFNULL(a.is_deleted, :activeStatus) = :activeStatus
                """);
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        params.put("statusType", Constant.CATEGORY_TYPE.TRANG_THAI_KKT);
        params.put("inputType", Constant.CATEGORY_TYPE.ACTION_TYPE);

        QueryUtils.filter(dto.getListStatus(), sql, params, "a.status");
        QueryUtils.filter(dto.getListInputType(), sql, params, "a.input_type");
        QueryUtils.filterGe(Utils.getLastDay(dto.getFromPeriodDate()), sql, params, "a.tax_period_date", "fromDate");
        QueryUtils.filterLe(Utils.getLastDay(dto.getToPeriodDate()), sql, params, "a.tax_period_date", "toDate");
        sql.append(" ORDER BY a.tax_period_date DESC, a.created_time desc");
    }

    public void updateDataMasterById(Long taxDeclareMasterId) {
        String sql = """
                UPDATE pit_tax_declare_masters AS m
                JOIN (
                  SELECT :taxDeclareMasterId tax_declare_master_id,
                        count(*) total_taxpayers,
                        SUM(income_taxable) AS total_income_taxable,
                        SUM(income_free_tax) AS total_income_free_tax,
                        SUM(insurance_deduction) AS total_insurance_deduction,
                        SUM(other_deduction) AS total_other_deduction,
                        SUM(tax_collected) AS total_tax_collected,
                        SUM(income_tax) AS total_income_tax,
                        SUM(month_retro_tax) AS total_month_retro_tax
                  FROM pit_tax_declare_details
                  WHERE tax_declare_master_id = :taxDeclareMasterId
                  and is_deleted = 'N'
                ) AS s
                ON m.tax_declare_master_id = s.tax_declare_master_id
                SET m.total_insurance_deduction = s.total_insurance_deduction
                    , m.total_other_deduction = s.total_other_deduction
                    , m.total_income_taxable = s.total_income_taxable
                    , m.total_income_free_tax = s.total_income_free_tax
                    , m.total_income_tax = s.total_income_tax
                    , m.total_tax_collected = s.total_tax_collected
                    , m.total_month_retro_tax = s.total_month_retro_tax
                    , m.total_taxpayers = s.total_taxpayers
                    , m.total_tax_payable = ifnull(s.total_income_tax,0) - ifnull(s.total_tax_collected,0)
                WHERE m.tax_declare_master_id = :taxDeclareMasterId;
                """;
        HashMap<String, Object> params = new HashMap<>();
        params.put("taxDeclareMasterId", taxDeclareMasterId);
        executeSqlDatabase(sql, params);
    }

    public TaxDeclareMastersEntity getTaxDeclareMaster(Date taxPeriodDate, String inputType) {
        String sql = "select a.* from pit_tax_declare_masters a " +
                " where a.tax_period_date = :taxPeriodDate" +
                " and a.input_type = :inputType" +
                " and a.is_deleted = 'N'";
        Map<String, Object> params = new HashMap<>();
        params.put("inputType", inputType);
        params.put("taxPeriodDate", taxPeriodDate);
        return getFirstData(sql, params, TaxDeclareMastersEntity.class);
    }

    public List<TaxDeclareDetailsResponse> getListExportKK02(Long masterId, List<Long> orgIds) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    d.tax_declare_detail_id,
                    d.emp_code,
                    d.full_name,
                    d.tax_no,
                    d.personal_id_no,
                    (select s.name from sys_categories s where s.value = d.emp_type_code and s.category_type = :empType) emp_type_code,
                    o.org_name_level_2 orgName,
                    (select org_name_level_2 from hr_organizations where organization_id = d.work_org_id) as workOrgName,
                    d.pos_name,
                    (select s.name from sys_categories s where s.value = d.tax_method and s.category_type = :taxMethodType) tax_method,
                    d.num_of_dependents,
                    d.dependent_deduction,
                    d.insurance_deduction,
                    d.other_deduction,
                    d.income_tax,
                    d.note,
                    d.tax_collected,
                    d.month_retro_tax
                FROM pit_tax_declare_masters m
                JOIN pit_tax_declare_details d ON m.tax_declare_master_id = d.tax_declare_master_id
                left JOIN hr_organizations o ON o.organization_id = d.declare_org_id
                WHERE m.tax_declare_master_id = :masterId
                AND d.is_deleted = 'N'
                """);
        Map<String, Object> params = new HashMap<>();
        params.put("empType", Constant.CATEGORY_TYPE.DIEN_DOI_TUONG_THUE);
        params.put("taxMethodType", Constant.CATEGORY_TYPE.CACH_TINH_THUE);
        params.put("masterId", masterId);
//        Long unitId = Utils.getPermissionUnitId();
        Long unitId = null;
        if (unitId != null) {
            sql.append(" and d.work_org_id in (select org_id from hr_organizations o" +
                    " where o.path_id like :permissionUnitIdPath)");
            params.put("permissionUnitIdPath", "%/" + unitId + "/%");
        }

        if (orgIds != null && !orgIds.isEmpty()) {
            sql.append("""
                    and exists (
                        select 1 from hr_organizations o, hr_organizations org
                        where o.organization_id in (:orgIds)
                            and org.org_id = d.work_org_id
                            and org.path_id like concat('%', o.path_id, '%')
                    )
                    """);
            params.put("orgIds", orgIds);
        }

        return getListData(sql.toString(), params, TaxDeclareDetailsResponse.class);
    }

    public List getListExportTaxAllocation(Long masterId, List<Long> orgIds) {
        StringBuilder sql = new StringBuilder("""
                select T.don_vi,
                    sum(T.thu_nhap_chiu_thue) as thu_nhap_chiu_thue,
                    sum(T.so_nguoi) as so_nguoi,
                    sum(T.tnct_co_khau_tru) as tnct_co_khau_tru,
                    sum(T.so_nguoi_khau_tru) as so_nguoi_khau_tru,
                    sum(T.thue_phai_nop) as thue_phai_nop,
                    sum(T.thue_phai_nop) as thue_da_nop,
                    sum(T.thue_phai_nop) as thue_con_phai_nop,
                    sum(T.thue_phai_nop) as thue_ke_khai,
                    sum(T.thue_truy_thu_ky_truoc) as thue_truy_thu_ky_truoc,
                    null as ghi_chu,
                    null ma_tinh,
                    null ma_so_thue
                 from  (
                 select
                    (select org_name_level_2
                        from hr_organizations org where org.organization_id = td.declare_org_id) as don_vi,
                    td.income_taxable thu_nhap_chiu_thue,
                    1 as so_nguoi,
                    case
                        when IFNULL(td.income_tax,0) + IFNULL(td.month_retro_tax,0) <> 0
                        then td.income_taxable
                        else 0
                    end tnct_co_khau_tru,
                    case
                        when IFNULL(td.income_tax,0) + IFNULL(td.month_retro_tax,0) <> 0
                        then 1
                        else 0
                    end so_nguoi_khau_tru,
                    td.income_tax thue_phai_nop,
                    td.tax_collected thue_da_nop,
                    td.tax_payable as thue_con_phai_nop,
                    IFNULL(td.income_tax,0) + IFNULL(td.month_retro_tax,0) as thue_ke_khai,
                    td.month_retro_tax thue_truy_thu_ky_truoc,
                    case
                        when td.tax_method = :luyTien then 1
                        else 0
                    end so_nguoi_hdld,
                    case
                        when IFNULL(td.income_tax,0) + IFNULL(td.month_retro_tax,0) <> 0
                            and (td.emp_code is not null or td.tax_no is not null)
                        then 1
                        else 0
                    end so_nguoi_khau_tru_cu_tru,
                    case
                        when (td.emp_code is not null or td.tax_no is not null)
                        then income_taxable
                        else 0
                    end thu_nhap_chiu_thue_cu_tru,
                    case
                        when IFNULL(td.income_tax,0) + IFNULL(td.month_retro_tax,0) <> 0
                            and (td.emp_code is not null or td.tax_no is not null)
                        then td.income_taxable
                        else 0
                    end tnct_co_khau_tru_cu_tru,
                    case
                        when IFNULL(td.income_tax,0) + IFNULL(td.month_retro_tax,0) <> 0
                            and (td.emp_code is not null or td.tax_no is not null)
                        then IFNULL(td.income_tax,0) + IFNULL(td.month_retro_tax,0)
                        else 0
                    end thue_ke_khai_cu_tru
                 from pit_tax_declare_details td
                 where td.is_deleted = 'N'
                 and td.tax_period_date = :periodDate
                 and td.tax_declare_master_id = :masterId %s
                 ) T
                 group by T.don_vi
                """);
        Map<String, Object> params = new HashMap<>();
        params.put("luyTien", Constant.TAXES_METHOD.LUY_TIEN);
        params.put("masterId", masterId);
        params.put("periodDate", get(TaxDeclareMastersEntity.class, masterId).getTaxPeriodDate());
        List<CategoryBean> listOrgs = categoryRepository.getListCategoryBeans(Constant.CATEGORY_TYPE.THUE_DON_VI_KE_KHAI);
        Map<String, CategoryBean> mapOrgs = new HashMap<>();
        listOrgs.forEach(bean -> {
            mapOrgs.put(bean.getName(), bean);
        });
        String condition = "";
        if (orgIds != null && !orgIds.isEmpty()) {
            condition = """
                    and exists (
                        select 1 from hr_organizations o, hr_organizations org
                        where o.organization_id in (:orgIds)
                            and org.organization_id = td.work_org_id
                            and org.path_id like concat('%', o.path_id, '%')
                    )
                    """;
            params.put("orgIds", orgIds);
        }
        String sqlQuery = sql.toString();
        sqlQuery = String.format(sqlQuery, condition);
        List<Map<String, Object>> listResults = getListData(sqlQuery, params);
        listResults.forEach(item -> {
            CategoryBean bean = mapOrgs.get(item.get("DON_VI"));
            if (bean != null && bean.getAttributes() != null) {
                item.putAll(bean.getAttributes());
            }
        });

        if (Utils.isNullOrEmpty(listResults)) {
            listResults.add(getMapEmptyAliasColumns(sql.toString()));
        }

        return listResults;
    }

    /**
     * lấy tổng số lượng nhân viên có khấu trừ
     *
     * @param masterId id kỳ quyet toan
     * @return map
     */
    public Map<String, Integer> getCountEmpTaxDeduction(Long masterId) {
        Map<String, Object> params = new HashMap<>();
        params.put("masterId", masterId);
        String sql = """
                SELECT
                     o.org_code,
                     o.org_name,
                     COUNT(d.emp_code) countEmp
                FROM pit_tax_declare_details d
                LEFT JOIN hr_organizations o ON o.organization_id = d.declare_org_id
                WHERE d.tax_declare_master_id = :masterId
                AND d.is_deleted = 'N'
                AND (IFNULL(d.insurance_deduction, 0) + IFNULL(d.dependent_deduction, 0)) > 0
                GROUP BY d.declare_org_id
                """;
        List<TaxDeclareDetailsResponse> listData = getListData(sql, params, TaxDeclareDetailsResponse.class);
        Map<String, Integer> mapResult = new HashMap<>();
        for (TaxDeclareDetailsResponse item : listData) {
            mapResult.put(item.getOrgCode(), item.getCountEmp());
        }
        return mapResult;
    }


    public void forceDeleteOldData(TaxDeclareMastersEntity mastersEntity) {
        String[] sql = new String[]{"delete a from pit_tax_declare_columns a where a.tax_period_date = :taxPeriodDate " +
                "   and a.tax_declare_master_id = :taxDeclareMasterId ",
                "delete a from pit_tax_declare_details a where a.tax_period_date = :taxPeriodDate " +
                        "   and a.tax_declare_master_id = :taxDeclareMasterId ",
                "delete a from pit_tax_declare_masters a where a.tax_period_date = :taxPeriodDate " +
                        "   and a.tax_declare_master_id = :taxDeclareMasterId ",
        };
        Map<String, Object> params = new HashMap<>();
        params.put("taxDeclareMasterId", mastersEntity.getTaxDeclareMasterId());
        params.put("taxPeriodDate", mastersEntity.getTaxPeriodDate());
        executeSqlDatabase(sql, params);
    }

    public void inactiveOldData(TaxDeclareMastersEntity mastersEntity) {
        String[] sql = new String[]{"""
                   update pit_tax_declare_columns a
                    set a.is_deleted = 'Y', a.modified_time = now(), a.modified_by = :userName
                    where a.tax_period_date = :taxPeriodDate
                    and a.tax_declare_master_id = :taxDeclareMasterId
                   """,
                """
                   update pit_tax_declare_details a
                    set a.is_deleted = 'Y', a.modified_time = now(), a.modified_by = :userName
                    where a.tax_period_date = :taxPeriodDate
                    and a.tax_declare_master_id = :taxDeclareMasterId
                   """,
                """
                   update pit_tax_declare_masters a
                    set a.is_deleted = 'Y', a.modified_time = now(), a.modified_by = :userName
                    where a.tax_period_date = :taxPeriodDate
                    and a.tax_declare_master_id = :taxDeclareMasterId
                   """,
        };
        Map<String, Object> params = new HashMap<>();
        params.put("taxDeclareMasterId", mastersEntity.getTaxDeclareMasterId());
        params.put("taxPeriodDate", mastersEntity.getTaxPeriodDate());
        params.put("userName", Utils.getUserNameLogin());
        executeSqlDatabase(sql, params);
    }

    public void updateIncomeItemMaster(List<Long> incomeItemMasterIds, String status) {
        String sql = "update pit_income_item_masters iim " +
                " set iim.status = :status," +
                "   iim.modified_time = now()," +
                "   iim.modified_by = :userName" +
                "   where iim.income_item_master_id in (:ids)";
        Map<String, Object> params = new HashMap<>();
        params.put("status", status);
        params.put("ids", incomeItemMasterIds);
        params.put("userName", Utils.getUserNameLogin());
        executeSqlDatabase(sql, params);
    }

    public void deleteOldData(TaxDeclareMastersEntity taxDeclareMastersEntity) {
        forceDeleteOldData(taxDeclareMastersEntity);
    }

    public List<Long> getIncomeItemMasterIds(Date taxPeriodDate) {
        String sql = "select a.income_item_master_id from pit_income_item_masters a" +
                " where a.tax_period_date = :taxPeriodDate" +
                "   and a.status not in (:status)" +
                "   and a.is_deleted = 'N'";
        Map<String, Object> params = new HashMap<>();
        params.put("status", IncomeItemMastersEntity.STATUS.DU_THAO);
        params.put("taxPeriodDate", taxPeriodDate);
        return getListData(sql, params, Long.class);
    }

    public void updateTaxDeclareOrgId(Long taxDeclareMasterId, Date taxPeriodDate) {
        String sql = """
                update pit_tax_declare_details a, (
                select
                	ei.tax_declare_detail_id,
                	case
                		when exists (
                		    select 1 from sys_categories sc, hr_organizations org1
                		    where sc.is_deleted = 'N'
                		    and sc.category_type = 'THUE_DON_VI_KE_KHAI'
                		    and sc.value = org1.organization_id
                		    and org.path_id like concat(org1.path_id, '%')
                		) then org.organization_id
                		else :idKCQ
                	end declare_org_id,
                	case
                	    when org.path_id like :orgVCCPath
                	    then org.organization_id
                		else :idKCQ
                	end work_org_id
                from pit_tax_declare_details ei
                join hr_employees e on e.employee_code = ei.emp_code
                left join hr_work_process wp on wp.employee_id = e.employee_id
                	and ei.tax_period_date BETWEEN wp.start_date and IFNULL(wp.end_date,:periodDate)
                left join hr_organizations org on wp.organization_id = org.organization_id
                join pit_tax_declare_masters iim on ei.tax_declare_master_id = iim.tax_declare_master_id
                where ei.is_deleted = 'N'
                and iim.is_deleted = 'N'
                and ei.tax_period_date = :periodDate
                and ei.tax_declare_master_id = :masterId
                and ei.declare_org_id is null
                ) T
                set a.declare_org_id = T.declare_org_id,a.work_org_id = T.work_org_id
                where a.tax_declare_detail_id = T.tax_declare_detail_id
                and a.tax_declare_master_id = :masterId
                and a.tax_period_date = :periodDate
                """;
        Map<String, Object> mapParam = new HashMap<>();
        mapParam.put("masterId", taxDeclareMasterId);
        mapParam.put("periodDate", taxPeriodDate);
        mapParam.put("idKCQ", Constant.COMMON.ID_KHOI_CO_QUAN);
        mapParam.put("orgVCCPath", "%/" + configParameterRepository.getConfigValue(Constant.CONFIG_PARAMETERS.ROOT_LEGACY_ID, taxPeriodDate, Long.class) + "/%");

        executeSqlDatabase(sql, mapParam);
    }

    public List<EmpTaxInfoDto> getEmpTaxInfos(List<String> empCodes, Date taxPeriodDate) throws ExecutionException, InterruptedException {
        String sql = """
                       select e.employee_code as empCode,
                       (select sc.value from sys_categories sc where sc.value = etp.emp_type_id and sc.category_type = :typeEmpTypeCode) empTypeCode,
                       e.tax_no,
                       pi.identity_no as personalIdNo,
                       e.full_name as fullName,
                       (select name from hr_jobs where job_id = e.job_id) as positionName,
                       case
                           when dt.type = 'OUT' then 'OUT'
                           when org.path_id not like :orgVcc then 'ORG_CHANGED'
                           else 'WORKING'
                       end as status,
                       case
                           when org.path_id not like :orgVcc
                           then org.organization_id
                       end as orgId,
                       (
                           select count(*) from hr_dependent_persons dp
                           where dp.is_deleted = 1
                           and dp.employee_id = e.employee_id
                           and :taxPeriodDate between dp.from_date and ifnull(dp.to_date,:taxPeriodDate)
                       ) as numOfDependents,
                       (
                           select max(income_amount) from pit_tax_commitments pc
                           where pc.employee_id = e.employee_id
                           and pc.is_deleted = 'N'
                           and :taxPeriodDate between pc.start_date and ifnull(pc.end_date,:taxPeriodDate)
                       ) as incomeCommitment
                       from hr_employees e
                       left join hr_contract_process etp on etp.employee_id = e.employee_id and etp.is_deleted = 'N' and etp.start_date <= :taxPeriodDate
                       left join hr_work_process wp on wp.employee_id = e.employee_id and wp.is_deleted = 'N' and wp.start_date <= :taxPeriodDate
                       left join hr_organizations org on org.organization_id = wp.organization_id
                       left join hr_document_types dt on dt.document_type_id = wp.document_type_id
                       left join (
                         select pi1.*
                         from hr_personal_identities pi1
                         where pi1.is_main = 'Y'
                         order by pi1.employee_id
                         limit 1
                       ) pi on pi.employee_id = e.employee_id
                       where e.employee_code in (:empCodes)
                       and e.is_deleted = 'N'
                       and not exists (
                           select 1 from hr_contract_process etp1
                           where etp1.employee_id = etp.employee_id
                           and etp1.start_date <= :taxPeriodDate
                           and etp1.start_date > etp.start_date
                           and etp1.is_deleted = 'N'
                       )
                       and not exists (
                           select 1 from hr_work_process wp1
                           where wp1.employee_id = wp.employee_id
                           and wp1.start_date <= :taxPeriodDate
                           and wp1.start_date > wp.start_date
                           and wp1.is_deleted = 'N'
                       )
                """;


        List<List<String>> partitions = Utils.partition(empCodes, Constant.BATCH_SIZE);
        List<EmpTaxInfoDto> result = new ArrayList<>();
        final Long orgId = configParameterRepository.getConfigValue(Constant.CONFIG_PARAMETERS.ROOT_LEGACY_ID, taxPeriodDate, Long.class);

        List<CompletableFuture<Object>> completableFutures = new ArrayList<>();
        partitions.stream().forEach(partition -> {
            Map mapParams = new HashMap();
            mapParams.put("taxPeriodDate", taxPeriodDate);
            mapParams.put("typeEmpTypeCode", Constant.CATEGORY_TYPE.DIEN_DOI_TUONG_THUE_TNCN);
            mapParams.put("orgVcc", "%/" + orgId + "/%");
            mapParams.put("empCodes", partition);
            Supplier<Object> getEmps = () -> getListData(sql, mapParams, EmpTaxInfoDto.class);
            completableFutures.add(CompletableFuture.supplyAsync(getEmps));
        });
        CompletableFuture<Void> allReturn = CompletableFuture.allOf(completableFutures.toArray(new CompletableFuture[completableFutures.size()]));
        CompletableFuture<List<Object>> allFutures = allReturn.thenApply(v -> completableFutures.stream().map(CompletableFuture::join).collect(Collectors.toList()));
        List<Object> objs = allFutures.get();

        objs.stream().forEach(item -> {
            result.addAll((Collection<? extends EmpTaxInfoDto>) item);
        });

        return result;
    }
}
