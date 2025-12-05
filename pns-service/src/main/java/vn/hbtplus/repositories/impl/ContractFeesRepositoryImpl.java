/*
 * Copyright (C) 2022 EcoIT. All rights reserved.
 * EcoIT. Use is subject to license terms.
 */
package vn.hbtplus.repositories.impl;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.stereotype.Repository;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.constants.Constant;
import vn.hbtplus.constants.Scope;
import vn.hbtplus.models.BaseDataTableDto;
import vn.hbtplus.models.PermissionDataDto;
import vn.hbtplus.models.dto.ContractFeesDTO;
import vn.hbtplus.models.response.ContractFeesResponse;
import vn.hbtplus.repositories.BaseRepository;
import vn.hbtplus.repositories.entity.ContractFeesEntity;
import vn.hbtplus.services.AuthorizationService;
import vn.hbtplus.utils.QueryUtils;
import vn.hbtplus.utils.Utils;

import java.util.*;

/**
 * Lop repository Impl ung voi bang PNS_CONTRACT_FEES
 *
 * @author tudd
 * @version 1.0
 * @since 1.0
 */

@Repository
@RequiredArgsConstructor
public class ContractFeesRepositoryImpl extends BaseRepository {

    private final AuthorizationService authorizationService;

    public BaseDataTableDto<ContractFeesResponse> searchData(ContractFeesDTO dto) {
        StringBuilder sql = new StringBuilder("""
                    SELECT
                        a.contract_fee_id,
                        a.employee_id,
                        e.employee_code,
                        e.full_name,
                        a.from_date,
                        a.to_date,
                        a.is_deleted,
                        a.created_by,
                        a.create_date,
                        a.last_updated_by,
                        a.last_update_date,
                        a.reject_reason,
                        o.full_name as orgName,
                        o.org_name_manage,
                        o.org_name_level1,
                        o.org_name_level2,
                        o.org_name_level3,
                        job.job_name,
                        case when a.status = 0 then 'Dự thảo' when a.status = 1
                             then 'Đã phê duyệt' when a.status = 2 then 'Từ chối' end status_name,
                        a.status,
                        a.amount_fee
                """);
        HashMap<String, Object> params = new HashMap<>();
        addCondition(sql, params, dto);
        return getListPagination(sql.toString(), params, dto, ContractFeesResponse.class);
    }

    public List<Map<String, Object>> getListExport(ContractFeesDTO dto) {
        StringBuilder sql = new StringBuilder("""
                     SELECT
                        a.contract_fee_id,
                        a.employee_id,
                        e.EMPLOYEE_CODE,
                        e.full_name,
                        a.from_date,
                        a.to_date,
                        a.status,
                        o.org_name_manage,
                        o.org_name_level1,
                        o.org_name_level2,
                        o.org_name_level3,
                        job.job_name,
                        case when a.status = 0 then 'Chờ phê duyệt' when a.status = 1
                             then 'Phê duyệt' when a.status = 2 then 'Từ chối' end status_name,
                        a.reject_reason,
                        a.amount_fee
                """);
        Map<String, Object> params = new HashMap<>();
        addCondition(sql, params, dto);
        return getListData(sql.toString(), params);
    }

    public List<ContractFeesEntity> getListDataByForm(ContractFeesDTO dto) {
        StringBuilder sql = new StringBuilder("SELECT a.*");
        Map<String, Object> params = new HashMap<>();
        addCondition(sql, params, dto);
        return getListData(sql.toString(), params, ContractFeesEntity.class);
    }

    private void addCondition(StringBuilder sql, Map<String, Object> params, ContractFeesDTO dto) {
        sql.append(" FROM pns_contract_fees a"
                + " JOIN hr_employees e ON e.employee_id = a.employee_id "
                + " JOIN hr_organizations o ON o.organization_id = e.organization_id "
                + " left  JOIN mp_jobs job ON e.job_id = job.job_id "
                + " WHERE IFNULL(a.is_deleted, :activeStatus) = :activeStatus");

        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        List<PermissionDataDto> permissionDataDtoList = authorizationService.getPermissionData(Scope.VIEW, Constant.RESOURCE.PNS_FEE, Utils.getUserNameLogin());
        QueryUtils.addConditionPermission(permissionDataDtoList, sql, params);

        if (!Utils.isNullObject(dto.getOrgId())) {
            QueryUtils.filter("/" + dto.getOrgId() + "/", sql, params, "o.path_id");
        }

        QueryUtils.filter(dto.getStatus(), sql, params, "a.status");
        QueryUtils.filter(dto.getEmployeeCode(), sql, params, "e.employee_code");
        QueryUtils.filter(dto.getFullName(), sql, params, "e.full_name");
        if (dto.getOrgId() != null && dto.getOrgId() > 0L) {
            QueryUtils.filter("/" + dto.getOrgId() + "/", sql, params, "o.path_id");
        }
        if (dto.getFromDate() != null || dto.getToDate() != null) {
            if (dto.getToDate() == null) {
                sql.append(" and a.from_date <= DATE(:fromDate) and IFNULL(a.to_date,DATE(:fromDate)) >= DATE(:fromDate)");
            } else if (dto.getFromDate() == null) {
                sql.append(" and a.from_date <= DATE(:toDate) and IFNULL(a.to_date,DATE(:toDate)) >= DATE(:toDate)");
            } else {
                sql.append(" and a.from_date <= DATE(:toDate) and IFNULL(a.to_date,DATE(:toDate)) >= DATE(:fromDate)");
            }
            if (dto.getFromDate() != null) {
                params.put("fromDate", dto.getFromDate());
            }
            if (dto.getToDate() != null) {
                params.put("toDate", dto.getToDate());
            }
        }

        if (!Utils.isNullOrEmpty(dto.getListStatus())) {
            sql.append(" AND a.status IN (:statusList) ");
            params.put("statusList", dto.getListStatus());
        }

        if (dto.getYear() != null && dto.getYear() > 0L) {
            sql.append(" AND DATE_FORMAT(a.from_date, '%Y') = :year");
            params.put("year", String.valueOf(dto.getYear()));
        }
        if (Utils.isNullOrEmpty(dto.getOrderBy())) {
            sql.append(" ORDER BY o.display_seq ASC, o.path_id ASC, a.from_date desc");
        } else {
            sql.append(" ORDER BY a.").append(dto.getOrderBy().trim());
        }
    }

