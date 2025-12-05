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
import vn.kpi.exceptions.BaseAppException;
import vn.kpi.feigns.PermissionFeignClient;
import vn.kpi.models.BaseDataTableDto;
import vn.kpi.models.dto.ConcurrentProcessDto;
import vn.kpi.models.dto.EmployeeDto;
import vn.kpi.models.dto.EmployeeInfoDto;
import vn.kpi.models.dto.ParameterDto;
import vn.kpi.models.request.EmployeeEvaluationsRequest;
import vn.kpi.models.response.EmployeeEvaluationsResponse;
import vn.kpi.models.response.EmployeeIndicatorsResponse;
import vn.kpi.repositories.BaseRepository;
import vn.kpi.repositories.entity.EmployeeEvaluationsEntity;
import vn.kpi.repositories.entity.EmployeeWorkPlanningsEntity;
import vn.kpi.repositories.entity.OrganizationEvaluationsEntity;
import vn.kpi.services.AuthorizationService;
import vn.kpi.utils.QueryUtils;
import vn.kpi.utils.Utils;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Lop repository Impl ung voi bang kpi_employee_evaluations
 *
 * @author tudd
 * @version 1.0
 * @since 1.0
 */

@Repository
@RequiredArgsConstructor
public class EmployeeEvaluationsRepository extends BaseRepository {

    private final PermissionFeignClient permissionFeignClient;
    private final HttpServletRequest httpServletRequest;
    private final AuthorizationService authorizationService;

