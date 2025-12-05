/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.kpi.repositories.impl;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Repository;
import vn.kpi.constants.BaseConstants;
import vn.kpi.constants.Constant;
import vn.kpi.constants.Scope;
import vn.kpi.feigns.PermissionFeignClient;
import vn.kpi.models.BaseDataTableDto;
import vn.kpi.models.BaseSearchRequest;
import vn.kpi.models.PermissionDataDto;
import vn.kpi.models.request.BankAccountsRequest;
import vn.kpi.models.request.EmployeesRequest;
import vn.kpi.models.response.BankAccountsResponse;
import vn.kpi.repositories.BaseRepository;
import vn.kpi.repositories.entity.BankAccountsEntity;
import vn.kpi.services.AuthorizationService;
import vn.kpi.utils.QueryUtils;
import vn.kpi.utils.Utils;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Lop repository Impl ung voi bang hr_bank_accounts
 *
 * @author tudd
 * @version 1.0
 * @since 1.0
 */

@Repository
@RequiredArgsConstructor
public class BankAccountsRepository extends BaseRepository {
    private final PermissionFeignClient permissionFeignClient;
    private final HttpServletRequest request;
    private final AuthorizationService authorizationService;

    public BaseDataTableDto<BankAccountsResponse.SearchResult> searchData(EmployeesRequest.SearchForm dto) {
        Pair<String, Map<String, Object>> pair = buildSql(dto);
        return getListPagination(pair.getLeft(), pair.getRight(), dto, BankAccountsResponse.SearchResult.class);
    }