    public boolean isConflictProcess(ContractFeesDTO contractFeesDTO) {
        String sql = """
                     SELECT pcf.from_date fromDate, pcf.to_date toDate
                     FROM pns_contract_fees pcf
                     WHERE pcf.employee_id = :employeeId
                     AND IFNULL(pcf.is_deleted, :flagStatus) = :flagStatus
                     AND pcf.contract_fee_id != :contractFeeId
                     AND pcf.status in (0, 1)
                     AND pcf.from_date = :fromDate
                """;

        HashMap<String, Object> params = new HashMap<>();
        params.put("employeeId", contractFeesDTO.getEmployeeId());
        params.put("flagStatus", BaseConstants.STATUS.NOT_DELETED);
        params.put("contractFeeId", Utils.NVL(contractFeesDTO.getContractFeeId(), 0L));
        params.put("fromDate", contractFeesDTO.getFromDate());
        List<ContractFeesDTO> lst = getListData(sql, params, ContractFeesDTO.class);
        return (lst != null && !lst.isEmpty());
    }

    public void updateContractFeeProcess(ContractFeesEntity contractFeesEntity) {

        String sql = """
                     update pns_contract_fees a
                     set a.to_date = (
                        select min(a1.from_date) -1
                        from pns_contract_fees a1
                        where a1.employee_id = :employeeId
                        and a1.status = 1
                        and a1.from_date > a.from_date
                     )
                     where a.status = 1
                     and a.employee_id = :employeeId
                     and a.is_deleted = :activeStatus
                     and exists (
                        select a1.from_date
                        from pns_contract_fees a1
                        where a1.employee_id = :employeeId
                        and a1.status = 1
                        and a1.from_date > a.from_date
                        and a.is_deleted = :activeStatus
                        and (a.to_date is null or a.to_date >= a1.from_date)
                     )
                """;
        HashMap<String, Object> params = new HashMap<>();
        params.put("employeeId", contractFeesEntity.getEmployeeId());
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        executeSqlDatabase(sql, params);
    }

    public Map<String, ContractFeesEntity> getMapContractFeeByEmpCodes(List<String> listEmpCode) {
        String sql = """
                    SELECT cf.*
                    FROM pns_contract_fees cf
                    JOIN hr_employees e ON e.employee_id = cf.employee_id
                    WHERE IFNULL(cf.is_deleted, :flagStatus) = :flagStatus
                    AND e.employee_code IN (:empCodes)
                """;
        HashMap<String, Object> params = new HashMap<>();
        params.put("flagStatus", BaseConstants.STATUS.NOT_DELETED);
        List<List<String>> listPartition = Utils.partition(listEmpCode, Constant.SIZE_PARTITION);
        Map<String, ContractFeesEntity> results = new HashMap<>();
        for (List<String> empCodes : listPartition) {
            params.put("empCodes", empCodes);
            List<ContractFeesEntity> listResult = getListData(sql, params, ContractFeesEntity.class);
            if (listResult != null && !listResult.isEmpty()) {
                for (ContractFeesEntity entity : listResult) {
                    results.put(entity.getEmployeeId() + Utils.formatDate(entity.getFromDate()), entity);
                }
            }
        }
        return results;
    }

    public List<ContractFeesEntity> getListContractFeeById(List<Long> contractFeeIds, Integer status) {
        if (contractFeeIds == null || contractFeeIds.isEmpty()) {
            return new ArrayList<>();
        }
        String sql = " SELECT * FROM pns_contract_fees where contract_fee_id in (:contractFeeIds) and status = :status ";
        Map<String, Object> params = new HashMap<>();
        params.put("contractFeeIds", contractFeeIds);
        params.put("status", status);

        return getListData(sql, params, ContractFeesEntity.class);
    }

    public void autoCancel(Long employeeId, Date dateReport, String userName){
        String sql = """
                    UPDATE pns_contract_fees
                    SET to_date = :toDate,
                        last_updated_by = :userName,
                        last_update_date = :sysdate
                    WHERE employee_id = :employeeId
                    AND status = :status
                    AND from_date <= :dateReport
                    AND (to_date >= :dateReport OR to_date IS NULL)
                    AND IFNULL(is_deleted, :activeStatus) = :activeStatus
                """;
        HashMap<String, Object> params = new HashMap<>();
        params.put("toDate", DateUtils.addDays(dateReport, -1));
        params.put("userName", userName);
        params.put("sysdate", new Date());
        params.put("employeeId", employeeId);
        params.put("status", ContractFeesEntity.STATUS.APPROVED);
        params.put("dateReport", dateReport);
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        executeSqlDatabase(sql, params);
    }
}
