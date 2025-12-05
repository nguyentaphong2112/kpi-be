/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.tax.income.repositories.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.tax.income.constants.Constant;
import vn.hbtplus.models.BaseDataTableDto;
import vn.hbtplus.repositories.BaseRepository;
import vn.hbtplus.tax.income.models.IncomeItemDetailsDto;
import vn.hbtplus.tax.income.models.dto.ContractProcessDTO;
import vn.hbtplus.tax.income.models.request.IncomeItemMastersRequest;
import vn.hbtplus.tax.income.models.response.IncomeItemMastersResponse;
import vn.hbtplus.tax.income.repositories.entity.IncomeItemColumnsEntity;
import vn.hbtplus.tax.income.repositories.entity.IncomeItemDetailsEntity;
import vn.hbtplus.tax.income.repositories.entity.IncomeItemMastersEntity;
import vn.hbtplus.tax.income.repositories.entity.TaxDeclareDetailsEntity;
import vn.hbtplus.tax.income.repositories.jpa.IncomeItemDetailsRepositoryJPA;
import vn.hbtplus.utils.QueryUtils;
import vn.hbtplus.utils.Utils;

import java.util.*;

/**
 * Lop repository Impl ung voi bang pit_income_item_masters
 *
 * @author tudd
 * @version 1.0
 * @since 1.0
 */

@Repository
@RequiredArgsConstructor
public class IncomeItemMastersRepository extends BaseRepository {
    private final IncomeItemDetailsRepositoryJPA incomeItemDetailsRepositoryJPA;
    private final IncomeTemplateRepository incomeTemplateRepository;
    private final ConfigParameterRepository configParameterRepository;

