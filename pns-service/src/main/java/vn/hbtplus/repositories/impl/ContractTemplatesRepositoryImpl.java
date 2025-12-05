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
import vn.hbtplus.models.dto.ContractTemplatesDTO;
import vn.hbtplus.models.dto.OrgDTO;
import vn.hbtplus.models.response.ContractTemplatesResponse;
import vn.hbtplus.repositories.BaseRepository;
import vn.hbtplus.services.AuthorizationService;
import vn.hbtplus.utils.QueryUtils;
import vn.hbtplus.utils.Utils;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Lop repository Impl ung voi bang PNS_CONTRACT_TEMPLATES
 *
 * @author tudd
 * @version 1.0
 * @since 1.0
 */
@Repository
@RequiredArgsConstructor
public class ContractTemplatesRepositoryImpl extends BaseRepository {
    private final AuthorizationService authorizationService;

    public BaseDataTableDto<ContractTemplatesResponse> searchData(ContractTemplatesDTO dto) {
        HashMap<String, Object> params = new HashMap<>();
        StringBuilder sql = createSqlSearch(dto, params);
        return getListPagination(sql.toString(), params, dto, ContractTemplatesResponse.class);
    }

    private StringBuilder createSqlSearch(ContractTemplatesDTO dto, HashMap<String, Object> params) {
        StringBuilder sql = new StringBuilder("""
                    SELECT
                        ct.contract_template_id,
                        ct.name,
                        ct.file_name,
                        CASE
                            WHEN ct.org_group IS NOT NULL THEN CASE ct.org_group  WHEN 'CN' THEN  'Chi nhánh'  WHEN 'PGD' THEN  'Phòng giao dịch'  ELSE 'Hội sở' END
                        END orgGroup,
                        o.name orgName,
                        CASE
                            WHEN ct.to_date IS NULL OR ct.to_date >= DATE(now()) AND ct.from_date <= DATE(now())  THEN 'Hiệu lực'
                            ELSE 'Hết hiệu lực'
                        END state,
                        CASE
                            WHEN o.name IS NULL THEN 'Nhóm đơn vị'
                            ELSE 'Đơn vị'
                        END scope,
                        ct.from_date,
                        ct.to_date,
                        (
                            SELECT GROUP_CONCAT(mpg.name)
                            FROM pns_template_pos_groups ptpg, hr_position_groups mpg
                            WHERE  ct.contract_template_id = ptpg.contract_template_id
                            AND ptpg.position_group_id = mpg.position_group_id
                            ORDER BY mpg.position_group_id
                        ) positionGroupStr,
                        hct.name contractTypeName,
                        et.name empTypeName
                    FROM pns_contract_templates ct
                    LEFT JOIN hr_organizations o ON ct.organization_id = o.organization_id
                    LEFT JOIN hr_contract_types hct ON ct.contract_type_id = hct.contract_type_id
                    LEFT JOIN hr_emp_types et ON hct.emp_type_id = et.emp_type_id
                    WHERE IFNULL(ct.is_deleted, :flagStatus) = :flagStatus
                """);
        params.put("flagStatus", BaseConstants.STATUS.NOT_DELETED);
        params.put("typeEmpTypeCode", Constant.LOOKUP_CODES.DOI_TUONG_CV);

        List<PermissionDataDto> permissionDataDtoList = authorizationService.getPermissionData(Scope.VIEW, Constant.RESOURCE.PNS_CONTRACT_TEMPLATES, Utils.getUserNameLogin());
        QueryUtils.addConditionPermission(permissionDataDtoList, sql, params);

        if (!Utils.isNullObject(dto.getOrganizationId())) {
            QueryUtils.filter("/" + dto.getOrganizationId() + "/", sql, params, "o.path_id");
        }

        QueryUtils.filter(dto.getOrgGroup(), sql, params, "ct.org_group");
        if (!Utils.isNullOrEmpty(dto.getPositionGroupId())) {
            sql.append(" AND EXISTS (SELECT 1 FROM pns_template_pos_groups ptpg WHERE ct.contract_template_id = ptpg.contract_template_id AND ptpg.position_group_id IN (:listPositionGroup) )");
            params.put("listPositionGroup", dto.getPositionGroupId());
        }
        QueryUtils.filter(dto.getName(), sql, params, "ct.name");
        QueryUtils.filter(dto.getContractTypeId(), sql, params, "ct.contract_type_id");
        QueryUtils.filter(dto.getListEmpTypeCode(), sql, params, "hct.emp_type_id");
        if (dto.getFromDate() != null && dto.getToDate() != null) {
            sql.append("""
                        AND IFNULL(ct.to_date,:fromDate) >= :fromDate
                        AND ct.from_date <= :toDate
                    """);
            params.put("fromDate", dto.getFromDate());
            params.put("toDate", dto.getToDate());
        } else if (dto.getFromDate() != null) {
            sql.append("""
                        AND IFNULL(ct.to_date,:fromDate) >= :fromDate
                        AND ct.from_date <= :fromDate
                    """);
            params.put("fromDate", dto.getFromDate());
        } else if (dto.getToDate() != null) {
            sql.append("""
                        AND IFNULL(ct.to_date,:toDate) >= :toDate 
                        AND ct.from_date <= :toDate
                    """);
            params.put("toDate", dto.getToDate());
        }

        sql.append(" ORDER BY ct.contract_template_id");
        return sql;
    }

