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
import vn.hbtplus.models.dto.ContractFeesDTO;
import vn.hbtplus.models.dto.ContractProposalsDTO;
import vn.hbtplus.models.response.ContractProposalsResponse;
import vn.hbtplus.repositories.BaseRepository;
import vn.hbtplus.repositories.entity.ContractFeesEntity;
import vn.hbtplus.repositories.entity.ContractProposalsEntity;
import vn.hbtplus.repositories.entity.HrEmployeesEntity;
import vn.hbtplus.services.AuthorizationService;
import vn.hbtplus.utils.QueryUtils;
import vn.hbtplus.utils.Utils;

import java.util.*;

/**
 * Lop repository Impl ung voi bang PNS_CONTRACT_PROPOSALS
 *
 * @author tudd
 * @version 1.0
 * @since 1.0
 */
@Repository
@RequiredArgsConstructor
public class ContractProposalsRepositoryImpl extends BaseRepository {

    private final AuthorizationService authorizationService;

    public BaseDataTableDto<ContractProposalsResponse> searchContinueSign(ContractProposalsDTO dto) {
        StringBuilder sql = new StringBuilder("""
                    SELECT
                        a.contract_proposal_id,
                        a.employee_id,
                        a.type,
                        a.contract_type_id,
                        a.from_date,
                        a.to_date,
                        a.to_date_by_law,
                        a.cur_from_date,
                        a.cur_to_date,
                        a.signer_position,
                        a.delegacy_no,
                        a.contract_number,
                        a.cur_contract_number curContractNumber,
                        a.status,
                        a.is_sent_mail,
                        (select t.name from hr_contract_types t where t.contract_type_id = a.cur_contract_type_id) curContractTypeName,
                        (select t.name from hr_contract_types t where t.contract_type_id = a.contract_by_law_id) contractByLawName,
                        (select t.name from hr_contract_types t where t.contract_type_id = a.contract_type_id) contractTypeName,
                        CASE pce.is_disciplined
                               WHEN 1 THEN CONCAT('Có, ', pce.disciplined_note)
                               WHEN 0 THEN 'Không'
                          END disciplinedName,
                        pce.kpi_point,
                        pce.rank_name,
                        (    select CONCAT(he.employee_code, '-',he.full_name)
                               from hr_employees he
                               where he.employee_id = a.signer_id
                        ) signerName,
                        (    select CONCAT(he.employee_code, '-',he.full_name)
                               from hr_employees he
                               where he.employee_id = pca1.approver_id
                        ) managerName,
                        pca1.is_liquidation managerOffer,
                        CASE
                             WHEN a.type = 2 AND pca1.from_date IS NOT NULL THEN CONCAT(DATE_FORMAT(pca1.from_date, '%d/%m/%Y'), ' - ', IFNULL(DATE_FORMAT(pca1.to_date, '%d/%m/%Y'), 'Không xác định'))
                             ELSE NULL
                         END managerContractTerm,
                        (select t.name from hr_contract_types t where t.contract_type_id = pca1.contract_type_id) managerContractTypeName,
                        (    select CONCAT(he.employee_code, '-', he.full_name)
                               from hr_employees he
                               where he.employee_id = pca2.approver_id
                        ) approveName,
                        pca2.is_liquidation approveOffer,
                        CASE
                             WHEN a.type = 2 AND pca2.from_date IS NOT NULL THEN CONCAT(DATE_FORMAT(pca2.from_date, '%d/%m/%Y'), '-', IFNULL(DATE_FORMAT(pca2.to_date, '%d/%m/%Y'), 'Không xác định'))
                             ELSE NULL
                         END approverContractTerm,
                        (select t.name from hr_contract_types t where t.contract_type_id = pca2.contract_type_id) approveContractTypeName,
                        (    select CONCAT(he.employee_code, ' - ',he.full_name)
                               from hr_employees he
                               where he.employee_id = pca3.approver_id
                        ) bossName,
                        pca3.is_liquidation bossOffer,
                        CASE
                             WHEN a.type = 2 AND pca3.from_date IS NOT NULL THEN CONCAT(DATE_FORMAT(pca3.from_date, '%d/%m/%Y'), ' - ', IFNULL(DATE_FORMAT(pca3.to_date, '%d/%m/%Y'), 'Không xác định'))
                             ELSE NULL
                         END bossContractTerm,
                        (select t.name from hr_contract_types t where t.contract_type_id = pca3.contract_type_id) bossContractTypeName,
                        e.employee_code,
                        e.full_name,
                        IFNULL(o.full_name, o.name) orgName,
                        (select p.pos_name from mp_positions p where p.pos_id = a.position_id) positionName,
                        (select cf.amount_fee from pns_contract_fees cf where cf.contract_fee_id = a.contract_fee_id) amountFee,
                        (select cf1.amount_fee from pns_contract_fees cf1 where cf1.from_date <= a.from_date AND a.type = :typeContractFee AND cf1.status = :statusContractFee ORDER BY cf1.from_date DESC limit 1) oldAmountFee
                """);
        HashMap<String, Object> params = new HashMap<>();
        addCondition(sql, params, dto);
        return getListPagination(sql.toString(), params, dto, ContractProposalsResponse.class);
    }

