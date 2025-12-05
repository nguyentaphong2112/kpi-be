/*
 * Copyright (C) 2022 EcoIT. All rights reserved.
 * EcoIT. Use is subject to license terms.
 */
package vn.hbtplus.repositories.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.constants.Constant;
import vn.hbtplus.constants.Scope;
import vn.hbtplus.models.BaseDataTableDto;
import vn.hbtplus.models.PermissionDataDto;
import vn.hbtplus.models.dto.ConfigApprovalsDTO;
import vn.hbtplus.models.dto.ContractApproversDTO;
import vn.hbtplus.models.dto.EmployeesDTO;
import vn.hbtplus.models.dto.OrgDTO;
import vn.hbtplus.models.response.ConfigApprovalsResponse;
import vn.hbtplus.repositories.BaseRepository;
import vn.hbtplus.services.AuthorizationService;
import vn.hbtplus.utils.QueryUtils;
import vn.hbtplus.utils.Utils;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Lop repository Impl ung voi bang PNS_CONFIG_APPROVALS
 *
 * @author tudd
 * @version 1.0
 * @since 1.0
 */

@Repository
@RequiredArgsConstructor
public class ConfigApprovalsRepositoryImpl extends BaseRepository {
    private final AuthorizationService authorizationService;

    public BaseDataTableDto<ConfigApprovalsResponse> searchData(ConfigApprovalsDTO dto) {
        StringBuilder sql = new StringBuilder("""
                    SELECT
                        pca.config_approval_id configApprovalId,
                        pca.document_no documentNo,
                        he.full_name approverName,
                        (SELECT sc.name FROM sys_categories sc WHERE sc.value = pca.type AND sc.category_type = :configTypeCode) typeStr,
                        NVL(o.full_name, o.name) orgName,
                        mpg.name empPgrName,
                        pca.job_approver_id,
                        pca.from_date fromDate,
                        pca.to_date toDate,
                        mj.name jobApproverName
                """
        );
        HashMap<String, Object> params = new HashMap<>();
        params.put("loaiChiNhanh", Constant.LOOKUP_CODES.LOAI_HINH_CHI_NHANH);
        addCondition(sql, params, dto);
        sql.append(" ORDER BY pca.config_approval_id");
        return getListPagination(sql.toString(), params, dto, ConfigApprovalsResponse.class);
    }

    public boolean isDuplicate(ConfigApprovalsDTO dto) {
        Map<String, Object> mapParams = new HashMap<>();
        StringBuilder sql = new StringBuilder();
        sql.append("""
                     SELECT COUNT(1)
                     FROM pns_config_approvals pca
                     WHERE IFNULL(pca.is_deleted, :flagStatus) = :flagStatus
                     AND pca.type = :type
                     AND pca.config_approval_id != :configApprovalId
                     AND IFNULL(pca.to_date,:fromDate) >= :fromDate
                     AND pca.from_date <= IFNULL(:toDate, pca.from_date)
                """);

        mapParams.put("flagStatus", BaseConstants.STATUS.ACTIVE);
        mapParams.put("type", dto.getType());
        mapParams.put("configApprovalId", Utils.NVL(dto.getConfigApprovalId()));
        mapParams.put("fromDate", dto.getFromDate());
        mapParams.put("toDate", dto.getToDate());
        if (Utils.isNullObject(dto.getOrganizationId())) {
            sql.append(" AND pca.organization_id IS NULL");
        } else {
            sql.append(" AND pca.organization_id = :orgId");
            mapParams.put("orgId", dto.getOrganizationId());
        }

        if (!Utils.isNullOrEmpty(dto.getOrgGroup())) {
            sql.append(" AND pca.org_group = :orgGroup");
            mapParams.put("orgGroup", dto.getOrgGroup());
        } else {
            sql.append(" AND pca.org_group IS NULL");
        }

        if (Utils.isNullObject(dto.getEmpPgrId())) {
            sql.append(" and pca.emp_pgr_id IS NULL");
        } else {
            sql.append(" and pca.emp_pgr_id = :positionGroupId");
            mapParams.put("positionGroupId", dto.getEmpPgrId());
        }
        if (Utils.isNullObject(dto.getBranchType())) {
            sql.append(" and pca.branch_type IS NULL");
        } else {
            sql.append(" and pca.branch_type = :branchType");
            mapParams.put("branchType", dto.getBranchType());
        }
        Integer count = queryForObject(sql.toString(), mapParams, Integer.class);
        return count > 0;
    }

