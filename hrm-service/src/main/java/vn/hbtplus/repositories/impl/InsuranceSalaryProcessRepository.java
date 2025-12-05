/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.repositories.impl;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Repository;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.constants.Constant;
import vn.hbtplus.constants.Scope;
import vn.hbtplus.feigns.PermissionFeignClient;
import vn.hbtplus.models.BaseDataTableDto;
import vn.hbtplus.models.BaseSearchRequest;
import vn.hbtplus.models.PermissionDataDto;
import vn.hbtplus.models.request.EmployeesRequest;
import vn.hbtplus.models.request.InsuranceSalaryProcessRequest;
import vn.hbtplus.models.response.InsuranceSalaryProcessResponse;
import vn.hbtplus.repositories.BaseRepository;
import vn.hbtplus.repositories.entity.InsuranceSalaryProcessEntity;
import vn.hbtplus.services.AuthorizationService;
import vn.hbtplus.utils.QueryUtils;
import vn.hbtplus.utils.Utils;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Lop repository Impl ung voi bang hr_insurance_salary_process
 *
 * @author tudd
 * @version 1.0
 * @since 1.0
 */

@Repository
@RequiredArgsConstructor
public class InsuranceSalaryProcessRepository extends BaseRepository {
    private final PermissionFeignClient permissionFeignClient;
    private final HttpServletRequest request;
    private final AuthorizationService authorizationService;

    public BaseDataTableDto<InsuranceSalaryProcessResponse.SearchResult> searchData(EmployeesRequest.SearchForm dto) {
        Pair<String, Map<String, Object>> pair = buildSql(null, dto);
        return getListPagination(pair.getLeft(), pair.getRight(), dto, InsuranceSalaryProcessResponse.SearchResult.class);
    }

    private Pair<String, Map<String, Object>> buildSql(String sqlSelect, EmployeesRequest.SearchForm dto) {
        StringBuilder sql = new StringBuilder(sqlSelect == null ? """
                SELECT
                    a.insurance_salary_process_id,
                    a.employee_id,
                    e.employee_code,
                    e.full_name,
                    a.salary_rank_id,
                    a.salary_grade_id,
                    ifnull(a.percent,100) as percent,
                    a.seniority_percent,
                    a.reserve_factor,
                    a.document_no,
                    a.document_signed_date,
                    a.start_date,
                    a.end_date,
                    a.created_by,
                    a.created_time,
                    a.modified_by,
                    a.modified_time,
                    a.last_update_time,
                    a.increment_date,
                    ifnull(a.amount, sg.amount) amount,
                    a.payroll_date,
                    NVL(o.full_name, o.name) orgName,
                    mj.name jobName,
                    (select et.name from hr_emp_types et where et.emp_type_id = a.emp_type_id) empTypeName,
                    (select sc.name from sys_categories sc where sc.value = e.status and sc.category_type = :empStatus) empStatusName,
                    (select sr.name from hr_salary_ranks sr where sr.salary_rank_id = a.salary_rank_id) salaryRankName,
                    (select sg.name from hr_salary_grades sg where sg.salary_grade_id = a.salary_grade_id) salaryGradeName,
                    (select hj.name from hr_jobs hj where hj.job_id = a.job_salary_id) jobSalaryName
                    FROM hr_insurance_salary_process a
                    JOIN hr_employees e ON (e.employee_id = a.employee_id AND NVL(e.is_deleted, :activeStatus) = :activeStatus)
                    LEFT JOIN hr_jobs mj ON mj.job_id = e.job_id
                    JOIN hr_organizations o ON o.organization_id = e.organization_id
                    LEFT JOIN hr_salary_grades sg ON sg.salary_grade_id = a.salary_grade_id
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
        QueryUtils.filter(dto.getListJobSalary(), sql, params, "a.job_salary_id");
        QueryUtils.filter(dto.getListSalaryRank(), sql, params, "a.salary_rank_id");
        QueryUtils.filter(dto.getDocumentNo(), sql, params, "a.document_no");
        if (!Utils.isNullOrEmpty(dto.getListDocumentSignedDate())) {
            QueryUtils.filterGe(Utils.stringToDate(dto.getListDocumentSignedDate().get(0)), sql, params, "a.document_signed_date", "fromDocumentSignedDate");
            QueryUtils.filterLe(dto.getListDocumentSignedDate().size() > 1 ? Utils.stringToDate(dto.getListDocumentSignedDate().get(1)) : null, sql, params, "a.document_signed_date", "toDocumentSignedDate");
        }

        if (!Utils.isNullOrEmpty(dto.getListStartDate())) {
            QueryUtils.filterGe(Utils.stringToDate(dto.getListStartDate().get(0)), sql, params, "a.start_date", "startDate");
            QueryUtils.filterLe(dto.getListStartDate().size() > 1 ? Utils.stringToDate(dto.getListStartDate().get(1)) : null, sql, params, "a.start_date", "endDate");
        }

        List<PermissionDataDto> permissionDataDtos = authorizationService.getPermissionData(
                Scope.VIEW, Constant.RESOURCES.INSURANCE_SALARY_PROCESS, Utils.getUserNameLogin()
        );
        QueryUtils.addConditionPermission(permissionDataDtos, sql, params);
        sql.append(" ORDER BY e.employee_id, a.start_date DESC");
    }

    public BaseDataTableDto<InsuranceSalaryProcessResponse.SearchResult> getTableList(Long employeeId, BaseSearchRequest request) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    a.insurance_salary_process_id,
                    a.start_date,
                    a.end_date,
                    a.employee_id,
                    a.salary_rank_id,
                    a.salary_grade_id,
                    ifnull(a.percent,100) as percent,
                    a.seniority_percent,
                    a.reserve_factor,
                    a.document_no,
                    a.document_signed_date,
                    a.created_by,
                    a.created_time,
                    a.modified_by,
                    a.modified_time,
                    a.last_update_time,
                    ifnull(a.amount, sg.amount) amount,
                    a.payroll_date,
                    NVL(o.full_name, o.name) AS orgName,
                    mj.name AS jobName,
                    (select et.name from hr_emp_types et where et.emp_type_id = a.emp_type_id) empTypeName,
                    (SELECT sc.name FROM sys_categories sc WHERE sc.value = e.status AND sc.category_type = :empStatus) AS empStatusName,
                    (SELECT sr.name FROM hr_salary_ranks sr WHERE sr.salary_rank_id = a.salary_rank_id) AS salaryRankName,
                    (SELECT sg.name FROM hr_salary_grades sg WHERE sg.salary_grade_id = a.salary_grade_id) AS salaryGradeName
                FROM
                    hr_insurance_salary_process a
                    JOIN hr_employees e ON e.employee_id = a.employee_id
                    LEFT JOIN hr_jobs mj ON mj.job_id = e.job_id
                    JOIN hr_organizations o ON o.organization_id = e.organization_id
                    LEFT JOIN hr_salary_grades sg ON sg.salary_grade_id = a.salary_grade_id
                WHERE
                    a.is_deleted = :activeStatus
                    AND a.employee_id = :employeeId
                ORDER BY
                    a.start_date DESC
                """);
        HashMap<String, Object> params = new HashMap<>();
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        params.put("employeeId", employeeId);
        params.put("empStatus", Constant.CATEGORY_CODES.HR_TRANG_THAI_NHAN_VIEN);
        return getListPagination(sql.toString(), params, request, InsuranceSalaryProcessResponse.SearchResult.class);

    }