    public BaseDataTableDto<ContractProposalsResponse> searchNewSignOrAddendumSign(ContractProposalsDTO dto) {
        StringBuilder sql = new StringBuilder("""
                    SELECT
                        a.contract_proposal_id,
                        a.employee_id,
                        a.type,
                        a.contract_type_id,
                        a.from_date,
                        a.to_date,
                        a.to_date_by_law,
                        a.cur_from_date,
                        a.cur_to_date,
                        a.signer_position,
                        a.delegacy_no,
                        a.contract_number,
                        a.cur_contract_number curContractNumber,
                        a.status,
                        a.is_sent_mail,
                        (select t.name from hr_contract_types t where t.contract_type_id = a.cur_contract_type_id) curContractTypeName,
                        (select t.name from hr_contract_types t where t.contract_type_id = a.contract_type_id) contractByLawName,
                        (select CONCAT(he.employee_code, '-',he.full_name) from hr_employees he where he.employee_id = a.signer_id) signerName,
                        e.employee_code,
                        e.full_name,
                        IFNULL(o.full_name, o.name) orgName,
                        (select p.pos_name from mp_positions p where p.pos_id = a.position_id) positionName,
                        (select cf.amount_fee from pns_contract_fees cf where cf.contract_fee_id = a.contract_fee_id) amountFee,
                        (select cf1.amount_fee from pns_contract_fees cf1 where cf1.from_date <= a.from_date AND a.type = :typeContractFee AND cf1.status = :statusContractFee ORDER BY cf1.from_date DESC limit 1) oldAmountFee
                """);
        HashMap<String, Object> params = new HashMap<>();
        addConditionSearchNewOrAppendix(sql, params, dto);
        return getListPagination(sql.toString(), params, dto, ContractProposalsResponse.class);
    }

    public List<Map<String, Object>> getContinueSignContract(ContractProposalsDTO dto) {
        StringBuilder sql = new StringBuilder("""
                    SELECT
                        CASE a.type
                               WHEN 1 THEN 'Ký mới'
                               WHEN 2 THEN 'Ký Tiếp'
                               WHEN 3 THEN 'Ký phụ lục thay đổi lương'
                               WHEN 4 THEN 'Đề xuất thay đổi phí dịch vụ'
                          END typeName,
                        f_get_label_status(a.status) statusName,
                        CASE a.type
                               WHEN 1 THEN NULL
                               ELSE (DATE_FORMAT(a.cur_from_date, '%d/%m/%Y'))
                          END curFromDate,
                        CASE a.type
                               WHEN 1 THEN NULL
                               ELSE IFNULL(DATE_FORMAT(a.cur_to_date, '%d/%m/%Y'), 'Không xác định')
                          END curToDate,
                        (DATE_FORMAT(a.from_date, '%d/%m/%Y')) fromDate,
                        CASE a.type
                               WHEN 2 THEN IFNULL(DATE_FORMAT(a.to_date_by_law, '%d/%m/%Y'), 'Không xác định')
                               ELSE IFNULL(DATE_FORMAT(a.to_date, '%d/%m/%Y'), 'Không xác định')
                          END toDate,
                        a.to_date_by_law,
                        a.signer_position,
                        a.delegacy_no,
                        a.contract_number contractNumber,
                        a.cur_contract_number curContractNumber,
                        (select t.name from hr_contract_types t where t.contract_type_id = a.cur_contract_type_id) curContractTypeName,
                        (select t.name from hr_contract_types t where t.contract_type_id = a.contract_by_law_id) contractByLawName,
                        (select t.name from hr_contract_types t where t.contract_type_id = a.contract_type_id) contractTypeName,
                        CASE pce.is_disciplined
                               WHEN 1 THEN CONCAT('Có, ', pce.disciplined_note)
                               WHEN 0 THEN 'Không'
                          END disciplinedName,
                        pce.kpi_point,
                        pce.rank_name,
                        (    select CONCAT(he.employee_code, '-',he.full_name)
                               from hr_employees he
                               where he.employee_id = a.signer_id
                          ) signerName,
                        (    select CONCAT(he.employee_code, '-',he.full_name)
                               from hr_employees he
                               where he.employee_id = pca1.approver_id
                        ) managerName,
                        pca1.is_liquidation managerOffer,
                        CASE pca1.is_liquidation
                               WHEN 1 THEN 'Thanh lý'
                               WHEN 0 THEN 'Ký tiếp'
                          END managerOfferName,
                        CASE a.type
                               WHEN 2 THEN DATE_FORMAT(pca1.from_date, '%d/%m/%Y')
                               ELSE NULL
                          END managerFromDate,
                        CASE
                               WHEN a.type = 2 AND pca1.from_date IS NOT NULL THEN IFNULL(DATE_FORMAT(pca1.to_date, '%d/%m/%Y'), 'Không xác định')
                               ELSE NULL
                          END managerToDate,
                        (select t.name from hr_contract_types t where t.contract_type_id = pca1.contract_type_id) managerContractTypeName,
                        (    select CONCAT(he.employee_code, '-',he.full_name)
                               from hr_employees he
                               where he.employee_id = pca2.approver_id
                        ) approveName,
                        pca2.is_liquidation approveOffer,
                        CASE pca2.is_liquidation
                               WHEN 1 THEN 'Thanh lý'
                               WHEN 0 THEN 'Ký tiếp'
                          END approveOfferName,
                        CASE a.type
                               WHEN 2 THEN DATE_FORMAT(pca2.from_date, '%d/%m/%Y')
                               ELSE NULL
                          END approverFromDate,
                        CASE
                               WHEN a.type = 2 AND pca2.from_date IS NOT NULL THEN IFNULL(DATE_FORMAT(pca2.to_date, '%d/%m/%Y'), 'Không xác định')
                               ELSE NULL
                          END approverToDate,
                        (select t.name from hr_contract_types t where t.contract_type_id = pca2.contract_type_id) approveContractTypeName,
                        (    select CONCAT(he.employee_code, '-',he.full_name)
                               from hr_employees he
                               where he.employee_id = pca3.approver_id
                          ) bossName,
                        pca3.is_liquidation bossOffer,
                        CASE pca3.is_liquidation
                               WHEN 1 THEN 'Thanh lý'
                               WHEN 0 THEN 'Ký tiếp'
                          END bossOfferName,
                        CASE a.type
                               WHEN 2 THEN DATE_FORMAT(pca3.from_date, '%d/%m/%Y')
                               ELSE NULL
                          END bossFromDate,
                        CASE
                               WHEN a.type = 2 AND pca3.from_date IS NOT NULL THEN IFNULL(DATE_FORMAT(pca3.to_date, '%d/%m/%Y'), 'Không xác định')
                               ELSE NULL
                          END bossToDate,
                        (select t.name from hr_contract_types t where t.contract_type_id = pca3.contract_type_id) bossContractTypeName,
                        e.employee_code,
                        e.full_name,
                        o.org_name_level1 orgName1,
                        o.org_name_level2 orgName2,
                        o.org_name_level3 orgName3,
                        o.org_name_manage orgNameManager,
                        IFNULL(o.full_name, o.name) orgName,
                        (select p.pos_name from mp_positions p where p.pos_id = a.position_id) positionName,
                        (select cf.amount_fee from pns_contract_fees cf where cf.contract_fee_id = a.contract_fee_id) amountFee,
                        (select cf1.amount_fee from pns_contract_fees cf1 where cf1.from_date <= a.from_date AND a.type = :typeContractFee AND cf1.status = :statusContractFee ORDER BY cf1.from_date DESC limit 1) oldAmountFee
                """);
        HashMap<String, Object> params = new HashMap<>();
        addCondition(sql, params, dto);
        return getListData(sql.toString(), params);
    }

