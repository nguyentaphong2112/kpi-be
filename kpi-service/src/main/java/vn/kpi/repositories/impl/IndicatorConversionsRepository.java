/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.kpi.repositories.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import vn.kpi.constants.BaseConstants;
import vn.kpi.constants.Constant;
import vn.kpi.models.BaseDataTableDto;
import vn.kpi.models.dto.OrgDto;
import vn.kpi.models.request.IndicatorConversionsRequest;
import vn.kpi.models.response.IndicatorConversionsResponse;
import vn.kpi.models.response.IndicatorsResponse;
import vn.kpi.repositories.BaseRepository;
import vn.kpi.repositories.entity.IndicatorConversionDetailEntity;
import vn.kpi.repositories.entity.IndicatorConversionsEntity;
import vn.kpi.repositories.entity.IndicatorMastersEntity;
import vn.kpi.repositories.entity.ObjectRelationsEntity;
import vn.kpi.repositories.entity.OrganizationEvaluationsEntity;
import vn.kpi.utils.QueryUtils;
import vn.kpi.utils.Utils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Lop repository Impl ung voi bang kpi_indicator_conversions
 *
 * @author tudd
 * @version 1.0
 * @since 1.0
 */

@Repository
@RequiredArgsConstructor
public class IndicatorConversionsRepository extends BaseRepository {