    public BaseDataTableDto searchData(EmployeeEvaluationsRequest.SearchForm dto) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    a.employee_evaluation_id,
                    a.evaluation_period_id,
                    a.employee_id,
                    e.employee_code,
                    e.full_name as employeeName,
                    ep.name as evaluation_period_name,
                    IFNULL(o.full_name, o.name) orgName,
                    mj.name jobName,
                    a.status,
                    a.self_total_point,
                    a.manager_total_point,
                    CASE
                        WHEN a.manager_total_point >= 4.2 THEN 'A1'
                        WHEN a.manager_total_point < 4.2 AND a.manager_total_point >= 3.4 THEN 'A2'
                        WHEN a.manager_total_point < 3.4 AND a.manager_total_point >= 2.6 THEN 'A3'
                        ELSE 'B'
                    END AS manager_grade,
                    a.final_point,
                    a.result_id,
                    a.final_result_id,
                    a.created_by,
                    a.created_time,
                    a.modified_by,
                    a.modified_time,
                    a.approved_by,
                    a.approved_time,
                    a.reason,
                    a.reason_request,
                    a.reason_manage_request,
                    (
                        select ho.name from kpi_approval_histories ah, hr_organizations ho
                        where ah.is_deleted = 'N'
                        and ho.organization_id = ah.approval_level
                        and ah.table_name = 'kpi_employee_evaluations'
                        and ah.object_id = a.employee_evaluation_id
                        order by ah.created_time desc
                        limit 1
                    ) as orgReviewName
                """);
        if (dto.getEmployeeId() == null) {
            sql.append(", case" +
                       "                        when EXISTS (" +
                       "                            SELECT 1 FROM hr_organizations op" +
                       "                            where op.organization_id in (:orgPermissionIds)" +
                       "                            and o.path_id like concat(op.path_id, '%')" +
                       "                        ) then 'N'" +
                       "                        else 'Y'" +
                       "                    end isConcurrent");
        }
        HashMap<String, Object> params = new HashMap<>();
        addCondition(sql, params, dto);
        return getListPagination(sql.toString(), params, dto, EmployeeEvaluationsResponse.SearchResult.class);
    }

    public List<EmployeeEvaluationsEntity> getListEntity(List<Long> listId) {
        StringBuilder sql = new StringBuilder(" SELECT a.* ");
        HashMap<String, Object> params = new HashMap<>();
        EmployeeEvaluationsRequest.SearchForm dto = new EmployeeEvaluationsRequest.SearchForm();
        dto.setIsEvaluation("Y");
        dto.setListId(listId);
        addCondition(sql, params, dto);
        return getListData(sql.toString(), params, EmployeeEvaluationsEntity.class);
    }

    public List<Map<String, Object>> getListExport(EmployeeEvaluationsRequest.SearchForm dto) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    a.employee_evaluation_id,
                    a.evaluation_period_id,
                    a.employee_id,
                    e.employee_code,
                    e.full_name as employeeName,
                    ep.name as evaluation_period_name,
                    IFNULL(o.full_name, o.name) orgName,
                    mj.name jobName,
                    a.status,
                    (select name from sys_categories sc where sc.code = a.status and sc.category_type = :statusCode) AS statusName,
                    a.self_total_point,
                    a.manager_total_point,
                    a.final_point,
                    a.result_id,
                    a.final_result_id,
                    a.created_by,
                    a.created_time,
                    a.modified_by,
                    a.modified_time,
                    a.approved_by,
                    a.approved_time,
                    a.reason,
                    a.reason_request,
                    a.reason_manage_request,
                    (
                        select ho.name from kpi_approval_histories ah, hr_organizations ho
                        where ah.is_deleted = 'N'
                        and ho.organization_id = ah.approval_level
                        and ah.table_name = 'kpi_employee_evaluations'
                        and ah.object_id = a.employee_evaluation_id
                        order by ah.created_time desc
                        limit 1
                    ) as orgReviewName
                """);
        if (dto.getEmployeeId() == null) {
            sql.append(", case" +
                       "                        when EXISTS (" +
                       "                            SELECT 1 FROM hr_organizations op" +
                       "                            where op.organization_id in (:orgPermissionIds)" +
                       "                            and o.path_id like concat(op.path_id, '%')" +
                       "                        ) then 'N'" +
                       "                        else 'Y'" +
                       "                    end isConcurrent");
        }
        Map<String, Object> params = new HashMap<>();
        params.put("statusCode", Constant.CATEGORY_TYPES.KPI_EMPLOYEE_EVALUATION_STATUS);
        addCondition(sql, params, dto);
        return getListData(sql.toString(), params);
    }

    public List<Map<String, Object>> getExportEmpSummary(EmployeeEvaluationsRequest.SearchForm dto) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                                    a.employee_evaluation_id,
                                    a.evaluation_period_id,
                                    a.employee_id,
                                    e.employee_code,
                                    e.full_name as employeeName,
                                    ep.name as evaluation_period_name,
                                    IFNULL(o.full_name, o.name) orgName,
                                    mj.name jobName,
                                    a.status,
                                    a.self_total_point,
                                    a.manager_total_point,
                                    CASE
                                        WHEN a.manager_total_point >= 4.2 THEN 'A1'
                                        WHEN a.manager_total_point < 4.2 AND a.manager_total_point >= 3.4 THEN 'A2'
                                        WHEN a.manager_total_point < 3.4 AND a.manager_total_point >= 2.6 THEN 'A3'
                                        ELSE 'B'
                                    END AS manager_grade,
                                    (select name from sys_categories sc where sc.code = a.status and sc.category_type ='KPI_EMPLOYEE_EVALUATION_STATUS') AS statusName,
                                    a.final_point,
                                    a.result_id,
                                    a.final_result_id
                """);
        Map<String, Object> params = new HashMap<>();
        addCondition(sql, params, dto);
        List results = getListData(sql.toString(), params);
        if (results.isEmpty()) {
            results.add(getMapEmptyAliasColumns(sql.toString()));
        }
        return results;
    }

    private void addCondition(StringBuilder sql, Map<String, Object> params, EmployeeEvaluationsRequest.SearchForm dto) {
        sql.append("""
                    FROM kpi_employee_evaluations a
                    JOIN hr_employees e ON e.employee_id = a.employee_id
                    JOIN hr_organizations o ON o.organization_id = a.organization_id
                    LEFT JOIN hr_jobs mj ON mj.job_id = e.job_id
                    JOIN kpi_evaluation_periods ep on a.evaluation_period_id = ep.evaluation_period_id
                    WHERE IFNULL(a.is_deleted, :activeStatus) = :activeStatus
                """);
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);

        if (dto.getEvaluationPeriodId() != null && dto.getEvaluationPeriodId() > 0) {
            sql.append(" and a.evaluation_period_id = :evaluationPeriodId ");
            params.put("evaluationPeriodId", dto.getEvaluationPeriodId());
        }
        if (!Utils.isNullObject(dto.getOrganizationId())) {
            sql.append(" AND o.path_id LIKE :orgId ");
            params.put("orgId", "%/" + dto.getOrganizationId() + "/%");
        }
        if (dto.getYear() != null && dto.getYear() > 0) {
            sql.append(" and ep.year = :year ");
            params.put("year", dto.getYear());
        }

        if (!Utils.isNullOrEmpty(dto.getKeySearch())) {
            sql.append(" and (ep.name LIKE :keySearch or e.full_name LIKE :keySearch or e.employee_code LIKE :keySearch or e.email LIKE :keySearch) ");
            params.put("keySearch", "%" + dto.getKeySearch() + "%");
        }
        if (dto.getEmployeeId() != null) {
            sql.append(" and a.employee_id = :employeeId ");
            params.put("employeeId", dto.getEmployeeId());
        } else {
            //add thong tin phan quyen
            List<Long> orgPermissionIds = authorizationService.getOrgHasPermission(Scope.VIEW, Constant.RESOURCES.EMPLOYEE_EVALUATIONS, Utils.getUserNameLogin());
            if (Utils.isNullOrEmpty(orgPermissionIds)) {
                sql.append(" and 0=1 ");
            } else {
                sql.append("""
                        AND EXISTS (
                            SELECT 1 FROM hr_organizations op
                            left join hr_organizations oc on FIND_IN_SET(oc.organization_id, a.org_concurrent_ids) > 0
                            where op.organization_id in (:orgPermissionIds)
                            and (
                                o.path_id like concat(op.path_id, '%')
                                or oc.path_id like concat(op.path_id, '%')
                            )
                        ) """);
            }
            params.put("orgPermissionIds", Utils.isNullOrEmpty(orgPermissionIds) ? List.of(-1L) : orgPermissionIds);
        }
        QueryUtils.filter(dto.getListId(), sql, params, "a.employee_evaluation_id");
        QueryUtils.filter(dto.getStatusList(), sql, params, "a.status");
        QueryUtils.filterEq(dto.getStatus(), sql, params, "a.status");

        if ("Y".equalsIgnoreCase(dto.getIsEvaluation())) {
            sql.append(" and a.status IN (:evaluationStatus) ");
            params.put("evaluationStatus", List.of(Constant.STATUS.PHE_DUYET, Constant.STATUS.DANH_GIA, Constant.STATUS.QLTT_DANH_GIA,
                    Constant.STATUS.YC_DANH_GIA_LAI, Constant.STATUS.CHO_QLTT_DANH_GIA, Constant.STATUS.DA_XAC_NHAN_KQ_DANH_GIA, Constant.STATUS.CHO_QLTT_DANH_GIA_LAI, Constant.STATUS.CHOT_KQ_DANH_GIA));
            sql.append(" ORDER BY IFNULL(a.manager_total_point, a.self_total_point) DESC ");
        } else if ("Y".equalsIgnoreCase(dto.getIsSynthetic())) {
            sql.append(" and a.status IN (:evaluationStatus) ");
            params.put("evaluationStatus", List.of(Constant.STATUS.DANH_GIA, Constant.STATUS.QLTT_DANH_GIA,
                    Constant.STATUS.CHO_QLTT_DANH_GIA, Constant.STATUS.DA_XAC_NHAN_KQ_DANH_GIA, Constant.STATUS.CHO_QLTT_DANH_GIA_LAI, Constant.STATUS.CHOT_KQ_DANH_GIA));
            sql.append(" ORDER BY IFNULL(a.manager_total_point, a.self_total_point) DESC ");
        }
    }


    public Map<Long, EmployeeEvaluationsResponse.EmpBean> getMapDataByOrg(Long id) {
        String sql = """
                SELECT a.employee_evaluation_id,
                       e.full_name AS employeeName,
                       ep.year as evaluationPeriodYear,
                       IFNULL(e.job_title, jb.name) AS jobName,
                       a.self_total_point,
                       a.manager_total_point,
                       case
                            WHEN o.path_level IN (1, 2)
                                 AND oe.emp_manager_id = a.employee_id then 'Y'
                            else 'N'
                        end isHeadLv2,
                       a.status
                FROM kpi_employee_evaluations a
                JOIN kpi_organization_evaluations oe ON oe.evaluation_period_id = a.evaluation_period_id
                JOIN hr_organizations o ON o.organization_id = oe.organization_id
                JOIN hr_organizations org ON org.organization_id = a.organization_id
                LEFT JOIN hr_employees e ON e.employee_id = a.employee_id
                LEFT JOIN hr_jobs jb ON e.job_id = jb.job_id
                LEFT JOIN kpi_evaluation_periods ep ON a.evaluation_period_id = ep.evaluation_period_id
                WHERE IFNULL(a.is_deleted, :activeStatus) = :activeStatus
                AND org.path_id like concat(o.path_id, '%')
                AND oe.organization_evaluation_id = :id
                """;
        Map<String, Object> params = new HashMap<>();
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        params.put("id", id);
        List<EmployeeEvaluationsResponse.EmpBean> data = getListData(sql, params, EmployeeEvaluationsResponse.EmpBean.class);
        return data.stream()
                .collect(Collectors.toMap(
                        EmployeeEvaluationsResponse.EmpBean::getEmployeeEvaluationId,
                        Function.identity(),
                        (existing, replacement) -> existing
                ));
    }


    public Map<Long, EmployeeDto> getMapEmpByIds(List<Long> employeeEvaluationIds) {
        String sql = """
                SELECT
                    scep.name as promotion_rank_name,
                    case
                        when mj.job_type = 'CONG_VIEC'
                        then mj.name
                        else wp.job_title
                    end jobName,
                    ke.employee_evaluation_id,
                    case
                        when mj.job_type = 'CHUC_VU'
                        then mj.name
                        else wp.job_title
                    end jobNameManage,
                    (select
                         GROUP_CONCAT(
                             CONCAT(
                                 (select sc.name from sys_categories sc where sc.value = hpp.position_title and sc.category_type = :positionTitle),
                                 ' - ',
                                 (select sc.name from sys_categories sc where sc.value = hpp.organization_type and sc.category_type = :organizationType)
                             ) SEPARATOR ', '
                         ) as positionConcurrent
                     from hr_political_participations hpp
                     where hpp.employee_id = e.employee_id
                           and hpp.is_deleted = 'N'
                           and DATE(now()) between hpp.start_date and ifnull(hpp.end_date, now())) as positionConcurrent
                FROM hr_employees e
                join kpi_employee_evaluations ke on e.employee_id = ke.employee_id
                left join hr_education_promotions ep on ep.employee_id = e.employee_id and ep.is_deleted = 'N'
                left join sys_categories scep on scep.category_type = :categoryTypeHocHam and ep.promotion_rank_id = scep.value
                left join hr_work_process wp on wp.employee_id = e.employee_id and DATE(now()) between wp.start_date and ifnull(wp.end_date, now()) and wp.is_deleted = 'N'
                left join hr_jobs mj on ifnull(wp.job_id, e.job_id) = mj.job_id
                where ke.employee_evaluation_id in (:employeeEvaluationIds)
                and not exists (
                	select 1 from hr_education_promotions ep1
                	where ep1.employee_id = ep.employee_id
                	and ep1.is_deleted = 'N'
                	and (
                		ep1.issued_year > ep.issued_year
                		or (ep1.issued_year = ep.issued_year and ep1.education_promotion_id > ep.education_promotion_id)
                	)
                )
                """;
        Map<String, Object> params = new HashMap<>();
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        params.put("employeeEvaluationIds", employeeEvaluationIds);
        params.put("organizationType", Constant.CATEGORY_TYPES.LOAI_TO_CHUC_CTR_XH);
        params.put("positionTitle", Constant.CATEGORY_TYPES.CHUC_DANH_CTR_XH);
        params.put("categoryTypeHocHam", Constant.CATEGORY_TYPES.HOC_HAM);
        List<EmployeeDto> data = getListData(sql, params, EmployeeDto.class);
        return data.stream()
                .collect(Collectors.toMap(
                        EmployeeDto::getEmployeeEvaluationId,
                        Function.identity(),
                        (existing, replacement) -> existing
                ));
    }

    public EmployeeEvaluationsResponse.EmpBean getExportEmployeeData(Long id) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    e.full_name AS employeeName,
                    ep.year as evaluationPeriodYear,
                    IFNULL(e.job_title, jb.name) AS jobName,
                    a.self_total_point,
                    a.manager_total_point,
                    case
                        when exists (
                        SELECT 1
                        FROM kpi_organization_evaluations oe
                        JOIN hr_organizations o on o.organization_id = oe.organization_id
                        WHERE oe.emp_manager_id = a.employee_id
                        AND oe.is_deleted = 'N'
                        AND o.path_level IN (2, 1)
                        ) then 'Y'
                        else 'N'
                    end isHeadLv2,
                    a.evaluation_period_id,
                    a.status
                FROM kpi_employee_evaluations a
                LEFT JOIN hr_employees e ON e.employee_id = a.employee_id
                LEFT JOIN hr_jobs jb ON e.job_id = jb.job_id
                LEFT JOIN kpi_evaluation_periods ep ON a.evaluation_period_id = ep.evaluation_period_id
                WHERE a.employee_evaluation_id = :id
                AND IFNULL(a.is_deleted, :activeStatus) = :activeStatus
                """);
        Map<String, Object> params = new HashMap<>();
        params.put("id", id);
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        return getFirstData(sql.toString(), params, EmployeeEvaluationsResponse.EmpBean.class);
    }

    public List<Map<String, Object>> getExportKpiData(Long id) {
        StringBuilder sql = new StringBuilder("""
                WITH target_data as (
                            SELECT koi.target, koi.indicator_id, koe.organization_id
                            FROM kpi_organization_indicators koi
                            JOIN kpi_organization_evaluations koe ON koi.organization_evaluation_id = koe.organization_evaluation_id
                            WHERE koi.is_deleted = :activeStatus
                            AND koe.status IN (:statusList)
                )
                
                SELECT
                    kei.percent,
                    kei.target,
                    CASE
                        WHEN kei.status = 'INACTIVE' THEN 'Xóa'
                        WHEN kei.status = 'ACTIVE' AND kei.old_percent IS NOT NULL THEN CONCAT('Điều chỉnh trọng số từ ', kei.old_percent, ' -> ', kei.percent)
                        ELSE 'Thêm mới'
                    END as note,
                    ki.name kpiName,
                    ki.rating_type,
                    kic.conversion_type,
                    kei.result,
                    kei.result_manage,
                    kei.manage_point,
                    kei.self_point,
                    (SELECT sc.name FROM sys_categories sc WHERE ki.unit_id = sc.value and sc.category_type = :donViTinh) unitName,
                    CASE
                        WHEN kic.conversion_type = 'DON_VI'
                        THEN COALESCE(
                           (
                             SELECT T.target
                             FROM target_data T
                             WHERE T.indicator_id = kei.indicator_id
                               AND (T.organization_id = a.organization_id
                                    OR FIND_IN_SET(T.organization_id, a.org_concurrent_ids) > 0)
                             LIMIT 1
                           ),
                           (
                             SELECT T.target
                             FROM target_data T
                             WHERE T.indicator_id = kei.indicator_id
                             AND T.organization_id = o.parent_id
                             LIMIT 1
                           )
                        )
                    END as targetStr,
                    GROUP_CONCAT(
                    CASE
                        WHEN kicd.min_comparison IS NOT NULL AND kicd.max_comparison IS NOT NULL THEN
                            CONCAT(
                                CASE
                                    WHEN kicd.min_comparison = 'GREATER_THAN_EQUAL' THEN CONCAT('>= ', kicd.min_value)
                                    WHEN kicd.min_comparison = 'LESS_THAN_EQUAL' THEN CONCAT('<= ', kicd.min_value)
                                    WHEN kicd.min_comparison = 'GREATER_THAN' THEN CONCAT('> ', kicd.min_value)
                                    WHEN kicd.min_comparison = 'LESS_THAN' THEN CONCAT('< ', kicd.min_value)
                                    WHEN kicd.min_comparison = 'EQUAL' THEN CONCAT('= ', kicd.min_value)
                                    ELSE ''
                                END,
                                ' và ',
                                CASE
                                    WHEN kicd.max_comparison = 'GREATER_THAN_EQUAL' THEN CONCAT('>= ', kicd.max_value, '#', kicd.result_id)
                                    WHEN kicd.max_comparison = 'LESS_THAN_EQUAL' THEN CONCAT('<= ', kicd.max_value, '#', kicd.result_id)
                                    WHEN kicd.max_comparison = 'GREATER_THAN' THEN CONCAT('> ', kicd.max_value, '#', kicd.result_id)
                                    WHEN kicd.max_comparison = 'LESS_THAN' THEN CONCAT('< ', kicd.max_value, '#', kicd.result_id)
                                    WHEN kicd.max_comparison = 'EQUAL' THEN CONCAT('= ', kicd.max_value, '#', kicd.result_id)
                                    ELSE ''
                                END
                            )
                        WHEN kicd.min_comparison IS NOT NULL THEN
                            CASE
                                WHEN kicd.min_comparison = 'GREATER_THAN_EQUAL' THEN CONCAT('>= ', kicd.min_value, '#', kicd.result_id)
                                WHEN kicd.min_comparison = 'LESS_THAN_EQUAL' THEN CONCAT('<= ', kicd.min_value, '#', kicd.result_id)
                                WHEN kicd.min_comparison = 'GREATER_THAN' THEN CONCAT('> ', kicd.min_value, '#', kicd.result_id)
                                WHEN kicd.min_comparison = 'LESS_THAN' THEN CONCAT('< ', kicd.min_value, '#', kicd.result_id)
                                WHEN kicd.min_comparison = 'EQUAL' THEN CONCAT('= ', kicd.min_value, '#', kicd.result_id)
                                ELSE NULL
                            END
                        WHEN kicd.max_comparison IS NOT NULL THEN
                            CASE
                                WHEN kicd.max_comparison = 'GREATER_THAN_EQUAL' THEN CONCAT('>= ', kicd.max_value, '#', kicd.result_id)
                                WHEN kicd.max_comparison = 'LESS_THAN_EQUAL' THEN CONCAT('<= ', kicd.max_value, '#', kicd.result_id)
                                WHEN kicd.max_comparison = 'GREATER_THAN' THEN CONCAT('> ', kicd.max_value, '#', kicd.result_id)
                                WHEN kicd.max_comparison = 'LESS_THAN' THEN CONCAT('< ', kicd.max_value, '#', kicd.result_id)
                                WHEN kicd.max_comparison = 'EQUAL' THEN CONCAT('= ', kicd.max_value, '#', kicd.result_id)
                                ELSE NULL
                            END
                        ELSE NULL
                    END ORDER BY kicd.result_id SEPARATOR '; '
                     ) as expressionList
                FROM kpi_employee_evaluations a
                JOIN kpi_employee_indicators kei ON kei.employee_evaluation_id = a.employee_evaluation_id AND kei.is_deleted = 'N'
                JOIN kpi_indicator_conversions kic ON kic.indicator_conversion_id = kei.indicator_conversion_id
                JOIN kpi_indicators ki ON kei.indicator_id = ki.indicator_id
                JOIN hr_organizations o on o.organization_id = a.organization_id
                LEFT JOIN kpi_indicator_conversion_details kicd ON kic.indicator_conversion_id = kicd.indicator_conversion_id and IFNULL(kicd.is_deleted, :activeStatus) = :activeStatus
                WHERE a.employee_evaluation_id = :id
                AND kei.status = 'ACTIVE'
                AND IFNULL(a.is_deleted, :activeStatus) = :activeStatus
                GROUP BY
                	kic.indicator_conversion_id
                """);
        Map<String, Object> params = new HashMap<>();
        params.put("id", id);
        params.put("donViTinh", Constant.CATEGORY_TYPES.DON_VI_TINH);
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        params.put("statusList", List.of(OrganizationEvaluationsEntity.STATUS.PHE_DUYET, OrganizationEvaluationsEntity.STATUS.DANH_GIA, OrganizationEvaluationsEntity.STATUS.QLTT_DANH_GIA));
        return getListData(sql.toString(), params);
    }


    public List<Map<String, Object>> getExportEmpWorkPlanningData(Long id) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    ep.year as evaluationPeriodYear,
                    kewp.content,
                    kewp.name
                FROM kpi_employee_evaluations a
                JOIN kpi_employee_work_plannings kewp ON kewp.employee_evaluation_id = a.employee_evaluation_id and kewp.is_deleted =:activeStatus
                LEFT JOIN kpi_evaluation_periods ep ON a.evaluation_period_id = ep.evaluation_period_id
                WHERE a.employee_evaluation_id = :id and a.is_deleted = :activeStatus
                """);
        Map<String, Object> params = new HashMap<>();
        params.put("id", id);
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        return getListData(sql.toString(), params);
    }


    public List<Long> getListOrgConcurrent(Long employeeId) {
        String sql = """
                select wp.organization_id as organizationId
                from hr_concurrent_process wp 
                where wp.employee_id = :employeeId
                and DATE(now()) between wp.start_date and ifnull(wp.end_date, now())
                and wp.is_deleted = :activeStatus
                """;
        Map<String, Object> params = new HashMap<>();
        params.put("employeeId", employeeId);
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        return getListData(sql, params, Long.class);
    }

    public EmployeeDto getEmployeeInfo(Long id) {
        String sql = """
                SELECT
                    scep.name as promotion_rank_name,
                    case
                        when mj.job_type = 'CONG_VIEC'
                        then mj.name
                        else wp.job_title
                    end jobName,
                    case
                        when mj.job_type = 'CHUC_VU'
                        then mj.name
                        else wp.job_title
                    end jobNameManage,
                    (select
                         GROUP_CONCAT(
                             CONCAT(
                                 (select sc.name from sys_categories sc where sc.value = hpp.position_title and sc.category_type = :positionTitle),
                                 ' - ',
                                 (select sc.name from sys_categories sc where sc.value = hpp.organization_type and sc.category_type = :organizationType)
                             ) SEPARATOR ', '
                         ) as positionConcurrent
                     from hr_political_participations hpp
                     where hpp.employee_id = e.employee_id
                           and hpp.is_deleted = 'N'
                           and DATE(now()) between hpp.start_date and ifnull(hpp.end_date, now())) as positionConcurrent
                FROM hr_employees e
                join kpi_employee_evaluations ke on e.employee_id = ke.employee_id
                left join hr_education_promotions ep on ep.employee_id = e.employee_id and ep.is_deleted = 'N'
                left join sys_categories scep on scep.category_type = :categoryTypeHocHam and ep.promotion_rank_id = scep.value
                left join hr_work_process wp on wp.employee_id = e.employee_id and DATE(now()) between wp.start_date and ifnull(wp.end_date, now()) and wp.is_deleted = 'N'
                left join hr_jobs mj on ifnull(wp.job_id, e.job_id) = mj.job_id
                where ke.employee_evaluation_id = :id
                and not exists (
                	select 1 from hr_education_promotions ep1
                	where ep1.employee_id = ep.employee_id
                	and ep1.is_deleted = 'N'
                	and (
                		ep1.issued_year > ep.issued_year
                		or (ep1.issued_year = ep.issued_year and ep1.education_promotion_id > ep.education_promotion_id)
                	)
                )
                """;
        Map<String, Object> params = new HashMap<>();
        params.put("id", id);
        params.put("organizationType", Constant.CATEGORY_TYPES.LOAI_TO_CHUC_CTR_XH);
        params.put("positionTitle", Constant.CATEGORY_TYPES.CHUC_DANH_CTR_XH);
        params.put("categoryTypeHocHam", Constant.CATEGORY_TYPES.HOC_HAM);
        return queryForObject(sql, params, EmployeeDto.class);
    }


    public List<EmployeeEvaluationsEntity> getDataByListId(List<Long> listId) throws BaseAppException {
        String sql = """
                SELECT a.*,
                       concat(e.employee_code,' - ', e.full_name) employeeName
                FROM kpi_employee_evaluations a
                JOIN hr_employees e on e.employee_id = a.employee_id
                WHERE a.is_deleted = :activeStatus
                AND a.employee_evaluation_id in (:listId)
                """;
        Map<String, Object> params = new HashMap<>();
        params.put("listId", listId);
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        return getListData(sql, params, EmployeeEvaluationsEntity.class);
    }

    public String getJobType(Long empId) throws BaseAppException {
        String sql = """
                SELECT f_get_current_job_type(:empId, now())
                FROM dual
                """;
        Map<String, Object> params = new HashMap<>();
        params.put("empId", empId);
        return getFirstData(sql, params, String.class);
    }

    ;


    public EmployeeInfoDto getEmployeeData(Long employeeId, ParameterDto dto) {
        String sql = """
                select
                	e.employee_id,
                	e.employee_code,
                	e.full_name,
                	e.email,
                	org.organization_id,
                	org.full_name organization_name,
                	org.org_level_manage,
                    case
                        when jb.job_type = 'CHUC_VU'
                        then jb.name
                    end positionName
                from hr_employees e
                left join hr_jobs jb on e.job_id = jb.job_id
                left join hr_organizations org on e.organization_id = org.organization_id
                where e.employee_id = :employeeId
                and e.is_deleted = :activeStatus
               
                """;
        Map mapParams = new HashMap();
        mapParams.put("employeeId", employeeId);
        mapParams.put("trinhDoDaoTao", Constant.CATEGORY_TYPES.TRINH_DO_DAO_TAO);
        mapParams.put("categoryTypeHocHam", Constant.CATEGORY_TYPES.HOC_HAM);
        mapParams.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        if (!Utils.isNullOrEmpty(dto.getListHeadId())) {
            sql = sql.replace("{fieldHead}", """
                    CASE WHEN jb.job_id IN (:listId) THEN 'Y'
                    ELSE 'N'
                    END as isHead
                    """);
            mapParams.put("listId", dto.getListHeadId());
        } else {
            sql = sql.replace("{fieldHead}", "");
        }

        if (!Utils.isNullOrEmpty(dto.getListProbationaryId())) {
            sql = sql.replace("{probationaryField}", """
                    CASE WHEN jb.job_id IN (:listProbationaryId) THEN 'Y'
                    ELSE 'N'
                    END as isProbationary,
                    """);
            mapParams.put("listProbationaryId", dto.getListProbationaryId());
        } else {
            sql = sql.replace("{probationaryField}", "");
        }

        if (!Utils.isNullOrEmpty(dto.getListJobFreeId())) {
            sql = sql.replace("{jobFreeField}", """
                    CASE WHEN jb.job_id IN (:listJobFreeId) THEN 'Y'
                    {jobFreeByOrgField}
                    ELSE 'N'
                    END as isJobFree,
                    """);
            if (!Utils.isNullOrEmpty(dto.getListJobFreeByOrgId()) && !Utils.isNullOrEmpty(dto.getListOrgId())) {
                StringBuilder sqlBuilder = new StringBuilder("""
                         WHEN jb.job_id IN (:listJobFreeByOrgId) AND ( 
                        """);
                for (int i = 0; i < dto.getListOrgId().size(); i++) {
                    String orgIdParam = "orgId" + i;
                    sqlBuilder.append(" org.path_id LIKE :").append(orgIdParam);
                    if (i < dto.getListOrgId().size() - 1) {
                        sqlBuilder.append(" OR");
                    }
                    mapParams.put(orgIdParam, "%/" + dto.getListOrgId().get(i) + "/%");
                }
                sqlBuilder.append(" ) THEN 'Y'");
                sql = sql.replace("{jobFreeByOrgField}", sqlBuilder.toString());
                mapParams.put("listJobFreeByOrgId", dto.getListJobFreeByOrgId());
            } else {
                sql = sql.replace("{jobFreeByOrgField}", "");
            }
            mapParams.put("listJobFreeId", dto.getListJobFreeId());
        } else {
            sql = sql.replace("{jobFreeField}", "");
        }


        return getFirstData(sql, mapParams, EmployeeInfoDto.class);
    }


    public Map<Long, EmployeeInfoDto> getMapEmployeeData(ParameterDto dto) {
        String sql = """
                select
                	e.employee_id,
                	e.employee_code,
                	e.full_name,
                	e.email,
                	org.full_name organization_name,
                	org.org_level_manage,
                	{jobFreeField}
                    case
                        when jb.job_type = 'CHUC_VU'
                        then jb.name
                    end positionName,
                    {probationaryField}
                	IFNULL((select name from sys_categories sc where sc.category_type = :trinhDoDaoTao and sc.`value` = ed.major_level_id),
                		ed.major_level_name) as major_level_name,
                	{fieldHead}
                	scep.name as promotion_rank_name
                from hr_employees e
                left join hr_jobs jb on e.job_id = jb.job_id
                left join hr_organizations org on e.organization_id = org.organization_id
                left join hr_education_degrees ed on ed.is_highest = 'Y' and ed.employee_id = e.employee_id and ed.is_deleted = 'N'
                left join hr_education_promotions ep on ep.employee_id = e.employee_id and ep.is_deleted = 'N'
                left join sys_categories scep on scep.category_type = :categoryTypeHocHam and ep.promotion_rank_id = scep.value
                where e.is_deleted = :activeStatus
                and not exists (
                    select 1 from hr_education_promotions ep1, sys_categories scep1
                    where ep1.employee_id = e.employee_id
                    and ep1.is_deleted = 'N'
                    and ep1.promotion_rank_id = scep1.value
                    and scep1.category_type = :categoryTypeHocHam
                    and scep1.order_number < scep.order_number
                )
                """;
        Map mapParams = new HashMap();
        mapParams.put("trinhDoDaoTao", Constant.CATEGORY_TYPES.TRINH_DO_DAO_TAO);
        mapParams.put("categoryTypeHocHam", Constant.CATEGORY_TYPES.HOC_HAM);
        mapParams.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        if (!Utils.isNullOrEmpty(dto.getListHeadId())) {
            sql = sql.replace("{fieldHead}", """
                    CASE WHEN jb.job_id IN (:listId) THEN 'Y'
                    ELSE 'N'
                    END as isHead,
                    """);
            mapParams.put("listId", dto.getListHeadId());
        } else {
            sql = sql.replace("{fieldHead}", "");
        }

        if (!Utils.isNullOrEmpty(dto.getListProbationaryId())) {
            sql = sql.replace("{probationaryField}", """
                    CASE WHEN jb.job_id IN (:listProbationaryId) THEN 'Y'
                    ELSE 'N'
                    END as isProbationary,
                    """);
            mapParams.put("listProbationaryId", dto.getListProbationaryId());
        } else {
            sql = sql.replace("{probationaryField}", "");
        }

        if (!Utils.isNullOrEmpty(dto.getListJobFreeId())) {
            sql = sql.replace("{jobFreeField}", """
                    CASE WHEN jb.job_id IN (:listJobFreeId) THEN 'Y'
                    {jobFreeByOrgField}
                    ELSE 'N'
                    END as isJobFree,
                    """);
            if (!Utils.isNullOrEmpty(dto.getListJobFreeByOrgId()) && !Utils.isNullOrEmpty(dto.getListOrgId())) {
                StringBuilder sqlBuilder = new StringBuilder("""
                         WHEN jb.job_id IN (:listJobFreeByOrgId) AND ( 
                        """);
                for (int i = 0; i < dto.getListOrgId().size(); i++) {
                    String orgIdParam = "orgId" + i;
                    sqlBuilder.append(" org.path_id LIKE :").append(orgIdParam);
                    if (i < dto.getListOrgId().size() - 1) {
                        sqlBuilder.append(" OR");
                    }
                    mapParams.put(orgIdParam, "%/" + dto.getListOrgId().get(i) + "/%");
                }
                sqlBuilder.append(" ) THEN 'Y'");
                sql = sql.replace("{jobFreeByOrgField}", sqlBuilder.toString());
                mapParams.put("listJobFreeByOrgId", dto.getListJobFreeByOrgId());
            } else {
                sql = sql.replace("{jobFreeByOrgField}", "");
            }
            mapParams.put("listJobFreeId", dto.getListJobFreeId());
        } else {
            sql = sql.replace("{jobFreeField}", "");
        }

        List<EmployeeInfoDto> listData = getListData(sql, mapParams, EmployeeInfoDto.class);
        Map<Long, EmployeeInfoDto> result = listData.stream()
                .collect(Collectors.toMap(
                        EmployeeInfoDto::getEmployeeId,
                        Function.identity(),
                        (existing, replacement) -> existing
                ));
        return result;
    }

    public Map<Long, List<EmployeeIndicatorsResponse.EmployeeEvaluation>> getMapEmpIndicator() {
        String sql = """
                WITH work_planning_data as (
                        select obj.*
                        from kpi_object_attributes obj
                        where obj.attribute_code = 'LA_KHCT'
                        and obj.is_deleted = 'N'
                        and obj.table_name = 'kpi_indicators'
                )
                
                
                SELECT a.*,
                       P.attribute_value as is_work_planning_index,
                       case
                        when idx.name = 'Mức độ hoàn thành kế hoạch công tác của đơn vị' then 'Y'
                        else 'N'
                       end as is_org
                FROM kpi_employee_indicators a
                JOIN kpi_indicators idx on a.indicator_id = idx.indicator_id
                LEFT JOIN work_planning_data P on P.object_id = a.indicator_id
                WHERE a.is_deleted = :activeStatus
                """;
        HashMap<String, Object> params = new HashMap<>();
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        List<EmployeeIndicatorsResponse.EmployeeEvaluation> data = getListData(sql, params, EmployeeIndicatorsResponse.EmployeeEvaluation.class);
        Map<Long, List<EmployeeIndicatorsResponse.EmployeeEvaluation>> result = data.stream()
                .collect(Collectors.groupingBy(
                        EmployeeIndicatorsResponse.EmployeeEvaluation::getEmployeeEvaluationId,
                        Collectors.mapping(Function.identity(), Collectors.toList())
                ));
        return result;
    }

    public Boolean checkGVVC(Long employeeId, String isGV) {
        StringBuilder sql = new StringBuilder("""
                SELECT count(1)
                FROM hr_work_process wp
                WHERE wp.is_deleted = :activeStatus
                and wp.employee_id = :employeeId
                and :date between wp.start_date and ifnull(wp.end_date,:date)
                and NOT EXISTS (
                    SELECT 1
                    FROM hr_concurrent_process cp
                    WHERE cp.is_deleted = :activeStatus
                    and cp.employee_id = :employeeId
                    and :date between cp.start_date and ifnull(cp.end_date,:date)
                )
                """);
        sql.append("Y".equalsIgnoreCase(isGV) ? " AND wp.job_id IN (:listJob) " : " AND wp.job_id NOT IN (:listJob) ");
        HashMap<String, Object> params = new HashMap<>();
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        params.put("employeeId", employeeId);
        params.put("date", Utils.truncDate(new Date()));
        params.put("listJob", List.of(43, 44, 45));  //id giảng viên
        return queryForObject(sql.toString(), params, Integer.class) > 0;
    }


    public List<EmployeeWorkPlanningsEntity> getListEmpWorkPlanning() {
        String sql = """
                SELECT a.*, e.employee_code, e.full_name, e.employee_id
                FROM kpi_employee_work_plannings a
                JOIN kpi_employee_evaluations b on a.employee_evaluation_id = b.employee_evaluation_id
                JOIN hr_employees e on b.employee_id = e.employee_id
                WHERE a.is_deleted = :activeStatus
                AND b.is_deleted = :activeStatus
                AND b.status IN (:statusList)
                """;
        HashMap<String, Object> params = new HashMap<>();
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
//        params.put("statusList", List.of(Constant.STATUS.DANH_GIA, Constant.STATUS.QLTT_DANH_GIA, Constant.STATUS.CHO_QLTT_DANH_GIA, Constant.STATUS.DA_XAC_NHAN_KQ_DANH_GIA, Constant.STATUS.CHO_QLTT_DANH_GIA_LAI));
        params.put("statusList", List.of(Constant.STATUS.QLTT_DANH_GIA, Constant.STATUS.DA_XAC_NHAN_KQ_DANH_GIA, Constant.STATUS.CHOT_KQ_DANH_GIA));
        return getListData(sql, params, EmployeeWorkPlanningsEntity.class);
    }

    public List<ConcurrentProcessDto> getListProcess(Long employeeId, Date date) {
        String sql = """
                    select mj.name as jobName,
                        ho.name as organizationName
                    FROM hr_concurrent_process a
                    LEFT JOIN hr_jobs mj ON mj.job_id = a.job_id
                    JOIN hr_organizations ho ON ho.organization_id = a.organization_id
                WHERE a.is_deleted = :activeStatus
                    and a.employee_id = :employeeId
                    and :date between a.start_date and ifnull(a.end_date,:date)
                    order by mj.order_number
                """;
        HashMap<String, Object> params = new HashMap<>();
        params.put("date", Utils.truncDate(date));
        params.put("employeeId", employeeId);
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        return getListData(sql, params, ConcurrentProcessDto.class);
    }

    public List<Map<String, Object>> getListValues() {
        String sql = """
                with v_kpi_organization_indicator as (
                	select a.* ,
                		REPLACE(JSON_EXTRACT(target, '$.m1'),'"','') muc_nguong,
                		REPLACE(JSON_EXTRACT(target, '$.m2'),'"','') muc_co_ban,
                		REPLACE(JSON_EXTRACT(target, '$.m3'),'"','') muc_day_manh,
                		id.name as ten_chi_so
                	from kpi_organization_indicators a,
                		kpi_indicators id
                	where a.is_deleted = 'N'
                	and a.indicator_id = id.indicator_id
                ),
                v_indicator_conversion_details as (
                select
                	id.indicator_conversion_id,
                	case
                		when id.min_comparison = 'EQUAL' and kp.rating_type = 'SELECT'
                		THEN CONCAT('#x == ''',id.min_value,'''')
                		when id.min_comparison = 'EQUAL'
                		then CONCAT('#x == ',id.min_value)
                		when id.min_comparison = 'GREATER_THAN_EQUAL'
                		then CONCAT('#x >=',id.min_value)
                		when id.min_comparison = 'GREATER_THAN'
                		then CONCAT('#x >',id.min_value)
                		when id.min_comparison = 'LESS_THAN_EQUAL'
                		then CONCAT('#x <=',id.min_value)
                		when id.min_comparison = 'LESS_THAN'
                		then CONCAT('#x <',id.min_value)
                	end min_value,
                	case
                		when id.max_comparison = 'EQUAL' and kp.rating_type = 'SELECT'
                		THEN CONCAT('#x == ''',id.max_value,'''')
                		when id.max_comparison = 'EQUAL'
                		then CONCAT('#x == ',id.max_value)
                		when id.max_comparison = 'GREATER_THAN_EQUAL'
                		then CONCAT('#x >=',id.max_value)
                		when id.max_comparison = 'GREATER_THAN'
                		then CONCAT('#x >',id.max_value)
                		when id.max_comparison = 'LESS_THAN_EQUAL'
                		then CONCAT('#x <=',id.max_value)
                		when id.max_comparison = 'LESS_THAN'
                		then CONCAT('#x <',id.max_value)
                	end max_value,
                	id.result_id
                from kpi_indicator_conversion_details id
                left join kpi_indicator_conversions ic on id.indicator_conversion_id = ic.indicator_conversion_id
                left join kpi_indicators kp on ic.indicator_id = kp.indicator_id
                where id.is_deleted = 'N')
                select
                	a.employee_indicator_id,
                	e.employee_code,
                	e.full_name,
                	kp.name as ten_chi_so,
                	kp.rating_type,
                	ic.is_focus_reduction,
                	a.target,
                	a.result,
                	a.self_point,
                	a.percent,
                	CONCAT_WS(' and ',id1.min_value, id1.max_value) as gia_tri_1,
                	CONCAT_WS(' and ',id2.min_value, id2.max_value) as gia_tri_2,
                	CONCAT_WS(' and ',id3.min_value, id3.max_value) as gia_tri_3,
                	CONCAT_WS(' and ',id4.min_value, id4.max_value) as gia_tri_4,
                	CONCAT_WS(' and ',id5.min_value, id5.max_value) as gia_tri_5,
                	od.muc_nguong,
                	od.muc_co_ban,
                	od.muc_day_manh,
                	case when ov.emp_manager_id = e.employee_id then 'Y' else 'N' end la_truong_don_vi
                FROM hr_employees e
                JOIN kpi_employee_evaluations ev ON e.employee_id = ev.employee_id
                left join kpi_organization_evaluations ov on ov.organization_evaluation_id = ev.organization_evaluation_id
                JOIN kpi_employee_indicators a ON a.employee_evaluation_id = ev.employee_evaluation_id
                left join kpi_indicator_conversions ic on a.indicator_conversion_id = ic.indicator_conversion_id
                LEFT JOIN v_indicator_conversion_details id1 ON a.indicator_conversion_id = id1.indicator_conversion_id and id1.result_id = '1'
                LEFT JOIN v_indicator_conversion_details id2 ON a.indicator_conversion_id = id2.indicator_conversion_id and id2.result_id = '2'
                LEFT JOIN v_indicator_conversion_details id3 ON a.indicator_conversion_id = id3.indicator_conversion_id and id3.result_id = '3'
                LEFT JOIN v_indicator_conversion_details id4 ON a.indicator_conversion_id = id4.indicator_conversion_id and id4.result_id = '4'
                LEFT JOIN v_indicator_conversion_details id5 ON a.indicator_conversion_id = id5.indicator_conversion_id and id5.result_id = '5'
                left join kpi_indicators kp on kp.indicator_id = a.indicator_id
                LEFT JOIN v_kpi_organization_indicator od
                  ON od.organization_evaluation_id = ev.organization_evaluation_id
                 AND od.ten_chi_so = kp.name
                 where a.is_deleted = 'N'
                 order by e.employee_code
                """;
        return getListData(sql, new HashMap<>());
    }

    public String getKPIConfig(Long employeeId, Long evaluationPeriodId) {
        String sql = "select f_get_min_num_of_kpi_emp(:employeeId, :evaluationPeriodId) from dual";
        Map<String, Object> params = new HashMap<>();
        params.put("employeeId", employeeId);
        params.put("evaluationPeriodId", evaluationPeriodId);
        return queryForObject(sql, params, String.class);
    }
}
