/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.repositories.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.constants.Constant;
import vn.hbtplus.constants.Scope;
import vn.hbtplus.feigns.PermissionFeignClient;
import vn.hbtplus.models.BaseDataTableDto;
import vn.hbtplus.models.request.IndicatorMastersRequest;
import vn.hbtplus.models.response.IndicatorConversionsResponse;
import vn.hbtplus.models.response.IndicatorMastersResponse;
import vn.hbtplus.repositories.BaseRepository;
import vn.hbtplus.repositories.entity.IndicatorConversionDetailEntity;
import vn.hbtplus.repositories.entity.IndicatorConversionsEntity;
import vn.hbtplus.repositories.entity.IndicatorMastersEntity;
import vn.hbtplus.repositories.entity.ParameterEntity;
import vn.hbtplus.services.AuthorizationService;
import vn.hbtplus.utils.QueryUtils;
import vn.hbtplus.utils.Utils;

import javax.naming.AuthenticationException;
import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Lop repository Impl ung voi bang kpi_indicator_masters
 *
 * @author tudd
 * @version 1.0
 * @since 1.0
 */

@Repository
@RequiredArgsConstructor
public class IndicatorMastersRepository extends BaseRepository {

    private final PermissionFeignClient permissionFeignClient;
    private final HttpServletRequest request;
    private final AuthorizationService authorizationService;