    public List<Map<String, Object>> getListMapNewOrAppendixContract(ContractProposalsDTO dto) {
        StringBuilder sql = new StringBuilder("""
                    SELECT
                        (DATE_FORMAT(a.from_date, '%d/%m/%Y')) fromDate,
                        IFNULL(DATE_FORMAT(a.to_date, '%d/%m/%Y'), 'Không xác định') toDate,
                        (DATE_FORMAT(a.cur_from_date, '%d/%m/%Y')) curFromDate,
                        IFNULL(DATE_FORMAT(a.cur_to_date, '%d/%m/%Y'), 'Không xác định') curToDate,
                        f_get_label_status(a.status) statusName,
                        a.to_date_by_law,
                        a.signer_position,
                        a.delegacy_no,
                        a.contract_number contractNumber,
                        a.cur_contract_number curContractNumber,
                        o.org_name_level1 orgName1,
                        o.org_name_level2 orgName2,
                        o.org_name_level3 orgName3,
                        o.org_name_manage orgNameManager,
                        (select t.name from hr_contract_types t where t.contract_type_id = a.cur_contract_type_id) curContractTypeName,
                        (select t.name from hr_contract_types t where t.contract_type_id = a.contract_type_id) contractTypeName,
                        (select CONCAT(he.employee_code, '-',he.full_name) from hr_employees he where he.employee_id = a.signer_id) signerName,
                        e.employee_code,
                        e.full_name,
                        IFNULL(o.full_name, o.name) orgName,
                        (select p.pos_name from mp_positions p where p.pos_id = a.position_id) positionName,
                        (select cf.amount_fee from pns_contract_fees cf where cf.contract_fee_id = a.contract_fee_id) amountFee,
                        (select cf1.amount_fee from pns_contract_fees cf1 where cf1.from_date <= a.from_date AND a.type = :typeContractFee AND cf1.status = :statusContractFee ORDER BY cf1.from_date DESC limit 1) oldAmountFee
                """);
        HashMap<String, Object> params = new HashMap<>();
        addConditionSearchNewOrAppendix(sql, params, dto);
        return getListData(sql.toString(), params);
    }

    public List<ContractProposalsEntity> getListIdByForm(ContractProposalsDTO dto) {
        StringBuilder sql = new StringBuilder("""
                    SELECT a.*
                    FROM pns_contract_proposals a
                    JOIN hr_employees e ON e.employee_id = a.employee_id
                    JOIN hr_organizations o ON o.organization_id = a.organization_id
                    WHERE IFNULL(a.is_deleted, :flagStatus) = :flagStatus
                    AND e.status = :empStatus
                """);
        HashMap<String, Object> params = new HashMap<>();
        params.put("empStatus", Constant.EMP_STATUS.WORK_IN);
        params.put("flagStatus", BaseConstants.STATUS.NOT_DELETED);

        List<PermissionDataDto> permissionDataDtoList = authorizationService.getPermissionData(Scope.VIEW, Constant.RESOURCE.PNS_CONTRACT_PROPOSALS, Utils.getUserNameLogin());
        QueryUtils.addConditionPermission(permissionDataDtoList, sql, params);

        if (!Utils.isNullObject(dto.getOrgId())) {
            QueryUtils.filter("/" + dto.getOrgId() + "/", sql, params, "o.path_id");
        }

        Utils.validateDate(dto.getFromDate(), dto.getToDate());

        QueryUtils.filter(dto.getListStatus(), sql, params, "a.status");
        QueryUtils.filterGe(dto.getFromDate(), sql, params, "a.from_date", "fromDate");
        QueryUtils.filterLe(dto.getToDate(), sql, params, "a.from_date", "toDate");
        QueryUtils.filterOriginal(Utils.NVL(dto.getEmployeeCode()).toUpperCase(), sql, params, "e.employee_code");
        QueryUtils.filter(dto.getFullName(), sql, params, "e.full_name");
        QueryUtils.filter(dto.getListEmpTypeCode(), sql, params, "e.emp_type_code");
        QueryUtils.filter(dto.getListPositionId(), sql, params, "a.job_id");
        QueryUtils.filter(dto.getType(), sql, params, "a.type");
        QueryUtils.filter(dto.getListContractTypeId(), sql, params, "a.contract_type_id");
        return getListData(sql.toString(), params, ContractProposalsEntity.class);
    }

