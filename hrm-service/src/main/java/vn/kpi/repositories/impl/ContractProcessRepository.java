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
import vn.kpi.models.request.ContractProcessRequest;
import vn.kpi.models.request.EmployeesRequest;
import vn.kpi.models.response.ContractProcessResponse;
import vn.kpi.repositories.BaseRepository;
import vn.kpi.repositories.entity.ContractProcessEntity;
import vn.kpi.services.AuthorizationService;
import vn.kpi.utils.QueryUtils;
import vn.kpi.utils.Utils;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * Lop repository Impl ung voi bang hr_contract_process
 *
 * @author tudd
 * @version 1.0
 * @since 1.0
 */

@Repository
@RequiredArgsConstructor
public class ContractProcessRepository extends BaseRepository {
    private final PermissionFeignClient permissionFeignClient;
    private final HttpServletRequest request;
    private final AuthorizationService authorizationService;

    public BaseDataTableDto<ContractProcessResponse.SearchResult> searchData(EmployeesRequest.SearchForm dto) {
        Pair<String, Map<String, Object>> pair = buildSql(null, dto);
        return getListPagination(pair.getLeft(), pair.getRight(), dto, ContractProcessResponse.SearchResult.class);
    }

    private Pair<String, Map<String, Object>> buildSql(String sqlSelect, EmployeesRequest.SearchForm dto) {
        StringBuilder sql = new StringBuilder(sqlSelect == null ?"""
                SELECT
                    a.contract_process_id,
                    a.employee_id,
                    e.employee_code,
                    e.full_name,
                    a.emp_type_id,
                    a.contract_type_id,
                    a.start_date,
                    a.end_date,
                    a.document_no,
                    a.document_signed_date,
                    a.created_by,
                    a.created_time,
                    a.modified_by,
                    a.modified_time,
                    a.last_update_time,
                    NVL(o.full_name, o.name) orgName,
                    mj.name jobName,
                    (select et.name from hr_emp_types et where et.emp_type_id = a.emp_type_id and et.is_deleted = :activeStatus) empTypeName,
                    (select ct.name from hr_contract_types ct where ct.contract_type_id = a.contract_type_id and ct.is_deleted = :activeStatus) contractTypeName,
                    (select sc.name from sys_categories sc where sc.value = e.status and sc.category_type = :empStatus) empStatusName
                    FROM hr_contract_process a
                    JOIN hr_employees e ON (e.employee_id = a.employee_id AND NVL(e.is_deleted, :activeStatus) = :activeStatus)
                    LEFT JOIN hr_jobs mj ON mj.job_id = e.job_id
                    JOIN hr_organizations o ON o.organization_id = e.organization_id
                    WHERE NVL(a.is_deleted, :activeStatus) = :activeStatus
                """ : sqlSelect);
        HashMap<String, Object> params = new HashMap<>();
        addCondition(sql, params, dto);

        return new MutablePair<>(sql.toString(), params);
    }

    public List<Map<String, Object>> getListExport(String sqlSelect, EmployeesRequest.SearchForm dto) {
        Pair<String, Map<String, Object>> pair = buildSql(sqlSelect, dto);
        List<Map<String, Object>> dataList = getListData(pair.getLeft(), pair.getRight());

        if (Utils.isNullOrEmpty(dataList)) {
            dataList.add(getMapEmptyAliasColumns(pair.getLeft()));
        }
        return dataList;
    }