    public BaseDataTableDto searchData(IndicatorMastersRequest.SearchForm dto) throws Exception {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    a.indicator_master_id,
                    a.organization_id,
                    sc.name orgTypeName,
                    jb.name jobName,
                    o.full_name organizationName,
                    a.job_id,
                    a.org_type_id,
                    f_get_kpi_level(a.organization_id, a.org_type_id, a.job_id) as kpiLevel,
                    (select sc1.name from sys_categories sc1
                        where sc1.value = f_get_kpi_level(a.organization_id, a.org_type_id, a.job_id)
                        and sc1.category_type = :CAP_KPI) AS kpiLevelName,
                    a.status_id status,
                    a.is_deleted,
                    a.created_by,
                    a.created_time,
                    a.modified_by,
                    a.modified_time,
                    a.last_update_time
                """);
        HashMap<String, Object> params = new HashMap<>();
        params.put("CAP_KPI", Constant.CATEGORY_TYPES.CAP_KPI);
        List<Long> orgIds = authorizationService.getOrgHasPermission(Scope.VIEW, Constant.RESOURCES.INDICATOR_CONVERSION, Utils.getUserNameLogin());
        if (Utils.isNullOrEmpty(orgIds)) {
            throw new AuthenticationException();
        }
        addCondition(sql, params, dto, orgIds);
        return getListPagination(sql.toString(), params, dto, IndicatorMastersResponse.class);
    }

    public List<Map<String, Object>> getListExport(IndicatorMastersRequest.SearchForm dto) throws Exception {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    a.indicator_master_id,
                    a.organization_id,
                    sc.name orgTypeName,
                    jb.name jobName,
                    o.full_name organizationName,
                    a.job_id,
                    a.org_type_id,
                    f_get_kpi_level(a.organization_id, a.org_type_id, a.job_id) as kpiLevel,
                    (select sc1.name from sys_categories sc1
                        where sc1.value = f_get_kpi_level(a.organization_id, a.org_type_id, a.job_id)
                        and sc1.category_type = :CAP_KPI) AS kpiLevelName,
                    a.status_id status,
                    (select sc.name from sys_categories sc where sc.code = a.status_id and sc.category_type = :statusCode) statusName,
                    a.is_deleted,
                    a.created_by,
                    a.created_time,
                    a.modified_by,
                    a.modified_time,
                    a.last_update_time
                """);
        Map<String, Object> params = new HashMap<>();
        List<Long> orgIds = authorizationService.getOrgHasPermission(Scope.VIEW, Constant.RESOURCES.INDICATOR_CONVERSION, Utils.getUserNameLogin());
        if (Utils.isNullOrEmpty(orgIds)) {
            throw new AuthenticationException();
        }
        params.put("CAP_KPI", Constant.CATEGORY_TYPES.CAP_KPI);
        addCondition(sql, params, dto, orgIds);
        params.put("statusCode", Constant.CATEGORY_TYPES.INDICATOR_CONVERSION_STATUS);
        return getListData(sql.toString(), params);
    }

    private void addCondition(StringBuilder sql, Map<String, Object> params, IndicatorMastersRequest.SearchForm dto, List<Long> orgIds) {
        sql.append("""
                    FROM kpi_indicator_masters a
                    left join sys_categories sc on a.org_type_id = sc.value and sc.category_type = :loaiHinhDonVi
                    left join hr_jobs jb on a.job_id = jb.job_id
                    left join hr_organizations o on o.organization_id = a.organization_id and o.is_deleted = 'N'
                    WHERE IFNULL(a.is_deleted, :activeStatus) = :activeStatus
                
                """);
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        params.put("loaiHinhDonVi", Constant.CATEGORY_TYPES.LOAI_HINH_DON_VI);
        QueryUtils.filter(dto.getJobId(), sql, params, "a.job_id");
        QueryUtils.filter(dto.getOrgTypeId(), sql, params, "a.org_type_id");
        QueryUtils.filter(dto.getKeySearch(), sql, params, "jb.name", "sc.name", "o.name");
        if (dto.getOrganizationId() != null && dto.getOrganizationId() > 0L) {
            sql.append(" AND o.path_id like :orgPath ");
            params.put("orgPath", "%/" + dto.getOrganizationId() + "/%");
        }
        if (!Utils.isNullOrEmpty(dto.getLevel())) {
            sql.append(" AND f_get_kpi_level(a.organization_id, a.org_type_id, a.job_id) like :kpiLevel");
            params.put("kpiLevel", dto.getLevel());
        }

        //xu ly phan quyen
        //chi duoc tim kiem kpi cua don vi thuoc phan quyen
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
                			oc.org_type_id = a.org_type_id
                			or p.job_id = a.job_id
                		)
                	)
                )
                """);
        params.put("orgIds", orgIds);

        sql.append(" ORDER BY kpiLevel, o.path_order, sc.order_number, jb.order_number, a.job_id");
    }

    public IndicatorMastersEntity getIndicatorMaster(Long organizationId, Long jobId, Long orgTypeId) {
        StringBuilder sql = new StringBuilder("""
                select a.* from kpi_indicator_masters a
                where a.organization_id = :organizationId
                and a.is_deleted = 'N'
                """);
        Map<String, Object> params = new HashMap<>();
        params.put("organizationId", organizationId);
        if (jobId != null && jobId > 0) {
            sql.append(" and a.job_id = :jobId");
            params.put("jobId", jobId);
        } else {
            sql.append(" AND a.job_id IS NULL");
        }
        if (!Utils.isNullObject(orgTypeId)) {
            sql.append(" and a.org_type_id = :orgTypeId");
            params.put("orgTypeId", orgTypeId);
        } else {
            sql.append(" AND a.org_type_id IS NULL");
        }
        return getFirstData(sql.toString(), params, IndicatorMastersEntity.class);
    }

    public void updateStatusByConversions(Long indicatorMasterId) {
        String sql = """
                update kpi_indicator_masters a
                set a.status_id = :statusApproved
                where a.indicator_master_id = :indicatorMasterId
                and not exists (
                    select 1 from kpi_indicator_conversions c
                    where c.indicator_master_id = :indicatorMasterId
                    and c.is_deleted = 'N'
                    and c.status in (:choPheDuyet)
                )
                and a.status_id <> :statusApproved
                """;
        Map<String, Object> params = new HashMap<>();
        params.put("indicatorMasterId", indicatorMasterId);
        params.put("statusWaitApproved", IndicatorMastersEntity.STATUS.CHO_PHE_DUYET);
        params.put("statusApproved", IndicatorMastersEntity.STATUS.PHE_DUYET);
        params.put("choPheDuyet", Arrays.asList(
                IndicatorConversionsEntity.STATUS.CHO_PHE_DUYET_HIEU_LUC_LAI,
                IndicatorConversionsEntity.STATUS.CHO_PHE_DUYET,
                IndicatorConversionsEntity.STATUS.DE_NGHI_XOA,
                IndicatorConversionsEntity.STATUS.TU_CHOI_PHE_DUYET
        ));

        executeSqlDatabase(sql, params);

        sql = """
                update kpi_indicator_masters a
                set a.status_id = :statusWaitApproved
                where indicator_master_id = :indicatorMasterId
                and exists (
                    select 1 from kpi_indicator_conversions c
                    where c.indicator_master_id = :indicatorMasterId
                    and c.is_deleted = 'N'
                    and c.status in (:choPheDuyet)
                )
                and a.status_id <> :statusWaitApproved
                """;
        executeSqlDatabase(sql, params);


    }

    public List<IndicatorConversionDetailEntity> getListConversionDetails(Long indicatorMasterId) {
        String sql = "select a.* from kpi_indicator_conversion_details a, kpi_indicator_conversions ic" +
                " where ic.indicator_master_id = :indicatorMasterId" +
                "  and ic.indicator_conversion_id = a.indicator_conversion_id" +
                "  and ic.is_deleted = 'N'" +
                "  and a.is_deleted = 'N'";
        Map<String, Object> params = new HashMap<>();
        params.put("indicatorMasterId", indicatorMasterId);
        return getListData(sql, params, IndicatorConversionDetailEntity.class);
    }

    public IndicatorConversionsResponse.Indicators getData(IndicatorMastersRequest.ImportRequest dto) {
        StringBuilder sql = new StringBuilder("""
                WITH a AS (
                    SELECT
                        :jobId AS job_id,
                        :orgId AS organization_id,
                        :orgTypeId AS org_type_id
                    FROM dual
                )
                SELECT
                    CASE
                        WHEN a.job_id IS NULL THEN ''
                        ELSE (
                            SELECT jb.name
                            FROM hr_jobs jb
                            WHERE jb.job_id = a.job_id AND jb.is_deleted = 'N'
                        )
                    END AS jobName,
                
                    (SELECT sc1.name
                     FROM sys_categories sc1
                     WHERE sc1.value = f_get_kpi_level(a.organization_id, a.org_type_id, a.job_id)
                       AND sc1.category_type = :CAP_KPI
                    ) AS kpiLevelName,
                
                    f_get_kpi_level(a.organization_id, a.org_type_id, a.job_id) AS kpiLevel,
                
                    (SELECT sc.name
                     FROM sys_categories sc
                     WHERE sc.value = a.org_type_id
                       AND sc.category_type = :loaiHinhDonVi
                    ) AS orgTypeName,
                
                    (SELECT o.name
                     FROM hr_organizations o
                     WHERE o.organization_id = a.organization_id
                       AND o.is_deleted = 'N'
                    ) AS organizationName
                FROM a
                """);
        Map<String, Object> params = new HashMap<>();
        params.put("loaiHinhDonVi", Constant.CATEGORY_TYPES.LOAI_HINH_DON_VI);
        params.put("CAP_KPI", Constant.CATEGORY_TYPES.CAP_KPI);
        params.put("orgId", dto.getOrganizationId());
        params.put("orgTypeId", dto.getOrgTypeId());
        params.put("jobId", dto.getJobId());
        return getFirstData(sql.toString(), params, IndicatorConversionsResponse.Indicators.class);
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
}
