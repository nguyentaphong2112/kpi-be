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
import vn.kpi.models.request.AwardProcessRequest;
import vn.kpi.models.request.EmployeesRequest;
import vn.kpi.models.response.AwardProcessResponse;
import vn.kpi.repositories.BaseRepository;
import vn.kpi.repositories.entity.AwardProcessEntity;
import vn.kpi.services.AuthorizationService;
import vn.kpi.utils.QueryUtils;
import vn.kpi.utils.Utils;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Lop repository Impl ung voi bang hr_award_process
 *
 * @author tudd
 * @version 1.0
 * @since 1.0
 */

@Repository
@RequiredArgsConstructor
public class AwardProcessRepository extends BaseRepository {
    private final PermissionFeignClient permissionFeignClient;
    private final HttpServletRequest request;
    private final AuthorizationService authorizationService;

    public BaseDataTableDto<AwardProcessResponse.SearchResult> searchData(String sqlSelect, EmployeesRequest.SearchForm dto) {
        Pair<String, Map<String, Object>> pair = buildSql(sqlSelect, dto);
        return getListPagination(pair.getLeft(), pair.getRight(), dto, AwardProcessResponse.SearchResult.class);
    }

    private Pair<String, Map<String, Object>> buildSql(String sqlSelect, EmployeesRequest.SearchForm dto) {
        StringBuilder sql = new StringBuilder(sqlSelect == null? """
                SELECT
                    a.award_process_id,
                    a.employee_id,
                    e.employee_code,
                    e.full_name,
                    a.award_form_id,
                    a.award_year,
                    a.document_no,
                    a.document_signed_date,
                    a.created_by,
                    a.created_time,
                    a.modified_by,
                    a.modified_time,
                    a.last_update_time,
                    NVL(o.full_name, o.name) orgName,
                    mj.name jobName,
                    (select et.name from hr_emp_types et where et.emp_type_id = e.emp_type_id) empTypeName,
                    (select sc.name from sys_categories sc where sc.value = e.status and sc.category_type = :empStatus) empStatusName,
                    (select sc.name from sys_categories sc where sc.value = a.award_form_id and sc.category_type = :awardFormCode) awardFormName
                    FROM hr_award_process a
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
        params.put("awardFormCode", Constant.CATEGORY_CODES.HINH_THUC_KHEN_THUONG);
        UtilsEmployeeSearchRepository.setParamEmployeeSearch(dto, sql, params);
        QueryUtils.filter(dto.getDocumentNo(), sql, params, "a.document_no");
        QueryUtils.filter(dto.getListAwardForm(), sql, params, "a.award_form_id");
        if (dto.getListYear() != null && !dto.getListYear().isEmpty()) {
            QueryUtils.filterGe(dto.getListYear().get(0), sql, params, "a.award_year", "fromYear");
            QueryUtils.filterLe(dto.getListYear().size() > 1 ? dto.getListYear().get(1) : null, sql, params, "a.award_year", "toYear");
        }
        if (!Utils.isNullOrEmpty(dto.getListDocumentSignedDate())) {
            QueryUtils.filterGe(Utils.stringToDate(dto.getListDocumentSignedDate().get(0)), sql, params, "a.document_signed_date", "fromDocumentSignedDate");
            QueryUtils.filterLe(dto.getListDocumentSignedDate().size() > 1 ? Utils.stringToDate(dto.getListDocumentSignedDate().get(1)) : null, sql, params, "a.document_signed_date", "toDocumentSignedDate");
        }
        List<PermissionDataDto> permissionDataDtos = authorizationService.getPermissionData(
                Scope.VIEW, Constant.RESOURCES.AWARD_PROCESS, Utils.getUserNameLogin()
        );
        QueryUtils.addConditionPermission(permissionDataDtos, sql, params);
        sql.append(" ORDER BY e.employee_id, a.award_year DESC");
    }

    public BaseDataTableDto<AwardProcessResponse.SearchResult> getTableList(Long employeeId, BaseSearchRequest request) {
        StringBuilder sql = new StringBuilder("""
                    SELECT
                        a.award_process_id,
                        a.employee_id,
                        a.award_form_id,
                        a.award_year,
                        a.document_no,
                        a.document_signed_date,
                        a.created_by,
                        a.created_time,
                        a.modified_by,
                        a.modified_time,
                        a.last_update_time,
                        (select sc.name from sys_categories sc where sc.value = a.award_form_id and sc.category_type = :awardFormCode) awardFormName
                        FROM hr_award_process a
                WHERE a.is_deleted = :activeStatus
                    and a.employee_id = :employeeId
                order by a.award_year desc
                    """);
        HashMap<String, Object> params = new HashMap<>();
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        params.put("awardFormCode", Constant.CATEGORY_CODES.HINH_THUC_KHEN_THUONG);
        params.put("employeeId", employeeId);
        return getListPagination(sql.toString(), params, request, AwardProcessResponse.SearchResult.class);
    }

    public boolean checkDuplicate(AwardProcessRequest.SubmitForm dto, Long employeeId, Long awardProcessId) {
        String sql = """
                    SELECT count(1)
                       FROM hr_award_process a
                       WHERE a.employee_id = :employeeId
                       AND a.award_year = :awardYear
                       AND a.award_form_id = :awardFormId
                       AND a.award_process_id <> :awardProcessId
                       AND NVL(a.is_deleted, :activeStatus) = :activeStatus
                """;
        HashMap<String, Object> params = new HashMap<>();
        params.put("employeeId", employeeId);
        params.put("awardYear", dto.getAwardYear());
        params.put("awardFormId", dto.getAwardFormId());
        params.put("awardProcessId", Utils.NVL(awardProcessId));
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        return queryForObject(sql, params, Integer.class) > 0;
    }

    public List<AwardProcessEntity> getListProcessByEmpCode(List<String> empCodeList) {
        String sql = "select a.* from hr_award_process a" +
                     " where a.employee_id in (" +
                     "  select e.employee_id from hr_employees e where e.employee_code in (:empCodes)" +
                     " )" +
                     " and a.is_deleted = 'N'";
        HashMap<String, Object> params = new HashMap<>();
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        params.put("empCodes", empCodeList);
        return getListData(sql, params, AwardProcessEntity.class);
    }
}