    public OrgDTO getOrg(Long orgId) {
        Map<String, Object> mapParam = new HashMap<>();
        mapParam.put("orgId", orgId);
        String sql = "SELECT org_id orgId, name orgName, path_id path FROM hr_organizations WHERE org_id = :orgId";
        return this.getFirstData(sql, mapParam, OrgDTO.class);
    }

    public EmployeesDTO getEmp(Long employeeId) {
        StringBuilder sql = new StringBuilder();
        Map<String, Object> mapParam = new HashMap<>();
        mapParam.put("employeeId", employeeId);
        sql.append(" SELECT");
        sql.append("    employee_id,");
        sql.append("    employee_code,");
        sql.append("    full_name");
        sql.append(" FROM");
        sql.append("    hr_employees");
        sql.append(" WHERE");
        sql.append("    employee_id = :employeeId");
        return this.getFirstData(sql.toString(), mapParam, EmployeesDTO.class);
    }

    public List<ConfigApprovalsDTO> getConfigApprovers(int type, Long empId, Long posId, Long orgId, Date reportDate) {
        String sql = """
                    select
                        o.organization_id organizationId,
                        a.document_no, a.job_approver_id, a.approver_id, a.emp_pgr_id, a.org_group
                    from pns_config_approvals a, hr_organizations o
                    where (a.organization_id = o.organization_id or a.org_group = o.org_group)
                    and a.type = :type
                    and IFNULL(a.approver_id,0) <> :empId
                    and (select path_id from hr_organizations where org_id = :orgId) like CONCAT(o.path_id, '%')
                    and :reportDate between a.from_date and IFNULL(a.to_date,:reportDate)
                    and (a.branch_type is null or a.branch_type = o.org_type_id)
                    and (
                        a.emp_pgr_id is null
                        or exists (
                            select 1 from mp_pos_of_groups mpg
                            where mpg.pgr_id = a.emp_pgr_id
                            and mpg.pos_id = :posId
                        )
                    )
                    and o.path_id is not null
                    and a.is_deleted = :activeStatus
                    order by o.path_level desc, IFNULL(a.organization_id,0) desc, IFNULL(a.branch_type,'ZZZ'), IFNULL(a.approver_id,0) desc , IFNULL(a.emp_pgr_id, 0) desc
                """;
        HashMap<String, Object> params = new HashMap<>();
        params.put("activeStatus", BaseConstants.STATUS.ACTIVE);
        params.put("type", type);
        params.put("posId", posId);
        params.put("empId", empId);
        params.put("reportDate", reportDate);
        params.put("orgId", orgId);
        return getListData(sql, params, ConfigApprovalsDTO.class);
    }

    public String getPosNameOfEmp(Long approverId) {
        String sql = """
                    select jb.job_name from mp_jobs jb, hr_employees e
                    where jb.job_id = e.job_id
                    and e.employee_id = :approverId
                """;
        HashMap<String, Object> params = new HashMap<>();
        params.put("approverId", approverId);
        return getFirstData(sql, params, String.class);
    }

    public ContractApproversDTO getApprover(Long organizationId, Long jobId, Date reportDate) {
        String sql = """
                    select wp.employee_id as employeeId,
                        (select job_name from mp_jobs where job_id = p.job_id) as positionName,
                        wp.position_id
                    from hr_work_process wp, hr_document_types dt, mp_positions p, hr_organizations o
                    where wp.document_type_id = dt.document_type_id
                    and wp.is_deleted = :activeStatus
                    and wp.position_id = p.pos_id
                    and :reportDate between wp.from_date and IFNULL(wp.to_date,:reportDate)
                    and dt.type <> 'OUT'
                    and p.pos_id = wp.position_id
                    and p.job_id = :jobId
                    and o.organization_id = wp.organization_id
                    and o.path_id like :orgPath
                    order by wp.employee_id
                """;
        HashMap<String, Object> params = new HashMap<>();
        params.put("activeStatus", BaseConstants.STATUS.ACTIVE);
        params.put("jobId", jobId);
        params.put("reportDate", reportDate);
        params.put("orgPath", "%/" + organizationId + "/%");
        List<ContractApproversDTO> approverDTOS = getListData(sql, params, ContractApproversDTO.class);
        return approverDTOS.isEmpty() ? null : approverDTOS.get(0);
    }

