/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.kpi.repositories.impl;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.time.DateUtils;
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
import vn.kpi.models.request.EmployeesRequest;
import vn.kpi.models.request.PositionSalaryProcessRequest;
import vn.kpi.models.response.PositionSalaryProcessResponse;
import vn.kpi.repositories.BaseRepository;
import vn.kpi.services.AuthorizationService;
import vn.kpi.utils.QueryUtils;
import vn.kpi.utils.Utils;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Lop repository Impl ung voi bang hr_position_salary_process
 *
 * @author tudd
 * @version 1.0
 * @since 1.0
 */

@Repository
@RequiredArgsConstructor
public class PositionSalaryProcessRepository extends BaseRepository {
    private final PermissionFeignClient permissionFeignClient;
    private final HttpServletRequest request;
    private final AuthorizationService authorizationService;

    public BaseDataTableDto searchData(EmployeesRequest.SearchForm dto) {
        Pair<String, Map<String, Object>> pair = buildSql(dto);
        return getListPagination(pair.getLeft(), pair.getRight(), dto, PositionSalaryProcessResponse.SearchResult.class);
    }

    private Pair<String, Map<String, Object>> buildSql(EmployeesRequest.SearchForm dto) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    a.position_salary_process_id,
                    a.employee_id,
                    e.employee_code,
                    e.full_name,
                    a.salary_grade_id,
                    a.percent,
                    a.start_date,
                    a.end_date,
                    a.document_no,
                    a.document_signed_date,
                    a.is_deleted,
                    a.created_by,
                    a.created_time,
                    a.modified_by,
                    a.modified_time,
                    a.last_update_time,
                    o.full_name orgName,
                    mj.name jobName,
                    (select sc.name from sys_categories sc where sc.value = e.status and sc.category_type = :empStatus) empStatusName,
                    (select sc.name from sys_categories sc where sc.value = a.salary_type and sc.category_type = :salaryTypeCode) salaryTypeName,
                    (select et.name from hr_emp_types et where et.emp_type_id = e.emp_type_id ) empTypeName,
                    (select hj.name from hr_jobs hj where hj.job_id = a.job_id ) salaryJobName,
                    sr.name salaryRankName,
                    sr.code salaryRankCode,
                    sg.amount salaryAmount,
                    concat(sg.name, ' (', sg.amount, ')') salaryGradeName
                """);
        HashMap<String, Object> params = new HashMap<>();
        addCondition(sql, params, dto);

        return new MutablePair<>(sql.toString(), params);
    }

    public List<Map<String, Object>> getListExport(EmployeesRequest.SearchForm dto) {
        Pair<String, Map<String, Object>> pair = buildSql(dto);
        List<Map<String, Object>> dataList = getListData(pair.getLeft(), pair.getRight());
        if (Utils.isNullOrEmpty(dataList)) {
            dataList.add(getMapEmptyAliasColumns(pair.getLeft()));
        }
        return dataList;
    }

    private void addCondition(StringBuilder sql, Map<String, Object> params, EmployeesRequest.SearchForm dto) {
        sql.append("""
                    FROM hr_position_salary_process a
                    JOIN hr_employees e ON (e.employee_id = a.employee_id AND NVL(e.is_deleted, :activeStatus) = :activeStatus)
                    LEFT JOIN hr_jobs mj ON mj.job_id = e.job_id
                    JOIN hr_organizations o ON o.organization_id = e.organization_id
                    LEFT JOIN hr_salary_ranks sr ON sr.salary_rank_id = a.salary_rank_id
                    LEFT JOIN hr_salary_grades sg ON sg.salary_grade_id = a.salary_grade_id
                    WHERE a.is_deleted = :activeStatus
                """);
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        params.put("empStatus", Constant.CATEGORY_CODES.HR_TRANG_THAI_NHAN_VIEN);
        params.put("salaryTypeCode", Constant.CATEGORY_CODES.PHAN_LOAI_LUONG_TRUONG);
        UtilsEmployeeSearchRepository.setParamEmployeeSearch(dto, sql, params);
        QueryUtils.filter(dto.getDocumentNo(), sql, params, "a.document_no");
        QueryUtils.filter(dto.getListSalaryRank(), sql, params, "a.salary_rank_id");
        if (!Utils.isNullOrEmpty(dto.getListDocumentSignedDate())) {
            QueryUtils.filterGe(Utils.stringToDate(dto.getListDocumentSignedDate().get(0)), sql, params, "a.document_signed_date", "fromDocumentSignedDate");
            QueryUtils.filterLe(dto.getListDocumentSignedDate().size() > 1 ? Utils.stringToDate(dto.getListDocumentSignedDate().get(1)) : null, sql, params, "a.document_signed_date", "toDocumentSignedDate");
        }
        if (!Utils.isNullOrEmpty(dto.getListStartDate())) {
            QueryUtils.filterGe(Utils.stringToDate(dto.getListStartDate().get(0)), sql, params, "a.start_date", "startDate");
            QueryUtils.filterLe(dto.getListStartDate().size() > 1 ? Utils.stringToDate(dto.getListStartDate().get(1)) : null, sql, params, "a.start_date", "endDate");
        }
        List<PermissionDataDto> permissionDataDtos = authorizationService.getPermissionData(
                Scope.VIEW, Constant.RESOURCES.POSITION_SALARY_PROCESS, Utils.getUserNameLogin()
        );
        QueryUtils.addConditionPermission(permissionDataDtos, sql, params);
        sql.append(" ORDER BY o.path_order, mj.order_number, e.employee_id, a.start_date DESC");
    }

    public BaseDataTableDto<PositionSalaryProcessResponse.SearchResult> getTableList(Long employeeId, BaseSearchRequest request) {
        StringBuilder sql = new StringBuilder("""
                    SELECT
                        a.position_salary_process_id,
                        a.employee_id,
                        a.salary_grade_id,
                        a.percent,
                        a.start_date,
                        a.end_date,
                        a.document_no,
                        a.document_signed_date,
                        a.is_deleted,
                        a.created_by,
                        a.created_time,
                        a.modified_by,
                        a.modified_time,
                        a.last_update_time,
                        concat(sg.name, ' (', sg.amount, ')') salaryGradeName,
                        sg.amount salaryAmount,
                        (select sr.code from hr_salary_ranks sr where sr.salary_rank_id = a.salary_rank_id) salaryRankCode,
                        (select sc.name from sys_categories sc where sc.value = a.salary_type and sc.category_type = :salaryTypeCode) salaryTypeName,
                        (select sr.name from hr_salary_ranks sr where sr.salary_rank_id = a.salary_rank_id) salaryRankName,
                        (select hj.name from hr_jobs hj where hj.job_id = a.job_id ) jobName
                        FROM hr_position_salary_process a
                        left join hr_salary_grades sg on a.salary_grade_id = sg.salary_grade_id
                WHERE a.is_deleted = :activeStatus
                and a.employee_id = :employeeId
                order by a.start_date desc
                    """);
        HashMap<String, Object> params = new HashMap<>();
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        params.put("salaryTypeCode", Constant.CATEGORY_CODES.PHAN_LOAI_LUONG_TRUONG);
        params.put("employeeId", employeeId);
        return getListPagination(sql.toString(), params, request, PositionSalaryProcessResponse.SearchResult.class);
    }

    public boolean checkConflictProcess(PositionSalaryProcessRequest.SubmitForm dto, Long employeeId, Long positionSalaryProcessId) {
        StringBuilder sql = new StringBuilder("""
                     SELECT count(1)
                     FROM hr_position_salary_process sp
                     WHERE sp.employee_id = :employeeId
                     AND NVL(sp.is_deleted, :activeStatus) = :activeStatus
                     AND sp.position_salary_process_id != :positionSalaryProcessId
                     and sp.start_date >= :startDate
                     and sp.salary_type = :salaryType
                """);

        HashMap<String, Object> params = new HashMap<>();
        if (dto.getEndDate() != null) {
            sql.append(" and sp.start_date <= :endDate");
            params.put("endDate", dto.getEndDate());
        }
        params.put("employeeId", employeeId);
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        params.put("positionSalaryProcessId", Utils.NVL(positionSalaryProcessId));
        params.put("startDate", dto.getStartDate());
        params.put("salaryType", dto.getSalaryType());

        return queryForObject(sql.toString(), params, Integer.class) > 0;
    }

    public void updatePositionSalaryProcess(Long employeeId, Date startDate) {
        String sql = """
                    update hr_position_salary_process sp
                    set sp.end_date = :endDate
                    where nvl(sp.is_deleted, :activeStatus) = :activeStatus
                    and sp.employee_id = :employeeId
                    and sp.start_date < :startDate
                    and (sp.end_date is null or sp.end_date >= :startDate)
                """;
        HashMap<String, Object> params = new HashMap<>();
        params.put("employeeId", employeeId);
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        params.put("startDate", startDate);
        params.put("endDate", DateUtils.addDays(startDate, -1));
        executeSqlDatabase(sql, params);
    }

    public PositionSalaryProcessResponse.DetailBean getCurrentProcess(Long employeeId) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    a.position_salary_process_id,
                    a.start_date,
                    a.end_date,
                    a.employee_id,
                    a.salary_rank_id,
                    a.salary_grade_id,
                    ifnull(a.percent,100) as percent,
                    sr.name AS salaryRankName,
                    sr.code AS salaryRankCode,
                    sg.name AS salaryGradeName,
                    sg.amount as salaryAmount
                FROM
                    hr_position_salary_process a,hr_salary_ranks sr,hr_salary_grades sg                    
                WHERE
                    a.is_deleted = :activeStatus
                    and sr.salary_rank_id = a.salary_rank_id
                    and sg.salary_grade_id = a.salary_grade_id
                    AND a.employee_id = :employeeId
                    and DATE(now()) between a.start_date and ifnull(a.end_date, now())
                ORDER BY
                    a.start_date DESC
                """);
        HashMap<String, Object> params = new HashMap<>();
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        params.put("employeeId", employeeId);
        return getFirstData(sql.toString(), params, PositionSalaryProcessResponse.DetailBean.class);
    }

    public boolean validateConflict(Long employeeId, Long positionSalaryProcessId, Date startDate, Date endDate) {
        HashMap<String, Object> params = new HashMap<>();
        params.put("employeeId", employeeId);
        params.put("startDate", startDate);
        StringBuilder sql = new StringBuilder("""
                select count(*) from hr_position_salary_process a
                where a.is_deleted = 'N'
                and a.employee_id = :employeeId                
                and a.start_date >= :startDate
                """);
        if (endDate != null) {
            sql.append(" and a.start_date <= :endDate");
            params.put("endDate", endDate);
        }
        if (positionSalaryProcessId != null) {
            sql.append("""
                        and a.start_date not in (
                            select start_date from hr_position_salary_process a1
                            where a1.employee_id = :employeeId
                            and a1.position_salary_process_id = :positionSalaryProcessId
                        )
                    """);
            params.put("positionSalaryProcessId", positionSalaryProcessId);
        }
        return queryForObject(sql.toString(), params, Integer.class) > 0;
    }

    public List<PositionSalaryProcessResponse.DetailBean> getListForEdit(Long employeeId, Long positionSalaryProcessId) {
        String sql = """
                select a.* from hr_position_salary_process a
                where a.is_deleted = 'N'
                and a.employee_id = :employeeId
                and a.start_date = (
                    select start_date from hr_position_salary_process a1
                    where a1.position_salary_process_id = :positionSalaryProcessId
                )
                order by a.order_number, a.position_salary_process_id
                """;
        HashMap<String, Object> params = new HashMap<>();
        params.put("employeeId", employeeId);
        params.put("positionSalaryProcessId", positionSalaryProcessId);
        return getListData(sql, params, PositionSalaryProcessResponse.DetailBean.class);
    }

    public void autoUpdatePreProcessForDelete(Long employeeId, Date oldStartDate) {
        String sql = """
                update hr_position_salary_process a
                set a.end_date = (
                    select DATE_ADD(min(a1.start_date) interval -1 day) from hr_position_salary_process a1
                    where a1.employee_id = :employeeId
                    and a1.is_deleted = 'N'
                    and a1.start_date > :oldStartDate
                ), a.modified_time = now(), a.modified_by = :userName
                where a.employee_id = :employee_id
                and a.end_date = :endDate 
                """;
        HashMap<String, Object> params = new HashMap<>();
        params.put("employeeId", employeeId);
        params.put("oldStartDate", oldStartDate);
        params.put("endDate", DateUtils.addDays(oldStartDate,-1));
        params.put("userName", Utils.getUserNameLogin());
        executeSqlDatabase(sql, params);

    }
}