    private void addConditionSearchNewOrAppendix(StringBuilder sql, HashMap<String, Object> params, ContractProposalsDTO dto) {
        sql.append("""
                    FROM pns_contract_proposals a
                    JOIN hr_employees e ON e.employee_id = a.employee_id
                    JOIN hr_organizations o ON o.organization_id = a.organization_id
                    WHERE IFNULL(a.is_deleted, :flagStatus) = :flagStatus
                    AND e.status = :empStatus
                """);
        params.put("flagStatus", BaseConstants.STATUS.NOT_DELETED);
        params.put("empStatus", Constant.EMP_STATUS.WORK_IN);
        if (dto.getTypeList() == null) {
            params.put("statusContractFee", ContractFeesEntity.STATUS.APPROVED);
            params.put("typeContractFee", Constant.CONTRACT_TYPE.CONTRACT_FEE);
        }

        List<PermissionDataDto> permissionDataDtoList = authorizationService.getPermissionData(Scope.VIEW, Constant.RESOURCE.PNS_CONTRACT_PROPOSALS, Utils.getUserNameLogin());
        QueryUtils.addConditionPermission(permissionDataDtoList, sql, params);

        if (!Utils.isNullObject(dto.getOrgId())) {
            QueryUtils.filter("/" + dto.getOrgId() + "/", sql, params, "o.path_id");
        }

        Utils.validateDate(dto.getFromDate(), dto.getToDate());
        QueryUtils.filter(dto.getListStatus(), sql, params, "a.status");
        QueryUtils.filterGe(dto.getFromDate(), sql, params, "a.from_date", "fromDate");
        QueryUtils.filterLe(dto.getToDate(), sql, params, "a.from_date", "toDate");
        QueryUtils.filterOriginal(Utils.NVL(dto.getEmployeeCode()).toUpperCase(), sql, params, "e.employee_code");
        QueryUtils.filter(dto.getFullName(), sql, params, "e.full_name");
        QueryUtils.filter(dto.getListEmpTypeCode(), sql, params, "e.emp_type_code");
        QueryUtils.filter(dto.getListPositionId(), sql, params, "a.job_id");
        QueryUtils.filter(dto.getType(), sql, params, "a.type");
        QueryUtils.filter(dto.getListContractTypeId(), sql, params, "a.contract_type_id");
        QueryUtils.filter(dto.getSignerId(), sql, params, "a.signer_id");
        QueryUtils.filter(dto.getSignerPosition(), sql, params, "a.signer_position");

        if (!Utils.isNullObject(dto.getIsSentMail())) {
            sql.append(dto.getIsSentMail() == 1 ? " AND a.is_sent_mail = 1" : " AND (a.is_sent_mail = 0 OR a.is_sent_mail IS NULL) ");
        }
    }

    private void addCondition(StringBuilder sql, HashMap<String, Object> params, ContractProposalsDTO dto) {
        params.put("statusContractFee", ContractFeesEntity.STATUS.APPROVED);
        params.put("typeContractFee", Constant.CONTRACT_TYPE.CONTRACT_FEE);

        if (dto.getTypeList() == null) {
            sql.append("""
                           FROM pns_contract_proposals a
                           JOIN hr_employees e ON e.employee_id = a.employee_id
                           JOIN hr_organizations o ON o.organization_id = a.organization_id
                           LEFT JOIN pns_contract_evaluations pce ON pce.contract_proposal_id = a.contract_proposal_id
                           LEFT JOIN pns_contract_approvers pca1 ON pca1.contract_proposal_id = a.contract_proposal_id AND pca1.approver_level = 1
                           LEFT JOIN pns_contract_approvers pca2 ON pca2.contract_proposal_id = a.contract_proposal_id AND pca2.approver_level = 2
                           LEFT JOIN pns_contract_approvers pca3 ON pca3.contract_proposal_id = a.contract_proposal_id AND pca3.approver_level = 3
                           WHERE IFNULL(a.is_deleted, :flagStatus) = :flagStatus
                           AND IFNULL(pca1.is_deleted, :flagStatus) = :flagStatus
                           AND IFNULL(pca2.is_deleted, :flagStatus) = :flagStatus
                           AND IFNULL(pca3.is_deleted, :flagStatus) = :flagStatus
                           AND e.status = :empStatus
                           AND IFNULL(pce.is_deleted, :flagStatus) = :flagStatus
                    """);
        } else {
            sql.append("""
                           FROM pns_contract_proposals a
                           JOIN hr_employees e ON e.employee_id = a.employee_id
                           JOIN hr_organizations o ON o.organization_id = a.organization_id
                           WHERE IFNULL(a.is_deleted, :flagStatus) = :flagStatus
                           AND e.status = :empStatus
                    """);
        }
        params.put("flagStatus", BaseConstants.STATUS.NOT_DELETED);
        params.put("empStatus", Constant.EMP_STATUS.WORK_IN);

        List<PermissionDataDto> permissionDataDtoList = authorizationService.getPermissionData(Scope.VIEW, Constant.RESOURCE.PNS_CONTRACT_PROPOSALS, Utils.getUserNameLogin());
        QueryUtils.addConditionPermission(permissionDataDtoList, sql, params);

        if (!Utils.isNullObject(dto.getOrgId())) {
            QueryUtils.filter("/" + dto.getOrgId() + "/", sql, params, "o.path_id");
        }

        Utils.validateDate(dto.getFromDate(), dto.getToDate());
        QueryUtils.filter(dto.getListStatus(), sql, params, "a.status");
        QueryUtils.filterGe(dto.getFromDate(), sql, params, "a.from_date", "fromDate");
        QueryUtils.filterLe(dto.getToDate(), sql, params, "a.from_date", "toDate");
        QueryUtils.filterOriginal(Utils.NVL(dto.getEmployeeCode()).toUpperCase(), sql, params, "e.employee_code");
        QueryUtils.filter(dto.getFullName(), sql, params, "e.full_name");
        QueryUtils.filter(dto.getListEmpTypeCode(), sql, params, "e.emp_type_code");
        QueryUtils.filter(dto.getListPositionId(), sql, params, "a.job_id");
        QueryUtils.filter(dto.getType(), sql, params, "a.type");
        QueryUtils.filter(dto.getListContractTypeId(), sql, params, "a.contract_type_id");
        QueryUtils.filter(dto.getSignerId(), sql, params, "a.signer_id");
        QueryUtils.filter(dto.getSignerPosition(), sql, params, "a.signer_position");
        if (!Utils.isNullObject(dto.getIsSentMail())) {
            sql.append(dto.getIsSentMail() == 1 ? " AND a.is_sent_mail = 1" : " AND (a.is_sent_mail = 0 OR a.is_sent_mail IS NULL) ");
        }
    }