    public BaseDataTableDto<IncomeItemMastersResponse> searchData(IncomeItemMastersRequest.SearchForm dto) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    master.income_item_master_id,
                    master.income_item_id,
                    master.tax_period_date,
                    item.salary_period_date,
                    master.is_tax_calculated,
                    master.status,
                    master.input_times,
                    master.total_income,
                    master.total_insurance_deduction,
                    master.total_income_taxable,
                    master.total_income_free_tax,
                    master.total_income_tax,
                    master.total_month_retro_tax,
                    master.total_year_retro_tax,
                    master.total_received,
                    master.created_by,
                    master.created_time,
                    master.tax_cal_by,
                    master.tax_date,
                    item.code as itemCode,
                    item.name as itemName,
                    cat.name as type_name,
                    (select cat.name from sys_categories cat
                        where cat.value = master.status
                        and cat.category_type = :categoryType
                    ) as statusName
                """);
        HashMap<String, Object> params = new HashMap<>();
        params.put("categoryType", Constant.CATEGORY_TYPE.THUE_TRANG_THAI);
        addCondition(sql, params, dto);
        return getListPagination(sql.toString(), params, dto, IncomeItemMastersResponse.class);
    }

    public List<Map<String, Object>> getListExport(IncomeItemMastersRequest.SearchForm dto) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                	date_format(master.tax_period_date, '%m/%Y') tax_period_date,
                	master.is_tax_calculated,
                	master.status,
                	master.input_times,
                	master.total_income,
                	master.total_insurance_deduction,
                	master.total_income_taxable,
                	master.total_income_free_tax,
                	master.total_income_tax,
                	master.total_received,
                	item.code as item_code,
                	item.name as item_name,
                	cat.name as type_name,
                	(select cat.name from sys_categories cat
                        where cat.value = master.status
                        and cat.category_type = :categoryType
                    ) as status_name,
                	master.created_by,
                    date_format(master.created_time, '%d/%m/%Y') created_time,
                    master.tax_cal_by,
                    date_format(master.tax_date, '%d/%m/%Y') tax_date
                """);
        Map<String, Object> params = new HashMap<>();
        addCondition(sql, params, dto);
        params.put("categoryType", Constant.CATEGORY_TYPE.THUE_TRANG_THAI);
        List<Map<String, Object>> resultList = getListData(sql.toString(), params);
        if (Utils.isNullOrEmpty(resultList)) {
            resultList.add(getMapEmptyAliasColumns(sql.toString()));
        }
        return resultList;
    }

    private void addCondition(StringBuilder sql, Map<String, Object> params, IncomeItemMastersRequest.SearchForm dto) {
        sql.append("""
                FROM pit_income_item_masters master
                    inner join pit_income_items item ON master.income_item_id = item.income_item_id
                    inner join pit_income_templates template ON item.income_template_id = template.income_template_id
                    inner join sys_categories cat ON template.type = cat.value and IFNULL(cat.is_deleted, :activeStatus) = :activeStatus and cat.category_type = 'THUE_LOAI_THU_NHAP'
                    WHERE IFNULL(master.is_deleted, :activeStatus) = :activeStatus
                """);

        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        if (dto.getTaxPeriodDate() != null) {
            QueryUtils.filterEq(Utils.formatDate(dto.getTaxPeriodDate(), Constant.SHORT_FORMAT_DATE), sql, params, "DATE_FORMAT(master.tax_period_date, '%m/%Y')");
        }
        QueryUtils.filter(dto.getIncomeName(), sql, params, "item.name");
        QueryUtils.filter(dto.getListStatus(), sql, params, "master.status");
        QueryUtils.filter(dto.getListType(), sql, params, "template.type");

        sql.append(" ORDER BY master.tax_period_date desc, item.name");
    }

    public void saveData(List<IncomeItemDetailsEntity> detailsEntities, Long incomeItemMasterId) {
        Long startDetailId = getNextId(IncomeItemDetailsEntity.class, detailsEntities.size());
        final String userName = Utils.getUserNameLogin();
        List<IncomeItemColumnsEntity> columnsEntities = new ArrayList<>();
        Map<String, List<String>> mapIncomeTypeColumns = incomeTemplateRepository.getMapIncomeTypeColumns();
        int idx = 0;
        for (IncomeItemDetailsEntity item : detailsEntities) {
            item.setIncomeItemDetailId(startDetailId + idx);
            item.setIncomeItemMasterId(incomeItemMasterId);
            item.setCreatedBy(userName);
            item.setCreatedTime(new Date());
            for (Map.Entry<String, Long> entry : item.getMapValues().entrySet()) {
                IncomeItemColumnsEntity columnsEntity = new IncomeItemColumnsEntity(item, entry);
                columnsEntities.add(columnsEntity);
                //add thu nhap tong cong
                item.addIncomeValue(mapIncomeTypeColumns, columnsEntity);
            }
            if (item.getIncomeAmount() != null && item.getIncomeAmount() < 0) {
                item.setNote(Utils.join(", ", item.getNote(), "Dữ liệu thu nhập nhỏ hơn 0"));
            }
            idx++;
        }
        insertBatch(IncomeItemDetailsEntity.class, detailsEntities, userName);
        incomeItemDetailsRepositoryJPA.flush();
        insertBatch(IncomeItemColumnsEntity.class, columnsEntities, userName);
        updateDataMasterById(incomeItemMasterId);
    }

    public void updateDataMasterById(Long incomeItemMasterId) {
        String sql = """
                UPDATE pit_income_item_masters AS m
                JOIN (
                  SELECT income_item_master_id,
                        SUM(income_amount) AS total_income,
                        SUM(income_taxable) AS total_income_taxable,
                        SUM(insurance_deduction) AS total_insurance_deduction,
                        SUM(other_deduction) AS total_other_deduction,
                        SUM(income_free_tax) AS total_income_free_tax,
                        SUM(income_tax) AS total_income_tax,
                        SUM(month_retro_tax) AS total_month_retro_tax,
                        SUM(year_retro_tax) AS total_year_retro_tax,
                        SUM(income_received) AS total_received
                  FROM pit_income_item_details
                  WHERE income_item_master_id = :incomeItemMasterId
                  and is_deleted = 'N'
                  GROUP BY income_item_master_id
                ) AS s
                ON m.income_item_master_id = s.income_item_master_id
                SET m.total_income = s.total_income
                    , m.total_insurance_deduction = s.total_insurance_deduction
                    , m.total_other_deduction = s.total_other_deduction
                    , m.total_income_taxable = s.total_income_taxable
                    , m.total_income_free_tax = s.total_income_free_tax
                    , m.total_income_tax = s.total_income_tax
                    , m.total_month_retro_tax = s.total_month_retro_tax
                    , m.total_year_retro_tax = s.total_year_retro_tax
                    , m.total_received = s.total_received
                WHERE m.income_item_master_id = :incomeItemMasterId;
                """;
        HashMap<String, Object> params = new HashMap<>();
        params.put("incomeItemMasterId", incomeItemMasterId);
        executeSqlDatabase(sql, params);
    }

    public List<IncomeItemDetailsDto> getPreIncomeItems(List<String> empCodes, Date taxPeriodDate, Long id) {
        String sql = """
                select itc.* from pit_income_item_details itc
                where itc.emp_code in (:empCodes)
                  and itc.tax_period_date = :taxPeriodDate
                  and itc.is_deleted = 'N'
                  and itc.income_item_master_id <> :id
                  and itc.income_item_master_id in (
                      select income_item_master_id from pit_income_item_masters im
                      where im.tax_period_date = :taxPeriodDate
                      and im.status not in (:status)
                  )
                  """;
        List<IncomeItemDetailsDto> result = new ArrayList<>();
        List<List<String>> partitions = Utils.partition(empCodes, Constant.BATCH_SIZE);
        partitions.stream().forEach(item -> {
            Map<String, Object> mapParams = new HashMap<>();
            mapParams.put("id", id);
            mapParams.put("taxPeriodDate", taxPeriodDate);
            mapParams.put("empCodes", item);
            mapParams.put("status", Arrays.asList(IncomeItemMastersEntity.STATUS.DU_THAO));
            result.addAll(getListData(sql, mapParams, IncomeItemDetailsDto.class));
        });
        return result;
    }

    public List<IncomeItemDetailsDto> getIncomeItemDetails(Long id, Date taxPeriodDate) {
        String sql = """
                select itd.* ,
                (select
                    sum(column_value) from pit_income_item_columns itc
                    where itc.tax_period_date = :taxPeriodDate
                    and itc.column_code in (:tax10Column)
                    and itc.income_item_detail_id = itd.income_item_detail_id
                ) as incomeTax10,
                (select
                    sum(column_value) from pit_income_item_columns itc
                    where itc.tax_period_date = :taxPeriodDate
                    and itc.column_code in (:tax20Column)
                    and itc.income_item_detail_id = itd.income_item_detail_id
                ) as incomeTax20,
                (select
                    sum(column_value) from pit_income_item_columns itc
                    where itc.tax_period_date = :taxPeriodDate
                    and itc.column_code in (:minIncomeDeductColumn)
                    and itc.income_item_detail_id = itd.income_item_detail_id
                ) as minIncomeDeduct
                from pit_income_item_details itd
                where itd.tax_period_date = :taxPeriodDate
                and itd.income_item_master_id = :id
                and itd.is_deleted = 'N'
                order by itd.income_amount
                """;
        Map<String, Object> mapParams = new HashMap<>();
        mapParams.put("id", id);
        mapParams.put("taxPeriodDate", taxPeriodDate);

        Map<String, List<String>> mapIncomeTypeColumns = incomeTemplateRepository.getMapIncomeTypeColumns();
        mapParams.put("minIncomeDeductColumn", mapIncomeTypeColumns.get(Constant.IncomeType.MINIMUM));
        mapParams.put("tax20Column", mapIncomeTypeColumns.get(Constant.IncomeType.TAX_20));
        mapParams.put("tax10Column", mapIncomeTypeColumns.get(Constant.IncomeType.TAX_10));
        return getListData(sql, mapParams, IncomeItemDetailsDto.class);
    }

    public void updateTax(List<IncomeItemDetailsDto> detailsEntities) {
        String sql = "update pit_income_item_details itc" +
                " set itc.income_tax = :incomeTax," +
                "   itc.tax_method = :taxMethod," +
                "   itc.num_of_dependents = :numOfDependents," +
                "   itc.income_received = :incomeReceived" +
                " where itc.income_item_detail_id = :id";
        List<Map> listParams = new ArrayList<>();
        detailsEntities.stream().forEach(item -> {
            Map<String, Object> mapParam = new HashMap<>();
            mapParam.put("incomeTax", item.getIncomeTax());
            mapParam.put("taxMethod", item.getTaxMethod());
            mapParam.put("incomeReceived", item.getIncomeReceived());
            mapParam.put("numOfDependents", item.getNumOfDependents());
            mapParam.put("id", item.getIncomeItemDetailId());
            listParams.add(mapParam);
        });
        executeBatch(sql, listParams);
    }


    public boolean isExistsItemInStatus(Date taxPeriodDate, String... status) {
        String sql = "select count(*) as counter " +
                "   from pit_income_item_masters a " +
                " where a.is_deleted = 'N'" +
                " and a.tax_period_date = :taxPeriodDate" +
                " and a.status in (:statusNotLocked)";
        Map<String, Object> mapParam = new HashMap<>();
        mapParam.put("taxPeriodDate", taxPeriodDate);
        mapParam.put("statusNotLocked", Arrays.asList(status));
        Integer counter = getFirstData(sql, mapParam, Integer.class);
        return counter > 0;
    }

    public List<TaxDeclareDetailsEntity> getEntities(Date taxPeriodDate) {
        String sql = """
                select itd.emp_code,
                        itd.full_name,
                        ifnull((select tax_no from hr_employees e where e.employee_code = itd.emp_code),itd.tax_no) tax_no,
                        ifnull((select personal_id_no from hr_employees e where e.employee_code = itd.emp_code),itd.tax_no) personal_id_no,
                        itd.type incomeType,
                        itd.declare_org_id,
                        sum(income_amount) income_amount,
                        sum(income_taxable) income_taxable,
                        sum(insurance_deduction) insurance_deduction,
                        sum(income_free_tax) income_free_tax,
                        sum(income_tax) taxCollected,
                        sum(month_retro_tax) month_retro_tax,
                        sum(year_retro_tax) year_retro_tax,
                        sum(income_received) income_received,
                        sum(other_deduction) other_deduction
                from (select itd.*,
                    	(select it.type from pit_income_items im, pit_income_templates it, pit_income_item_masters iim
                    	where im.income_item_id = iim.income_item_id
                    	and it.income_template_id = im.income_template_id
                    	and iim.income_item_master_id = itd.income_item_master_id) type
                    from pit_income_item_details itd
                    join pit_income_item_masters its on its.income_item_master_id = itd.income_item_master_id and its.is_deleted = 'N'
                    where itd.tax_period_date = :taxPeriodDate
                    and itd.is_deleted = 'N'
                ) itd                
                group by itd.emp_code,
                    itd.full_name,
                    itd.tax_no,
                    itd.personal_id_no,
                    itd.declare_org_id,
                    itd.type
                order by itd.created_time
                    """;
        Map<String, Object> mapParam = new HashMap<>();
        mapParam.put("taxPeriodDate", taxPeriodDate);
        return getListData(sql, mapParam, TaxDeclareDetailsEntity.class);
    }

    public IncomeItemMastersEntity getIncomeItemMaster(Long incomeItemId, Date taxPeriodDate) {
        String sql = "select a.* " +
                "   from pit_income_item_masters a " +
                " where a.is_deleted = 'N'" +
                " and a.tax_period_date = :taxPeriodDate" +
                " and a.income_item_id = :incomeItemId";
        Map<String, Object> mapParam = new HashMap<>();
        mapParam.put("taxPeriodDate", taxPeriodDate);
        mapParam.put("incomeItemId", incomeItemId);
        return getFirstData(sql, mapParam, IncomeItemMastersEntity.class);
    }

    public void deleteOldData(IncomeItemMastersEntity mastersEntity) {
        String sql[] = new String[]{
                """
                    delete a from pit_income_item_columns a
                    where a.tax_period_date = :taxPeriodDate
                    and a.income_item_master_id = :incomeItemMasterId
                """,
                """
                    delete a from pit_income_item_details a
                    where a.tax_period_date = :taxPeriodDate
                    and a.income_item_master_id = :incomeItemMasterId
                """,
                """
                    delete a from pit_income_item_masters a
                    where a.tax_period_date = :taxPeriodDate
                    and a.income_item_master_id = :incomeItemMasterId
                """,
        };
        Map<String, Object> mapParam = new HashMap<>();
        mapParam.put("taxPeriodDate", mastersEntity.getTaxPeriodDate());
        mapParam.put("incomeItemMasterId", mastersEntity.getIncomeItemMasterId());
        executeSqlDatabase(sql, mapParam);
    }

    public void updateTaxDeclareOrgId(Long incomeItemMasterId, Date periodDate) {
        String sql = """
                update pit_income_item_details a, (
                select
                	ei.income_item_detail_id,
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
                from pit_income_item_details ei
                join hr_employees e on e.employee_code = ei.emp_code
                left join hr_work_process wp on wp.employee_id = e.employee_id
                	and ei.tax_period_date BETWEEN wp.start_date and IFNULL(wp.end_date,:periodDate)
                left join hr_organizations org on wp.organization_id = org.organization_id
                join pit_income_item_masters iim on ei.income_item_master_id = iim.income_item_master_id
                where ei.is_deleted = 'N'
                and iim.is_deleted = 'N'
                and ei.tax_period_date = :periodDate
                and ei.income_item_master_id = :masterId
                ) T
                set a.declare_org_id = T.declare_org_id, a.work_org_id = T.work_org_id
                where a.income_item_detail_id = T.income_item_detail_id
                and a.income_item_master_id = :masterId
                and a.tax_period_date = :periodDate
                """;
        Map<String, Object> mapParam = new HashMap<>();
        mapParam.put("masterId", incomeItemMasterId);
        mapParam.put("periodDate", periodDate);
        mapParam.put("idKCQ", Constant.COMMON.ID_KHOI_CO_QUAN);
        mapParam.put("orgVCCPath", "%/" + configParameterRepository.getConfigValue(Constant.CONFIG_PARAMETERS.ROOT_LEGACY_ID, periodDate, Long.class) + "/%");

        executeSqlDatabase(sql, mapParam);
    }

    public ContractProcessDTO getEmployeeByCode(String employeeCode) {
        String sql = """
                SELECT
                	a.start_date,
                	a.end_date
                FROM
                	hr_contract_process a
                LEFT JOIN hr_employees he ON he.employee_id = a.employee_id
                WHERE ifnull(a.is_deleted, :isDelete) = :isDelete and he.employee_code =:employeeCode
                """;
        Map<String, Object> mapParam = new HashMap<>();
        mapParam.put("employeeCode", employeeCode);
        mapParam.put("isDelete", BaseConstants.STATUS.NOT_DELETED);
        return getFirstData(sql, mapParam, ContractProcessDTO.class);
    }

}