    public OrgDTO getOrg(Long orgId) {
        Map<String, Object> mapParam = new HashMap<>();
        mapParam.put("orgId", orgId);
        String sql = "SELECT organization_id orgId, name orgName, path_id path FROM hr_organizations WHERE organization_id = :orgId";
        return this.getFirstData(sql, mapParam, OrgDTO.class);
    }

    public boolean isConflictProcess(ContractTemplatesDTO dto) {
        StringBuilder sql = new StringBuilder("""
                     SELECT ct.from_date fromDate, ct.to_date toDate
                     FROM pns_contract_templates ct
                     WHERE ct.contract_template_id != :contractTemplateId
                     AND IFNULL(ct.is_deleted, :activeStatus) = :activeStatus
                     AND (ct.to_date IS NULL OR :fromDate <= ct.to_date)
                """);
        HashMap<String, Object> params = new HashMap<>();
        QueryUtils.filter(dto.getOrganizationId(), sql, params, "ct.organization_id");
        QueryUtils.filterEq(dto.getOrgGroup(), sql, params, "ct.org_group");
        QueryUtils.filter(dto.getContractTypeId(), sql, params, "ct.contract_type_id");
        QueryUtils.filterLe(dto.getToDate(), sql, params, "ct.from_date");
        if (!Utils.isNullOrEmpty(dto.getPosition())) {
            sql.append("""
                         AND EXISTS(
                            select 1 from pns_template_pos_groups tg
                            where tg.contract_template_id = ct.contract_template_id
                            and tg.position_group_id IN (:listPositionGroupId)
                         )
                    """);
            params.put("listPositionGroupId", dto.getPosition());
        }
        params.put("contractTemplateId", Utils.NVL(dto.getContractTemplateId()));
        params.put("fromDate", dto.getFromDate());
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);

        List<ContractTemplatesResponse> lst = getListData(sql.toString(), params, ContractTemplatesResponse.class);
        if (lst == null || lst.isEmpty()) {
            return false;
        } else if (lst.size() > 1) {
            return true;
        } else {
            ContractTemplatesResponse contractTemplatesResponse = lst.get(0);
            return dto.getToDate() != null || !dto.getFromDate().before(contractTemplatesResponse.getFromDate());
        }
    }

    public ContractTemplatesResponse getContractTemplate(Long contractTypeId, Long orgId, Long posId, Date reportDate) {
        String sql = """
                    select
                        tpl.*,
                        case
                            when exists (
                                select 1 from pns_template_pos_groups tpg
                                where tpg.contract_template_id = tpg.contract_template_id
                            ) then 1
                            else 0
                        end isExistsConfigPos
                    from pns_contract_templates tpl, hr_organizations o
                    where (tpl.organization_id = o.organization_id or tpl.org_group = o.org_group)
                    and (select path_id from hr_organizations where org_id = :orgId) like CONCAT(o.path_id, '%')
                    and tpl.contract_type_id = :contractTypeId
                    and (
                        not exists (
                            select 1 from pns_template_pos_groups tpg
                            where tpg.contract_template_id = tpl.contract_template_id
                        )
                        or exists (
                            select 1 from pns_template_pos_groups tpg, hr_position_group_values mpg
                            where tpg.contract_template_id = tpl.contract_template_id
                            and tpg.position_group_id = mpg.position_group_id
                            and mpg.position_id = :posId
                        )
                    )
                    and :reportDate between tpl.from_date and IFNULL(tpl.to_date,:reportDate)
                    and tpl.is_deleted = :activeStatus
                    order by o.path_level desc, IFNULL(o.organization_id,0) desc, isExistsConfigPos desc
                """;
        Map<String, Object> mapParam = new HashMap<>();
        mapParam.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        mapParam.put("orgId", orgId);
        mapParam.put("contractTypeId", contractTypeId);
        mapParam.put("reportDate", reportDate);
        mapParam.put("posId", posId);
        List<ContractTemplatesResponse> templatesDTOS = getListData(sql, mapParam, ContractTemplatesResponse.class);
        return templatesDTOS.isEmpty() ? null : templatesDTOS.get(0);
    }

    public List<Map<String, Object>> getListContractTemplate(ContractTemplatesDTO dto) {
        HashMap<String, Object> params = new HashMap<>();
        StringBuilder sql = createSqlSearch(dto, params);
        return getListData(sql.toString(), params);
    }
}
