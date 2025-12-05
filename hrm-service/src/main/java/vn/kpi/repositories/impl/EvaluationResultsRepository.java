/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.kpi.repositories.impl;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Repository;
import vn.kpi.constants.BaseConstants;
import vn.kpi.constants.Constant;
import vn.kpi.constants.Scope;
import vn.kpi.feigns.PermissionFeignClient;
import vn.kpi.models.BaseDataTableDto;
import vn.kpi.models.BaseSearchRequest;
import vn.kpi.models.PermissionDataDto;
import vn.kpi.models.request.EmployeesRequest;
import vn.kpi.models.response.EvaluationResultsResponse;
import vn.kpi.repositories.BaseRepository;
import vn.kpi.repositories.entity.EvaluationResultsEntity;
import vn.kpi.services.AuthorizationService;
import vn.kpi.utils.QueryUtils;
import vn.kpi.utils.Utils;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Lop repository Impl ung voi bang hr_evaluation_results
 *
 * @author tudd
 * @version 1.0
 * @since 1.0
 */

@Repository
@RequiredArgsConstructor
public class EvaluationResultsRepository extends BaseRepository {
    private final PermissionFeignClient permissionFeignClient;
    private final HttpServletRequest request;
    private final AuthorizationService authorizationService;

    public BaseDataTableDto searchData(EmployeesRequest.SearchForm dto) {
        Pair<String, Map<String, Object>> pair = buildSql(dto);
        return getListPagination(pair.getLeft(), pair.getRight(), dto, EvaluationResultsResponse.SearchResult.class);
    }

