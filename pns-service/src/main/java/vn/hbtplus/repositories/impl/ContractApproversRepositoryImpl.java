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
import vn.hbtplus.models.dto.ContractApproversDTO;
import vn.hbtplus.models.response.ContractApproversResponse;
import vn.hbtplus.repositories.BaseRepository;
import vn.hbtplus.repositories.entity.ContractApproversEntity;
import vn.hbtplus.services.AuthorizationService;
import vn.hbtplus.utils.QueryUtils;
import vn.hbtplus.utils.Utils;

import java.util.HashMap;
import java.util.List;

/**
 * Lop repository Impl ung voi bang PNS_CONTRACT_APPROVERS
 *
 * @author tudd
 * @version 1.0
 * @since 1.0
 */

@Repository
@RequiredArgsConstructor
public class ContractApproversRepositoryImpl extends BaseRepository {

    private final AuthorizationService authorizationService;

    public BaseDataTableDto<ContractApproversResponse> searchData(ContractApproversDTO dto) {
        StringBuilder sql = new StringBuilder("""
                    SELECT
                    	pcp.contract_proposal_id contractProposalId,
                    	pcp.cur_contract_number curContractNumber,
                    	pcp.employee_id employeeId,
                    	he.employee_code employeeCode,
                    	he.full_name empName,
                    	he.position_id positionId,
                    	(SELECT pos_name FROM mp_positions WHERE pos_id = he.position_id) positionName,
                    	he.organization_id orgId,
                    	(SELECT IFNULL(full_name, name) FROM hr_organizations WHERE org_id = he.organization_id) orgName,
                    	pcp.type type,
                    	pcp.cur_contract_type_id curContractTypeId,
                    	(SELECT name FROM hr_contract_types WHERE contract_type_id = pcp.cur_contract_type_id) curContractTypeName,
                    	pcp.contract_by_law_id contractByLawId,
                    	(SELECT name FROM hr_contract_types WHERE contract_type_id = pcp.contract_by_law_id) contractByLawName,
                    	pcp.from_date fromDate,
                    	pcp.to_date toDate,
                    	pce.is_disciplined isDisciplined,
                    	CASE pce.is_disciplined WHEN 1 THEN 'Có' WHEN 0 THEN 'Không' END disciplinedName,
                    	pce.kpi_point kpiPoint,
                    	pce.rank_code rankCode,
                    	pce.rank_name rankName ,
                    	pcp.signer_id signerId,
                    	(SELECT full_name FROM hr_employees WHERE employee_id = pcp.signer_id) signerName,
                    	pcp.signer_position signerPosition,
                    	pcp.delegacy_no delegacyNo,
                    	pcp.status status,
                    	pca1.approver_id directManagerId,
                    	pca1.contract_approver_id contractApproverId1,
                    	(SELECT full_name FROM hr_employees WHERE employee_id = pca1.approver_id) directManagerName,
                    	pca1.is_liquidation dmIsLiquidation,
                    	CASE pca1.is_liquidation WHEN 0 THEN 'Thanh lý' WHEN 1 THEN 'Ký tiếp' END dmLiquidationName,
                    	pca1.contract_type_id dmContractTypeId,
                    	(SELECT name FROM hr_contract_types WHERE contract_type_id = pca1.contract_type_id) dmContractTypeName,
                    	pca2.approver_id approvalLevelPersonId,
                    	pca2.contract_approver_id contractApproverId2,
                    	(SELECT full_name FROM hr_employees WHERE employee_id = pca2.approver_id) approvalLevelPersonName,
                    	pca2.is_liquidation alIsLiquidation,
                    	CASE pca2.is_liquidation WHEN 0 THEN 'Thanh lý' WHEN 1 THEN 'Ký tiếp' END alLiquidationName,
                    	pca2.contract_type_id alContractTypeId,
                    	(SELECT name FROM hr_contract_types WHERE contract_type_id = pca2.contract_type_id) alContractTypeName,
                    	pca3.approver_id changeApprovalLevelId,
                    	pca3.contract_approver_id contractApproverId3,
                    	(SELECT full_name FROM hr_employees WHERE employee_id = pca3.approver_id) changeApprovalLevelName,
                    	pca3.is_liquidation calIsLiquidation,
                    	CASE pca3.is_liquidation WHEN 0 THEN 'Thanh lý' WHEN 1 THEN 'Ký tiếp' END calLiquidationName,
                    	pca3.contract_type_id calContractTypeId,
                    	(SELECT name FROM hr_contract_types WHERE contract_type_id = pca3.contract_type_id) calContractTypeName,
                    	pca1.cursor_current dmCursorCurrent,
                    	pca1.approver_level dmApproverLevel,
                    	pca2.cursor_current alCursorCurrent,
                    	pca2.approver_level alApproverLevel,
                    	pca3.cursor_current calCursorCurrent,
                    	pca3.approver_level calApproverLevel
                    FROM pns_contract_proposals pcp
                    JOIN hr_employees he ON pcp.employee_id = he.employee_id
                    LEFT JOIN hr_organizations o ON o.organization_id = he.organization_id
                    LEFT JOIN pns_contract_evaluations pce ON pcp.contract_proposal_id = pce.contract_proposal_id
                    LEFT JOIN pns_contract_approvers pca1 ON pcp.contract_proposal_id = pca1.contract_proposal_id AND pca1.approver_level = 1
                    LEFT JOIN pns_contract_approvers pca2 ON pcp.contract_proposal_id = pca2.contract_proposal_id AND pca2.approver_level = 2
                    LEFT JOIN pns_contract_approvers pca3 ON pcp.contract_proposal_id = pca3.contract_proposal_id AND pca3.approver_level = 3
                    WHERE IFNULL(pcp.is_deleted, :flagStatus) = :flagStatus
                    AND IFNULL(pce.is_deleted, :flagStatus) = :flagStatus
                    AND IFNULL(pca1.is_deleted, :flagStatus) = :flagStatus
                    AND IFNULL(pca2.is_deleted, :flagStatus) = :flagStatus
                    AND IFNULL(pca3.is_deleted, :flagStatus) = :flagStatus
                    AND EXISTS(
                        select 1
                        from pns_contract_approvers pca
                        where pcp.contract_proposal_id = pca.contract_proposal_id
                        and pca.approver_id = :approverId
                        and (pca.cursor_current = 1 OR pca.is_liquidation IS NOT NULL)
                        and IFNULL(pca.is_deleted, :flagStatus) = :flagStatus 
                    )
                """);
        HashMap<String, Object> params = new HashMap<>();
        params.put("approverId", dto.getEmployeeId());
        params.put("flagStatus", BaseConstants.STATUS.NOT_DELETED);

        QueryUtils.filter(dto.getListContractTypeId(), sql, params, "pcp.contract_type_id");
        QueryUtils.filter(dto.getListStatus(), sql, params, "pcp.status");
        QueryUtils.filterOriginal(Utils.NVL(dto.getEmployeeCode()).toUpperCase(), sql, params, "he.employee_code");
        QueryUtils.filter(dto.getFullName(), sql, params, "he.full_name");
        QueryUtils.filter(dto.getListPositionId(), sql, params, "pcp.job_id");
        QueryUtils.filterGe(dto.getFromDate(), sql, params, "pcp.from_date", "fromDate");
        QueryUtils.filterLe(dto.getToDate(), sql, params, "pcp.from_date", "toDate");
        List<PermissionDataDto> permissionDataDtoList = authorizationService.getPermissionData(Scope.VIEW, Constant.RESOURCE.PNS_CONTRACT_APPROVAL, Utils.getUserNameLogin());
        QueryUtils.addConditionPermission(permissionDataDtoList, sql, params);

        if (!Utils.isNullObject(dto.getOrgId())) {
            QueryUtils.filter("/" + dto.getOrgId() + "/", sql, params, "o.path_id");
        }

        return getListPagination(sql.toString(), params, dto, ContractApproversResponse.class);
    }

