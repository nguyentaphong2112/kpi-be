/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.tax.income.repositories.impl;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Repository;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.models.BaseDataTableDto;
import vn.hbtplus.tax.income.constants.Constant;
import vn.hbtplus.repositories.BaseRepository;
import vn.hbtplus.tax.income.models.request.IncomeItemsRequest;
import vn.hbtplus.tax.income.models.response.IncomeItemsResponse;
import vn.hbtplus.tax.income.repositories.entity.IncomeTemplatesEntity;
import vn.hbtplus.utils.QueryUtils;
import vn.hbtplus.utils.Utils;

import java.util.*;

/**
 * Lop repository Impl ung voi bang pit_income_items
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Repository
@RequiredArgsConstructor
public class IncomeItemsRepository extends BaseRepository {

    public BaseDataTableDto searchData(IncomeItemsRequest.SearchForm dto) {
        StringBuilder sql = new StringBuilder("""
                select temp.income_item_id,
                       temp.income_template_id,
                       temp.code,
                       temp.name,
                       temp.salary_period_date,
                       temp.type,
                       temp.type_name,
                       case when temp.count > 0 then 1 else 0 end status,
                       case when temp.count > 0 then 'Đã sử dụng' else 'Chưa sử dụng' end status_name
                """);
        HashMap<String, Object> params = new HashMap<>();
        addCondition(sql, params, dto);
        return getListPagination(sql.toString(), params, dto, IncomeItemsResponse.class);
    }

    public List<Map<String, Object>> getListExport(IncomeItemsRequest.SearchForm dto) {
        StringBuilder sql = new StringBuilder("""
                select temp.income_item_id,
                       temp.code,
                       temp.name,
                       date_format(temp.salary_period_date, '%m/%Y') salary_period_date,
                       temp.type,
                       temp.type_name,
                       case when temp.count > 0 then 1 else 0 end status,
                       case when temp.count > 0 then 'Đã sử dụng' else 'Chưa sử dụng' end status_name
                """);
        Map<String, Object> params = new HashMap<>();
        addCondition(sql, params, dto);
        List<Map<String, Object>>  resultList = getListData(sql.toString(), params);
        if (Utils.isNullOrEmpty(resultList)) {
            resultList.add(getMapEmptyAliasColumns(sql.toString()));
        }
        return resultList;
    }

    private void addCondition(StringBuilder sql, Map<String, Object> params, IncomeItemsRequest.SearchForm dto) {
        sql.append("""
                FROM (
                    SELECT
                        item.income_item_id,
                        item.income_template_id,
                        item.code,
                        item.name,
                        item.salary_period_date,
                        template.type,
                        cat.name as type_name,
                        (select count(1) count from pit_income_item_masters master
                            where master.income_item_id = item.income_item_id and ifnull(master.is_deleted, :isDelete) = :isDelete) count
                    FROM pit_income_items item
                        inner join pit_income_templates template ON template.income_template_id = item.income_template_id
                        inner join sys_categories cat ON template.type = cat.value and IFNULL(cat.is_deleted, :isDelete) = :isDelete and cat.category_type = 'THUE_LOAI_THU_NHAP'
                    WHERE ifnull(item.is_deleted, :isDelete) = :isDelete
                ) temp WHERE 1=1
                """);

        params.put("isDelete", BaseConstants.STATUS.NOT_DELETED);

        QueryUtils.filter(dto.getListType(), sql, params, "temp.type");
        QueryUtils.filter(dto.getName(), sql, params, "temp.name");
        if (dto.getStatus() != null) {
            if (dto.getStatus() == 0) {
                sql.append(" AND temp.count = 0 ");
            } else {
                sql.append(" AND temp.count > 0 ");
            }
        }

        if (dto.getSalaryPeriodDate() != null) {
            sql.append(" AND date_format(temp.salary_period_date, '%m/%Y') = :salaryPeriodDate ");
            params.put("salaryPeriodDate", Utils.formatDate(dto.getSalaryPeriodDate(), Constant.SHORT_FORMAT_DATE));
        }

        sql.append(" ORDER BY temp.salary_period_date desc, temp.name, temp.code");
    }

    public List<IncomeItemsResponse> getDataBySalaryPeriod(String salaryPeriodDate, String isImport) {
        if (StringUtils.isBlank(salaryPeriodDate)) {
            return new ArrayList<>();
        }
        String sql = """
                SELECT
                    item.income_item_id,
                    item.code,
                    item.name
                FROM pit_income_items item
                WHERE IFNULL(item.is_deleted, :isDelete) = :isDelete and date_format(item.salary_period_date, '%m/%Y') = :salaryPeriodDate
                """;
        Map<String, Object> params = new HashMap<>();
        params.put("isDelete", BaseConstants.STATUS.NOT_DELETED);
        int index = salaryPeriodDate.indexOf("/");
        params.put("salaryPeriodDate", salaryPeriodDate.substring(index + 1));

        if (!Utils.isNullOrEmpty(isImport) && StringUtils.equals(BaseConstants.IS_IMPORT.NO, isImport)) {
            sql += """
                     AND NOT EXISTS (
                        SELECT 1 FROM pit_income_item_masters iim
                             INNER JOIN pit_income_items ii ON iim.income_item_id = ii.income_item_id AND IFNULL(ii.is_deleted, :isDelete) = :isDelete
                        WHERE IFNULL(iim.is_deleted, :isDelete) = :isDelete
                        AND date_format(ii.salary_period_date, '%m/%Y') = :salaryPeriodDate AND iim.income_item_id = item.income_item_id)
                    """;
        }
        return getListData(sql, params, IncomeItemsResponse.class);
    }

    public boolean checkDuplicateName(String name, Long incomeItemId, Date salaryPeriodDate) {
        String sql = """
                select count(1) from pit_income_items item
                where ifnull(item.is_deleted, :isDelete) = :isDelete
                    and date_format(item.salary_period_date, '%m/%Y') = :salaryPeriodDate
                    and lower(name) like :name
                """;
        Map<String, Object> params = new HashMap<>();
        params.put("isDelete", BaseConstants.STATUS.NOT_DELETED);
        params.put("salaryPeriodDate", Utils.formatDate(salaryPeriodDate, Constant.SHORT_FORMAT_DATE));
        params.put("name", name.toLowerCase());
        if (incomeItemId != null && incomeItemId > 0L) {
            sql += " AND income_item_id != :incomeItemId ";
            params.put("incomeItemId", incomeItemId);
        }

        Integer count = queryForObject(sql, params, Integer.class);
        return count > 0;
    }

    public String generateCodeByTemplate(String prefix) {
        String sql = """
                select max(SUBSTRING(code, :startWith))
                from pit_income_items where code like :prefix and ifnull(is_deleted, :isDelete) = :isDelete
                """;
        Map<String, Object> params = new HashMap<>();
        params.put("startWith", prefix.length() + 1);
        params.put("isDelete", BaseConstants.STATUS.NOT_DELETED);
        params.put("prefix", prefix);
        String max = queryForObject(sql, params, String.class);
        int result = StringUtils.isBlank(max) ? 1 : Integer.parseInt(max) + 1;
        return prefix + String.format("%02d", result);
    }

    public List<IncomeTemplatesEntity> getTemplateNotExisItems(Date periodDate) {
        String sql = "select a.* from pit_income_templates  a" +
                " where a.is_deleted = 'N'" +
                "   and not exists (" +
                "       select 1 from pit_income_items b" +
                "       where b.salary_period_date = :periodDate" +
                "       and b.income_template_id = a.income_template_id" +
                "       and b.is_deleted = 'N'" +
                "   )";
        Map<String, Object> params = new HashMap<>();
        params.put("periodDate", periodDate);
        return getListData(sql, params, IncomeTemplatesEntity.class);
    }

    public boolean checkUserIncomeItemById(Long incomeItemId) {
        String sql = """
                select count(1) count from pit_income_item_masters master
                where master.income_item_id = :incomeItemId and ifnull(master.is_deleted, :isDelete) = :isDelete
                """;
        Map<String, Object> params = new HashMap<>();
        params.put("incomeItemId", incomeItemId);
        params.put("isDelete", BaseConstants.STATUS.NOT_DELETED);

        Long result =   queryForObject(sql, params, Long.class);

        return result > 0L;
    }
}
