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
import vn.hbtplus.models.dto.ConfigSeqContractsDTO;
import vn.hbtplus.models.response.ConfigApprovalsResponse;
import vn.hbtplus.models.response.ConfigSeqContractsResponse;
import vn.hbtplus.repositories.BaseRepository;
import vn.hbtplus.repositories.entity.ConfigSeqContractsEntity;
import vn.hbtplus.services.AuthorizationService;
import vn.hbtplus.utils.QueryUtils;
import vn.hbtplus.utils.Utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Lop repository Impl ung voi bang PNS_CONFIG_SEQ_CONTRACTS
 *
 * @author tudd
 * @version 1.0
 * @since 1.0
 */

@Repository
@RequiredArgsConstructor
public class ConfigSeqContractsRepositoryImpl extends BaseRepository {
    private final AuthorizationService authorizationService;

    public BaseDataTableDto<ConfigSeqContractsResponse> searchData(ConfigSeqContractsDTO dto) {
        StringBuilder sql = new StringBuilder("""
                    SELECT
                        pceq.config_seq_contract_id configSeqContractId,
                        mpg.pgr_name positionGroupName,
                        CASE
                            WHEN pceq.to_date is NULL OR pceq.to_date >= DATE(now()) AND pceq.from_date <= DATE(now())  THEN 'Hiệu lực'
                            ELSE 'Hết hiệu lực'
                        END state,
                        vl.label empTypeName,
                        CASE
                           WHEN pceq.org_group IS NOT NULL THEN CASE pceq.org_group  WHEN 'CN' THEN  'Chi nhánh'  WHEN 'PGD' THEN  'Phòng giao dịch'  ELSE 'Hội sở' END
                        END orgGroup,
                        o.name orgName,
                        pceq.from_date fromDate,
                        pceq.to_date toDate,
                        GROUP_CONCAT(hct.name) contractTypeNameStr
                    FROM pns_config_seq_contracts pceq
                    LEFT JOIN hr_organizations o ON pceq.organization_id = o.organization_id
                    LEFT JOIN mp_position_groups mpg ON pceq.position_group_id = mpg.pgr_id
                    LEFT JOIN pns_config_seq_details pcsd ON pceq.config_seq_contract_id = pcsd.config_seq_contract_id
                    LEFT JOIN hr_contract_types hct ON pcsd.contract_type_id = hct.contract_type_id
                    LEFT JOIN sys_categories vl ON vl.code = pceq.emp_type_code AND vl.category_type = :typeCode
                    WHERE IFNULL(pceq.is_deleted, :activeStatus) = :activeStatus
                """);
        HashMap<String, Object> params = new HashMap<>();
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        params.put("typeCode", Constant.LOOKUP_CODES.DOI_TUONG_CV);

        List<PermissionDataDto> permissionDataDtoList = authorizationService.getPermissionData(Scope.VIEW, Constant.RESOURCE.PNS_CONFIG_SEQ, Utils.getUserNameLogin());
        QueryUtils.addConditionPermission(permissionDataDtoList, sql, params);

        if (!Utils.isNullObject(dto.getOrganizationId())) {
            QueryUtils.filter("/" + dto.getOrganizationId() + "/", sql, params, "o.path_id");
        }

        QueryUtils.filter(dto.getOrgGroup(), sql, params, "pceq.org_group");
        QueryUtils.filterEq(dto.getEmpTypeCode(), sql, params, "vl.code");
        QueryUtils.filter(dto.getPositionGroupId(), sql, params, "pceq.position_group_id");

        if (dto.getFromDate() != null && dto.getToDate() != null) {
            sql.append(" AND IFNULL(pceq.to_date,:fromDate) >= :fromDate" +
                       " AND pceq.from_date <= :toDate");
            params.put("fromDate", dto.getFromDate());
            params.put("toDate", dto.getToDate());
        } else if (dto.getFromDate() != null) {
            sql.append(" AND IFNULL(pceq.to_date,:fromDate) >= :fromDate" +
                       " AND pceq.from_date <= :fromDate");
            params.put("fromDate", dto.getFromDate());
        } else if (dto.getToDate() != null) {
            sql.append(" AND IFNULL(pceq.to_date,:toDate) >= :toDate" +
                       " AND pceq.from_date <= :toDate");
            params.put("toDate", dto.getToDate());
        }

        sql.append("""
                     GROUP BY
                        pceq.config_seq_contract_id,
                        mpg.pgr_name,
                        o.name,
                        pceq.org_group,
                        pceq.from_date,
                        pceq.to_date,
                        vl.label
                     ORDER BY pceq.config_seq_contract_id
                """);
        params.put("flagStatus", BaseConstants.STATUS.NOT_DELETED);
        return getListPagination(sql.toString(), params, dto, ConfigSeqContractsResponse.class);
    }