    public List<ContractApproversEntity> getListDataByForm(ContractApproversDTO dto){
        StringBuilder sql = new StringBuilder("""
                    SELECT pca.*, pcp.contract_by_law_id, pcp.employee_id, pcp.organization_id
                    FROM pns_contract_proposals pcp
                    JOIN hr_employees he ON pcp.employee_id = he.employee_id
                    JOIN pns_contract_approvers pca ON pcp.contract_proposal_id = pca.contract_proposal_id
                    WHERE pca.approver_id = :approverId
                    AND IFNULL(pcp.is_deleted, :flagStatus) = :flagStatus
                    AND IFNULL(pca.is_deleted, :flagStatus) = :flagStatus
                    AND pca.cursor_current = 1
                """);
        HashMap<String, Object> params = new HashMap<>();
        params.put("approverId", dto.getEmployeeId());
        params.put("flagStatus", BaseConstants.STATUS.NOT_DELETED);
        QueryUtils.filter(dto.getListContractTypeId(), sql, params, "pcp.contract_type_id");
        QueryUtils.filter(dto.getListStatus(), sql, params, "pcp.status");
        QueryUtils.filterOriginal(Utils.NVL(dto.getEmployeeCode()).toUpperCase(), sql, params, "he.employee_code");
        QueryUtils.filter(dto.getFullName(), sql, params, "he.full_name");
        QueryUtils.filter(dto.getListPositionId(), sql, params, "pcp.job_id");
        QueryUtils.filterGe(dto.getFromDate(), sql, params, "pcp.from_date", "fromDate");
        QueryUtils.filterLe(dto.getToDate(), sql, params, "pcp.from_date", "toDate");
        return getListData(sql.toString(), params, ContractApproversEntity.class);
    }

