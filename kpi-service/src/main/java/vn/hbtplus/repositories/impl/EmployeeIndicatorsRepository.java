/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.repositories.impl;

import org.springframework.stereotype.Repository;
import lombok.RequiredArgsConstructor;
import vn.hbtplus.constants.Constant;
import vn.hbtplus.constants.Scope;
import vn.hbtplus.models.BaseDataTableDto;
import vn.hbtplus.models.response.IndicatorConversionsResponse;
import vn.hbtplus.repositories.BaseRepository;
import vn.hbtplus.models.request.EmployeeIndicatorsRequest;
import vn.hbtplus.models.response.EmployeeIndicatorsResponse;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.repositories.entity.EmployeeIndicatorsEntity;
import vn.hbtplus.repositories.entity.ObjectRelationsEntity;
import vn.hbtplus.repositories.entity.OrganizationEvaluationsEntity;
import vn.hbtplus.repositories.entity.ParameterEntity;
import vn.hbtplus.services.AuthorizationService;
import vn.hbtplus.utils.Utils;

import java.util.*;

/**
 * Lop repository Impl ung voi bang kpi_employee_indicators
 *
 * @author tudd
 * @version 1.0
 * @since 1.0
 */

@Repository
@RequiredArgsConstructor
public class EmployeeIndicatorsRepository extends BaseRepository {

    private final AuthorizationService authorizationService;
    private final OrganizationEvaluationsRepository organizationEvaluationsRepository;