    public List<Map<String, Object>> getDataConfigApproval(ConfigApprovalsDTO dto) {
        StringBuilder sql = new StringBuilder("""
                    SELECT
                        pca.config_approval_id,
                        pca.document_no documentNo,
                        he.full_name approverName,
                        (SELECT sc.name FROM sys_categories sc WHERE sc.value = pca.type AND sc.category_type = :configTypeCode) typeStr,
                        NVL(o.full_name, o.name) orgName,
                        pca.type,
                        (SELECT sc.name FROM sys_categories sc WHERE sc.value = pca.branch_type AND sc.category_type = :loaiChiNhanh) branchType,
                        mpg.name empPgrName,
                        pca.job_approver_id,
                        pca.from_date fromDate,
                        pca.to_date toDate,
                        pca.note note,
                        mj.name jobApproverName
                """
        );
        HashMap<String, Object> params = new HashMap<>();
        params.put("loaiChiNhanh", Constant.LOOKUP_CODES.LOAI_HINH_CHI_NHANH);
        addCondition(sql, params, dto);
        sql.append(" ORDER BY pca.config_approval_id");
        return getListData(sql.toString(), params);
    }

    private void addCondition(StringBuilder sql, HashMap<String, Object> params, ConfigApprovalsDTO dto) {
        sql.append("""
                     FROM pns_config_approvals pca
                     LEFT JOIN hr_organizations o ON pca.organization_id = o.organization_id
                     LEFT JOIN hr_employees he ON he.employee_id = pca.approver_id
                     LEFT JOIN hr_position_groups mpg ON mpg.position_group_id = pca.emp_pgr_id
                     LEFT JOIN hr_jobs mj ON mj.job_id = pca.job_approver_id
                     WHERE IFNULL(pca.is_deleted, :flagStatus) = :flagStatus
                """);
        params.put("flagStatus", BaseConstants.STATUS.NOT_DELETED);
        params.put("configTypeCode", Constant.LOOKUP_CODES.PNS_CONFIG_APPROVAL_TYPES);
        List<PermissionDataDto> permissionDataDtoList = authorizationService.getPermissionData(Scope.VIEW, Constant.RESOURCE.PNS_CONFIG_APPROVALS, Utils.getUserNameLogin());
        QueryUtils.addConditionPermission(permissionDataDtoList, sql, params);

        if (!Utils.isNullObject(dto.getOrganizationId())) {
            QueryUtils.filter("/" + dto.getOrganizationId() + "/", sql, params, "o.path_id");
        }
        QueryUtils.filter(dto.getType(), sql, params, "pca.type");
        QueryUtils.filter(Utils.NVL(dto.getDocumentNo()), sql, params, "pca.document_no");
        QueryUtils.filter(dto.getOrgGroup(), sql, params, "pca.org_group");

        if (dto.getFromDate() != null && dto.getToDate() != null) {
            sql.append(" AND IFNULL(pca.to_date,:fromDate) >= :fromDate" +
                       " AND pca.from_date <= :toDate");
            params.put("fromDate", dto.getFromDate());
            params.put("toDate", dto.getToDate());
        } else if (dto.getFromDate() != null) {
            sql.append(" AND IFNULL(pca.to_date,:fromDate) >= :fromDate" +
                       " AND pca.from_date <= :fromDate");
            params.put("fromDate", dto.getFromDate());
        } else if (dto.getToDate() != null) {
            sql.append(" AND IFNULL(pca.to_date,:toDate) >= :toDate" +
                       " AND pca.from_date <= :toDate");
            params.put("toDate", dto.getToDate());
        }
    }

}