    public ConfigSeqContractsEntity isDuplicate(ConfigSeqContractsDTO dto) {
        Map<String, Object> mapParams = new HashMap<>();
        StringBuilder sql = new StringBuilder("""
                    SELECT cc.*
                    FROM pns_config_seq_contracts cc
                    WHERE IFNULL(cc.is_deleted, :flagStatus) = :flagStatus
                    AND cc.emp_type_code = :empTypeCode
                    AND IFNULL(cc.to_date,:fromDate) >= :fromDate
                    AND cc.from_date <= IFNULL(:toDate, cc.from_date)
                    AND cc.config_seq_contract_id != :configSeqContractId
                """);
        if (dto.getOrganizationId() != null && !dto.getOrganizationId().equals(0L)) {
            sql.append(" and cc.organization_id = :orgId");
            mapParams.put("orgId", dto.getOrganizationId());
        } else {
            sql.append(" and cc.organization_id is null");
        }

        if (!Utils.isNullOrEmpty(dto.getOrgGroup())) {
            sql.append(" and cc.org_group = :orgGroup");
            mapParams.put("orgGroup", dto.getOrgGroup());
        } else {
            sql.append(" and cc.org_group is null");
        }
        if (dto.getPositionGroupId() != null && !dto.getPositionGroupId().equals(0L)) {
            sql.append(" and cc.position_group_id = :positionGroupId");
            mapParams.put("positionGroupId", dto.getPositionGroupId());
        } else {
            sql.append(" and cc.position_group_id is null");
        }
        sql.append(" limit 1");
        mapParams.put("empTypeCode", dto.getEmpTypeCode());
        mapParams.put("flagStatus", BaseConstants.STATUS.NOT_DELETED);
        mapParams.put("fromDate", dto.getFromDate());
        mapParams.put("toDate", dto.getToDate());
        mapParams.put("configSeqContractId", Utils.NVL(dto.getConfigSeqContractId()));
        return queryForObject(sql.toString(), mapParams, ConfigSeqContractsEntity.class);
    }

    public List<Map<String, Object>> getDataConfigSeq(ConfigSeqContractsDTO dto) {
        StringBuilder sql = new StringBuilder("""
                     SELECT
                         pceq.config_seq_contract_id configSeqContractId,
                         mpg.pgr_name positionGroupName,
                         CASE
                             WHEN pceq.to_date is NULL OR pceq.to_date >= DATE(now()) AND pceq.from_date <= DATE(now())  THEN 'Hiệu lực'
                             ELSE 'Hết hiệu lực'
                         END state,
                         vl.label empTypeName,
                     CASE
                         WHEN pceq.org_group IS NOT NULL THEN CASE pceq.org_group  WHEN 'CN' THEN  'Chi nhánh'  WHEN 'PGD' THEN  'Phòng giao dịch'  ELSE 'Hội sở' END
                     END orgGroup,
                     o.name orgName,
                     pceq.from_date fromDate,
                     pceq.to_date toDate,
                     GROUP_CONCAT(hct.name ORDER BY pcsd.order_number) contractTypeNameStr
                     FROM pns_config_seq_contracts pceq
                     LEFT JOIN hr_organizations o ON pceq.organization_id = o.organization_id
                     LEFT JOIN mp_position_groups mpg ON pceq.position_group_id = mpg.pgr_id
                     LEFT JOIN pns_config_seq_details pcsd ON pceq.config_seq_contract_id = pcsd.config_seq_contract_id
                     LEFT JOIN hr_contract_types hct ON pcsd.contract_type_id = hct.contract_type_id
                     LEFT JOIN v_lookup vl ON vl.code = pceq.emp_type_code AND vl.type_code = :typeCode
                     WHERE IFNULL(pceq.is_deleted, :activeStatus) = :activeStatus
                """);
        HashMap<String, Object> params = new HashMap<>();
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        params.put("typeCode", Constant.LOOKUP_CODES.DOI_TUONG_CV);

        List<PermissionDataDto> permissionDataDtoList = authorizationService.getPermissionData(Scope.VIEW, Constant.RESOURCE.PNS_CONFIG_SEQ, Utils.getUserNameLogin());
        QueryUtils.addConditionPermission(permissionDataDtoList, sql, params);

        if (!Utils.isNullObject(dto.getOrganizationId())) {
            QueryUtils.filter("/" + dto.getOrganizationId() + "/", sql, params, "o.path_id");
        }

        QueryUtils.filter(dto.getOrgGroup(), sql, params, "pceq.org_group");
        QueryUtils.filterEq(dto.getEmpTypeCode(), sql, params, "vl.code");
        QueryUtils.filter(dto.getPositionGroupId(), sql, params, "pceq.position_group_id");

        if (dto.getFromDate() != null && dto.getToDate() != null) {
            sql.append(" AND IFNULL(pceq.to_date,:fromDate) >= :fromDate" +
                       "    AND pceq.from_date <= :toDate");
            params.put("fromDate", dto.getFromDate());
            params.put("toDate", dto.getToDate());
        } else if (dto.getFromDate() != null) {
            sql.append(" AND IFNULL(pceq.to_date,:fromDate) >= :fromDate" +
                       "    AND pceq.from_date <= :fromDate");
            params.put("fromDate", dto.getFromDate());
        } else if (dto.getToDate() != null) {
            sql.append(" AND IFNULL(pceq.to_date,:toDate) >= :toDate" +
                       "    AND pceq.from_date <= :toDate");
            params.put("toDate", dto.getToDate());
        }

        sql.append(" GROUP BY");
        sql.append("    pceq.config_seq_contract_id,");
        sql.append("    mpg.pgr_name,");
        sql.append("    o.name,");
        sql.append("    pceq.org_group,");
        sql.append("    pceq.from_date,");
        sql.append("    pceq.to_date,");
        sql.append("    vl.label");
        sql.append(" ORDER BY pceq.config_seq_contract_id");
        params.put("flagStatus", BaseConstants.STATUS.NOT_DELETED);

        return getListData(sql.toString(), params);
    }

}
