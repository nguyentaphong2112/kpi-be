/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.kpi.repositories.impl;

import org.springframework.stereotype.Repository;
import lombok.RequiredArgsConstructor;
import vn.kpi.constants.Constant;
import vn.kpi.models.BaseDataTableDto;
import vn.kpi.models.request.OrganizationEvaluationsRequest;
import vn.kpi.models.response.OrganizationEvaluationsResponse;
import vn.kpi.repositories.BaseRepository;
import vn.kpi.models.request.OrganizationIndicatorsRequest;
import vn.kpi.models.response.OrganizationIndicatorsResponse;
import vn.kpi.constants.BaseConstants;
import vn.kpi.repositories.entity.ObjectRelationsEntity;
import vn.kpi.utils.QueryUtils;
import vn.kpi.utils.Utils;

import java.util.*;

/**
 * Lop repository Impl ung voi bang kpi_organization_indicators
 *
 * @author tudd
 * @version 1.0
 * @since 1.0
 */

@Repository
@RequiredArgsConstructor
public class OrganizationIndicatorsRepository extends BaseRepository {

    public BaseDataTableDto searchData(OrganizationIndicatorsRequest.SearchForm dto) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    a.organization_indicator_id,
                    a.indicator_conversion_id,
                    a.indicator_id,
                    a.organization_evaluation_id,
                    a.percent,
                    a.target,
                    a.created_by,
                    a.created_time,
                    a.modified_by,
                    a.modified_time,
                    a.last_update_time
                """);
        HashMap<String, Object> params = new HashMap<>();
        addCondition(sql, params, dto);
        return getListPagination(sql.toString(), params, dto, OrganizationIndicatorsResponse.SearchResult.class);
    }

    public List<Map<String, Object>> getListExport(OrganizationIndicatorsRequest.SearchForm dto) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    a.organization_indicator_id,
                    a.indicator_conversion_id,
                    a.indicator_id,
                    a.organization_evaluation_id,
                    a.percent,
                    a.target,
                    a.created_by,
                    a.created_time,
                    a.modified_by,
                    a.modified_time,
                    a.last_update_time
                """);
        Map<String, Object> params = new HashMap<>();
        addCondition(sql, params, dto);
        return getListData(sql.toString(), params);
    }

    private void addCondition(StringBuilder sql, Map<String, Object> params, OrganizationIndicatorsRequest.SearchForm dto) {
        sql.append("""
                    FROM kpi_organization_indicators a
                
                
                    WHERE IFNULL(a.is_deleted, :activeStatus) = :activeStatus
                """);
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        //QueryUtils.filterGe(dto.getFromDate(), sql, params, "a.from_date", "fromDate");
        //QueryUtils.filterLe(dto.getToDate(), sql, params, "a.from_date", "toDate");
        sql.append(" ORDER BY mo.display_seq, mo.path_id");
    }

    public List<Long> getListId(Long organizationEvaluationId) {
        StringBuilder sql = new StringBuilder("""
                select
                    a.organization_indicator_id
                FROM kpi_organization_indicators a
                where a.is_deleted = :activeStatus
                and a.organization_evaluation_id = :organizationEvaluationId
                """);
        Map<String, Object> params = new HashMap<>();
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        params.put("organizationEvaluationId", organizationEvaluationId);
        return getListData(sql.toString(), params, Long.class);
    }

    public List<OrganizationIndicatorsResponse.OrganizationEvaluation> getDataByEvaluationId(OrganizationEvaluationsRequest.SearchForm dto) {
        StringBuilder sql = new StringBuilder("""
                select
                    a.*,
                    idx.name as indicator_name,
                    kic.conversion_type,
                    kic.is_focus_reduction,
                    idx.significance,
                    idx.measurement,
                    idx.system_info,
                    idx.rating_type,
                    idx.list_values,
                    idx.type,
                    kic.note,
                    CASE
                                 WHEN a.leader_type = 1 THEN
                                     (SELECT GROUP_CONCAT(o.name SEPARATOR ', ')
                                      FROM hr_organizations o
                                      WHERE a.leader_ids REGEXP CONCAT('(^|;)', o.organization_id, '(;|$)'))
                                 WHEN a.leader_type = 2 THEN
                                     (SELECT GROUP_CONCAT(sc.name SEPARATOR ', ')
                                      FROM sys_categories sc
                                      WHERE a.leader_ids REGEXP CONCAT('(^|;)', sc.value, '(;|$)')
                                      AND sc.category_type = 'HR_LOAI_HINH_DON_VI')
                                 WHEN a.leader_type = 3 THEN
                                     (SELECT GROUP_CONCAT(e.full_name SEPARATOR ', ')
                                      FROM hr_employees e
                                      WHERE a.leader_ids REGEXP CONCAT('(^|;)', e.employee_id, '(;|$)'))
                                 ELSE ''
                            END AS leader_name,
                        CASE
                                 WHEN a.collaborator_type = 1 THEN
                                     (SELECT GROUP_CONCAT(o.name SEPARATOR ', ')
                                      FROM hr_organizations o
                                      WHERE a.collaborator_ids REGEXP CONCAT('(^|;)', o.organization_id, '(;|$)'))
                                 WHEN a.collaborator_type = 2 THEN
                                     (SELECT GROUP_CONCAT(sc.name SEPARATOR ', ')
                                      FROM sys_categories sc
                                      WHERE a.collaborator_ids REGEXP CONCAT('(^|;)', sc.value, '(;|$)')
                                      AND sc.category_type = 'HR_LOAI_HINH_DON_VI')
                                 WHEN a.collaborator_type = 3 THEN
                                     (SELECT GROUP_CONCAT(e.full_name SEPARATOR ', ')
                                      FROM hr_employees e
                                      WHERE a.collaborator_ids REGEXP CONCAT('(^|;)', e.employee_id, '(;|$)'))
                                 ELSE ''
                            END AS collaborator_name,
                    (SELECT sc.name FROM sys_categories sc WHERE idx.unit_id = sc.value and sc.category_type = :donViTinh) unitName,
                    (SELECT sc.name FROM sys_categories sc WHERE idx.period_type = sc.value and sc.category_type = :chuKy) periodTypeName,
                    (SELECT sc.name FROM sys_categories sc WHERE idx.type = sc.value and sc.category_type = :phanLoai) typeName,
                    (SELECT sc.name FROM sys_categories sc WHERE a.status_level1 = sc.value and sc.category_type = :statusLevel1) statusNameLevel1
                FROM kpi_organization_indicators a
                join kpi_indicators idx on a.indicator_id = idx.indicator_id
                join kpi_indicator_conversions kic on a.indicator_conversion_id = kic.indicator_conversion_id
                join kpi_organization_evaluations oe on a.organization_evaluation_id = oe.organization_evaluation_id
                where a.is_deleted = :activeStatus
                and a.status = 'ACTIVE'
                """);
        Map<String, Object> params = new HashMap<>();
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        params.put("donViTinh", Constant.CATEGORY_TYPES.DON_VI_TINH);
        params.put("chuKy", Constant.CATEGORY_TYPES.CHU_KY);
        params.put("phanLoai", Constant.CATEGORY_TYPES.PHAN_LOAI);
        params.put("statusLevel1", Constant.CATEGORY_TYPES.STATUS_KPI_LEVEL1);
        params.put("tableName", ObjectRelationsEntity.TABLE_NAMES.INDICATORS);
        params.put("referTableName", ObjectRelationsEntity.TABLE_NAMES.INDICATORS);
        params.put("functionCode", ObjectRelationsEntity.FUNCTION_CODES.CHI_SO_LIEN_QUAN);
        params.put("referTableName2", ObjectRelationsEntity.TABLE_NAMES.ORGANIZATIONS);
        params.put("functionCode2", ObjectRelationsEntity.FUNCTION_CODES.PHAM_VI_AP_DUNG);
        QueryUtils.filter(dto.getOrganizationEvaluationId(), sql, params, "a.organization_evaluation_id");
        QueryUtils.filter(dto.getKeySearch(), sql, params, "idx.name");
        QueryUtils.filter(dto.getEvaluationPeriodId(), sql, params, "oe.evaluation_period_id");
        QueryUtils.filter(dto.getOrganizationId(), sql, params, "oe.organization_id");
        return getListData(sql.toString(), params, OrganizationIndicatorsResponse.OrganizationEvaluation.class);
    }

    public Map<Long, Set<Long>> getOrgValid(Set<Long> leaderIdSetAll, List<Long> orgPermissionIds) {
        StringBuilder sql = new StringBuilder("""
            SELECT org.organization_id AS parent_id, o.organization_id AS child_id, o.path_level AS path_level
            FROM hr_organizations o, hr_organizations org
            WHERE o.path_id LIKE CONCAT(org.path_id, '%')
              AND o.organization_id IN (:orgPermissionIds)
              AND org.organization_id IN (:leaderIdSetAll)
            """);

        Map<String, Object> params = new HashMap<>();
        params.put("orgPermissionIds", orgPermissionIds);
        params.put("leaderIdSetAll", new ArrayList<>(leaderIdSetAll));

        List<Map<String, Object>> rows = getListData(sql.toString(), params);

        Map<Long, Set<Long>> result = new HashMap<>();
        for (Map<String, Object> row : rows) {
            Long parentId = ((Number) row.get("parent_id")).longValue();
            Long childId;
            int pathLevel = ((Number) row.get("path_level")).intValue();

            if (pathLevel >= 4) {
                childId = parentId;
            } else {
                childId = ((Number) row.get("child_id")).longValue();
            }


            result.computeIfAbsent(parentId, k -> new HashSet<>()).add(childId);
        }

        return result;
    }


    public BaseDataTableDto getDataTableByEvaluationId(OrganizationEvaluationsRequest.OrgParent data) {
        StringBuilder sql = new StringBuilder("""
                select
                    a.target,
                    a.percent,
                    idx.name as indicator_name,
                    CASE
                                 WHEN a.leader_type = 1 THEN
                                     (SELECT GROUP_CONCAT(o.name SEPARATOR ', ')
                                      FROM hr_organizations o
                                      WHERE a.leader_ids REGEXP CONCAT('(^|;)', o.organization_id, '(;|$)'))
                                 WHEN a.leader_type = 2 THEN
                                     (SELECT GROUP_CONCAT(sc.name SEPARATOR ', ')
                                      FROM sys_categories sc
                                      WHERE a.leader_ids REGEXP CONCAT('(^|;)', sc.value, '(;|$)')
                                      AND sc.category_type = 'HR_LOAI_HINH_DON_VI')
                                 WHEN a.leader_type = 3 THEN
                                     (SELECT GROUP_CONCAT(e.full_name SEPARATOR ', ')
                                      FROM hr_employees e
                                      WHERE a.leader_ids REGEXP CONCAT('(^|;)', e.employee_id, '(;|$)'))
                                 ELSE ''
                            END AS leader_name,
                        CASE
                                 WHEN a.collaborator_type = 1 THEN
                                     (SELECT GROUP_CONCAT(o.name SEPARATOR ', ')
                                      FROM hr_organizations o
                                      WHERE a.collaborator_ids REGEXP CONCAT('(^|;)', o.organization_id, '(;|$)'))
                                 WHEN a.collaborator_type = 2 THEN
                                     (SELECT GROUP_CONCAT(sc.name SEPARATOR ', ')
                                      FROM sys_categories sc
                                      WHERE a.collaborator_ids REGEXP CONCAT('(^|;)', sc.value, '(;|$)')
                                      AND sc.category_type = 'HR_LOAI_HINH_DON_VI')
                                 WHEN a.collaborator_type = 3 THEN
                                     (SELECT GROUP_CONCAT(e.full_name SEPARATOR ', ')
                                      FROM hr_employees e
                                      WHERE a.collaborator_ids REGEXP CONCAT('(^|;)', e.employee_id, '(;|$)'))
                                 ELSE ''
                            END AS collaborator_name
                FROM kpi_organization_indicators a
                join kpi_indicators idx on a.indicator_id = idx.indicator_id
                join kpi_organization_evaluations oe on oe.organization_id = :organizationId and oe.evaluation_period_id = :periodId and oe.is_deleted = :activeStatus
                where a.is_deleted = :activeStatus
                and a.status = 'ACTIVE'
                and a.organization_evaluation_id = oe.organization_evaluation_id
                """);
        Map<String, Object> params = new HashMap<>();
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        params.put("organizationId", data.getOrgParentId());
        params.put("periodId", data.getEvaluationPeriodId());
        return getListPagination(sql.toString(), params, data, OrganizationEvaluationsResponse.OrgParent.class);
    }

    public void deActiveByListId(List<Long> listId) {
        StringBuilder sql = new StringBuilder("""
                UPDATE kpi_organization_indicators
                       SET status = 'INACTIVE',
                       modified_by = :userName,
                       modified_time = now()
                       WHERE organization_indicator_id IN (:ids)
                """);
        Map<String, Object> params = new HashMap<>();
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        params.put("userName", Utils.getUserNameLogin());
        params.put("ids", listId);
        executeSqlDatabase(sql.toString(), params);
    }
}