    public List<ContractProposalsEntity> getListContractProposalByIds(List<Long> listId) {
        if (Utils.isNullOrEmpty(listId)) {
            return null;
        }
        String sql = """
                    SELECT a.*, e.email, e.employee_code, e.full_name
                    FROM pns_contract_proposals a
                    JOIN hr_employees e ON e.employee_id  = a.employee_id
                    WHERE IFNULL(a.is_deleted, :flagStatus) = :flagStatus
                    AND a.contract_proposal_id IN (:ids)
                """;
        Map<String, Object> params = new HashMap<>();
        params.put("flagStatus", BaseConstants.STATUS.NOT_DELETED);
        params.put("ids", listId);
        return getListData(sql, params, ContractProposalsEntity.class);
    }

    public List<ContractProposalsEntity> getListContractProposalByEmpCodes(List<String> listEmployeeCode, int type) {
        if (Utils.isNullOrEmpty(listEmployeeCode)) {
            return null;
        }
        StringBuilder sql = new StringBuilder("""
                    SELECT a.*, e.employee_code
                    FROM pns_contract_proposals a
                    JOIN hr_employees e ON e.employee_id = a.employee_id
                    JOIN hr_organizations o ON o.organization_id = a.organization_id
                    WHERE IFNULL(a.is_deleted, :flagStatus) = :flagStatus
                    AND e.employee_code IN (:empCodes)
                    AND a.status = :status
                    AND a.type = :type
                """);
        Map<String, Object> params = new HashMap<>();
        params.put("flagStatus", BaseConstants.STATUS.NOT_DELETED);
        params.put("status", Constant.CONTRACT_STATUS.WAITING_SIGN);
        params.put("type", type);

        List<PermissionDataDto> permissionDataDtoList = authorizationService.getPermissionData(Scope.UPDATE, Constant.RESOURCE.PNS_CONTRACT_PROPOSALS, Utils.getUserNameLogin());
        QueryUtils.addConditionPermission(permissionDataDtoList, sql, params);

        List<List<String>> listPartition = Utils.partition(listEmployeeCode, Constant.SIZE_PARTITION);
        List<ContractProposalsEntity> result = new ArrayList<>();
        for (List<String> empCodes : listPartition) {
            params.put("empCodes", empCodes);
            List<ContractProposalsEntity> listResultPartition = getListData(sql.toString(), params, ContractProposalsEntity.class);
            if (!Utils.isNullOrEmpty(listResultPartition)) {
                result.addAll(listResultPartition);
            }
        }
        return result;
    }

    public List<ContractProposalsEntity> getListContractProposalByEmpIds(List<ContractProposalsDTO> listDTO) {
        if (Utils.isNullOrEmpty(listDTO)) {
            return null;
        }
        Map<Long, String> mapEmp = new HashMap<>();
        List<Long> listEmployeeId = new ArrayList<>();
        for (ContractProposalsDTO dto : listDTO) {
            mapEmp.put(dto.getEmployeeId(), dto.getContractNumber());
            listEmployeeId.add(dto.getEmployeeId());
        }

        StringBuilder sql = new StringBuilder("""
                    SELECT a.contract_proposal_id,
                       a.type,
                       a.contract_fee_id,
                       a.contract_number,
                       e.employee_code,
                       e.full_name,
                       e.email,
                       (select hct.name from hr_contract_types hct where hct.contract_type_id = a.contract_type_id) contractTypeName,
                       (select mj.job_name from mp_jobs mj where mj.job_id = e.job_id) jobName,
                       a.employee_id,
                       a.from_date
                    FROM pns_contract_proposals a
                    JOIN hr_employees e ON e.employee_id = a.employee_id
                    WHERE IFNULL(a.is_deleted, :flagStatus) = :flagStatus
                    AND a.employee_id IN (:empIds)
                    AND a.status = :status
                """);
        Map<String, Object> params = new HashMap<>();
        params.put("flagStatus", BaseConstants.STATUS.NOT_DELETED);
        params.put("status", Constant.CONTRACT_STATUS.SIGNED);
        List<List<Long>> listPartition = Utils.partition(listEmployeeId, Constant.SIZE_PARTITION);
        List<ContractProposalsEntity> listData = new ArrayList<>();
        for (List<Long> empIds : listPartition) {
            params.put("empIds", empIds);
            List<ContractProposalsEntity> listResultPartition = getListData(sql.toString(), params, ContractProposalsEntity.class);
            if (!Utils.isNullOrEmpty(listResultPartition)) {
                listData.addAll(listResultPartition);
            }
        }

        List<ContractProposalsEntity> result = new ArrayList<>();
        for (ContractProposalsEntity entity : listData) {
            if (Utils.NVL(entity.getContractNumber()).equals(mapEmp.get(entity.getEmployeeId()))) {
                result.add(entity);
            }
        }

        return result;
    }