    public BaseDataTableDto searchData(EmployeeIndicatorsRequest.SearchForm dto) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    a.employee_indicator_id,
                    a.indicator_conversion_id,
                    a.indicator_id,
                    a.employee_evaluation_id,
                    a.percent,
                    a.target,
                    a.created_by,
                    a.created_time,
                    a.modified_by,
                    a.modified_time,
                    a.last_update_time,
                    a.is_deleted
                """);
        HashMap<String, Object> params = new HashMap<>();
        addCondition(sql, params, dto);
        return getListPagination(sql.toString(), params, dto, EmployeeIndicatorsResponse.SearchResult.class);
    }

    public List<Map<String, Object>> getListExport(EmployeeIndicatorsRequest.SearchForm dto) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    a.employee_indicator_id,
                    a.indicator_conversion_id,
                    a.indicator_id,
                    a.employee_evaluation_id,
                    a.percent,
                    a.target,
                    a.created_by,
                    a.created_time,
                    a.modified_by,
                    a.modified_time,
                    a.last_update_time,
                    a.is_deleted
                """);
        Map<String, Object> params = new HashMap<>();
        addCondition(sql, params, dto);
        return getListData(sql.toString(), params);
    }

    private void addCondition(StringBuilder sql, Map<String, Object> params, EmployeeIndicatorsRequest.SearchForm dto) {
        sql.append("""
                    FROM kpi_employee_indicators a
                    
                    
                    WHERE IFNULL(a.is_deleted, :activeStatus) = :activeStatus
                """);
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        //QueryUtils.filterGe(dto.getFromDate(), sql, params, "a.from_date", "fromDate");
        //QueryUtils.filterLe(dto.getToDate(), sql, params, "a.from_date", "toDate");
//        sql.append(" ORDER BY mo.display_seq, mo.path_id");
    }

    public List<Long> getListId(Long employeeEvaluationId) {
        StringBuilder sql = new StringBuilder("""
                select
                    a.employee_indicator_id
                FROM kpi_employee_indicators a
                where a.is_deleted = :activeStatus
                and a.employee_evaluation_id = :employeeEvaluationId
                """);
        Map<String, Object> params = new HashMap<>();
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        params.put("employeeEvaluationId", employeeEvaluationId);
        return getListData(sql.toString(), params, Long.class);
    }


    public List<EmployeeIndicatorsResponse.EmployeeEvaluation> getDataByEvaluationId(Long employeeEvaluationId, boolean isGetAll) {
        StringBuilder sql = new StringBuilder("""
                WITH target_data as (
                            SELECT koi.target, koi.result, koi.result_manage, koi.indicator_id, koe.organization_id, koe.emp_manager_id, koe.evaluation_period_id
                            FROM kpi_organization_indicators koi
                            JOIN kpi_organization_evaluations koe ON koi.organization_evaluation_id = koe.organization_evaluation_id
                            WHERE koi.is_deleted = :activeStatus
                            AND koe.status IN (:statusList)
                )
                
                select
                    a.*,
                    idx.name as indicator_name,
                    kic.conversion_type,
                    kic.is_focus_reduction,
                    case
                        when idx.name = 'Mức độ hoàn thành kế hoạch công tác của đơn vị' then 'Y'
                        else 'N'
                    end as is_org,
                    T3.result as resultOrg,
                    T3.result_manage as resultManageOrg,
                    idx.significance,
                    idx.measurement,
                    idx.system_info,
                    idx.rating_type,
                    idx.list_values,
                    kic.note,
                    (SELECT GROUP_CONCAT(CONCAT('- ', kis.name) ORDER BY kis.name SEPARATOR '\n')
                        FROM kpi_indicators kis
                        JOIN kpi_object_relations kor2 ON (idx.indicator_id = kor2.object_id 
                        and kor2.table_name = :tableName
                        and kor2.refer_object_id = kis.indicator_id
                        and kor2.refer_table_name = :referTableName
                        and kor2.function_code = :functionCode
                        and IFNULL(kor2.is_deleted, :activeStatus) = :activeStatus)) AS relatedNames,
                    (SELECT GROUP_CONCAT(CONCAT('- ', o.name) ORDER BY o.name SEPARATOR '\n')
                        FROM hr_organizations o
                        JOIN kpi_object_relations kor ON (a.indicator_id = kor.object_id 
                        and kor.table_name = :tableName
                        and kor.refer_object_id = o.organization_id
                        and kor.refer_table_name = :referTableName2
                        and kor.function_code = :functionCode2
                        and IFNULL(kor.is_deleted, :activeStatus) = :activeStatus)) AS scopeNames,
                    (SELECT sc.name FROM sys_categories sc WHERE idx.unit_id = sc.value and sc.category_type = :donViTinh) unitName,
                    (SELECT sc.name FROM sys_categories sc WHERE idx.period_type = sc.value and sc.category_type = :chuKy) periodTypeName,
                    (SELECT sc.name FROM sys_categories sc WHERE idx.type = sc.value and sc.category_type = :phanLoai) typeName,
                    CASE 
                        WHEN kic.conversion_type = 'DON_VI' 
                        THEN COALESCE(
                            T3.target,
                            (select T1.target FROM target_data T1 WHERE (T1.indicator_id = a.indicator_id
                                              AND kee.evaluation_period_id = T1.evaluation_period_id
                                              AND (T1.organization_id = kee.organization_id
                                              OR FIND_IN_SET(T1.organization_id, kee.org_concurrent_ids) > 0))),
                            (select T2.target FROM target_data T2 WHERE (T2.indicator_id = a.indicator_id
                                              AND kee.evaluation_period_id = T2.evaluation_period_id
                                              AND T2.organization_id = o2.parent_id))
                         )
                    END as targetStr
                FROM kpi_employee_indicators a
                JOIN kpi_indicators idx on a.indicator_id = idx.indicator_id
                JOIN kpi_indicator_conversions kic on a.indicator_conversion_id = kic.indicator_conversion_id
                JOIN kpi_indicator_masters im on im.indicator_master_id = kic.indicator_master_id
                JOIN hr_organizations o on o.organization_id = im.organization_id
                JOIN kpi_employee_evaluations kee ON kee.employee_evaluation_id = a.employee_evaluation_id
                JOIN hr_organizations o2 on o2.organization_id = kee.organization_id
                LEFT JOIN target_data T3 on (T3.indicator_id = a.indicator_id AND kee.employee_id = T3.emp_manager_id AND kee.evaluation_period_id = T3.evaluation_period_id)
                where a.is_deleted = :activeStatus
                and a.status = 'ACTIVE'
                and a.employee_evaluation_id = :employeeEvaluationId
                """);
        Map<String, Object> params = new HashMap<>();
        if(!isGetAll) {
            List<Long> orgIds = authorizationService.getOrgHasPermission(Scope.VIEW, Constant.RESOURCES.EMPLOYEE_EVALUATIONS, Utils.getUserNameLogin());
            sql.append("""
                 and (
                	exists (
                		select 1 from hr_organizations org
                		where org.organization_id in (:orgIds)
                		and o.path_id like CONCAT(org.path_id,'%')
                	)
                	or exists (
                		select 1 from hr_organizations org ,
                		hr_organizations oc
                		left join hr_positions p on oc.organization_id = p.organization_id and p.is_deleted = 'N'
                		where org.organization_id in (:orgIds)
                		and oc.path_id like CONCAT(org.path_id,'%')
                		and oc.path_id like CONCAT(o.path_id,'%')
                		and (
                			oc.org_type_id = im.org_type_id
                			or p.job_id = im.job_id
                		)
                	)
                )
                """);
            params.put("orgIds", orgIds);
        }
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        params.put("donViTinh", Constant.CATEGORY_TYPES.DON_VI_TINH);
        params.put("chuKy", Constant.CATEGORY_TYPES.CHU_KY);
        params.put("phanLoai", Constant.CATEGORY_TYPES.PHAN_LOAI);
        params.put("tableName", ObjectRelationsEntity.TABLE_NAMES.INDICATORS);
        params.put("referTableName", ObjectRelationsEntity.TABLE_NAMES.INDICATORS);
        params.put("functionCode", ObjectRelationsEntity.FUNCTION_CODES.CHI_SO_LIEN_QUAN);
        params.put("referTableName2", ObjectRelationsEntity.TABLE_NAMES.ORGANIZATIONS);
        params.put("functionCode2", ObjectRelationsEntity.FUNCTION_CODES.PHAM_VI_AP_DUNG);
        params.put("employeeEvaluationId", employeeEvaluationId);
        params.put("statusList", List.of(OrganizationEvaluationsEntity.STATUS.PHE_DUYET, OrganizationEvaluationsEntity.STATUS.DANH_GIA, OrganizationEvaluationsEntity.STATUS.QLTT_DANH_GIA,
                Constant.STATUS.CHO_QLTT_DANH_GIA, Constant.STATUS.CHO_QLTT_DANH_GIA_LAI, Constant.STATUS.YC_DANH_GIA_LAI));
//        ParameterEntity parameterEntity = organizationEvaluationsRepository.getParameter("LIST_HEAD_ID", "PARAMETER_KPI");
//        List<String> listHeadId = !Utils.isNullOrEmpty(parameterEntity.getConfigValue()) ? Arrays.stream(parameterEntity.getConfigValue().split(",")).toList() : new ArrayList<>();
//        params.put("listHeadId", listHeadId);
        return getListData(sql.toString(), params, EmployeeIndicatorsResponse.EmployeeEvaluation.class);
    }

    public void deActiveByListId(List<Long> listId) {
        StringBuilder sql = new StringBuilder("""
                UPDATE kpi_employee_indicators
                       SET status = 'INACTIVE',
                       modified_by = :userName,
                       modified_time = now()
                       WHERE employee_indicator_id IN (:ids)
                """);
        Map<String, Object> params = new HashMap<>();
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        params.put("userName", Utils.getUserNameLogin());
        params.put("ids", listId);
        executeSqlDatabase(sql.toString(), params);
    }
}