    private void addCondition(StringBuilder sql, Map<String, Object> params, EmployeesRequest.SearchForm dto) {
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        params.put("empStatus", Constant.CATEGORY_CODES.HR_TRANG_THAI_NHAN_VIEN);
        UtilsEmployeeSearchRepository.setParamEmployeeSearch(dto, sql, params);
        QueryUtils.filter(dto.getDocumentNo(), sql, params, "a.document_no");
        QueryUtils.filter(dto.getListContractType(), sql, params, "a.contract_type_id");
        if (!Utils.isNullOrEmpty(dto.getListDocumentSignedDate())) {
            QueryUtils.filterGe(Utils.stringToDate(dto.getListDocumentSignedDate().get(0)), sql, params, "a.document_signed_date", "fromDocumentSignedDate");
            QueryUtils.filterLe(dto.getListDocumentSignedDate().size() > 1 ? Utils.stringToDate(dto.getListDocumentSignedDate().get(1)) : null, sql, params, "a.document_signed_date", "toDocumentSignedDate");
        }

        if (!Utils.isNullOrEmpty(dto.getListStartDate())) {
            QueryUtils.filterGe(Utils.stringToDate(dto.getListStartDate().get(0)), sql, params, "a.start_date", "startDate");
            QueryUtils.filterLe(dto.getListStartDate().size() > 1 ? Utils.stringToDate(dto.getListStartDate().get(1)) : null, sql, params, "a.start_date", "endDate");
        }

        List<PermissionDataDto> permissionDataDtos = authorizationService.getPermissionData(
                Scope.VIEW, Constant.RESOURCES.CONTRACT_PROCESS, Utils.getUserNameLogin()
        );
        QueryUtils.addConditionPermission(permissionDataDtos, sql, params);
        sql.append(" ORDER BY e.employee_id, a.start_date DESC");
    }

    public boolean checkExitContractProcess(Long employeeId, Date fromDate, Date toDate, Long contractProcessId) {
        String sql = """
                SELECT
                    count(1)
                FROM
                    hr_contract_process a
                WHERE
                    a.employee_id = :employeeId
                    AND a.classify_code =:classifyCode
                    AND NVL(a.is_deleted, :activeStatus) = :activeStatus
                    AND a.start_date <= :startDate
                    AND (a.end_date >= :endDate OR a.end_date IS NULL)
                    AND a.contract_process_id != :contractProcessId
                """;
        HashMap<String, Object> params = new HashMap<>();
        params.put("employeeId", employeeId);
        params.put("startDate", Utils.truncDate(fromDate));
        params.put("endDate", Utils.truncDate(Utils.NVL(toDate, Utils.stringToDate("30/12/5555"))));
        params.put("classifyCode", Constant.CLASSIFY_CONTRACT.HOP_DONG);
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        params.put("contractProcessId", Utils.NVL(contractProcessId));
        return queryForObject(sql, params, Integer.class) > 0;
    }

    public List<ContractProcessResponse.DetailBean> getConflictProcess(ContractProcessRequest.SubmitForm dto, Long employeeId, Long id) {
        StringBuilder sql = new StringBuilder("""
                     SELECT sp.start_date, sp.end_date, sp.document_signed_date, sp.contract_type_id
                     FROM hr_contract_process sp
                     LEFT JOIN hr_contract_types t ON sp.contract_type_id = t.contract_type_id
                     WHERE sp.employee_id = :employeeId
                     AND t.classify_code = :typeCode
                     AND NVL(sp.is_deleted, :activeStatus) = :activeStatus
                     AND sp.contract_process_id != :contractProcessId
                     AND (sp.end_date IS NULL OR :fromDate <= sp.end_date)
                """);

        HashMap<String, Object> params = new HashMap<>();
        params.put("employeeId", employeeId);
        params.put("typeCode", dto.getClassifyCode());
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        params.put("contractProcessId", Utils.NVL(id, 0L));
        params.put("fromDate", dto.getStartDate());
        if (dto.getEndDate() != null) {
            sql.append(" AND sp.start_date <= :toDate");
            params.put("toDate", dto.getEndDate());
        }
        return getListData(sql.toString(), params, ContractProcessResponse.DetailBean.class);
    }