    public List<ContractProposalsDTO> getListEmpExpiredContract(Date fromDate, Date toDate) {
        String sql = """
                    SELECT hcp.employee_id,
                        e.employee_code,
                        f_get_org_abbreviation(wp.organization_id) orgCode,
                        f_get_suggest_contract_type(2, hcp.contract_type_id, wp.position_id, wp.organization_id) contractTypeId,
                        wp.position_id,
                        wp.job_id,
                        (   select hmp.manager_id
                            from hr_manager_process hmp
                            where hmp.employee_id = hcp.employee_id
                            and IFNULL(hmp.is_deleted, :flagStatus) = :flagStatus
                            and hmp.from_date <= hcp.to_date + 1
                            and (hmp.to_date >= hcp.to_date + 1 OR hmp.to_date IS NULL)
                              limit 1
                        ) managerId,
                        wp.organization_id,
                        hcp.contract_type_id curContractTypeId,
                        (select mp.pgr_id from mp_positions mp where mp.pos_id = wp.position_id) positionGroupId,
                        hcp.contract_number curContractNumber,
                        hcp.from_date curFromDate,
                        hcp.to_date curToDate
                    FROM hr_contract_process hcp
                    JOIN hr_employees e ON e.employee_id = hcp.employee_id
                    JOIN hr_contract_types hct ON hct.contract_type_id = hcp.contract_type_id
                    JOIN hr_work_process wp ON wp.employee_id = hcp.employee_id
                    JOIN hr_document_types dt ON dt.document_type_id = wp.document_type_id
                    WHERE hcp.to_date >= DATE(:fromDate)
                    AND hcp.to_date <= DATE(:toDate)
                    AND hct.classify_code = :classifyCode
                    AND IFNULL(hcp.is_deleted, :flagStatus) = :flagStatus
                    AND IFNULL(e.is_deleted, :flagStatus) = :flagStatus
                    AND wp.from_date <= hcp.to_date + 1
                    AND (wp.to_date >= hcp.to_date + 1 OR wp.to_date IS NULL)
                    AND IFNULL(wp.is_deleted, :flagStatus) = :flagStatus
                    AND dt.type = 'IN'
                    AND e.status = :empStatus
                    AND hcp.from_date = (
                        select max(hcp1.from_date) from hr_contract_process hcp1, hr_contract_types hct1
                        where hct1.contract_type_id = hcp1.contract_type_id
                        and hct1.classify_code = :classifyCode
                        and hcp1.employee_id = hcp.employee_id
                        and IFNULL(hcp1.is_deleted, :flagStatus) = :flagStatus
                    )
                    AND NOT EXISTS (
                        select contract_proposal_id
                        from pns_contract_proposals pcp
                        where pcp.employee_id = hcp.employee_id
                        and pcp.from_date = hcp.to_date + 1
                        and pcp.type = :type
                        and IFNULL(pcp.is_deleted, :flagStatus) = :flagStatus
                    )
                """;
        HashMap<String, Object> params = new HashMap<>();
        params.put("fromDate", fromDate);
        params.put("toDate", toDate);
        params.put("flagStatus", BaseConstants.STATUS.NOT_DELETED);
        params.put("empStatus", Constant.EMP_STATUS.WORK_IN);
        params.put("type", Constant.CONTRACT_TYPE.CONTINUE);
        params.put("classifyCode", Constant.CLASSIFY_CODE.HDLD);
        return getListData(sql, params, ContractProposalsDTO.class);
    }

    public List<ContractProposalsDTO> getListEmpNewContract() {
        String sql = """
                    SELECT e.employee_id,
                        e.employee_code,
                        f_get_org_abbreviation(wp.organization_id) orgCode,
                        f_get_suggest_new_contract(e.employee_id, wp.from_date, wp.position_id, wp.organization_id) contractTypeId,
                        wp.position_id,
                        wp.job_id,
                        wp.organization_id,
                        (select mp.pgr_id from mp_positions mp where mp.pos_id = wp.position_id) positionGroupId,
                        wp.from_date fromDate
                    FROM hr_work_process wp
                    JOIN hr_employees e ON e.employee_id = wp.employee_id
                    JOIN hr_document_types dt ON dt.document_type_id = wp.document_type_id
                    WHERE dt.type = 'IN'
                    AND IFNULL(wp.is_deleted, :flagStatus) = :flagStatus
                    AND IFNULL(e.is_deleted, :flagStatus) = :flagStatus
                    AND e.status = :empStatus
                    AND NOT EXISTS (
                        select 1 from hr_work_process wp1, hr_document_types dt1
                        where wp1.employee_id = wp.employee_id
                        and wp1.to_date = wp.from_date - 1
                        and dt1.document_type_id = wp1.document_type_id
                        and IFNULL(wp1.is_deleted, :flagStatus) = :flagStatus
                        and dt1.type <> 'OUT'
                    )
                    AND NOT EXISTS (
                        select 1 from hr_contract_process cp
                        where cp.employee_id = wp.employee_id
                        and IFNULL(cp.is_deleted, :flagStatus) = :flagStatus
                        and cp.from_date >= wp.from_date
                    )
                    AND NOT EXISTS (
                        select contract_proposal_id
                        from pns_contract_proposals pcp
                        where pcp.employee_id = wp.employee_id
                        and pcp.type = :type
                        and IFNULL(pcp.is_deleted, :flagStatus) = :flagStatus
                        and pcp.from_date = wp.from_date
                    )
                """;
        HashMap<String, Object> params = new HashMap<>();
        params.put("flagStatus", BaseConstants.STATUS.NOT_DELETED);
        params.put("empStatus", Constant.EMP_STATUS.WORK_IN);
        params.put("type", Constant.CONTRACT_TYPE.NEW);
        return getListData(sql, params, ContractProposalsDTO.class);
    }