    private Pair<String, Map<String, Object>> buildSql(EmployeesRequest.SearchForm dto) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    a.evaluation_result_id,
                    CAST(a.year AS CHAR) AS year,
                    a.evaluation_period_id,
                    a.employee_id,
                    e.employee_code,
                    e.full_name,
                    a.kpi_point,
                    a.kpi_result,
                    a.note,
                    a.is_deleted,
                    a.created_by,
                    a.created_time,
                    a.modified_by,
                    a.modified_time,
                    a.last_update_time,
                    o.full_name as orgName,
                    mj.name as jobName,
                    (select sc.name from sys_categories sc where sc.value = e.status and sc.category_type = :empStatus) empStatusName,
                    (select et.name from hr_emp_types et where et.emp_type_id = e.emp_type_id) empTypeName,
                    (select sc.name from sys_categories sc where sc.value = a.evaluation_type and sc.category_type = :evaluationTypeCode) evaluationTypeName,
                    (select ep.name from kpi_evaluation_periods ep where ep.evaluation_period_id = a.evaluation_period_id) evaluationPeriodName
                """);
        HashMap<String, Object> params = new HashMap<>();
        addCondition(sql, params, dto);

        return new MutablePair<>(sql.toString(), params);
    }

    public List<Map<String, Object>> getListExport(EmployeesRequest.SearchForm dto) {
        Pair<String, Map<String, Object>> pair = buildSql(dto);
        List<Map<String, Object>> dataList = getListData(pair.getLeft(), pair.getRight());

        if (Utils.isNullOrEmpty(dataList)) {
            dataList.add(getMapEmptyAliasColumns(pair.getLeft()));
        }
        return dataList;
    }

    private void addCondition(StringBuilder sql, Map<String, Object> params, EmployeesRequest.SearchForm dto) {
        sql.append("""
                    FROM hr_evaluation_results a
                    JOIN hr_employees e ON (e.employee_id = a.employee_id AND NVL(e.is_deleted, :activeStatus) = :activeStatus)
                    LEFT JOIN hr_jobs mj ON mj.job_id = e.job_id
                    JOIN hr_organizations o ON o.organization_id = e.organization_id
                    WHERE a.is_deleted = :activeStatus
                """);
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        params.put("empStatus", Constant.CATEGORY_CODES.HR_TRANG_THAI_NHAN_VIEN);
        params.put("evaluationTypeCode", Constant.CATEGORY_CODES.KPI_LOAI_DANH_GIA);
        UtilsEmployeeSearchRepository.setParamEmployeeSearch(dto, sql, params);
        if (dto.getListYear() != null && !dto.getListYear().isEmpty()) {
            QueryUtils.filterGe(dto.getListYear().get(0), sql, params, "a.year", "fromYear");
            QueryUtils.filterLe(dto.getListYear().size() > 1 ? dto.getListYear().get(1) : null, sql, params, "a.year", "toYear");
        }
        QueryUtils.filter(dto.getListEvaluationPeriod(), sql, params, "a.evaluation_period_id");
        List<PermissionDataDto> permissionDataDtos = authorizationService.getPermissionData(
                 Scope.VIEW, Constant.RESOURCES.EVALUATION_RESULTS, Utils.getUserNameLogin()
        );
        QueryUtils.addConditionPermission(permissionDataDtos, sql, params);
        sql.append(" ORDER BY o.path_order, mj.order_number, e.employee_id, a.year DESC");
    }

    public BaseDataTableDto<EvaluationResultsResponse.SearchResult> getTableList(Long employeeId, BaseSearchRequest request) {
        StringBuilder sql = new StringBuilder("""
                    SELECT
                        a.evaluation_result_id,
                        a.year,
                        a.evaluation_period_id,
                        a.employee_id,
                        a.kpi_point,
                        a.kpi_result,
                        a.note,
                        a.is_deleted,
                        a.created_by,
                        a.created_time,
                        a.modified_by,
                        a.modified_time,
                        a.last_update_time,
                        (select ep.name from kpi_evaluation_periods ep where ep.evaluation_period_id = a.evaluation_period_id) evaluationPeriodName,
                        (select sc.name from sys_categories sc where sc.value = a.evaluation_type and sc.category_type = :evaluationTypeCode) evaluationTypeName
                        FROM hr_evaluation_results a
                WHERE a.is_deleted = :activeStatus
                and a.employee_id = :employeeId
                order by a.year desc, a.evaluation_period_id desc
                    """);
        HashMap<String, Object> params = new HashMap<>();
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        params.put("evaluationTypeCode", Constant.CATEGORY_CODES.KPI_LOAI_DANH_GIA);
        params.put("employeeId", employeeId);
        return getListPagination(sql.toString(), params, request, EvaluationResultsResponse.SearchResult.class);
    }

    public EvaluationResultsResponse.DetailBean getLastEvaluation(Long employeeId) {
        String sql = """
                select ep.name evaluation_period_name, a.kpi_result, a.kpi_point
                from hr_evaluation_results a ,
                kpi_evaluation_periods ep
                where a.is_deleted = 'N'
                and ep.evaluation_period_id = a.evaluation_period_id
                and a.employee_id = :employeeId
                order by ep.end_date desc, a.year desc
                limit 1
                """;
        HashMap<String, Object> params = new HashMap<>();
        params.put("employeeId", employeeId);
        return getFirstData(sql, params, EvaluationResultsResponse.DetailBean.class);
    }

    public List<EvaluationResultsResponse.EvaluationPeriods> getListEvaluationPeriods(Integer year, String evaluationType) {
        StringBuilder sql = new StringBuilder("""
                    select a.*, a.evaluation_period_id value
                    from kpi_evaluation_periods a
                    where a.is_deleted = 'N'
                """);
        HashMap<String, Object> params = new HashMap<>();
        if (!Utils.isNullOrEmpty(evaluationType)) {
            sql.append(" and a.evaluation_type = :evaluationType");
            params.put("evaluationType", evaluationType);
        }
        QueryUtils.filter(year, sql, params, "a.year");
        sql.append(" ORDER BY a.end_date DESC, a.year DESC");
        return getListData(sql.toString(), params, EvaluationResultsResponse.EvaluationPeriods.class);
    }

    public List<EvaluationResultsEntity> getListEntities(Long periodId, List<String> empCodeList) {
        String sql = "select a.* from hr_evaluation_results a, hr_employees e " +
                     " where a.period_id = :periodId" +
                     " and a.is_deleted = 'N'" +
                     " and a.employee_id = e.employee_id" +
                     " and e.employee_code in (:empCodeList)";
        HashMap<String, Object> params = new HashMap<>();
        params.put("periodId", periodId);
        params.put("empCodeList", empCodeList);
        return getListData(sql, params, EvaluationResultsEntity.class);
    }
}