    private Pair<String, Map<String, Object>> buildSql(EmployeesRequest.SearchForm dto) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    a.bank_account_id,
                    a.employee_id,
                    e.employee_code,
                    e.full_name,
                    a.account_no,
                    a.bank_id,
                    a.bank_branch,
                    a.is_main,
                    CASE
                		WHEN a.is_main = 'Y' THEN 'Có'
                		ELSE 'Không'
                	END AS isMain,
                    a.is_deleted,
                    a.created_by,
                    a.created_time,
                    a.modified_by,
                    a.modified_time,
                    a.last_update_time,
                    NVL(o.full_name, o.name) orgName,
                    mj.name jobName,
                    (select et.name from hr_emp_types et where et.emp_type_id = e.emp_type_id) empTypeName,
                    (select sc.name from sys_categories sc where sc.value = e.status and sc.category_type = :empStatus) empStatusName,
                    (select concat(sc.name, ' (', sc.code, ')') from sys_categories sc where sc.value = a.bank_id and sc.category_type = :bankCode) bankName,
                    (select sc.name from sys_categories sc where sc.value = a.account_type_id and sc.category_type = :accountTypeCode) accountTypeName
                """);
        HashMap<String, Object> params = new HashMap<>();
        addCondition(sql, params, dto);

        return new MutablePair<>(sql.toString(), params);
    }

    public List<Map<String, Object>> getListExport(EmployeesRequest.SearchForm dto) {
        Pair<String, Map<String, Object>> pair = buildSql(dto);

        List<Map<String, Object>> dataList = getListData(pair.getLeft(), pair.getRight());
        if (Utils.isNullOrEmpty(dataList)) {
            dataList.add(getMapEmptyAliasColumns(pair.getKey()));
        }

        return dataList;
    }

    private void addCondition(StringBuilder sql, Map<String, Object> params, EmployeesRequest.SearchForm dto) {
        sql.append("""
                    FROM hr_bank_accounts a
                    JOIN hr_employees e ON (e.employee_id = a.employee_id AND NVL(e.is_deleted, :activeStatus) = :activeStatus)
                    LEFT JOIN hr_jobs mj ON mj.job_id = e.job_id
                    JOIN hr_organizations o ON o.organization_id = e.organization_id
                    WHERE NVL(a.is_deleted, :activeStatus) = :activeStatus
                """);
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        params.put("empStatus", Constant.CATEGORY_CODES.HR_TRANG_THAI_NHAN_VIEN);
        params.put("bankCode", Constant.CATEGORY_CODES.NGAN_HANG);
        params.put("accountTypeCode", Constant.CATEGORY_CODES.LOAI_TAI_KHOAN);
        UtilsEmployeeSearchRepository.setParamEmployeeSearch(dto, sql, params);
        QueryUtils.filter(dto.getListAccountType(), sql, params, "a.account_type_id");
        QueryUtils.filter(dto.getAccountNo(), sql, params, "a.account_no");
        QueryUtils.filter(dto.getListBank(), sql, params, "a.bank_id");
        QueryUtils.filter(dto.getBankBranch(), sql, params, "a.bank_branch");
        List<PermissionDataDto> permissionDataDtos = authorizationService.getPermissionData(
                 Scope.VIEW, Constant.RESOURCES.BANK_ACCOUNTS, Utils.getUserNameLogin()
        );
        QueryUtils.addConditionPermission(permissionDataDtos, sql, params);
        sql.append(" ORDER BY e.employee_id");
    }

    public BaseDataTableDto<BankAccountsResponse.SearchResult> getBankAccounts(Long employeeId, BaseSearchRequest request) {
        String sql = """
                 SELECT
                	ba.bank_account_id bankAccountId,
                	sc.code accountTypeCode,
                	concat(sc.name, ' (', sc.code, ')') accountTypeName,
                	ba.account_no accountNo,
                	ba.bank_id bankId,
                	ba.employee_id employeeId,
                	ba.bank_branch bankBranch,
                 	ba.is_main,
                 	CASE WHEN ba.is_main = 'Y'
                 	    THEN 'Có' ELSE 'Không'
                    END AS isMain,
                    ba.created_by createdBy,
                    ba.created_time createdTime,
                    ba.modified_by modifiedBy,
                    ba.modified_time modifiedTime
                FROM hr_bank_accounts ba
                LEFT JOIN sys_categories sc ON
                	sc.value = ba.bank_id AND sc.category_type = :categoryTypeCode
                WHERE ba.is_deleted = :isDeleted AND employee_id = :employeeId
                ORDER BY ba.is_main, ba.bank_account_id
                """;
        HashMap<String, Object> hashMapParams = new HashMap<>();
        hashMapParams.put("isDeleted", BaseConstants.STATUS.NOT_DELETED);
        hashMapParams.put("employeeId", employeeId);
        hashMapParams.put("categoryTypeCode", BaseConstants.CATEGORY_CODES.NGAN_HANG);
        return getListPagination(sql, hashMapParams, request, BankAccountsResponse.SearchResult.class);
    }

    public boolean checkDuplicateBankAccount(BankAccountsRequest.SubmitForm bankAccountInfo, Long bankAccountId) {
        String sql = """
                    select count(1)
                    from hr_bank_accounts p
                    where NVL(p.is_deleted, :activeStatus) = :activeStatus
                    and p.account_no = :accountNo
                    and bank_account_id != :bankAccountId
                """;
        Map<String, Object> params = new HashMap<>();
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        params.put("accountNo", bankAccountInfo.getAccountNo());
        params.put("bankAccountId", Utils.NVL(bankAccountId));
        return queryForObject(sql, params, Integer.class) > 0;
    }

    public void updateBankAccount(Long employeeId, Long bankAccountId) {
        String sql = """
                    UPDATE hr_bank_accounts
                    SET is_main = 'N', modified_by = :userName, modified_time = now()
                    WHERE is_deleted = 'N'
                    AND employee_id = :employeeId
                    AND is_main = 'Y'
                    AND bank_account_id != :bankAccountId
                """;
        Map<String, Object> params = new HashMap<>();
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        params.put("bankAccountId", bankAccountId);
        params.put("employeeId", employeeId);
        params.put("userName", Utils.getUserNameLogin());
        executeSqlDatabase(sql, params);
    }

    public BankAccountsResponse.DetailBean getBankAccount(Long employeeId) {
        String sql = """
                SELECT
                    ba.account_no,
                    (select sc.code from sys_categories sc where sc.value = ba.bank_id and sc.category_type = :bankCode) bankCode
                FROM hr_bank_accounts ba
                where ba.is_deleted = 'N'
                and ba.is_main = 'Y'
                and ba.employee_id = :employeeId
                """;
        HashMap<String, Object> params = new HashMap<>();
        params.put("employeeId", employeeId);
        params.put("bankCode", BaseConstants.CATEGORY_CODES.NGAN_HANG);
        return getFirstData(sql, params, BankAccountsResponse.DetailBean.class);
    }


    public Map<Long, List<BankAccountsEntity>> getMapDataByCode(List<String> empCodes) {
        String sql = """
                select a.*
                from hr_bank_accounts a
                where a.is_deleted = 'N'
                and a.employee_id in (
                    select employee_id from hr_employees where employee_code in (:empCodes)
                )
                """;
        Map<String, Object> params = new HashMap<>();
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        params.put("empCodes", empCodes);
        List<BankAccountsEntity> listData = getListData(sql, params, BankAccountsEntity.class);
        Map<Long, List<BankAccountsEntity>> result = new HashMap<>();
        for (BankAccountsEntity entity : listData) {
            result.computeIfAbsent(entity.getEmployeeId(), k -> new ArrayList<>());
            result.get(entity.getEmployeeId()).add(entity);
        }

        return result;
    }

    public Map<String, List<BankAccountsEntity>> getMapAllData() {
        String sql = """
                select a.*
                from hr_bank_accounts a
                where a.is_deleted = 'N'
                """;
        Map<String, Object> params = new HashMap<>();
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        List<BankAccountsEntity> listData = getListData(sql, params, BankAccountsEntity.class);
        Map<String, List<BankAccountsEntity>> result = new HashMap<>();
        for (BankAccountsEntity entity : listData) {
            if (entity.getAccountNo() != null) {
                if (result.get(entity.getAccountNo().toLowerCase()) == null) {
                    List<BankAccountsEntity> data = new ArrayList<>();
                    data.add(entity);
                    result.put(entity.getAccountNo().toLowerCase(), data);
                } else {
                    result.get(entity.getAccountNo().toLowerCase()).add(entity);
                }
            }
        }

        return result;
    }
}