    public ContractProposalsDTO getCurrentContractInfo(Long employeeId, Date fromDate, Date toDate) {
        StringBuilder sql = new StringBuilder("""
                    SELECT hcp.contract_type_id curContractTypeId,
                        hcp.contract_number curContractNumber,
                        hcp.from_date curFromDate,
                        hcp.to_date curToDate,
                        f_get_org_abbreviation(wp.organization_id) orgCode,
                        wp.position_id,
                        wp.job_id,
                        wp.organization_id,
                        (
                            select count(cp.contract_process_id)
                            from hr_contract_process cp, hr_contract_types hct
                            where cp.employee_id = hcp.employee_id
                            and IFNULL(cp.is_deleted, :flagStatus) = :flagStatus
                            and hct.contract_type_id = cp.contract_type_id
                            and hct.classify_code = :classifyCodeContract
                            and cp.from_date >= hcp.from_date
                            and (cp.from_date <= hcp.to_date  OR hcp.to_date IS NULL)
                        ) countSign
                    FROM hr_contract_process hcp
                    JOIN hr_work_process wp ON wp.employee_id = hcp.employee_id
                    JOIN hr_contract_types ct ON ct.contract_type_id = hcp.contract_type_id
                    WHERE IFNULL(hcp.is_deleted, :flagStatus) = :flagStatus
                    AND IFNULL(ct.is_deleted, :flagStatus) = :flagStatus
                    AND IFNULL(wp.is_deleted, :flagStatus) = :flagStatus
                    AND ct.classify_code = :classifyCode
                    AND hcp.employee_id = :employeeId
                    AND hcp.from_date <= :fromDate
                    AND wp.from_date <= hcp.from_date
                    AND IFNULL(wp.to_date, hcp.from_date) >= hcp.from_date
                    AND wp.from_date = (
                        select max(from_date) from hr_work_process wp1
                        where wp1.employee_id = wp.employee_id
                        AND wp1.from_date <= hcp.from_date
                        AND IFNULL(wp1.is_deleted, :flagStatus) = :flagStatus
                    )
                """);
        HashMap<String, Object> params = new HashMap<>();
        if (toDate != null) {
            sql.append(" AND IFNULL(hcp.to_date, :toDate) >= :toDate");
            params.put("toDate", toDate);
        }

        params.put("flagStatus", BaseConstants.STATUS.NOT_DELETED);
        params.put("classifyCode", Constant.CLASSIFY_CODE.HDLD);
        params.put("classifyCodeContract", Constant.CLASSIFY_CODE.PLHD);
        params.put("employeeId", employeeId);
        params.put("fromDate", fromDate);
        sql.append(" ORDER BY hcp.from_date DESC FETCH FIRST 1 ROWS ONLY");
        return getFirstData(sql.toString(), params, ContractProposalsDTO.class);
    }

    public List<ContractProposalsDTO> getListEmpAppendixContract() {
        String sql = """
                    SELECT sp.employee_id,
                        e.employee_code employeeCode,
                        f_get_org_abbreviation(wp.organization_id) orgCode,
                        f_get_suggest_contract_type(3, hcp.contract_type_id, wp.position_id, wp.organization_id) contractTypeId,
                        wp.position_id,
                        wp.job_id,
                        wp.organization_id,
                        (select mp.pgr_id from mp_positions mp where mp.pos_id = wp.position_id) positionGroupId,
                        sp.from_date fromDate,
                        sp.salary_amount amountSalary,
                        sp.salary_percent,
                        hcp.contract_type_id curContractTypeId,
                        hcp.contract_number curContractNumber,
                        hcp.from_date curFromDate,
                        hcp.to_date curToDate,
                        (
                            select count(cp.contract_process_id)
                            from hr_contract_process cp, hr_contract_types ct
                            where cp.employee_id = sp.employee_id
                            and IFNULL(cp.is_deleted, :flagStatus) = :flagStatus
                            and ct.contract_type_id = cp.contract_type_id
                            and ct.classify_code = :classifyCode
                            and cp.from_date >= hcp.from_date
                            and (cp.from_date <= hcp.to_date  OR hcp.to_date IS NULL)
                        ) countSign
                    FROM hr_salary_process sp
                    JOIN hr_work_process wp ON wp.employee_id = sp.employee_id
                    JOIN hr_employees e ON e.employee_id = sp.employee_id
                    JOIN hr_contract_process hcp ON hcp.employee_id = sp.employee_id
                    WHERE IFNULL(sp.is_deleted, :flagStatus) = :flagStatus
                    AND IFNULL(wp.is_deleted, :flagStatus) = :flagStatus
                    AND IFNULL(e.is_deleted, :flagStatus) = :flagStatus
                    AND e.status = :empStatus
                    AND hcp.from_date <= sp.from_date
                    AND (hcp.to_date >= sp.from_date OR hcp.to_date IS NULL)
                    AND hcp.contract_type_id IN (select ct.contract_type_id from hr_contract_types ct where ct.classify_code = :classifyCodeContract)
                    AND wp.from_date <= sp.from_date
                    AND (wp.to_date >= sp.from_date OR wp.to_date IS NULL)
                    AND sp.from_date = (
                        select max(sp1.from_date) from hr_salary_process sp1
                        where sp1.employee_id = sp.employee_id
                        and IFNULL(sp1.is_deleted, :flagStatus) = :flagStatus
                        and sp1.is_approve = 'Y'
                     )
                    AND sp.is_approve = 'Y'
                    and sp.from_date >= '01-jan-2022'
                    AND EXISTS (
                        select 1 from hr_salary_process sp1
                        where sp1.employee_id = sp.employee_id
                        and IFNULL(sp1.is_deleted, :flagStatus) = :flagStatus
                        and sp1.is_approve = 'Y'
                        and DATE(sp1.to_date) = DATE(sp.from_date - 1)
                        and sp.salary_amount < sp1.salary_amount
                    )
                    AND EXISTS (
                        select 1 from hr_work_process wp1, hr_document_types dt1
                        where wp1.employee_id = wp.employee_id
                        and wp1.document_type_id = dt1.document_type_id
                        and dt1.type <> 'OUT'
                        and sp.from_date - 1 between wp1.from_date and IFNULL(wp1.to_date, sp.from_date)
                    )
                    AND NOT EXISTS (
                        select 1
                        from hr_contract_process cp, hr_contract_types ct
                        where cp.employee_id = sp.employee_id
                        and cp.from_date = sp.from_date
                        and ct.contract_type_id = cp.contract_type_id
                        and ct.classify_code = :classifyCode
                        and IFNULL(cp.is_deleted, :flagStatus) = :flagStatus
                    )
                    AND NOT EXISTS (
                        select contract_proposal_id
                        from pns_contract_proposals pcp
                        where pcp.employee_id = sp.employee_id
                        and pcp.from_date = sp.from_date
                        and pcp.type = :type
                        and IFNULL(pcp.is_deleted, :flagStatus) = :flagStatus
                    )
                """;
        HashMap<String, Object> params = new HashMap<>();
        params.put("flagStatus", BaseConstants.STATUS.NOT_DELETED);
        params.put("empStatus", Constant.EMP_STATUS.WORK_IN);
        params.put("type", Constant.CONTRACT_TYPE.APPENDIX_SALARY);
        params.put("classifyCode", Constant.CLASSIFY_CODE.PLHD);
        params.put("classifyCodeContract", Constant.CLASSIFY_CODE.HDLD);
        return getListData(sql, params, ContractProposalsDTO.class);
    }