    public BaseDataTableDto searchData(IndicatorConversionsRequest.SearchForm dto) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    a.indicator_conversion_id,
                    a.org_type_id,
                    sc.name orgTypeName,
                    jb.name jobName,
                    a.job_id,
                    a.organization_id,
                    case
                        when not exists (
                            select 1 from kpi_indicator_conversions a1
                            where ifnull(a1.org_type_id,'N/A') = ifnull(a.org_type_id,'N/A')
                            and ifnull(a1.organization_id,0) = ifnull(a.organization_id,0)
                            and ifnull(a1.job_id,0) = ifnull(a.job_id,0)
                            and a1.is_deleted = 'N'
                            and a1.status <> 'PHE_DUYET'
                        ) then 'PHE_DUYET'
                        when not exists (
                            select 1 from kpi_indicator_conversions a1
                            where ifnull(a1.org_type_id,'N/A') = ifnull(a.org_type_id,'N/A')
                            and ifnull(a1.organization_id,0) = ifnull(a.organization_id,0)
                            and ifnull(a1.job_id,0) = ifnull(a.job_id,0)
                            and a1.is_deleted = 'N'
                            and a1.status <> 'HET_HIEU_LUC'
                        ) then 'HET_HIEU_LUC'
                        ELSE 'CHO_PHE_DUYET'
                    END AS status,
                    o.name organizationName,
                    max(a.created_by) as created_by,
                    min(a.created_time) as created_time,
                    max(a.modified_by) as modified_by,
                    max(a.modified_time) as modified_time
                """);
        HashMap<String, Object> params = new HashMap<>();
        addCondition(sql, params, dto);
        return getListPagination(sql.toString(), params, dto, IndicatorConversionsResponse.SearchResult.class);
    }

    public List<Map<String, Object>> getListExport(IndicatorConversionsRequest.SearchForm dto) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    a.indicator_conversion_id,
                    a.org_type_id,
                    sc.name orgTypeName,
                    jb.name jobName,
                    a.job_id,
                    a.organization_id,
                    case
                        when not exists (
                            select 1 from kpi_indicator_conversions a1
                            where ifnull(a1.org_type_id,'N/A') = ifnull(a.org_type_id,'N/A')
                            and ifnull(a1.organization_id,0) = ifnull(a.organization_id,0)
                            and ifnull(a1.job_id,0) = ifnull(a.job_id,0)
                            and a1.is_deleted = 'N'
                            and a1.status <> 'PHE_DUYET'
                        ) then 'PHE_DUYET'
                        when not exists (
                            select 1 from kpi_indicator_conversions a1
                            where ifnull(a1.org_type_id,'N/A') = ifnull(a.org_type_id,'N/A')
                            and ifnull(a1.organization_id,0) = ifnull(a.organization_id,0)
                            and ifnull(a1.job_id,0) = ifnull(a.job_id,0)
                            and a1.is_deleted = 'N'
                            and a1.status <> 'HET_HIEU_LUC'
                        ) then 'HET_HIEU_LUC'
                        ELSE 'CHO_PHE_DUYET'
                    END AS status,
                    o.name organizationName,
                    max(a.created_by) as created_by,
                    min(a.created_time) as created_time,
                    max(a.modified_by) as modified_by,
                    max(a.modified_time) as modified_time
                """);
        Map<String, Object> params = new HashMap<>();
        addCondition(sql, params, dto);
        return getListData(sql.toString(), params);
    }

    private void addCondition(StringBuilder sql, Map<String, Object> params, IndicatorConversionsRequest.SearchForm dto) {
        sql.append("""
                    FROM kpi_indicator_conversions a
                    LEFT JOIN sys_categories sc on a.org_type_id = sc.value and sc.category_type = :loaiHinhDonVi
                    LEFT JOIN hr_jobs jb on a.job_id = jb.job_id
                    LEFT JOIN hr_organizations o on o.organization_id = a.organization_id and o.is_deleted = 'N'
                    WHERE a.is_deleted = :activeStatus
                """);
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        params.put("loaiHinhDonVi", Constant.CATEGORY_TYPES.LOAI_HINH_DON_VI);
        if (dto.getJobId() != null && dto.getJobId() > 0) {
            sql.append(" AND a.job_id = :jobId");
            params.put("jobId", dto.getJobId());
        }
        if (dto.getOrgTypeId() != null && dto.getOrgTypeId() > 0) {
            sql.append(" AND a.org_type_id = :orgTypeId");
            params.put("orgTypeId", dto.getOrgTypeId());
        }
        if (dto.getOrganizationId() != null && dto.getOrganizationId() > 0L) {
            sql.append(" AND o.path_id like :orgPath");
            params.put("orgPath", "%/" + dto.getOrganizationId() + "/%");
        }
        if (!Utils.isNullOrEmpty(dto.getKeySearch())) {
            sql.append(" AND (jb.name like :keySearch or sc.name like :keySearch or  o.name like :keySearch)");
            params.put("keySearch", "%" + dto.getKeySearch() + "%");
        }
        sql.append(" group by sc.order_number, a.organization_id, sc.name, jb.order_number, a.job_id");
        sql.append(" ORDER BY sc.order_number, a.organization_id, sc.name, jb.order_number, a.job_id");
    }


    public BaseDataTableDto getListIndicator(IndicatorConversionsRequest.SearchForm dto) {
        StringBuilder sql = new StringBuilder("""
                select
                    a.indicator_conversion_id,
                    a.indicator_id,
                    a.note,
                    a.conversion_type,
                    a.status,
                    idx.name as indicator_name,
                    (SELECT sc.name FROM sys_categories sc WHERE idx.unit_id = sc.value and sc.category_type = :donViTinh) unitName,
                    (SELECT sc.name FROM sys_categories sc WHERE idx.period_type = sc.value and sc.category_type = :chuKy) periodTypeName
                FROM kpi_indicator_conversions a
                left join hr_jobs jb on a.job_id = jb.job_id 
                join kpi_indicators idx on a.indicator_id = idx.indicator_id
                where a.is_deleted = :activeStatus
                and a.org_type_id = :orgTypeId
                and a.organization_id = :organizationId
                """);
        Map<String, Object> params = new HashMap<>();
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        params.put("donViTinh", Constant.CATEGORY_TYPES.DON_VI_TINH);
        params.put("chuKy", Constant.CATEGORY_TYPES.CHU_KY);
        params.put("orgTypeId", dto.getOrgTypeId());
        params.put("organizationId", dto.getOrganizationId());
        if (dto.getJobId() != null) {
            sql.append(" and a.job_id = :jobId");
            params.put("jobId", dto.getJobId());
        } else {
            sql.append(" AND a.job_id IS NULL");
        }
        sql.append(" ORDER BY a.created_time");
        return getListPagination(sql.toString(), params, dto, IndicatorConversionsResponse.Indicator.class);
    }

    public BaseDataTableDto getListIndicator(Long indicatorMasterId, IndicatorConversionsRequest.SearchForm dto) {
        StringBuilder sql = new StringBuilder("""
                select
                    a.indicator_conversion_id,
                    a.indicator_id,
                    a.note,
                    a.conversion_type,
                    a.status,
                    CASE
                        WHEN a.is_required = 'Y' THEN 'Có'
                        ELSE 'Không'
                    END AS isRequiredName,
                    idx.name as indicator_name,
                    (SELECT sc.name FROM sys_categories sc WHERE idx.unit_id = sc.value and sc.category_type = :donViTinh) unitName,
                    (SELECT sc.name FROM sys_categories sc WHERE idx.period_type = sc.value and sc.category_type = :chuKy) periodTypeName
                FROM kpi_indicator_conversions a
                join kpi_indicators idx on a.indicator_id = idx.indicator_id
                where a.is_deleted = :activeStatus
                and a.indicator_master_id = :indicatorMasterId
                """);
        Map<String, Object> params = new HashMap<>();
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        params.put("donViTinh", Constant.CATEGORY_TYPES.DON_VI_TINH);
        params.put("chuKy", Constant.CATEGORY_TYPES.CHU_KY);
        params.put("indicatorMasterId", indicatorMasterId);
        sql.append(" ORDER BY a.created_time DESC");
        return getListPagination(sql.toString(), params, dto, IndicatorConversionsResponse.Indicator.class);
    }

    public List<Map<String, Object>> getListIndicatorExport(Long id) {
        StringBuilder sql = new StringBuilder("""
                select
                    a.indicator_conversion_id,
                    a.indicator_id,
                    a.note,
                    a.conversion_type,
                    a.status,
                    (select sc.name from sys_categories sc where sc.code = a.status and sc.category_type = :statusCode) statusName,
                    idx.name as indicator_name,
                    (SELECT sc.name FROM sys_categories sc WHERE idx.unit_id = sc.value and sc.category_type = :donViTinh) unitName,
                    (SELECT sc.name FROM sys_categories sc WHERE idx.period_type = sc.value and sc.category_type = :chuKy) periodTypeName
                FROM kpi_indicator_conversions a
                join kpi_indicators idx on a.indicator_id = idx.indicator_id
                where a.is_deleted = :activeStatus
                and a.indicator_master_id = :indicatorMasterId
                """);
        Map<String, Object> params = new HashMap<>();
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        params.put("donViTinh", Constant.CATEGORY_TYPES.DON_VI_TINH);
        params.put("chuKy", Constant.CATEGORY_TYPES.CHU_KY);
        params.put("statusCode", Constant.CATEGORY_TYPES.INDICATOR_CONVERSION_STATUS);
        params.put("indicatorMasterId", id);
        sql.append(" ORDER BY a.created_time");
        return getListData(sql.toString(), params);
    }


    public void updateStatusApprove(Long indicatorMasterId, String status) {

        String sqlQuery;
        Map<String, Object> params = new HashMap<>();
        if (IndicatorMastersEntity.STATUS.PHE_DUYET.equalsIgnoreCase(status)) {
            //neu phe duyet thi cap nhat cac ban ghi dang cho phe duyet --> phe duyet
            //ban ghi tu choi phe duyet --> phe duyet
            //ban ghi de nghi xoa --> het hieu luc
            //ban ghi de nghi active lai --> phe duyet
            //cac ban ghi phe duyet roi, het hieu luc : khong tac dong den
            sqlQuery = """
                    update kpi_indicator_conversions a
                    	set a.status = 
                    	    case 
                    	        when a.status in (:waitApproval) 
                    	        then :pheDuyet
                    	        when a.status in (:waitInactive)
                    	        then :inactive
                    	    end,
                    	    a.modified_by = :user,
                    	    a.modified_time = now()
                    	where a.indicator_master_id = :indicatorMasterId
                    	and a.is_deleted = 'N'
                    	and a.status in (:choPheDuyet)
                    """;
            params.put("choPheDuyet", Arrays.asList(IndicatorMastersEntity.STATUS.CHO_PHE_DUYET,
                    IndicatorMastersEntity.STATUS.TU_CHOI_PHE_DUYET,
                    IndicatorMastersEntity.STATUS.CHO_PHE_DUYET_HIEU_LUC_LAI,
                    IndicatorMastersEntity.STATUS.DE_NGHI_XOA
            ));
            params.put("waitApproval", Arrays.asList(IndicatorMastersEntity.STATUS.CHO_PHE_DUYET,
                    IndicatorMastersEntity.STATUS.TU_CHOI_PHE_DUYET,
                    IndicatorMastersEntity.STATUS.CHO_PHE_DUYET_HIEU_LUC_LAI
            ));
            params.put("waitInactive", Arrays.asList(IndicatorMastersEntity.STATUS.DE_NGHI_XOA));
            params.put("pheDuyet", IndicatorMastersEntity.STATUS.PHE_DUYET);
            params.put("inactive", IndicatorMastersEntity.STATUS.HET_HIEU_LUC);
        } else {
            //neu tu choi phe duyet : ban ghi cho phe duyet --> tu choi phe duyet
            //ban ghi de nghi xoa --> phe duyet
            //ban ghi cho phe duyet hieu luc lai --> het hieu luc
            sqlQuery = """
                    update kpi_indicator_conversions a
                    	set a.status =
                    	    case
                    	        when a.status in (:waitApproval)
                    	        then :tuchoiPheDuyet
                    	        when a.status in (:waitInactive)
                    	        then :pheDuyet
                    	        when a.status in (:waitActive)
                    	        then :inactive
                    	    end,
                    	    a.modified_by = :user,
                    	    a.modified_time = now()
                    	where a.indicator_master_id = :indicatorMasterId
                    	and a.is_deleted = 'N'
                    	and a.status in (:choPheDuyet)
                    """;
            params.put("choPheDuyet", Arrays.asList(IndicatorMastersEntity.STATUS.CHO_PHE_DUYET,
                    IndicatorMastersEntity.STATUS.CHO_PHE_DUYET_HIEU_LUC_LAI,
                    IndicatorMastersEntity.STATUS.DE_NGHI_XOA
            ));
            params.put("waitApproval", Arrays.asList(IndicatorMastersEntity.STATUS.CHO_PHE_DUYET));
            params.put("waitInactive", Arrays.asList(IndicatorMastersEntity.STATUS.DE_NGHI_XOA));
            params.put("tuchoiPheDuyet", IndicatorMastersEntity.STATUS.TU_CHOI_PHE_DUYET);
            params.put("pheDuyet", IndicatorMastersEntity.STATUS.PHE_DUYET);
            params.put("waitActive", IndicatorMastersEntity.STATUS.CHO_PHE_DUYET_HIEU_LUC_LAI);
            params.put("inactive", IndicatorMastersEntity.STATUS.HET_HIEU_LUC);
        }

        params.put("indicatorMasterId", indicatorMasterId);
        params.put("user", Utils.getUserNameLogin());
        executeSqlDatabase(sqlQuery, params);

    }


    public BaseDataTableDto getListIndicatorConversion(IndicatorConversionsRequest.SearchForm dto) {
        StringBuilder sql = new StringBuilder("""
                WITH target_data as (
                            SELECT koi.target, koi.indicator_id, koe.organization_id
                            FROM kpi_organization_indicators koi
                            JOIN kpi_organization_evaluations koe ON koi.organization_evaluation_id = koe.organization_evaluation_id
                            WHERE koi.is_deleted = :activeStatus
                            and koe.evaluation_period_id = :evaluationPeriodId
                            AND koe.status IN (:statusList)
                )
                
                SELECT a.indicator_conversion_id,
                    a.indicator_id,
                    a.note,
                    a.conversion_type,
                    idx.name as indicator_name,
                    idx.significance,
                    idx.measurement,
                    idx.system_info,
                    idx.rating_type,
                    idx.list_values,
                    (SELECT sc.name FROM sys_categories sc WHERE idx.unit_id = sc.value and sc.category_type = :donViTinh) unitName,
                    (SELECT sc.name FROM sys_categories sc WHERE idx.period_type = sc.value and sc.category_type = :chuKy) periodTypeName,
                    (SELECT sc.name FROM sys_categories sc WHERE idx.type = sc.value and sc.category_type = :phanLoai) typeName,
                    CASE 
                        WHEN a.conversion_type = 'DON_VI' 
                        THEN COALESCE(
                           (
                             SELECT T.target
                             FROM target_data T
                             WHERE T.indicator_id = a.indicator_id
                               AND T.organization_id = o.organization_id
                             LIMIT 1
                           ),
                           (
                             SELECT T.target
                             FROM target_data T
                             WHERE T.indicator_id = a.indicator_id
                             AND T.organization_id = o.parent_id
                             LIMIT 1
                           )
                        )
                    END as target
                from kpi_indicator_masters im,
                hr_organizations org,
                kpi_indicator_conversions a,
                kpi_indicators idx
                join hr_organizations o on o.organization_id = :organizationId
                where im.indicator_master_id = a.indicator_master_id
                and im.status_id in (:status)
                and a.status in (:statusChild)
                and a.is_deleted = 'N'
                and im.is_deleted = 'N'
                and im.organization_id = org.organization_id
                and a.indicator_id = idx.indicator_id        
                """);
        Map<String, Object> params = new HashMap<>();
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        params.put("loaiHinhDonVi", Constant.CATEGORY_TYPES.LOAI_HINH_DON_VI);
        params.put("donViTinh", Constant.CATEGORY_TYPES.DON_VI_TINH);
        params.put("chuKy", Constant.CATEGORY_TYPES.CHU_KY);
        params.put("phanLoai", Constant.CATEGORY_TYPES.PHAN_LOAI);
        params.put("tableName", ObjectRelationsEntity.TABLE_NAMES.INDICATORS);
        params.put("referTableName", ObjectRelationsEntity.TABLE_NAMES.INDICATORS);
        params.put("functionCode", ObjectRelationsEntity.FUNCTION_CODES.CHI_SO_LIEN_QUAN);
        params.put("referTableName2", ObjectRelationsEntity.TABLE_NAMES.ORGANIZATIONS);
        params.put("functionCode2", ObjectRelationsEntity.FUNCTION_CODES.PHAM_VI_AP_DUNG);
        params.put("status", List.of(Constant.STATUS.PHE_DUYET, Constant.STATUS.CHO_PHE_DUYET, Constant.STATUS.CHO_PHE_DUYET_HIEU_LUC_LAI));
        params.put("statusChild", List.of(Constant.STATUS.PHE_DUYET));
        params.put("organizationId", dto.getOrgChildId() != null ? dto.getOrgChildId() : dto.getOrganizationId());
        params.put("statusList", List.of(OrganizationEvaluationsEntity.STATUS.PHE_DUYET, OrganizationEvaluationsEntity.STATUS.DANH_GIA, OrganizationEvaluationsEntity.STATUS.QLTT_DANH_GIA));
        if (Utils.isNullOrEmpty(dto.getListEmpId())) {
            //xac dinh lay ra ngan hang kpi
            Long indicatorMasterId = null;
            if (dto.getOrgChildId() != null) {
                indicatorMasterId = getIndicatorMasterId(dto.getOrgChildId(), dto.getJobId());
            }
            if (indicatorMasterId == null) {
                indicatorMasterId = getIndicatorMasterId(dto.getOrganizationId(), dto.getJobId());
            }
            sql.append(" and a.indicator_master_id = :indicatorMasterId ");
            params.put("indicatorMasterId", indicatorMasterId);
        } else {
            QueryUtils.filter(dto.getListEmpId(), sql, params, "a.indicator_conversion_id");
        }
        QueryUtils.filter(dto.getName(), sql, params, "idx.name");
        if (!Utils.isNullOrEmpty(dto.getListId())) {
            String sqlValueSelect = ("""
                    SELECT
                        CASE
                            WHEN a.indicator_conversion_id IN (:selectedValue) THEN 1
                            ELSE 0
                        END AS valueSelect, """);
            sql.replace(0, sql.length(), sql.toString().replaceFirst("SELECT a.indicator_conversion_id,", sqlValueSelect + " a.indicator_conversion_id,"));
            sql.append(" ORDER BY valueSelect DESC ");
            params.put("selectedValue", dto.getListId());
        }
        //Đoạn này cần fix lại truyền evaluationPeriodId hoặc lấy theo kỳ đang hiệu lực
        params.put("evaluationPeriodId", Utils.NVL(dto.getEvaluationPeriodId(), 46L));
        return getListPagination(sql.toString(), params, dto, IndicatorConversionsResponse.Indicator.class);
    }

    private Long getIndicatorMasterId(Long organizationId, Long jobId) {
        Map<String, Object> params = new HashMap<>();
        StringBuilder sql = new StringBuilder("""
                select im.indicator_master_id from kpi_indicator_masters im,
                hr_organizations org
                join hr_organizations o on o.organization_id = :organizationId
                where im.status_id in (:status)
                and im.organization_id = org.organization_id
                and im.is_deleted = 'N'
                and (((o.path_id like CONCAT(org.path_id , '%')
                            or org.path_id like CONCAT(o.path_id , '%')
                        )
                        and o.org_type_id = im.org_type_id
                        )
                        or (im.org_type_id is null and im.organization_id = :organizationId)
                    )
                """);
        if (jobId == null) {
            sql.append(" AND im.job_id IS NULL ");
        } else {
            sql.append(" and im.job_id = :jobId ");
            params.put("jobId", jobId);
        }
        params.put("organizationId", organizationId);
        params.put("status", List.of(Constant.STATUS.PHE_DUYET, Constant.STATUS.CHO_PHE_DUYET, Constant.STATUS.CHO_PHE_DUYET_HIEU_LUC_LAI));
        sql.append(" ORDER BY org.path_level DESC limit 1");
        return queryForObject(sql.toString(), params, Long.class);
    }


    public List<IndicatorConversionsResponse.Organization> getListOrganization(Long organizationId, Long orgTypeId) {
        StringBuilder sql = new StringBuilder("""
                select
                    a.organization_id,
                    o.name,
                    a.org_type_id,
                    sc.name as orgTypeName,
                    a.job_id,
                    jb.name as jobName
                FROM kpi_indicator_conversions a
                JOIN hr_organizations o ON o.organization_id = a.organization_id
                LEFT JOIN sys_categories sc ON (sc.value = a.org_type_id and sc.category_type = :loaiHinhDonVi AND sc.is_deleted = 'N')
                LEFT JOIN hr_jobs jb ON (jb.job_id = a.job_id and jb.is_deleted = 'N')
                where a.is_deleted = :activeStatus
                and a.status = :status
                and a.indicator_id IS NOT NULL
                """);
        Map<String, Object> params = new HashMap<>();
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        params.put("loaiHinhDonVi", Constant.CATEGORY_TYPES.LOAI_HINH_DON_VI);
        params.put("status", Constant.STATUS.PHE_DUYET);
        if (orgTypeId == null && organizationId == null) {
            sql.append(" GROUP BY a.organization_id");
        }
        if (organizationId != null) {
            sql.append(" and a.organization_id = :organizationId");
            params.put("organizationId", organizationId);
            if (orgTypeId != null) {
                sql.append(" and a.org_type_id = :orgTypeId and a.job_id IS NOT NULL");
                params.put("orgTypeId", orgTypeId);
                sql.append(" GROUP BY a.job_id");
            } else {
                sql.append(" GROUP BY a.org_type_id");
            }
        }
        return getListData(sql.toString(), params, IndicatorConversionsResponse.Organization.class);
    }


    public IndicatorConversionsResponse.Indicators getIndicators(Long orgTypeId, Long jobId, String organizationId) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    (select o.name from hr_organizations o where o.organization_id = :organizationId and o.is_deleted = 'N') organizationName
                """);
        HashMap<String, Object> params = new HashMap<>();
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        params.put("organizationId", organizationId);
        if (!Utils.isNullObject(orgTypeId)) {
            sql.append(", (select sc.name FROM sys_categories sc WHERE sc.value = :orgTypeId and sc.category_type = :loaiHinhDonVi) orgTypeName");
            params.put("orgTypeId", orgTypeId);
            params.put("loaiHinhDonVi", Constant.CATEGORY_TYPES.LOAI_HINH_DON_VI);
        }
        if (jobId != null) {
            sql.append(", (select jb.name from hr_jobs jb where jb.job_id = :jobId  and jb.is_deleted = 'N') jobName");
            params.put("jobId", jobId);
        }
        return getFirstData(sql.toString(), params, IndicatorConversionsResponse.Indicators.class);
    }

    public IndicatorConversionsResponse.Indicators getIndicatorsByMasterId(Long indicatorMasterId) {
        String sql = """
                select
                    a.full_name as   organizationName,
                    jb.name as jobName,
                    sc.name as orgTypeName
                from kpi_indicator_masters km 
                left join hr_organizations a on a.organization_id = km.organization_id
                left join hr_jobs jb on km.job_id = jb.job_id
                left join sys_categories sc on sc.value = km.org_type_id 
                    and sc.category_type = :loaiHinhDonVi
                WHERE km.indicator_master_id = :masterId
                """;
        HashMap<String, Object> params = new HashMap<>();
        params.put("masterId", indicatorMasterId);
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        params.put("loaiHinhDonVi", Constant.CATEGORY_TYPES.LOAI_HINH_DON_VI);
        return getFirstData(sql.toString(), params, IndicatorConversionsResponse.Indicators.class);
    }

    public IndicatorConversionsResponse.DetailBean getById(Long id) {
        StringBuilder sql = new StringBuilder("""
                select
                    a.indicator_conversion_id,
                    a.indicator_id,
                    a.note,
                    a.is_required,
                    a.conversion_type,
                    idx.unit_id,
                    idx.period_type,
                    idx.type as indicator_type,
                    idx.name as indicator_name,
                    idx.significance,
                    idx.measurement,
                    idx.system_info,
                    idx.rating_type,
                    idx.list_values,
                    (SELECT sc.name FROM sys_categories sc WHERE idx.unit_id = sc.value and sc.category_type = :donViTinh) unitName,
                    (SELECT sc.name FROM sys_categories sc WHERE idx.period_type = sc.value and sc.category_type = :chuKy) periodTypeName,
                    (SELECT sc.name FROM sys_categories sc WHERE idx.type = sc.value and sc.category_type = :phanLoai) typeName 
                FROM kpi_indicator_conversions a
                left join kpi_indicators idx on a.indicator_id = idx.indicator_id
                where a.is_deleted = 'N'
                and a.indicator_conversion_id = :id
                """);
        Map<String, Object> params = new HashMap<>();
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        params.put("donViTinh", Constant.CATEGORY_TYPES.DON_VI_TINH);
        params.put("chuKy", Constant.CATEGORY_TYPES.CHU_KY);
        params.put("phanLoai", Constant.CATEGORY_TYPES.PHAN_LOAI);
        params.put("id", id);
        return queryForObject(sql.toString(), params, IndicatorConversionsResponse.DetailBean.class);
    }

    public List<IndicatorConversionDetailEntity> getDetails(Long id) {
        String sql = """
                select a.* from kpi_indicator_conversion_details a
                left join sys_categories sc on sc.value = a.result_id and sc.category_type = 'KPI_THANG_DO'
                where a.indicator_conversion_id = :id
                and a.is_deleted = 'N'
                order by sc.order_number
                """;
        Map<String, Object> params = new HashMap<>();
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        params.put("id", id);
        return getListData(sql, params, IndicatorConversionDetailEntity.class);
    }

    public List<Long> getDataByOrgTypeId(Long orgTypeId, Long organizationId, Long jobId, List<String> statusList) {
        StringBuilder sql = new StringBuilder("""
                select
                    a.indicator_conversion_id
                FROM kpi_indicator_conversions a
                where a.is_deleted = :activeStatus
                and a.org_type_id = :orgTypeId
                and a.organization_id = :organizationId
                """);
        Map<String, Object> params = new HashMap<>();
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        params.put("orgTypeId", orgTypeId);
        params.put("organizationId", organizationId);
        if (jobId != null) {
            sql.append(" and a.job_id = :jobId");
            params.put("jobId", jobId);
        } else {
            sql.append(" AND a.job_id IS NULL");
        }
        if (!Utils.isNullOrEmpty(statusList)) {
            sql.append(" AND a.status NOT IN (:statusList)");
            params.put("statusList", statusList);
        }
        return getListData(sql.toString(), params, Long.class);
    }

    public Map<Long, IndicatorConversionsEntity> getDataDup(Long indicatorMasterId, Long id) {
        StringBuilder sql = new StringBuilder("""
                SELECT a.*
                FROM kpi_indicator_conversions a
                WHERE a.is_deleted = :activeStatus
                AND a.indicator_master_id = :indicatorMasterId
                """);
        Map<String, Object> params = new HashMap<>();
        if (id != null && id > 0) {
            sql.append(" AND a.indicator_conversion_id != :id");
            params.put("id", id);
        }
        params.put("indicatorMasterId", indicatorMasterId);
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        Map<Long, IndicatorConversionsEntity> result = new HashMap<>();
        List<IndicatorConversionsEntity> data = getListData(sql.toString(), params, IndicatorConversionsEntity.class);
        for (IndicatorConversionsEntity entity : data) {
            result.put(entity.getIndicatorId(), entity);
        }
        return result;
    }

    public IndicatorConversionsResponse.Indicators getJobName(Long indicatorMasterId) {
        StringBuilder sql = new StringBuilder("""
                select
                     CASE
                         WHEN a.job_id IS NULL THEN ''
                         ELSE (SELECT jb.name FROM hr_jobs jb WHERE jb.job_id = a.job_id AND jb.is_deleted = 'N')
                     END AS jobName,
                     (select sc1.name from sys_categories sc1
                        where sc1.value = f_get_kpi_level(a.organization_id, a.org_type_id, a.job_id)
                        and sc1.category_type = :CAP_KPI) AS kpiLevelName,
                     f_get_kpi_level(a.organization_id, a.org_type_id, a.job_id) as kpiLevel,
                    (select sc.name FROM sys_categories sc WHERE sc.value = a.org_type_id and sc.category_type = :loaiHinhDonVi) orgTypeName,
                    (select o.name from hr_organizations o where o.organization_id = a.organization_id and o.is_deleted = 'N') organizationName
                FROM kpi_indicator_masters a
                where a.is_deleted = :activeStatus
                and a.indicator_master_id = :indicatorMasterId
                """);
        Map<String, Object> params = new HashMap<>();
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        params.put("loaiHinhDonVi", Constant.CATEGORY_TYPES.LOAI_HINH_DON_VI);
        params.put("CAP_KPI", Constant.CATEGORY_TYPES.CAP_KPI);
        params.put("indicatorMasterId", indicatorMasterId);
        return getFirstData(sql.toString(), params, IndicatorConversionsResponse.Indicators.class);
    }

    public List<IndicatorsResponse.DataList> getIndicators(Long orgId) {
        StringBuilder sql = new StringBuilder("""
                SELECT 
                    i.*       
                FROM 
                    kpi_indicators i
                WHERE NVL(i.is_deleted, :activeStatus) = :activeStatus       
                AND exists (SELECT 1
                            FROM hr_organizations o
                            JOIN kpi_object_relations kor 
                            ON (i.indicator_id = kor.object_id
                            and kor.table_name = :tableName
                            and kor.refer_object_id = o.organization_id
                            and kor.refer_table_name = :referTableName
                            and kor.function_code = :functionCode
                            and NVL(kor.is_deleted, :activeStatus) = :activeStatus)
                            and exists (
                                    select 1 from hr_organizations pOrg
                                    where pOrg.organization_id = :referObjectId
                                    and pOrg.path_id like concat(o.path_id,'%')
                                ))    
                """);
        HashMap<String, Object> params = new HashMap<>();
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        params.put("tableName", ObjectRelationsEntity.TABLE_NAMES.INDICATORS);
        params.put("referTableName", ObjectRelationsEntity.TABLE_NAMES.ORGANIZATIONS);
        params.put("functionCode", ObjectRelationsEntity.FUNCTION_CODES.PHAM_VI_AP_DUNG);
        params.put("referObjectId", orgId);
        return getListData(sql.toString(), params, IndicatorsResponse.DataList.class);
    }

    public void copyConversionDetail(Long indicatorConversionIdSrc, Long indicatorConversionIdDest) {
        String sql = """
                insert into kpi_indicator_conversion_details(indicator_conversion_detail_id, indicator_conversion_id, 
                min_value, max_value, min_comparison, max_comparison, result_id, 
                is_deleted, created_by, created_time, modified_by, modified_time, note)
                select null, :indicatorConversionIdDest, min_value, max_value, min_comparison, 
                max_comparison, result_id, 
                is_deleted, created_by, now(), :userName, now(), note
                from kpi_indicator_conversion_details a 
                where a.indicator_conversion_id = :indicatorConversionIdSrc
                and a.is_deleted = 'N'
                """;
        Map<String, Object> params = new HashMap<>();
        params.put("indicatorConversionIdSrc", indicatorConversionIdSrc);
        params.put("indicatorConversionIdDest", indicatorConversionIdDest);
        params.put("userName", Utils.getUserNameLogin());
        executeSqlDatabase(sql, params);
    }

    public List<OrgDto> getOrgList(Long employeeId) {
        String sql = """
                select
                	distinct T.organization_id,
                	    T.full_name, T.type, T.path_order
                from
                	(
                	select
                		op.organization_id,
                		op.`name` full_name,
                		1 as type,
                		op.path_order
                	from
                		hr_employees wp ,
                		hr_organizations org,
                		hr_organizations op
                	where
                		wp.is_deleted = 'N'
                		and org.organization_id = wp.organization_id
                		and org.path_id like CONCAT(op.path_id, '%')
                		and (op.org_level_manage = 2
                		    or op.organization_id in (2, 3)
                		)
                		and wp.employee_id = :employeeId
                	) T
                order by
                	T.type,
                	T.path_order
                """;
        Map<String, Object> params = new HashMap<>();
        params.put("employeeId", employeeId);
        return getListData(sql, params, OrgDto.class);
    }
}
