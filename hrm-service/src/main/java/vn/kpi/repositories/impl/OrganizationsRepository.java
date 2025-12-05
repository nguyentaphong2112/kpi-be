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
import vn.kpi.models.dto.EmployeeDto;
import vn.kpi.models.dto.OrganizationDto;
import vn.kpi.models.request.OrganizationsRequest;
import vn.kpi.models.response.EmployeesResponse;
import vn.kpi.models.response.OrganizationsResponse;
import vn.kpi.repositories.BaseRepository;
import vn.kpi.repositories.entity.OrganizationsEntity;
import vn.kpi.utils.QueryUtils;
import vn.kpi.utils.Utils;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Lop repository Impl ung voi bang hr_organizations
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Repository
@RequiredArgsConstructor
public class OrganizationsRepository extends BaseRepository {

    public BaseDataTableDto<OrganizationsResponse.SearchResult> searchData(OrganizationsRequest.SearchForm dto) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    a.organization_id,
                    a.code,
                    a.name,
                    a.parent_id,
                    a.order_number,
                    a.path_order,
                    a.path_id,
                    a.path_level,
                    a.org_level_manage,
                    a.org_name_level_1,
                    a.org_name_level_2,
                    a.org_name_level_3,
                    a.org_name_level_4,
                    a.org_name_level_5,
                    a.full_name,
                    a.start_date,
                    a.end_date,
                    a.org_type_id,
                    a.is_deleted,
                    a.created_by,
                    a.created_time,
                    a.modified_by,
                    a.modified_time,
                    a.last_update_time
                """);
        HashMap<String, Object> params = new HashMap<>();
        addCondition(sql, params, dto);
        sql.append(" order by a.path_level, a.path_order");
        return getListPagination(sql.toString(), params, dto, OrganizationsResponse.SearchResult.class);
    }

    public List<Map<String, Object>> getListExport(OrganizationsRequest.SearchForm dto) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    a.organization_id,
                    a.code,
                    a.name,
                    a.parent_id,
                    a.order_number,
                    a.path_order,
                    a.path_id,
                    a.path_level,
                    a.org_level_manage,
                    a.org_name_level_1,
                    a.org_name_level_2,
                    a.org_name_level_3,
                    a.org_name_level_4,
                    a.org_name_level_5,
                    a.full_name,
                    a.start_date,
                    a.end_date,
                    a.org_type_id,
                    a.is_deleted,
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

    private void addCondition(StringBuilder sql, Map<String, Object> params, OrganizationsRequest.SearchForm dto) {
        sql.append("""
            FROM hr_organizations a
            WHERE IFNULL(a.is_deleted, :activeStatus) = :activeStatus
        """);
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        if (dto.getOrganizationId() != null && dto.getOrganizationId() > 0L) {
            sql.append(" AND a.path_id like :orgPath");
            params.put("orgPath", "%/" + dto.getOrganizationId() + "/%");
        }

        if (dto.getEffectiveStatus() != null) {
            if (BaseConstants.YES.equals(dto.getEffectiveStatus())) { // co hieu luc
                sql.append(" and IFNULL(a.start_date, a.start_date) <= :sysDate and IFNULL(a.end_date, :sysDate) >= :sysDate");
            } else {
                sql.append(" and (a.start_date > :sysDate or a.end_date < :sysDate)");
            }
            params.put("sysDate", Utils.truncDate(new Date()));
        }
        QueryUtils.filter(dto.getCode(), sql, params, "a.code");
        QueryUtils.filter(dto.getName(), sql, params, "a.name");
    }

    public OrganizationsResponse.DetailBean getOrganizationById(Long organizationId) {
        String sql = """
                select a.*,
                (select ho.name from hr_organizations ho where ho.organization_id = a.parent_id) parentName
                from hr_organizations a
                where IFNULL(a.is_deleted, :activeStatus) = :activeStatus
                and a.organization_id = :organizationId
                """;
        Map<String, Object> params = new HashMap<>();
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        params.put("organizationId", organizationId);
        return queryForObject(sql, params, OrganizationsResponse.DetailBean.class);
    }

    public List<OrganizationsResponse.DetailBean> getListOrg() {
        String sql = """
                select 
                	a.organization_id, a.`name`
                from hr_organizations a
                left join hr_organizations op on op.organization_id = a.parent_id
                where a.is_deleted = 'N'
                and	(a.org_level_manage = 2
                		or (
                			a.org_level_manage is null
                			and not exists (
                				select 1 from hr_organizations org
                				where org.parent_id = a.organization_id
                			)
                			and op.org_level_manage = 1
                		)
                	)
                order by a.path_order
                """;
        Map<String, Object> params = new HashMap<>();
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        return getListData(sql, params, OrganizationsResponse.DetailBean.class);
    };


    public List<OrganizationsResponse.DetailBean> getListOrg2() {
        String sql = """
                select 
                	a.organization_id, a.`name`
                from hr_organizations a
                left join hr_organizations op on op.organization_id = a.parent_id
                where a.is_deleted = 'N'
                and	op.org_level_manage = 2
                order by a.path_order
                """;
        Map<String, Object> params = new HashMap<>();
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        return getListData(sql, params, OrganizationsResponse.DetailBean.class);
    };

    public List<OrganizationsResponse.RelatedOrgDTO> getListRelatedOrg(Long organizationId) {
        String sql = """
                    select a.constraint_org_id, ho.name constraintOrgName
                    from hr_related_organizations a
                    join hr_organizations ho on ho.organization_id = a.constraint_org_id
                    where IFNULL(a.is_deleted, :activeStatus) = :activeStatus
                    and a.organization_id = :organizationId
                """;
        Map<String, Object> params = new HashMap<>();
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        params.put("organizationId", organizationId);
        return getListData(sql, params, OrganizationsResponse.RelatedOrgDTO.class);
    }

    public void updateOrgPath(Long organizationId) {
        String sql = "CALL proc_update_org_path(:orgId)";
        HashMap<String, Object> params = new HashMap<>();
        params.put("orgId", organizationId);
        executeSqlDatabase(sql, params);
    }

    public void updateOrgOrder() {
        String sql = "CALL proc_reset_org_order()";
        HashMap<String, Object> params = new HashMap<>();
        executeSqlDatabase(sql, params);
    }

    public void deleteRelatedOrg(Long organizationId) {
        String sql = """
                delete a from hr_related_organizations a 
                where a.organization_id = :organizationId                
                """;
        Map<String, Object> map = new HashMap<>();
        map.put("organizationId", organizationId);
        executeSqlDatabase(sql, map);
    }

    public void deleteAttributes(Long organizationId, String tableName) {
        String sql = """
                delete a from hr_object_attributes a 
                where a.object_id = :organizationId
                and a.table_name = :tableName               
                """;
        Map<String, Object> map = new HashMap<>();
        map.put("organizationId", organizationId);
        map.put("tableName", tableName);
        executeSqlDatabase(sql, map);
    }

    public BaseDataTableDto<EmployeesResponse.SearchResult> searchListPayroll(OrganizationsRequest.SearchForm dto) {
        StringBuilder sql = new StringBuilder("""
                    SELECT
                    e.employee_id,
                    e.employee_code,
                    e.full_name,
                    e.email,
                    e.mobile_number,
                    ho.name orgName,
                    hj.name jobName
                """);
        HashMap<String, Object> params = new HashMap<>();
        addConditionSearchListPayroll(dto, sql, params);
        return getListPagination(sql.toString(), params, dto, EmployeesResponse.SearchResult.class);
    }

    private void addConditionSearchListPayroll(OrganizationsRequest.SearchForm dto, StringBuilder sql, HashMap<String, Object> params) {
        sql.append("""
                    FROM hr_employees e
                    JOIN hr_organizations ho ON ho.organization_id = e.organization_id
                    LEFT JOIN hr_jobs hj ON hj.job_id = e.job_id
                    WHERE IFNULL(e.is_deleted, :activeStatus) = :activeStatus
                """);
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        if (!Utils.isNullOrEmpty(dto.getKeySearch())) {
            sql.append(" and (");
            sql.append("    upper(e.full_name) like :keyword");
            sql.append("    or upper(e.email) like :keyword");
            sql.append("    or e.mobile_number like :keyword");
            sql.append("    or e.employee_code like :keyword");
            sql.append(" )");
            params.put("keyword", "%" + dto.getKeySearch().trim().toUpperCase() + "%");
        }

        if (dto.getOrganizationId() != null && dto.getOrganizationId() > 0L) {
            sql.append(" AND ho.path_id like :orgPath");
            params.put("orgPath", "%/" + dto.getOrganizationId() + "/%");
        }
        sql.append(" ORDER BY e.employee_id");
    }

    public boolean checkUsedOrgById(Long organizationId) {
        String sql = """
                SELECT count(1)
                FROM hr_organizations ho
                WHERE IFNULL(ho.is_deleted, :activeStatus) = :activeStatus
                AND (
                    ho.parent_id = :organizationId
                    OR EXISTS (
                        SELECT 1 FROM hr_related_organizations ro
                        WHERE IFNULL(ro.is_deleted, :activeStatus) = :activeStatus
                        AND ro.constraint_org_id = :organizationId
                    )
                    OR EXISTS (
                        SELECT 1 FROM hr_employees e
                        WHERE IFNULL(e.is_deleted, :activeStatus) = :activeStatus
                        AND e.organization_id = :organizationId
                    )
                )
                """;
        HashMap<String, Object> params = new HashMap<>();
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        params.put("organizationId", organizationId);
        return queryForObject(sql, params, Integer.class) > 0;
    }

    public boolean isDuplicateOrgCode(Long orgId, OrganizationsRequest.SubmitForm dto) {
        StringBuilder sql = new StringBuilder("""
            SELECT count(1)
            FROM hr_organizations
            WHERE IFNULL(is_deleted, :activeStatus) = :activeStatus
            AND organization_id <> :orgId
            AND LOWER(code) = :code
            AND IFNULL(end_date, :startDate) >= :startDate
        """);
        Map<String, Object> params = new HashMap<>();
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        params.put("code", dto.getCode().toLowerCase());
        params.put("orgId", Utils.NVL(orgId));
        params.put("startDate", dto.getStartDate());
        if(dto.getEndDate() != null){
            sql.append(" AND start_date <= :endDate");
            params.put("endDate", dto.getEndDate());
        }
        return queryForObject(sql.toString(), params, Integer.class) > 0;
    }

    public List<EmployeeDto> getEmployeeForHierarchy(Long orgId) {
        String sql = """
                select e.employee_id, e.employee_code,
                    e.full_name, jb.name as job_name, org.name as organizationName
                    from hr_organizations org, hr_employees e
                    left join hr_jobs jb on e.job_id = jb.job_id                     
                    where org.organization_id = e.organization_id
                    and org.path_id like :orgPath
                    and org.management_org_id = :orgManagementId
                    and ifnull(e.is_deleted, :isDeleted) = :isDeleted
                    order by jb.order_number, jb.name
                """;
        Map<String, Object> params = new HashMap<>();
        params.put("isDeleted", BaseConstants.STATUS.NOT_DELETED);
        params.put("orgPath", "%/" + orgId + "/%");
        params.put("orgManagementId", get(OrganizationsEntity.class, orgId).getManagementOrgId());
        return getListData(sql, params, EmployeeDto.class);
    }

    public List<OrganizationDto> getOrgForHierarchy(Long orgId) {
        String sql = """
                select org.organization_id, org.name,
                    ifnull(sc.name,'Chưa xác định') as orgTypeName
                    from hr_organizations op, hr_organizations org
                    left join sys_categories sc on sc.value = org.org_type_id 
                        and sc.category_type = 'HR_LOAI_HINH_DON_VI'                     
                    where org.parent_id = op.organization_id
                    and org.path_id like :orgPath
                    and op.management_org_id = :orgManagementId
                    and ifnull(op.is_deleted, :isDeleted) = :isDeleted
                    and not exists (
                        select 1 from sys_category_attributes st
                        where st.category_id = sc.category_id
                        and st.attribute_code = 'LA_DON_VI_AO'
                        and st.attribute_value = 'Y'
                    )
                    order by sc.order_number, sc.name
                """;
        Map<String, Object> params = new HashMap<>();
        params.put("isDeleted", BaseConstants.STATUS.NOT_DELETED);
        params.put("orgPath", "%/" + orgId + "/%");
        params.put("orgManagementId", get(OrganizationsEntity.class, orgId).getManagementOrgId());
        return getListData(sql, params, OrganizationDto.class);
    }

    public List<OrganizationsResponse.ChartDto> getChartByGender(Long organizationId) {
        StringBuilder sql = new StringBuilder("""
            SELECT COUNT(e.employee_id) total, sc.name
            FROM hr_employees e
            JOIN hr_organizations o ON o.organization_id = e.organization_id
            JOIN sys_categories sc ON (sc.value = e.gender_id and sc.category_type = :genderTypeCode)
            WHERE e.is_deleted = 'N'
            AND e.status = :statusWorking
            AND o.path_id like :pathId
        """);
        HashMap<String, Object> params = new HashMap<>();
        params.put("genderTypeCode", Constant.CATEGORY_CODES.GIOI_TINH);
        params.put("statusWorking", Constant.EMP_STATUS.WORK_IN);
        params.put("pathId", "%/" + organizationId + "/%");
        sql.append(" GROUP BY sc.name");
        return getListData(sql.toString(), params, OrganizationsResponse.ChartDto.class);
    }

    public List<OrganizationsResponse.ChartDto> getChartByEducationLevel(Long organizationId) {
        StringBuilder sql = new StringBuilder("""
            SELECT COUNT(e.employee_id) total, sc.name
            FROM hr_employees e
            JOIN hr_organizations o ON o.organization_id = e.organization_id
            JOIN sys_categories sc ON (sc.value = e.education_level_id and sc.category_type = :educationLevelCode)
            WHERE e.is_deleted = 'N'
            AND e.status = :statusWorking
            AND o.path_id like :pathId
        """);
        HashMap<String, Object> params = new HashMap<>();
        params.put("educationLevelCode", Constant.CATEGORY_CODES.TRINH_DO_VAN_HOA);
        params.put("statusWorking", Constant.EMP_STATUS.WORK_IN);
        params.put("pathId", "%/" + organizationId + "/%");
        sql.append(" GROUP BY sc.name");
        return getListData(sql.toString(), params, OrganizationsResponse.ChartDto.class);
    }

    public List<OrganizationsResponse.ChartDto> getChartByEmpType(Long organizationId) {
        StringBuilder sql = new StringBuilder("""
            SELECT COUNT(e.employee_id) total, et.name
            FROM hr_employees e
            JOIN hr_organizations o ON o.organization_id = e.organization_id
            JOIN hr_emp_types et ON et.emp_type_id = e.emp_type_id
            WHERE e.is_deleted = 'N'
            AND e.status = :statusWorking
            AND o.path_id like :pathId
        """);
        HashMap<String, Object> params = new HashMap<>();
        params.put("statusWorking", Constant.EMP_STATUS.WORK_IN);
        params.put("pathId", "%/" + organizationId + "/%");
        sql.append(" GROUP BY et.name");
        return getListData(sql.toString(), params, OrganizationsResponse.ChartDto.class);
    }

    public List<OrganizationsResponse.ChartDto> getChartByOrg(Long organizationId, Date toDate) {
        StringBuilder sql = new StringBuilder("""
                    SELECT COUNT(e.employee_id) total, o.name name
                    FROM hr_employees e
                    JOIN hr_work_process wp ON wp.employee_id = e.employee_id
                    JOIN hr_organizations o ON o.organization_id = wp.organization_id
                    JOIN hr_document_types dt ON dt.document_type_id = wp.document_type_id
                    WHERE e.is_deleted = 'N'
                    AND wp.is_deleted = 'N'
                    AND e.status = :statusWorking
                    AND dt.type != 'OUT'
                    AND wp.start_date <= :toDate
                    AND (wp.end_date is null or wp.end_date >= :toDate)
                    AND EXISTS (
                        select 1 from hr_organizations pOrg
                        where pOrg.organization_id = :organizationId
                        and o.path_id like concat(pOrg.path_id, '%')
                    )
                """);
        HashMap<String, Object> params = new HashMap<>();
        params.put("statusWorking", Constant.EMP_STATUS.WORK_IN);
        params.put("toDate", toDate);
        params.put("organizationId", organizationId);
        sql.append("GROUP BY o.organization_id, o.name");
        return getListData(sql.toString(), params, OrganizationsResponse.ChartDto.class);
    }

    public List<OrganizationsResponse.ChartDto> getChartLaborStructure(Long organizationId, Date dateReport) {
        String sql = """
                    SELECT count(wp.employee_id) total, et.name
                    FROM hr_work_process wp
                    JOIN hr_document_types dt ON dt.document_type_id = wp.document_type_id
                    JOIN hr_organizations ho ON ho.organization_id = wp.organization_id
                    JOIN hr_emp_types et ON et.emp_type_id = (
                        SELECT emp_type_id FROM hr_contract_process
                        WHERE is_deleted = 'N'
                        AND start_date >= wp.start_date
                        AND start_date <= :reportDate
                        AND (end_date >= :reportDate OR end_date IS NULL)
                        AND employee_id = wp.employee_id
                        limit 1
                    )
                    WHERE wp.is_deleted = 'N'
                    AND dt.type <> 'OUT'
                    AND wp.start_date <= :reportDate
                    AND (wp.end_date >= :reportDate OR wp.end_date IS NULL)
                    AND ho.path_id LIKE :pathId
                    GROUP BY et.name
                """;
        HashMap<String, Object> params = new HashMap<>();
        params.put("reportDate", dateReport);
        params.put("pathId", "%/" + organizationId + "/%");
        return getListData(sql, params, OrganizationsResponse.ChartDto.class);
    }
}