    public ContractProposalsResponse getSalaryProcessByEmpId(Long empId, Date dateReport) {
        String sql = """
                    SELECT sp.salary_amount amountSalary, sp.salary_percent salaryPercent
                    FROM hr_salary_process sp
                    WHERE IFNULL(sp.is_deleted, :flagStatus) = :flagStatus
                    AND sp.employee_id = :empId
                    AND sp.from_date <= :dateReport
                    AND (sp.to_date >= :dateReport OR sp.to_date IS NULL)
                    LIMIT 1
                """;
        Map<String, Object> params = new HashMap<>();
        params.put("flagStatus", BaseConstants.STATUS.NOT_DELETED);
        params.put("empId", empId);
        params.put("dateReport", dateReport);
        return queryForObject(sql, params, ContractProposalsResponse.class);
    }

    public void deleteContractProposal(List<Long> listId) {
        String sqlDeleteEvaluation = "DELETE FROM pns_contract_evaluations WHERE contract_proposal_id IN (:ids)";
        String sqlDeleteApprover = "DELETE FROM pns_contract_approvers WHERE contract_proposal_id IN (:ids)";
        String sqlDeleteProposal = "DELETE FROM pns_contract_proposals WHERE contract_proposal_id IN (:ids)";
        List<List<Long>> listPartition = Utils.partition(listId, Constant.SIZE_PARTITION);
        for (List<Long> ids : listPartition) {
            HashMap<String, Object> params = new HashMap<>();
            params.put("ids", ids);
            executeSqlDatabase(sqlDeleteEvaluation, params);
            executeSqlDatabase(sqlDeleteApprover, params);
            executeSqlDatabase(sqlDeleteProposal, params);
        }
    }

    public List<HrEmployeesEntity> getEmpByListId(List<Long> listEmployeeId) {
        String sql = "SELECT * FROM hr_employees WHERE employee_id IN (:listEmpId) AND IFNULL(is_deleted, :flagStatus) = :flagStatus";
        Map<String, Object> hashMap = new HashMap<>();
        hashMap.put("listEmpId", listEmployeeId);
        hashMap.put("flagStatus", BaseConstants.STATUS.NOT_DELETED);
        return getListData(sql, hashMap, HrEmployeesEntity.class);
    }

    public void updateSendMailStatus(List<Long> listContractProId) {  // cap nhat trang thai thai gui mail
        String sql = "UPDATE pns_contract_proposals SET is_sent_mail = 1 WHERE contract_proposal_id IN (:listContractProId)";
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("listContractProId", listContractProId);
        executeSqlDatabase(sql, hashMap);
    }

    public void updateStatus(List<Long> listContractProId, int status, String updateBy) {  // cap nhat trang thai thai gui mail
        String sql = """
                    UPDATE pns_contract_proposals
                    SET status = :status,
                        last_updated_by = :updateBy,
                        last_update_date = now()
                    WHERE contract_proposal_id IN (:listContractProId)
                """;
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("listContractProId", listContractProId);
        hashMap.put("status", status);
        hashMap.put("updateBy", updateBy);
        executeSqlDatabase(sql, hashMap);
    }

    public boolean isConflictProcessContractFee(Long empId, Long contractFeeId, Date fromDate, Date toDate) {
        String sql = """
                    SELECT pcf.from_date fromDate, pcf.to_date toDate
                    FROM pns_contract_fees pcf
                    WHERE pcf.employee_id = :employeeId
                    AND IFNULL(pcf.is_deleted, :flagStatus) = :flagStatus
                    AND pcf.contract_fee_id != :contractFeeId
                    AND pcf.from_date = :fromDate
                """;

        HashMap<String, Object> params = new HashMap<>();
        params.put("employeeId", empId);
        params.put("flagStatus", BaseConstants.STATUS.NOT_DELETED);
        params.put("contractFeeId", Utils.NVL(contractFeeId));
        params.put("fromDate", fromDate);
        if (toDate != null) {
            params.put("toDate", fromDate);
        }
        List<ContractFeesDTO> lst = getListData(sql, params, ContractFeesDTO.class);
        return (lst != null && !lst.isEmpty());
    }

    public String getOrgCode(Long orgId) {
        String sql = "SELECT f_get_org_abbreviation(:orgId) FROM dual";
        HashMap<String, Object> params = new HashMap<>();
        params.put("orgId", orgId);
        return queryForObject(sql, params, String.class);
    }

    public List<ContractProposalsEntity> searchContinueSignFullList(ContractProposalsDTO dto) {
        StringBuilder sql = new StringBuilder("SELECT a.*, e.email, e.employee_code, e.full_name");
        HashMap<String, Object> params = new HashMap<>();
        dto.setTypeList(1);
        addCondition(sql, params, dto);
        return getListData(sql.toString(), params, ContractProposalsEntity.class);
    }

    public List<ContractProposalsEntity> searchNewSignOrAddendumSignFullList(ContractProposalsDTO dto) {
        StringBuilder sql = new StringBuilder("SELECT a.*, e.email, e.employee_code, e.full_name");
        HashMap<String, Object> params = new HashMap<>();
        dto.setTypeList(1);
        addConditionSearchNewOrAppendix(sql, params, dto);
        return getListData(sql.toString(), params, ContractProposalsEntity.class);
    }


}