    public BaseDataTableDto<ContractProcessResponse.SearchResult> getTableList(Long employeeId, BaseSearchRequest request) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    a.contract_process_id,
                    a.employee_id,
                    a.emp_type_id,
                    a.contract_type_id,
                    a.start_date,
                    a.end_date,
                    a.document_no,
                    a.document_signed_date,
                    a.created_by,
                    a.created_time,
                    a.modified_by,
                    a.modified_time,
                    a.last_update_time,
                    (select et.name from hr_emp_types et where et.emp_type_id = a.emp_type_id) empTypeName,
                    (select ct.name from hr_contract_types ct where ct.contract_type_id = a.contract_type_id) contractTypeName,
                    (select sc.name from sys_categories sc where sc.value = e.status and sc.category_type = :empStatus) empStatusName
                    FROM hr_contract_process a
                    JOIN hr_employees e ON e.employee_id = a.employee_id
                    WHERE a.is_deleted = :activeStatus
                    and a.employee_id = :employeeId
                """);
        HashMap<String, Object> params = new HashMap<>();
        params.put("employeeId", employeeId);
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        params.put("empStatus", Constant.CATEGORY_CODES.HR_TRANG_THAI_NHAN_VIEN);
        return getListPagination(sql.toString(), params, request, ContractProcessResponse.SearchResult.class);
    }

    public void autoUpdateEmpType(String userName, Long employeeId) {
        StringBuilder sql = new StringBuilder("""
                    update hr_employees e
                    left join (
                		select
                			cp1.employee_id,
                			cp1.emp_type_id
                		from hr_contract_process cp1
                		where cp1.is_deleted = 'N'
                		and DATE(now()) BETWEEN cp1.start_date and IFNULL(cp1.end_date, now())
                		and cp1.classify_code = :classifyCode
                	) current on e.employee_id = current.employee_id
                	left join (
                		select
                			cp1.employee_id,
                			cp1.emp_type_id
                		from hr_contract_process cp1
                		where cp1.is_deleted = 'N'
                		and cp1.classify_code = :classifyCode
                		and cp1.start_date = (
                			select max(cp2.start_date) from hr_contract_process cp2
                			where cp2.employee_id = cp1.employee_id
                			and cp2.is_deleted = 'N'
                			and cp2.classify_code = :classifyCode
                		)
                	) last on e.employee_id = last.employee_id
                    set
                        e.modified_time = now(),
                        e.modified_by = :userName,
                        e.emp_type_id = IFNULL(current.emp_type_id, last.emp_type_id)
                    where e.is_deleted = :activeStatus
                    and IFNULL(e.emp_type_id, -1) <> IFNULL(current.emp_type_id, IFNULL(last.emp_type_id, e.emp_type_id))
                """);
        HashMap<String, Object> params = new HashMap<>();
        if (employeeId != null && employeeId > 0L) {
            sql.append(" and e.employee_id = :employeeId");
            params.put("employeeId", employeeId);
        }

        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        params.put("classifyCode", Constant.CLASSIFY_CONTRACT.HOP_DONG);
        params.put("userName", userName);
        executeSqlDatabase(sql.toString(), params);
    }


    public Map<Long, List<ContractProcessEntity>> getMapDataByCode(List<String> empCodes) {
        String sql = """
                select a.*
                from hr_contract_process a
                where a.is_deleted = 'N'
                and a.employee_id in (
                    select employee_id from hr_employees where employee_code in (:empCodes)
                )
                ORDER BY a.start_date DESC
                """;
        Map<String, Object> params = new HashMap<>();
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        params.put("empCodes", empCodes);
        List<ContractProcessEntity> listData = getListData(sql, params, ContractProcessEntity.class);
        Map<Long, List<ContractProcessEntity>> result = new HashMap<>();
        for (ContractProcessEntity entity : listData) {
            result.computeIfAbsent(entity.getEmployeeId(), k -> new ArrayList<>());
            result.get(entity.getEmployeeId()).add(entity);
        }

        return result;
    }
}