    public Integer countApprovalRecord(ContractApproversDTO dto){
        StringBuilder sql = new StringBuilder("""
                    SELECT COUNT(1)
                    FROM pns_contract_proposals pcp
                    JOIN hr_employees he ON pcp.employee_id = he.employee_id
                    JOIN pns_contract_approvers pca ON pcp.contract_proposal_id = pca.contract_proposal_id
                    WHERE pca.approver_id = :approverId
                    AND IFNULL(pcp.is_deleted, :flagStatus) = :flagStatus
                    AND IFNULL(pca.is_deleted, :flagStatus) = :flagStatus
                    AND pca.cursor_current = 1
                """);
        HashMap<String, Object> params = new HashMap<>();
        params.put("approverId", dto.getEmployeeId());
        params.put("flagStatus", BaseConstants.STATUS.NOT_DELETED);
        QueryUtils.filter(dto.getListContractTypeId(), sql, params, "pcp.contract_type_id");
        QueryUtils.filter(dto.getListStatus(), sql, params, "pcp.status");
        QueryUtils.filterOriginal(Utils.NVL(dto.getEmployeeCode()).toUpperCase(), sql, params, "he.employee_code");
        QueryUtils.filter(dto.getFullName(), sql, params, "he.full_name");
        QueryUtils.filter(dto.getListPositionId(), sql, params, "pcp.job_id");
        QueryUtils.filterGe(dto.getFromDate(), sql, params, "pcp.from_date", "fromDate");
        QueryUtils.filterLe(dto.getToDate(), sql, params, "pcp.from_date", "toDate");
        return queryForObject(sql.toString(), params, Integer.class);
    }

    public ContractApproversEntity getContractApprovers(Long contractProposalId, Long approverId){
        String sql = """
                    SELECT pca.* FROM pns_contract_approvers pca
                    WHERE pca.contract_proposal_id = :contractProposalId
                    AND pca.approver_id = :approverId
                    AND IFNULL(pca.is_deleted, :activeStatus) = :activeStatus
                    AND (
                        pca.is_liquidation IS NULL
                        OR EXISTS(
                            select 1 from pns_contract_approvers pca1
                            where pca1.contract_proposal_id = pca.contract_proposal_id
                            and pca1.approver_level = pca.approver_level + 1
                            and pca1.is_liquidation is null
                        )
                    )
                    ORDER BY pca.approver_level
                """;
        HashMap<String, Object> params = new HashMap<>();
        params.put("contractProposalId", contractProposalId);
        params.put("approverId", approverId);
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        List<ContractApproversEntity> listResult = getListData(sql, params, ContractApproversEntity.class);
        if(Utils.isNullOrEmpty(listResult)){
            return null;
        } else {
            return listResult.get(0);
        }
    }
}