    public boolean checkConflictProcess(InsuranceSalaryProcessRequest.SubmitForm dto, Long employeeId, Long insuranceSalaryProcessId) {
        String sql = """
                     SELECT count(1)
                     FROM hr_insurance_salary_process sp
                     WHERE sp.employee_id = :employeeId
                     AND NVL(sp.is_deleted, :activeStatus) = :activeStatus
                     AND sp.insurance_salary_process_id != :insuranceSalaryProcessId
                     and sp.start_date = :startDate
                """;

        HashMap<String, Object> params = new HashMap<>();
        params.put("employeeId", employeeId);
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        params.put("insuranceSalaryProcessId", Utils.NVL(insuranceSalaryProcessId));
        params.put("startDate", dto.getStartDate());

        return queryForObject(sql, params, Integer.class) > 0;
    }
    public void updateSalaryProcess(List<Long> employeeIds) {
        String sql = """
                    update hr_insurance_salary_process sp
                    set sp.end_date = (
                       select min(DATE_ADD(start_date,INTERVAL -1 day))
                       from hr_insurance_salary_process sp1
                       where sp1.employee_id = sp.employee_id
                       and sp1.start_date > sp.start_date
                       and nvl(sp1.is_deleted, :activeStatus) = :activeStatus
                    )
                    where nvl(sp.is_deleted, :activeStatus) = :activeStatus
                    and sp.employee_id in (:employeeIds)
                """;
        HashMap<String, Object> params = new HashMap<>();
        params.put("employeeIds", employeeIds);
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        executeSqlDatabase(sql, params);
    }

    public void updateSalaryProcess(Long employeeId) {
        updateSalaryProcess(Utils.castToList(employeeId));
    }

    public InsuranceSalaryProcessResponse.DetailBean getCurrentProcess(Long employeeId) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    a.insurance_salary_process_id,
                    a.start_date,
                    a.end_date,
                    a.employee_id,
                    a.salary_rank_id,
                    a.salary_grade_id,
                    a.seniority_percent,
                    ifnull(a.percent,100) as percent,
                    a.reserve_factor,
                    sr.name AS salaryRankName,
                    sr.code AS salaryRankCode,
                    sg.name AS salaryGradeName,
                    ifnull(sg.amount,a.amount) as salaryAmount,
                    (select name from hr_jobs where job_id= a.job_salary_id) as jobSalaryName,
                    a.increment_date
                FROM
                    hr_insurance_salary_process a
                    left join hr_salary_ranks sr on sr.salary_rank_id = a.salary_rank_id
                    left join hr_salary_grades sg on sg.salary_grade_id = a.salary_grade_id
                WHERE
                    a.is_deleted = :activeStatus
                    AND a.employee_id = :employeeId
                    and DATE(now()) between a.start_date and ifnull(a.end_date, now())
                ORDER BY
                    a.start_date DESC
                """);
        HashMap<String, Object> params = new HashMap<>();
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        params.put("employeeId", employeeId);
        params.put("empStatus", Constant.CATEGORY_CODES.HR_TRANG_THAI_NHAN_VIEN);
        return getFirstData(sql.toString(), params, InsuranceSalaryProcessResponse.DetailBean.class);
    }

    public List<InsuranceSalaryProcessEntity> getListProcessByEmpCode(List<String> empCodeList) {
        String sql = "select a.* from hr_insurance_salary_process a" +
                     " where a.employee_id in (" +
                     "  select e.employee_id from hr_employees e where e.employee_code in (:empCodes)" +
                     " )" +
                     " and a.is_deleted = 'N'";
        HashMap<String, Object> params = new HashMap<>();
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        params.put("empCodes", empCodeList);
        return getListData(sql, params, InsuranceSalaryProcessEntity.class);
    }
}
