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
import vn.kpi.models.BaseDataTableDto;
import vn.kpi.models.dto.CategoryDto;
import vn.kpi.models.request.OrganizationEvaluationsRequest;
import vn.kpi.models.response.OrganizationEvaluationsResponse;
import vn.kpi.repositories.BaseRepository;
import vn.kpi.repositories.entity.*;
import vn.kpi.services.AuthorizationService;
import vn.kpi.utils.QueryUtils;
import vn.kpi.utils.Utils;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Lop repository Impl ung voi bang kpi_organization_evaluations
 *
 * @author tudd
 * @version 1.0
 * @since 1.0
 */

@Repository
@RequiredArgsConstructor
public class OrganizationEvaluationsRepository extends BaseRepository {
    private final AuthorizationService authorizationService;

    public BaseDataTableDto<OrganizationEvaluationsResponse.SearchResult> searchData(OrganizationEvaluationsRequest.SearchForm dto) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    a.organization_evaluation_id,
                    a.organization_id,
                    o.full_name as organizationName,
                    o.org_name_level_1 as orgNameLevel1,
                    o.org_name_level_2 as orgNameLevel2,
                    o.org_name_level_3 as orgNameLevel3,
                    a.evaluation_period_id,
                    a.status,
                    a.emp_manager_id,
                    o.org_type_id,
                    e.full_name as emp_manager_name,
                    e.employee_code as emp_manager_code,
                    ep.name as evaluation_period_name,
                    a.self_total_point,
                    a.manager_total_point,
                    CASE
                        WHEN a.manager_total_point >= 4.2 THEN 'A1'
                        WHEN a.manager_total_point < 4.2 AND a.manager_total_point >= 3.4 THEN 'A2'
                        WHEN a.manager_total_point < 3.4 AND a.manager_total_point >= 2.6 THEN 'A3'
                        ELSE 'B'
                    END AS manager_grade,
                    a.result_id,
                    a.final_result_id,
                    a.final_point,
                    a.created_by,
                    a.created_time,
                    a.modified_by,
                    a.modified_time,
                    a.approved_by,
                    a.approved_time,
                    a.reason,
                    a.reason_request,
                    o.path_level
                    from hr_organizations o,kpi_organization_evaluations a
                    left join hr_employees e on e.employee_id = a.emp_manager_id
                    join kpi_evaluation_periods ep on a.evaluation_period_id = ep.evaluation_period_id
                """);
        HashMap<String, Object> params = new HashMap<>();
        addCondition(sql, params, dto);
        return getListPagination(sql.toString(), params, dto, OrganizationEvaluationsResponse.SearchResult.class);
    }

    public List<Map<String, Object>> getListExport(OrganizationEvaluationsRequest.SearchForm dto) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    a.organization_evaluation_id,
                    a.organization_id,
                    o.full_name as organizationName,
                    o.org_name_level_1 as orgNameLevel1,
                    o.org_name_level_2 as orgNameLevel2,
                    o.org_name_level_3 as orgNameLevel3,
                    a.evaluation_period_id,
                    a.status,
                    (select name from sys_categories sc where sc.code = a.status and sc.category_type = :statusCode) AS statusName,
                    a.emp_manager_id,
                    e.full_name as emp_manager_name,
                    ep.name as evaluation_period_name,
                    a.self_total_point,
                    a.manager_total_point,
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
                    o.path_level
                    from hr_organizations o,kpi_organization_evaluations a
                    left join hr_employees e on e.employee_id = a.emp_manager_id
                    join kpi_evaluation_periods ep on a.evaluation_period_id = ep.evaluation_period_id
                """);
        Map<String, Object> params = new HashMap<>();
        params.put("statusCode", Constant.CATEGORY_TYPES.KPI_ORGANIZATION_EVALUATION_STATUS);
        addCondition(sql, params, dto);
        return getListData(sql.toString(), params);
    }

    public List<Map<String, Object>> getExportOrgSummary(OrganizationEvaluationsRequest.SearchForm dto) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    a.organization_evaluation_id,
                    a.organization_id,
                    o.full_name as organizationName,
                    o.org_name_level_1 as orgNameLevel1,
                    o.org_name_level_2 as orgNameLevel2,
                    o.org_name_level_3 as orgNameLevel3,
                    a.evaluation_period_id,
                    a.status,
                    a.emp_manager_id,
                    o.org_type_id,
                    e.full_name as emp_manager_name,
                    ep.name as evaluation_period_name,
                    a.self_total_point,
                    a.manager_total_point,
                    (select name from sys_categories sc where sc.code = a.status and sc.category_type = :statusCode) AS statusName,
                    CASE
                        WHEN a.manager_total_point >= 4.2 THEN 'A1'
                        WHEN a.manager_total_point < 4.2 AND a.manager_total_point >= 3.4 THEN 'A2'
                        WHEN a.manager_total_point < 3.4 AND a.manager_total_point >= 2.6 THEN 'A3'
                        ELSE 'B'
                    END AS manager_grade,
                    a.result_id,
                    a.final_result_id,
                    a.final_point
                    from hr_organizations o,kpi_organization_evaluations a
                    left join hr_employees e on e.employee_id = a.emp_manager_id
                    join kpi_evaluation_periods ep on a.evaluation_period_id = ep.evaluation_period_id
                """);
        Map<String, Object> params = new HashMap<>();
        params.put("statusCode", Constant.CATEGORY_TYPES.KPI_ORGANIZATION_EVALUATION_STATUS);
        addCondition(sql, params, dto);
        List results = getListData(sql.toString(), params);
        if (results.isEmpty()) {
            results.add(getMapEmptyAliasColumns(sql.toString()));
        }
        return results;
    }

    private void addCondition(StringBuilder sql, Map<String, Object> params, OrganizationEvaluationsRequest.SearchForm dto) {
        sql.append("""
                    WHERE o.organization_id = a.organization_id AND
                    IFNULL(a.is_deleted, :activeStatus) = :activeStatus
                """);
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        QueryUtils.filterOrg(dto.getOrganizationId(), sql, params, "o.path_id");
        QueryUtils.filter(dto.getEvaluationPeriodId(), sql, params, "a.evaluation_period_id");
        QueryUtils.filter(dto.getYear(), sql, params, "ep.year");
        QueryUtils.filter(dto.getKeySearch(), sql, params, "ep.name", "o.full_name");
        QueryUtils.filter(dto.getOrgTypeIdList(), sql, params, "o.org_type_id");
        if (!Utils.isNullOrEmpty(dto.getLevel())) {
            sql.append(" AND f_get_kpi_level(o.organization_id, o.org_type_id, null) like :kpiLevel");
            params.put("kpiLevel", dto.getLevel());
        }
        //add thong tin phan quyen
        List<Long> orgPermissionIds = authorizationService.getOrgHasPermission(Scope.VIEW, Constant.RESOURCES.ORGANIZATION_EVALUATION, Utils.getUserNameLogin());
        if (Utils.isNullOrEmpty(orgPermissionIds)) {
            sql.append(" and 0=1 ");
        } else {
            sql.append("""
                    
                    AND EXISTS (
                        SELECT 1 FROM hr_organizations op
                        where op.organization_id in (:orgPermissionIds)
                        and o.path_id like concat(op.path_id, '%')
                    ) """);
            params.put("orgPermissionIds", orgPermissionIds);
        }
        QueryUtils.filter(dto.getListId(), sql, params, "a.organization_evaluation_id");
        QueryUtils.filter(dto.getStatusList(), sql, params, "a.status");
        QueryUtils.filter(dto.getOrgIdList(), sql, params, "a.organization_id");
        QueryUtils.filterEq(dto.getStatus(), sql, params, "a.status");
        if ("Y".equalsIgnoreCase(dto.getIsEvaluation())) {
            sql.append(" and a.status IN (:evaluationStatus) ");
            params.put("evaluationStatus", List.of(Constant.STATUS.PHE_DUYET, Constant.STATUS.DANH_GIA, Constant.STATUS.QLTT_DANH_GIA,
                    Constant.STATUS.YC_DANH_GIA_LAI, Constant.STATUS.CHO_QLTT_DANH_GIA, Constant.STATUS.DA_XAC_NHAN_KQ_DANH_GIA, Constant.STATUS.CHO_QLTT_DANH_GIA_LAI, Constant.STATUS.CHOT_KQ_DANH_GIA));
            sql.append(" ORDER BY IFNULL(a.manager_total_point, a.self_total_point) DESC ");
        } else if ("Y".equalsIgnoreCase(dto.getIsSynthetic())) {
//            sql.append(" and a.status IN (:evaluationStatus) ");
//            params.put("evaluationStatus", List.of(Constant.STATUS.DANH_GIA, Constant.STATUS.QLTT_DANH_GIA,
//                    Constant.STATUS.CHO_QLTT_DANH_GIA, Constant.STATUS.DA_XAC_NHAN_KQ_DANH_GIA, Constant.STATUS.CHO_QLTT_DANH_GIA_LAI, Constant.STATUS.CHOT_KQ_DANH_GIA));
            sql.append(" ORDER BY IFNULL(a.manager_total_point, a.self_total_point) DESC ");
        }
    }

    public OrganizationEvaluationsEntity getDataById(Long id) {
        String sql = """
                SELECT a.*,
                       concat(e.employee_code, ' - ', e.full_name) empManagerName,
                       e.employee_code empManagerCode,
                       o.name orgName,
                       o.org_level_manage
                FROM kpi_organization_evaluations a
                LEFT JOIN hr_employees e ON a.emp_manager_id = e.employee_id
                LEFT JOIN hr_organizations o ON o.organization_id = a.organization_id
                WHERE IFNULL(a.is_deleted, :activeStatus) = :activeStatus
                AND organization_evaluation_id = :id
                """;
        Map<String, Object> params = new HashMap<>();
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        params.put("id", id);
        return getFirstData(sql, params, OrganizationEvaluationsEntity.class);
    }

    public List<CategoryDto> getCategoryByListCode(List<String> groupCodes) {
        StringBuilder sql = new StringBuilder("""
                SELECT c.*, ca.attribute_value
                FROM sys_categories c
                JOIN sys_category_attributes ca on c.category_id = ca.category_id
                WHERE IFNULL(c.is_deleted, :activeStatus) = :activeStatus
                and c.category_type = :categoryType
                and ca.attribute_code = :attributeCode
                """);
        Map<String, Object> params = new HashMap<>();
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        params.put("categoryType", Constant.CATEGORY_TYPES.KPI_PHAN_NHOM);
        params.put("attributeCode", Constant.ATTRIBUTE_CODE.ORG_IDS);
        QueryUtils.filter(groupCodes, sql, params, "c.code");
        return getListData(sql.toString(), params, CategoryDto.class);
    }

    public List<Map<String, Object>> getExportOrgData(Long id) {
        StringBuilder sql = new StringBuilder("""
                with v_work_process as (
                    select 
                    	wp.employee_id,
                    	sc.`name` trinh_do,
                    	org.path_id,
                    	(
                    		select sum(months_between(LEAST(IFNULL(wp1.end_date,:endDate), :endDate), wp1.start_date))
                    		from hr_work_process wp1, hr_document_types dt1
                    		where wp1.employee_id = wp.employee_id
                    		and wp1.is_deleted = 'N'
                    		and wp1.document_type_id = dt1.document_type_id
                    		and wp1.start_date <= :endDate
                    	) as tham_nien
                    from hr_organizations org,
                    hr_document_types dt,
                    hr_work_process wp
                    left join hr_education_degrees ed on wp.employee_id = ed.employee_id
                    	and ed.is_deleted = 'N'
                    left join sys_categories sc on sc.`value` = ed.major_level_id and sc.category_type = 'TRINH_DO_DAO_TAO'
                    where wp.organization_id = org.organization_id
                    and DATE(:endDate) BETWEEN wp.start_date and IFNULL(wp.end_date, DATE(:endDate))
                    and wp.is_deleted = 'N'
                    and not exists (
                    		select 1 from hr_education_degrees a1
                    		left join sys_categories sc1 on sc1.`value` = a1.major_level_id and sc1.category_type = 'TRINH_DO_DAO_TAO'
                    		where a1.employee_id = wp.employee_id
                    		and (
                    			sc1.order_number < sc.order_number
                    			or (sc1.order_number = sc.order_number
                    				and a1.education_degree_id > ed.education_degree_id
                    			)
                    		)
                    )
                    and wp.document_type_id = dt.document_type_id
                    and dt.type <> 'OUT'
                )
                SELECT
                    ep.year as evaluationPeriodYear,
                    e.name AS orgName,
                    e.name as ten_don_vi,
                    e.org_level_manage,
                    e.org_type_id,
                    a.self_total_point,
                    a.manager_total_point,
                    a.organization_id,
                    a.status,
                    a.evaluation_period_id,
                    (
                        select count(*) from v_work_process wp
                        where wp.path_id like concat(e.path_id, '%')
                    ) as so_nhan_vien,
                    (
                        select count(*) from v_work_process wp
                        where wp.path_id like concat(e.path_id, '%')
                        and wp.trinh_do like 'Tiến sỹ'
                    ) as so_tien_sy,
                    (
                        select count(*) from v_work_process wp
                        where wp.path_id like concat(e.path_id, '%')
                        and wp.trinh_do like 'Thạc sỹ'
                    ) as so_thac_sy,
                    (
                        select count(*) from v_work_process wp
                        where wp.path_id like concat(e.path_id, '%')
                        and wp.trinh_do like 'Đại học'
                    ) as so_cu_nhan,
                    (
                        select count(*) from v_work_process wp
                        where wp.path_id like concat(e.path_id, '%')
                        and ifnull(wp.trinh_do,'N/A') not in ('Tiến sỹ','Thạc sỹ','Đại học')
                    ) as so_trinh_do_khac,
                    (
                        select count(*) from v_work_process wp
                        where wp.path_id like concat(e.path_id, '%')
                        and wp.tham_nien < 5*12
                    ) as so_tham_nien_duoi_5,
                    (
                        select count(*) from v_work_process wp
                        where wp.path_id like concat(e.path_id, '%')
                        and wp.tham_nien >= 5*12
                    ) as so_tham_nien_tren_5
                FROM kpi_organization_evaluations a
                LEFT JOIN hr_organizations e ON e.organization_id = a.organization_id
                LEFT JOIN kpi_evaluation_periods ep ON a.evaluation_period_id = ep.evaluation_period_id
                WHERE a.organization_evaluation_id = :id
                AND IFNULL(a.is_deleted, :activeStatus) = :activeStatus
                """);
        Map<String, Object> params = new HashMap<>();
        params.put("id", id);
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        params.put("endDate", new Date());
        return getListData(sql.toString(), params);
    }

    public List<Map<String, Object>> getExportKpiData(Long id) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    koi.percent,
                    koi.target,
                    kicd.result_id,
                    CASE
                      WHEN koi.status = 'INACTIVE' THEN 'Xóa'
                      WHEN koi.status = 'ACTIVE' AND koi.old_percent IS NOT NULL THEN CONCAT('Điều chỉnh trọng số từ ', koi.old_percent, ' -> ', koi.percent)
                      ELSE 'Thêm mới'
                    END as note,
                    ki.name kpiName,
                    ki.rating_type,
                    ki.type,
                    kic.conversion_type,
                    koi.result,
                    koi.result_manage,
                    koi.manage_point,
                    koi.self_point,
                    (SELECT sc.name FROM sys_categories sc WHERE ki.unit_id = sc.value and sc.category_type = :donViTinh) unitName,
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
                FROM kpi_organization_evaluations a
                JOIN kpi_organization_indicators koi ON koi.organization_evaluation_id = a.organization_evaluation_id and IFNULL(koi.is_deleted, :activeStatus) = :activeStatus
                JOIN kpi_indicator_conversions kic ON kic.indicator_conversion_id = koi.indicator_conversion_id and IFNULL(kic.is_deleted, :activeStatus) = :activeStatus
                JOIN kpi_indicators ki ON koi.indicator_id = ki.indicator_id and IFNULL(ki.is_deleted, :activeStatus) = :activeStatus
                LEFT JOIN kpi_indicator_conversion_details kicd ON kic.indicator_conversion_id = kicd.indicator_conversion_id and IFNULL(kicd.is_deleted, :activeStatus) = :activeStatus
                WHERE a.organization_evaluation_id = :id
                AND IFNULL(a.is_deleted, :activeStatus) = :activeStatus
                GROUP BY
                  kic.indicator_conversion_id
                """);
        Map<String, Object> params = new HashMap<>();
        params.put("id", id);
        params.put("donViTinh", Constant.CATEGORY_TYPES.DON_VI_TINH);
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        return getListData(sql.toString(), params);
    }

    public List<Map<String, Object>> getInitial(OrganizationEvaluationsRequest.SearchForm dto) {
        StringBuilder sql = new StringBuilder("""
                WITH org_indicators AS (
                    SELECT a.*, i.name
                    FROM kpi_organization_indicators a
                    JOIN kpi_indicators i ON a.indicator_id = i.indicator_id
                    WHERE a.is_deleted = 'N'
                )
                
                select
                    o.full_name,
                    koe.organization_evaluation_id,
                        (
                    SELECT
                        COUNT(a.organization_indicator_id)
                    from
                        org_indicators a
                    where
                        a.organization_evaluation_id = koe.organization_evaluation_id) as totalKpi,
                
                        koe.manager_total_point,
                
                        koe.self_total_point,
                
                        koe.final_point,
                
                        koe.result_id,
                
                        koe.final_result_id
                
                """);
        Map<String, Object> params = new HashMap<>();
        addConditionAggregate(sql, params, dto);
        return getListData(sql.toString(), params);
    }


    public Map<Long, Map<String, Object>> getTyLeVuotDayManh(OrganizationEvaluationsRequest.SearchForm dto) {
        StringBuilder sql = new StringBuilder("""
                WITH org_indicators AS (
                    SELECT a.*, i.name
                    FROM kpi_organization_indicators a
                    JOIN kpi_indicators i ON a.indicator_id = i.indicator_id
                    WHERE a.is_deleted = 'N'
                ),
                cte_vuot_chi_tieu AS (
                    SELECT
                        a.organization_evaluation_id,
                        COUNT(*) AS total,
                        SUM(
                            CASE
                                WHEN REGEXP_REPLACE(JSON_UNQUOTE(JSON_EXTRACT(a.target, '$.m3')), '[^0-9.]', '') != ''
                                     AND CAST(a.result_manage AS DECIMAL(20, 5)) >
                                         CAST(REGEXP_REPLACE(JSON_UNQUOTE(JSON_EXTRACT(a.target, '$.m3')), '[^0-9.]', '') AS DECIMAL(20, 5))
                                THEN 1
                                ELSE 0
                            END
                        ) AS vuot_chi_tieu
                    FROM org_indicators a
                    GROUP BY a.organization_evaluation_id)
                
                SELECT *,
                    RANK() OVER (ORDER BY CAST(data AS DECIMAL(20, 5)) DESC) AS xep_loai
                FROM (
                    SELECT
                        koe.organization_evaluation_id,
                        ROUND(
                            (SELECT vct.vuot_chi_tieu * 100.0 / NULLIF(vct.total, 0)
                             FROM cte_vuot_chi_tieu vct
                             WHERE vct.organization_evaluation_id = koe.organization_evaluation_id), 2
                        ) AS data
                """);
        Map<String, Object> params = new HashMap<>();
        addConditionAggregate(sql, params, dto);
        sql.append(") t");
        return getResult(getListData(sql.toString(), params));
    }


    public Map<Long, Map<String, Object>> getTongTrongSoVuotDayManh(OrganizationEvaluationsRequest.SearchForm dto) {
        StringBuilder sql = new StringBuilder("""
                WITH org_indicators AS (
                    SELECT a.*, i.name
                    FROM kpi_organization_indicators a
                    JOIN kpi_indicators i ON a.indicator_id = i.indicator_id
                    WHERE a.is_deleted = 'N'
                )
                
                SELECT *,
                    RANK() OVER (ORDER BY CAST(data AS DECIMAL(20, 5)) DESC) AS xep_loai
                    FROM (
                    SELECT
                        koe.organization_evaluation_id,
                        IFNULL((
                            SELECT SUM(a.percent)
                            FROM org_indicators a
                            WHERE
                                a.organization_evaluation_id = koe.organization_evaluation_id
                                AND REGEXP_REPLACE(JSON_UNQUOTE(JSON_EXTRACT(a.target, '$.m3')), '[^0-9.]', '') != ''
                                AND CAST(a.result_manage AS DECIMAL(20, 5)) >
                                    CAST(REGEXP_REPLACE(JSON_UNQUOTE(JSON_EXTRACT(a.target, '$.m3')), '[^0-9.]', '') AS DECIMAL(20, 5))
                        ), 0) AS data
                """);
        Map<String, Object> params = new HashMap<>();
        addConditionAggregate(sql, params, dto);
        sql.append(") t");
        return getResult(getListData(sql.toString(), params));
    }


    public Map<Long, Map<String, Object>> getMucVuotVuotDayManh(OrganizationEvaluationsRequest.SearchForm dto) {
        StringBuilder sql = new StringBuilder("""
                WITH org_indicators AS (
                    SELECT a.*, i.name
                    FROM kpi_organization_indicators a
                    JOIN kpi_indicators i ON a.indicator_id = i.indicator_id
                    WHERE a.is_deleted = 'N'
                )
                
                SELECT t.organization_evaluation_id,
                       ROUND(t.data, 2) AS data,
                       RANK() OVER (
                           ORDER BY CAST(ROUND(t.data, 2) AS DECIMAL(20, 5)) DESC
                       ) AS xep_loai
                FROM (
                       SELECT
                           koe.organization_evaluation_id,
                           IFNULL((
                               SELECT
                                   SUM(
                                       (CAST(a.result_manage AS DECIMAL(20, 5)) -
                                        CAST(REGEXP_REPLACE(JSON_UNQUOTE(JSON_EXTRACT(a.target, '$.m3')), '[^0-9.]', '') AS DECIMAL(20, 5)))
                                       /
                                       NULLIF(CAST(REGEXP_REPLACE(JSON_UNQUOTE(JSON_EXTRACT(a.target, '$.m3')), '[^0-9.]', '') AS DECIMAL(20, 5)), 0)
                                   )
                               FROM org_indicators a
                               WHERE
                                   a.organization_evaluation_id = koe.organization_evaluation_id
                                   AND REGEXP_REPLACE(JSON_UNQUOTE(JSON_EXTRACT(a.target, '$.m3')), '[^0-9.]', '') NOT IN ('', '0')
                                   AND CAST(a.result_manage AS DECIMAL(20, 5)) >
                                       CAST(REGEXP_REPLACE(JSON_UNQUOTE(JSON_EXTRACT(a.target, '$.m3')), '[^0-9.]', '') AS DECIMAL(20, 5))
                           ), 0) AS data
                """);
        Map<String, Object> params = new HashMap<>();
        addConditionAggregate(sql, params, dto);
        sql.append(") t");
        return getResult(getListData(sql.toString(), params));
    }


    public Map<Long, Map<String, Object>> getKHCTDonVi(OrganizationEvaluationsRequest.SearchForm dto) {
        StringBuilder sql = new StringBuilder("""
                WITH org_indicators AS (
                    SELECT a.*, i.name
                    FROM kpi_organization_indicators a
                    JOIN kpi_indicators i ON a.indicator_id = i.indicator_id
                    WHERE a.is_deleted = 'N'
                )
                
                SELECT *,
                    RANK() OVER (
                        ORDER BY CAST(t.data AS DECIMAL(20, 5)) DESC
                    ) AS xep_loai
                FROM (
                    SELECT
                        koe.organization_evaluation_id,
                        IFNULL((
                            SELECT a.result_manage
                            FROM org_indicators a
                            WHERE a.organization_evaluation_id = koe.organization_evaluation_id
                              AND a.name = 'Mức độ hoàn thành kế hoạch công tác của đơn vị'
                            LIMIT 1
                        ), 0) AS data
                """);
        Map<String, Object> params = new HashMap<>();
        addConditionAggregate(sql, params, dto);
        sql.append(") t");
        return getResult(getListData(sql.toString(), params));
    }


    public Map<Long, Map<String, Object>> getTyLeVuotMucKHCT(OrganizationEvaluationsRequest.SearchForm dto) {
        StringBuilder sql = new StringBuilder("""
                WITH org_work_planning as (
                             SELECT a.*
                             FROM kpi_organization_work_plannings  a
                             WHERE a.is_deleted = 'N'
                 )
                
                SELECT t.organization_evaluation_id,
                    ROUND(t.ty_le_vuot, 2) AS data,
                    RANK() OVER (
                        ORDER BY CAST(ROUND(t.ty_le_vuot, 2) AS DECIMAL(10,2)) DESC
                    ) AS xep_loai
                FROM (
                    SELECT
                        koe.organization_evaluation_id,
                        (
                            SELECT
                                COUNT(CASE WHEN jt.managePoint > 100 THEN 1 END) * 100.0 /
                                NULLIF(COUNT(jt.managePoint), 0)
                            FROM org_work_planning a
                            CROSS JOIN JSON_TABLE(
                                a.content,
                                '$[*]' COLUMNS (
                                    managePoint DECIMAL(10,2) PATH '$.managePoint'
                                )
                            ) AS jt
                            WHERE a.organization_evaluation_id = koe.organization_evaluation_id
                        ) AS ty_le_vuot
                """);
        Map<String, Object> params = new HashMap<>();
        addConditionAggregate(sql, params, dto);
        sql.append(") t");
        return getResult(getListData(sql.toString(), params));
    }


    public Map<Long, Map<String, Object>> getTyLeKhongHoanThanhKHCT(OrganizationEvaluationsRequest.SearchForm dto) {
        StringBuilder sql = new StringBuilder("""
                WITH org_work_planning as (
                             SELECT a.*
                             FROM kpi_organization_work_plannings  a
                             WHERE a.is_deleted = 'N'
                 )
                
                SELECT t.organization_evaluation_id,
                       ROUND(t.ty_le_khong_hoan_thanh, 2) AS data,
                       RANK() OVER (
                           ORDER BY CAST(ROUND(t.ty_le_khong_hoan_thanh, 2) AS DECIMAL(10, 2))
                       ) AS xep_loai
                   FROM (
                       SELECT
                           koe.organization_evaluation_id,
                           (
                               SELECT
                                   COUNT(CASE WHEN jt.managePoint < 100 THEN 1 END) * 100.0 /
                                   NULLIF(COUNT(jt.managePoint), 0)
                               FROM org_work_planning a
                               CROSS JOIN JSON_TABLE(
                                   a.content,
                                   '$[*]' COLUMNS (
                                       managePoint DECIMAL(20, 5) PATH '$.managePoint'
                                   )
                               ) jt
                               WHERE a.organization_evaluation_id = koe.organization_evaluation_id
                           ) AS ty_le_khong_hoan_thanh
                """);
        Map<String, Object> params = new HashMap<>();
        addConditionAggregate(sql, params, dto);
        sql.append(") t");
        return getResult(getListData(sql.toString(), params));
    }


    public Map<Long, Map<String, Object>> getTongSoGioGiang(OrganizationEvaluationsRequest.SearchForm dto) {
        StringBuilder sql = new StringBuilder("""
                WITH org_work_planning as (
                             SELECT a.*
                             FROM kpi_organization_work_plannings  a
                             WHERE a.is_deleted = 'N'
                 ),
                idx AS (
                    SELECT 0 AS i UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4
                    UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9
                    UNION ALL SELECT 10 UNION ALL SELECT 11 UNION ALL SELECT 12 UNION ALL SELECT 13 UNION ALL SELECT 14
                    UNION ALL SELECT 15 UNION ALL SELECT 16 UNION ALL SELECT 17 UNION ALL SELECT 18 UNION ALL SELECT 19
                    UNION ALL SELECT 20 UNION ALL SELECT 21 UNION ALL SELECT 22 UNION ALL SELECT 23 UNION ALL SELECT 24
                    UNION ALL SELECT 25 UNION ALL SELECT 26 UNION ALL SELECT 27 UNION ALL SELECT 28 UNION ALL SELECT 29
                    UNION ALL SELECT 30 UNION ALL SELECT 31 UNION ALL SELECT 32 UNION ALL SELECT 33 UNION ALL SELECT 34
                    UNION ALL SELECT 35 UNION ALL SELECT 36 UNION ALL SELECT 37 UNION ALL SELECT 38 UNION ALL SELECT 39
                    UNION ALL SELECT 40 UNION ALL SELECT 41 UNION ALL SELECT 42 UNION ALL SELECT 43 UNION ALL SELECT 44
                    UNION ALL SELECT 45 UNION ALL SELECT 46 UNION ALL SELECT 47 UNION ALL SELECT 48 UNION ALL SELECT 49
                )
                
                SELECT t.*,
                      RANK() OVER (
                          ORDER BY CAST(t.data AS DECIMAL(10, 2)) DESC
                      ) AS xep_loai
                  FROM (
                      SELECT
                          koe.organization_evaluation_id,
                          ROUND((
                              SELECT SUM(
                                  CASE
                                      WHEN JSON_UNQUOTE(JSON_EXTRACT(a.content, CONCAT('$[', idx.i, '].unit'))) = '1'
                                           AND JSON_EXTRACT(a.content, CONCAT('$[', idx.i, '].resultManage')) IS NOT NULL
                                      THEN CAST(JSON_EXTRACT(a.content, CONCAT('$[', idx.i, '].resultManage')) AS DECIMAL(20,5))
                                      ELSE 0
                                  END
                              )
                              FROM org_work_planning a
                              JOIN idx ON 1=1
                              WHERE a.organization_evaluation_id = koe.organization_evaluation_id
                          ), 2) AS data
                """);
        Map<String, Object> params = new HashMap<>();
        addConditionAggregate(sql, params, dto);
        sql.append(") t");
        return getResult(getListData(sql.toString(), params));
    }


    public Map<Long, Map<String, Object>> getVuotMuc(OrganizationEvaluationsRequest.SearchForm dto, Long indicatorId) {
        StringBuilder sql = new StringBuilder("""
                WITH org_indicators as (
                                            SELECT a.*,i.name
                                            FROM kpi_organization_indicators a
                                            JOIN kpi_indicators i on a.indicator_id = i.indicator_id
                                            WHERE a.is_deleted = 'N'
                                )
                
                SELECT t.*,
                      RANK() OVER (
                          ORDER BY CAST(t.data AS DECIMAL(10, 2)) DESC
                      ) AS xep_loai
                  FROM (
                      SELECT
                          koe.organization_evaluation_id,
                          ROUND((
                              SELECT
                                  CASE
                                      WHEN REGEXP_REPLACE(JSON_UNQUOTE(JSON_EXTRACT(a.target, '$.m3')), '[^0-9.]', '') REGEXP '^[0-9]+\\\\.?[0-9]*$'
                                           AND REGEXP_REPLACE(JSON_UNQUOTE(JSON_EXTRACT(a.target, '$.m3')), '[^0-9.]', '') NOT IN ('', '0')
                                      THEN SUM(((CAST(a.result_manage AS DECIMAL(20,5)) - CAST(REGEXP_REPLACE(JSON_UNQUOTE(JSON_EXTRACT(a.target, '$.m3')), '[^0-9.]', '') AS DECIMAL(20,5))) * 100.0) / CAST(REGEXP_REPLACE(JSON_UNQUOTE(JSON_EXTRACT(a.target, '$.m3')), '[^0-9.]', '') AS DECIMAL(20,5)))
                                      ELSE 0
                                  END
                              FROM org_indicators a
                              WHERE a.organization_evaluation_id = koe.organization_evaluation_id
                                  AND CAST(a.result_manage AS DECIMAL(20,5)) > CAST(REGEXP_REPLACE(JSON_UNQUOTE(JSON_EXTRACT(a.target, '$.m3')), '[^0-9.]', '') AS DECIMAL(20,5))
                                  AND a.indicator_id = :indicatorId
                                  AND REGEXP_REPLACE(JSON_UNQUOTE(JSON_EXTRACT(a.target, '$.m3')), '[^0-9.]', '') NOT IN ('', '0')
                          ), 2) AS data
                
                """);
        Map<String, Object> params = new HashMap<>();
        addConditionAggregate(sql, params, dto);
        sql.append(") t");
        params.put("indicatorId", indicatorId);
        return getResult(getListData(sql.toString(), params));
    }


    public Map<Long, Map<String, Object>> getGeneralData(OrganizationEvaluationsRequest.SearchForm dto, List<Long> indicatorIds) {
        StringBuilder sql = new StringBuilder("""
                WITH org_indicators as (
                                            SELECT a.*,i.name
                                            FROM kpi_organization_indicators a
                                            JOIN kpi_indicators i on a.indicator_id = i.indicator_id
                                            WHERE a.is_deleted = 'N'
                                ),
                 valid_organizations AS (
                     SELECT o.organization_id
                     FROM hr_organizations o
                     WHERE
                         o.parent_id IN (18, 71)
                         OR (
                             o.parent_id IN (
                                 SELECT oa.organization_id
                                 FROM hr_organizations oa
                                 WHERE oa.parent_id IN (18, 71)
                             )
                             AND o.name LIKE 'Bộ môn%'
                         )
                 )
                
                SELECT t.*,
                      RANK() OVER (
                          ORDER BY CAST(t.data AS DECIMAL(10,2)) DESC
                      ) AS xep_loai
                  FROM (
                      SELECT
                          koe.organization_evaluation_id,
                          IFNULL(ROUND((
                              SELECT a.result_manage
                              FROM org_indicators a
                              WHERE
                                  a.organization_evaluation_id = koe.organization_evaluation_id
                                  AND a.indicator_id IN (:indicatorIds)
                                  AND koe.organization_id IN (SELECT organization_id FROM valid_organizations)
                              LIMIT 1
                          ), 2), 0) AS data
                
                """);
        Map<String, Object> params = new HashMap<>();
        addConditionAggregate(sql, params, dto);
        sql.append(") t");
        params.put("indicatorIds", indicatorIds);
        return getResult(getListData(sql.toString(), params));
    }


    public Map<Long, Map<String, Object>> getSoGiangVienBaoVeLATS(OrganizationEvaluationsRequest.SearchForm dto) {
        StringBuilder sql = new StringBuilder("""
                WITH org_indicators as (
                                            SELECT a.*,i.name
                                            FROM kpi_organization_indicators a
                                            JOIN kpi_indicators i on a.indicator_id = i.indicator_id
                                            WHERE a.is_deleted = 'N'
                 ),
                 org_work_planning as (
                             SELECT a.*
                             FROM kpi_organization_work_plannings  a
                             WHERE a.is_deleted = 'N'
                 ),
                 idx AS (
                     SELECT 0 AS i UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4
                     UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9
                     UNION ALL SELECT 10 UNION ALL SELECT 11 UNION ALL SELECT 12 UNION ALL SELECT 13 UNION ALL SELECT 14
                     UNION ALL SELECT 15 UNION ALL SELECT 16 UNION ALL SELECT 17 UNION ALL SELECT 18 UNION ALL SELECT 19
                     UNION ALL SELECT 20 UNION ALL SELECT 21 UNION ALL SELECT 22 UNION ALL SELECT 23 UNION ALL SELECT 24
                     UNION ALL SELECT 25 UNION ALL SELECT 26 UNION ALL SELECT 27 UNION ALL SELECT 28 UNION ALL SELECT 29
                     UNION ALL SELECT 30 UNION ALL SELECT 31 UNION ALL SELECT 32 UNION ALL SELECT 33 UNION ALL SELECT 34
                     UNION ALL SELECT 35 UNION ALL SELECT 36 UNION ALL SELECT 37 UNION ALL SELECT 38 UNION ALL SELECT 39
                     UNION ALL SELECT 40 UNION ALL SELECT 41 UNION ALL SELECT 42 UNION ALL SELECT 43 UNION ALL SELECT 44
                     UNION ALL SELECT 45 UNION ALL SELECT 46 UNION ALL SELECT 47 UNION ALL SELECT 48 UNION ALL SELECT 49
                 ),
                 valid_organizations AS (
                     SELECT o.organization_id
                     FROM hr_organizations o
                     WHERE
                         o.parent_id IN (18, 71)
                         OR (
                             o.parent_id IN (
                                 SELECT oa.organization_id
                                 FROM hr_organizations oa
                                 WHERE oa.parent_id IN (18, 71)
                             )
                             AND o.name LIKE 'Bộ môn%'
                         )
                 )
                
                SELECT t.*,
                       RANK() OVER (
                           ORDER BY CAST(t.data AS DECIMAL(10,2)) DESC
                       ) AS xep_loai
                   FROM (
                       SELECT
                           koe.organization_evaluation_id,
                
                           IFNULL(ROUND((
                               SELECT a.result_manage
                               FROM org_indicators a
                               WHERE
                                   a.organization_evaluation_id = koe.organization_evaluation_id
                                   AND a.indicator_id IN (32, 110, 274, 472)
                                   AND koe.organization_id IN (SELECT organization_id FROM valid_organizations)
                               LIMIT 1
                           ), 2), 0)
                           +
                           IFNULL(ROUND((
                               SELECT SUM(
                                   CASE
                                       WHEN JSON_UNQUOTE(JSON_EXTRACT(a.content, CONCAT('$[', idx.i, '].name'))) = '- Đạt chuẩn PGS, GS'
                                            AND JSON_EXTRACT(a.content, CONCAT('$[', idx.i, '].resultManage')) IS NOT NULL
                                       THEN CAST(JSON_EXTRACT(a.content, CONCAT('$[', idx.i, '].resultManage')) AS DECIMAL(20,5))
                                       ELSE 0
                                   END
                               )
                               FROM org_work_planning a
                               JOIN idx ON 1=1
                               WHERE a.organization_evaluation_id = koe.organization_evaluation_id
                           ), 2), 0) AS data
                
                """);
        Map<String, Object> params = new HashMap<>();
        addConditionAggregate(sql, params, dto);
        sql.append(") t");
        return getResult(getListData(sql.toString(), params));
    }

    public OrganizationEvaluationsEntity getOrgEvaluationLevel1(Long periodId) {
        String sql = """
                SELECT a.*
                FROM kpi_organization_evaluations a
                JOIN hr_organizations o ON a.organization_id = o.organization_id
                WHERE a.is_deleted = :activeStatus
                AND o.org_level_manage = 1
                AND a.evaluation_period_id = :periodId
                """;
        Map<String, Object> params = new HashMap<>();
        params.put("periodId", periodId);
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        return getFirstData(sql, params, OrganizationEvaluationsEntity.class);
    }

    public Map<Long, Map<String, Object>> getResult(List<Map<String, Object>> data) {
        return data.stream()
                .filter(row -> row.get("organization_evaluation_id") != null)
                .collect(Collectors.toMap(
                        row -> ((Number) row.get("organization_evaluation_id")).longValue(),
                        row -> row
                ));
    }

    private void addConditionAggregate(StringBuilder sql, Map<String, Object> params, OrganizationEvaluationsRequest.SearchForm dto) {
        sql.append("""
                
                FROM
                	kpi_organization_evaluations koe
                JOIN hr_organizations o ON
                	koe.organization_id = o.organization_id
                where
                	koe.is_deleted = 'N'
                AND koe.manager_total_point >= 4.2
                AND NOT EXISTS (
                SELECT
                		1
                FROM
                		kpi_organization_indicators koi
                WHERE
                		koi.is_deleted = 'N'
                	and koi.organization_evaluation_id = koe.organization_evaluation_id
                	and koi.manage_point = 2
                )
                AND (
                    EXISTS (
                SELECT
                		1
                FROM
                		kpi_organization_evaluations koe1
                JOIN hr_organizations o1 ON
                		koe1.organization_id = o1.organization_id
                WHERE
                		koe1.is_deleted = 'N'
                	AND o1.parent_id = o.organization_id
                	AND koe1.evaluation_period_id = koe.evaluation_period_id
                	AND koe1.manager_total_point >= 4.2
                    )
                OR
                
                    (
                      NOT EXISTS (
                SELECT
                		1
                FROM
                		kpi_organization_evaluations koe1
                JOIN hr_organizations o1 ON
                		koe1.organization_id = o1.organization_id
                WHERE
                		koe1.is_deleted = 'N'
                	AND o1.parent_id = o.organization_id
                	AND koe1.evaluation_period_id = koe.evaluation_period_id
                      )
                AND EXISTS (
                SELECT
                		1
                FROM
                		kpi_employee_evaluations kee
                WHERE
                		kee.is_deleted = 'N'
                	AND kee.evaluation_period_id = koe.evaluation_period_id
                	AND kee.manager_total_point >= 4.2
                      )
                    )
                  )
                AND koe.status IN ('QLTT_DANH_GIA', 'DA_XAC_NHAN_KQ_DANH_GIA', 'CHOT_KQ_DANH_GIA')
                
                """);
        QueryUtils.filter(dto.getEvaluationPeriodId(), sql, params, "koe.evaluation_period_id");
        QueryUtils.filter(dto.getOrgIdList(), sql, params, "koe.organization_id");
    }


    public List<Map<String, Object>> getExportKpiOrgInvalid(OrganizationEvaluationsRequest.SearchForm dto) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    o.full_name don_vi,
                    CONCAT_WS(' ',
                        IF(koe.manager_total_point < 4.2,
                           CONCAT('• Điểm KPI < 4.2 (', koe.manager_total_point, '); '),
                           ''),
                        IF(kpi_below_threshold.kpi_vi_pham > 0,
                           CONCAT('• Số KPI ở dưới mức “Ngưỡng” > 0 (', kpi_below_threshold.kpi_vi_pham, '); '),
                           ''),
                        IF(a1_count.a1_total = 0,
                           '• Không có đơn vị cấu thành hoặc nhân viên đạt mức A1;',
                           '')
                    ) AS ly_do
                FROM
                    kpi_organization_evaluations koe
                JOIN hr_organizations o ON koe.organization_id = o.organization_id
                
                
                LEFT JOIN (
                    SELECT
                        koi.organization_evaluation_id,
                        COUNT(*) AS kpi_vi_pham
                    FROM kpi_organization_indicators koi
                    WHERE koi.is_deleted = 'N' AND koi.manage_point = 2
                    GROUP BY koi.organization_evaluation_id
                ) kpi_below_threshold ON kpi_below_threshold.organization_evaluation_id = koe.organization_evaluation_id
                
                
                LEFT JOIN (
                    SELECT
                        parent_org.organization_id AS parent_id,
                        koe1.evaluation_period_id,
                        COUNT(*) AS a1_total
                    FROM kpi_organization_evaluations koe1
                    JOIN hr_organizations child_org ON koe1.organization_id = child_org.organization_id
                    JOIN hr_organizations parent_org ON child_org.parent_id = parent_org.organization_id
                    WHERE koe1.is_deleted = 'N' AND koe1.manager_total_point >= 4.2
                    GROUP BY parent_org.organization_id, koe1.evaluation_period_id
                
                    UNION ALL
                
                    SELECT
                        o.organization_id,
                        kee.evaluation_period_id,
                        COUNT(*) AS a1_total
                    FROM kpi_employee_evaluations kee
                    JOIN hr_organizations o ON 1=1 
                    WHERE kee.is_deleted = 'N' AND kee.manager_total_point >= 4.2
                    GROUP BY o.organization_id, kee.evaluation_period_id
                ) a1_count ON a1_count.parent_id = o.organization_id
                           AND a1_count.evaluation_period_id = koe.evaluation_period_id
                WHERE
                    koe.is_deleted = 'N'
                    AND koe.status IN ('QLTT_DANH_GIA', 'DA_XAC_NHAN_KQ_DANH_GIA', 'CHOT_KQ_DANH_GIA')
                    AND (
                            koe.manager_total_point < 4.2
                            OR IFNULL(kpi_below_threshold.kpi_vi_pham, 0) > 0
                            OR IFNULL(a1_count.a1_total, 0) = 0
                        )
                
                """);
        Map<String, Object> params = new HashMap<>();
        QueryUtils.filter(dto.getOrgIds(), sql, params, "koe.organization_id");
        QueryUtils.filter(dto.getEvaluationPeriodId(), sql, params, "koe.evaluation_period_id");
        sql.append(" GROUP BY o.organization_id ");
        return getListData(sql.toString(), params);
    }

    public List<Map<String, Object>> getExportEmpWorkPlanningData(Long id) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    ep.year as evaluationPeriodYear,
                    kowp.content
                FROM kpi_organization_evaluations a
                JOIN kpi_organization_work_plannings kowp  ON kowp.organization_evaluation_id = a.organization_evaluation_id  and kowp.is_deleted =:activeStatus
                LEFT JOIN kpi_evaluation_periods ep ON a.evaluation_period_id = ep.evaluation_period_id
                WHERE a.organization_evaluation_id = :id and a.is_deleted = :activeStatus
                """);
        Map<String, Object> params = new HashMap<>();
        params.put("id", id);
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        return getListData(sql.toString(), params);
    }

    public boolean checkOrg(List<Long> orgIds, Long approvalLevel) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    count(1)
                FROM hr_organizations o
                WHERE o.organization_id = :approvalLevel
                AND o.is_deleted = :activeStatus
                """);
        Map<String, Object> params = new HashMap<>();
        if (!orgIds.isEmpty()) {
            sql.append("AND (");
            for (int i = 0; i < orgIds.size(); i++) {
                sql.append("o.path_id LIKE :orgId").append(i);
                if (i < orgIds.size() - 1) {
                    sql.append(" OR ");
                }
                params.put("orgId" + i, "%/" + orgIds.get(i) + "/%");
            }
            sql.append(")");
        }
        params.put("approvalLevel", approvalLevel);
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        return queryForObject(sql.toString(), params, Integer.class) > 0;
    }

    public List<OrganizationEvaluationsResponse.OrganizationDto> getOrgParent(Long periodId, List<Long> orgIds) {
        String sql = """
                select org.organization_id as organizationId,
                    org.name as organizationName
                from hr_organizations org
                where exists (
                    select 1 from kpi_organization_evaluations kp
                    where kp.organization_id = org.organization_id
                    and kp.is_deleted = :activeStatus
                    and kp.evaluation_period_id = :periodId
                )
                and exists (
                    select 1 from hr_organizations op
                    where op.organization_id in (:orgIds)
                    and op.path_id like concat(org.path_id, '%')
                )
                order by org.path_order desc
                """;
        Map<String, Object> params = new HashMap<>();
        params.put("periodId", periodId);
        params.put("orgIds", orgIds);
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        return getListData(sql, params, OrganizationEvaluationsResponse.OrganizationDto.class);
    }

    public Long getManagedOrganization(Long employeeId, Long evaluationPeriodId) {
        String sql = """
                select o.organization_id as organizationId
                from kpi_organization_evaluations o, hr_organizations org
                where o.evaluation_period_id = :evaluationPeriodId
                and o.is_deleted = :activeStatus
                and org.organization_id = o.organization_id
                and o.emp_manager_id = :employeeId 
                order by org.path_order
                """;
        Map<String, Object> params = new HashMap<>();
        params.put("evaluationPeriodId", evaluationPeriodId);
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        params.put("employeeId", employeeId);
        return getFirstData(sql, params, Long.class);
    }

    public boolean checkApprove(Long organizationId, List<Long> orgIds) {
        if (Utils.isNullOrEmpty(orgIds)) {
            return false;
        }
        String sql = """
                select 1 from hr_organizations o, hr_organizations op 
                where o.organization_id = :organizationId
                and op.organization_id in (:orgIds)
                and o.path_id like concat(op.path_id, '%')
                and (op.parent_id is null or op.path_level <= o.path_level)
                limit 1 
                """;
        Map<String, Object> params = new HashMap<>();
        params.put("organizationId", organizationId);
        params.put("orgIds", orgIds);
        return queryForObject(sql, params, Long.class) != null;
    }

    public List<OrganizationWorkPlanningsEntity> getListOrgWorkPlanning() {
        String sql = """
                SELECT a.*, b.organization_id
                FROM kpi_organization_work_plannings a
                JOIN kpi_organization_evaluations b on a.organization_evaluation_id = b.organization_evaluation_id
                WHERE a.is_deleted = :activeStatus
                AND b.is_deleted = :activeStatus
                AND b.status IN (:statusList)
                """;
        HashMap<String, Object> params = new HashMap<>();
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        params.put("statusList", List.of(Constant.STATUS.DANH_GIA, Constant.STATUS.QLTT_DANH_GIA, Constant.STATUS.CHO_QLTT_DANH_GIA, Constant.STATUS.DA_XAC_NHAN_KQ_DANH_GIA, Constant.STATUS.CHO_QLTT_DANH_GIA_LAI));
        return getListData(sql, params, OrganizationWorkPlanningsEntity.class);
    }

    public List<OrganizationEvaluationsEntity> getListEntity(List<Long> listId) {
        StringBuilder sql = new StringBuilder(" SELECT a.* from hr_organizations o,kpi_organization_evaluations a ");
        HashMap<String, Object> params = new HashMap<>();
        OrganizationEvaluationsRequest.SearchForm dto = new OrganizationEvaluationsRequest.SearchForm();
        dto.setIsEvaluation("Y");
        dto.setListId(listId);
        addCondition(sql, params, dto);
        return getListData(sql.toString(), params, OrganizationEvaluationsEntity.class);
    }

    public String getParentNotApproved(Long orgParentId, Long evaluationPeriodId) {
        String sql = """
                select op.name 
                from hr_organizations o, kpi_organization_evaluations kp, hr_organizations op
                where kp.evaluation_period_id = :evaluationPeriodId
                and kp.is_deleted = :activeStatus
                and op.organization_id = kp.organization_id
                and o.path_id like concat(op.path_id, '%')
                and o.organization_id in (:orgParentId)
                and kp.status in (:statusNotApproved)
                order by o.path_order desc
                """;
        Map<String, Object> params = new HashMap<>();
        params.put("evaluationPeriodId", evaluationPeriodId);
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        params.put("orgParentId", orgParentId);
        params.put("statusNotApproved", List.of(OrganizationEvaluationsEntity.STATUS.KHOI_TAO,
                OrganizationEvaluationsEntity.STATUS.DU_THAO,
                OrganizationEvaluationsEntity.STATUS.CHO_PHE_DUYET,
                OrganizationEvaluationsEntity.STATUS.CHO_XET_DUYET,
                OrganizationEvaluationsEntity.STATUS.TU_CHOI_PHE_DUYET,
                OrganizationEvaluationsEntity.STATUS.TU_CHOI_XET_DUYET
        ));
        return getFirstData(sql, params, String.class);
    }

    public ParameterEntity getParameter(String code, String codeGroup) {
        String sql = """
                select a.* from sys_parameters a
                where a.is_deleted = 'N'
                and a.start_date <= now()
                and ifnull(a.end_date, now()) >= DATE(now())
                and a.config_code = :code
                and a.config_group = :codeGroup
                LIMIT 1
                """;
        HashMap<String, Object> params = new HashMap<>();
        params.put("code", code);
        params.put("codeGroup", codeGroup);
        return queryForObject(sql, params, ParameterEntity.class);
    }


    public List<OrganizationEvaluationsResponse.Content> getDataByOrganizationEvaluationId(Long id, List<String> paramData, List<String> parentKey) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    JSON_UNQUOTE(JSON_EXTRACT(a.content, CONCAT('$[', idx.i, '].key'))) AS key_value,
                    JSON_UNQUOTE(JSON_EXTRACT(a.content, CONCAT('$[', idx.i, '].param'))) AS param,
                    JSON_EXTRACT(a.content, CONCAT('$[', idx.i, '].note')) AS note,
                    JSON_UNQUOTE(JSON_EXTRACT(a.content, CONCAT('$[', idx.i, '].unit'))) AS unit,
                    JSON_UNQUOTE(JSON_EXTRACT(a.content, CONCAT('$[', idx.i, '].resultManage'))) AS resultManage,
                    JSON_EXTRACT(a.content, CONCAT('$[', idx.i, '].managePoint')) AS managePoint,
                    JSON_EXTRACT(a.content, CONCAT('$[', idx.i, '].stepOne')) AS stepOne,
                    JSON_EXTRACT(a.content, CONCAT('$[', idx.i, '].stepTwo')) AS stepTwo,
                    JSON_UNQUOTE(JSON_EXTRACT(a.content, CONCAT('$[', idx.i, '].fullYear'))) AS fullYear,
                    a.organization_evaluation_id
                FROM kpi_organization_work_plannings a
                CROSS JOIN (
                    SELECT 0 AS i UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4
                    UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9
                    UNION ALL SELECT 10 UNION ALL SELECT 11 UNION ALL SELECT 12 UNION ALL SELECT 13 UNION ALL SELECT 14
                    UNION ALL SELECT 15 UNION ALL SELECT 16 UNION ALL SELECT 17 UNION ALL SELECT 18 UNION ALL SELECT 19
                    UNION ALL SELECT 20 UNION ALL SELECT 21 UNION ALL SELECT 22 UNION ALL SELECT 23 UNION ALL SELECT 24
                    UNION ALL SELECT 25 UNION ALL SELECT 26 UNION ALL SELECT 27 UNION ALL SELECT 28 UNION ALL SELECT 29
                    UNION ALL SELECT 30 UNION ALL SELECT 31 UNION ALL SELECT 32 UNION ALL SELECT 33 UNION ALL SELECT 34
                    UNION ALL SELECT 35 UNION ALL SELECT 36 UNION ALL SELECT 37 UNION ALL SELECT 38 UNION ALL SELECT 39
                    UNION ALL SELECT 40 UNION ALL SELECT 41 UNION ALL SELECT 42 UNION ALL SELECT 43 UNION ALL SELECT 44
                    UNION ALL SELECT 45 UNION ALL SELECT 46 UNION ALL SELECT 47 UNION ALL SELECT 48 UNION ALL SELECT 49
                    UNION ALL SELECT 50 UNION ALL SELECT 51 UNION ALL SELECT 52 UNION ALL SELECT 53 UNION ALL SELECT 54
                    UNION ALL SELECT 55 UNION ALL SELECT 56 UNION ALL SELECT 57 UNION ALL SELECT 58 UNION ALL SELECT 59
                ) AS idx
                WHERE a.is_deleted = 'N'
                  AND a.organization_evaluation_id = :id
                  AND JSON_UNQUOTE(JSON_EXTRACT(a.content, CONCAT('$[', idx.i, '].param'))) IN (:paramData)
                """);
        HashMap<String, Object> params = new HashMap<>();
        params.put("id", id);
        params.put("paramData", paramData);
        if (!Utils.isNullOrEmpty(parentKey)) {
            sql.append(" AND JSON_UNQUOTE(JSON_EXTRACT(a.content, CONCAT('$[', idx.i, '].parentKey'))) IN (:parentKey)");
            params.put("parentKey", parentKey);
        }
        return getListData(sql.toString(), params, OrganizationEvaluationsResponse.Content.class);
    }


    public List<OrganizationIndicatorsEntity> getIndicatorByOrganizationEvaluationId(Long id, List<Long> indicatorIds) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    a.*,
                    (SELECT sc.name FROM sys_categories sc WHERE idx.unit_id = sc.value and sc.category_type = :donViTinh) unitName
                FROM kpi_organization_indicators a
                JOIN kpi_indicators idx on a.indicator_id = idx.indicator_id
                WHERE a.is_deleted = 'N'
                AND a.organization_evaluation_id = :id
                """);
        HashMap<String, Object> params = new HashMap<>();
        QueryUtils.filter(indicatorIds, sql, params, "a.indicator_id");
        params.put("id", id);
        params.put("donViTinh", Constant.CATEGORY_TYPES.DON_VI_TINH);

        return getListData(sql.toString(), params, OrganizationIndicatorsEntity.class);
    }

    public String getKPIConfig(Long organizationId, Long evaluationPeriodId) {
        String sql = "select f_get_min_num_of_kpi_org(:organizationId, :evaluationPeriodId) from dual";
        Map<String, Object> params = new HashMap<>();
        params.put("organizationId", organizationId);
        params.put("evaluationPeriodId", evaluationPeriodId);
        return queryForObject(sql, params, String.class);
    }
}
