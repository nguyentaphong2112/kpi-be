/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.kpi.repositories.impl;

import org.springframework.stereotype.Repository;
import lombok.RequiredArgsConstructor;
import vn.kpi.constants.Constant;
import vn.kpi.models.BaseDataTableDto;
import vn.kpi.repositories.BaseRepository;
import vn.kpi.models.request.EvaluationPeriodsRequest;
import vn.kpi.models.response.EvaluationPeriodsResponse;
import vn.kpi.constants.BaseConstants;
import vn.kpi.utils.Utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Lop repository Impl ung voi bang kpi_evaluation_periods
 *
 * @author tudd
 * @version 1.0
 * @since 1.0
 */

@Repository
@RequiredArgsConstructor
public class EvaluationPeriodsRepository extends BaseRepository {

    public BaseDataTableDto searchData(EvaluationPeriodsRequest.SearchForm dto) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    a.evaluation_period_id,
                    a.year,
                    a.name,
                    a.evaluation_type,
                    a.start_date,
                    a.end_date,
                    a.status,
                    (select sc.name from sys_categories sc where sc.value = a.evaluation_type and sc.category_type = :evaluationType) evaluationTypeName
                """);
        HashMap<String, Object> params = new HashMap<>();
        addCondition(sql, params, dto);
        params.put("evaluationType", Constant.CATEGORY_TYPES.LOAI_DANH_GIA);
        return getListPagination(sql.toString(), params, dto, EvaluationPeriodsResponse.SearchResult.class);
    }

    public List<Map<String, Object>> getListExport(EvaluationPeriodsRequest.SearchForm dto) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    a.evaluation_period_id,
                    a.year,
                    a.name,
                    a.start_date,
                    a.end_date
                """);
        Map<String, Object> params = new HashMap<>();
        addCondition(sql, params, dto);
        return getListData(sql.toString(), params);
    }

    private void addCondition(StringBuilder sql, Map<String, Object> params, EvaluationPeriodsRequest.SearchForm dto) {
        sql.append("""
                    FROM kpi_evaluation_periods a
                    WHERE IFNULL(a.is_deleted, :activeStatus) = :activeStatus
                """);
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        if (!Utils.isNullOrEmpty(dto.getKeySearch())) {
            sql.append(" AND lower(a.name) like :keySearch");
            params.put("keySearch", "%" + dto.getKeySearch() + "%");
        }
        sql.append(" ORDER BY a.year desc");
    }

    public EvaluationPeriodsResponse.MaxYear getDataByMaxYear() {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    max(a.year) as year
                    FROM kpi_evaluation_periods a
                    WHERE IFNULL(a.is_deleted, :activeStatus) = :activeStatus
                """);
        HashMap<String, Object> params = new HashMap<>();
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        return getFirstData(sql.toString(), params, EvaluationPeriodsResponse.MaxYear.class);
    }

    public void initOrganization(Long id, String userName) {
        String sql = "call proc_init_organization_evaluation(:id, :userName)";
        HashMap<String, Object> params = new HashMap<>();
        params.put("id", id);
        params.put("userName", userName);
        executeSqlDatabase(sql, params);
    }

    public void initEmployee(Long id, String userName) {
        String sql = "call proc_init_employee_evaluation(:id, :userName)";
        HashMap<String, Object> params = new HashMap<>();
        params.put("id", id);
        params.put("userName", userName);
        executeSqlDatabase(sql, params);
    }

    public boolean checkEmployeeEvaluations(Long evaluationPeriodId,String status) {
        String sql = """
                    SELECT COUNT(1)
                    FROM kpi_employee_evaluations a
                    WHERE a.is_deleted = 'N'
                    AND a.evaluation_period_id = :evaluationPeriodId
                    AND a.status != :status
                """;
        HashMap<String, Object> params = new HashMap<>();
        params.put("status", status);
        params.put("evaluationPeriodId", evaluationPeriodId);
        return queryForObject(sql, params, Integer.class) > 0;
    };

    public boolean checkOrganizationEvaluations(Long evaluationPeriodId,String status) {
        String sql = """
                    SELECT COUNT(1)
                    FROM kpi_organization_evaluations a
                    WHERE a.is_deleted = 'N'
                    AND a.evaluation_period_id = :evaluationPeriodId
                    AND a.status != :status
                """;
        HashMap<String, Object> params = new HashMap<>();
        params.put("status", status);
        params.put("evaluationPeriodId", evaluationPeriodId);
        return queryForObject(sql, params, Integer.class) > 0;
    };
}
