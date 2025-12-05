/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.repositories.impl;

import org.apache.commons.lang3.time.DateUtils;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Repository;
import lombok.RequiredArgsConstructor;
import vn.hbtplus.constants.Constant;
import vn.hbtplus.models.BaseDataTableDto;
import vn.hbtplus.models.BaseSearchRequest;
import vn.hbtplus.models.request.EmployeesRequest;
import vn.hbtplus.repositories.BaseRepository;
import vn.hbtplus.models.request.PoliticalParticipationsRequest;
import vn.hbtplus.models.response.PoliticalParticipationsResponse;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.utils.Utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Lop repository Impl ung voi bang hr_political_participations
 *
 * @author tudd
 * @version 1.0
 * @since 1.0
 */

@Repository
@RequiredArgsConstructor
public class PoliticalParticipationsRepository extends BaseRepository {

    public BaseDataTableDto searchData(EmployeesRequest.SearchForm dto) {
        Pair<String, Map<String, Object>> pair = buildSql(dto);
        return getListPagination(pair.getLeft(), pair.getRight(), dto, PoliticalParticipationsResponse.SearchResult.class);
    }

    private Pair<String, Map<String, Object>> buildSql(EmployeesRequest.SearchForm dto) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    a.participation_id,
                    a.employee_id,
                    e.employee_code,
                    e.full_name,
                    a.start_date,
                    a.end_date,
                    o.full_name orgName,
                    a.organization_name,
                    mj.name jobName,
                    (select sc.name from sys_categories sc where sc.value = a.organization_type and sc.category_type = :organizationType) organizationTypeName,
                    (select sc.name from sys_categories sc where sc.value = a.position_title and sc.category_type = :positionTitle) positionTitleName,
                    (select et.name from hr_emp_types et where et.emp_type_id = e.emp_type_id ) empTypeName,
                    a.created_by,
                    a.created_time,
                    a.modified_by,
                    a.modified_time,
                    a.last_update_time
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
                    FROM hr_political_participations a
                    JOIN hr_employees e ON e.employee_id = a.employee_id
                    LEFT JOIN hr_jobs mj ON mj.job_id = e.job_id
                    JOIN hr_organizations o ON o.organization_id = e.organization_id
                    WHERE IFNULL(a.is_deleted, :activeStatus) = :activeStatus
                """);
        UtilsEmployeeSearchRepository.setParamEmployeeSearch(dto, sql, params);
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        params.put("organizationType", Constant.CATEGORY_CODES.LOAI_TO_CHUC_CTR_XH);
        params.put("positionTitle", Constant.CATEGORY_CODES.CHUC_DANH_CTR_XH);
        sql.append(" ORDER BY o.path_order, mj.order_number, e.employee_id, a.start_date DESC");
    }

    public BaseDataTableDto<PoliticalParticipationsResponse.SearchResult> getTableList(Long employeeId, BaseSearchRequest request) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    a.participation_id,
                    a.employee_id,
                    a.start_date,
                    a.end_date,
                    a.organization_name,
                    (select sc.name from sys_categories sc where sc.value = a.organization_type and sc.category_type = :organizationType) organizationTypeName,
                    (select sc.name from sys_categories sc where sc.value = a.position_title and sc.category_type = :positionTitle) positionTitleName,
                    a.created_by,
                    a.created_time,
                    a.modified_by,
                    a.modified_time,
                    a.last_update_time
                FROM hr_political_participations a
                WHERE IFNULL(a.is_deleted, :activeStatus) = :activeStatus
                and a.employee_id = :employeeId
                ORDER BY a.start_date DESC
                """);
        HashMap<String, Object> params = new HashMap<>();
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        params.put("organizationType", Constant.CATEGORY_CODES.LOAI_TO_CHUC_CTR_XH);
        params.put("positionTitle", Constant.CATEGORY_CODES.CHUC_DANH_CTR_XH);
        params.put("employeeId", employeeId);
        return getListPagination(sql.toString(), params, request, PoliticalParticipationsResponse.SearchResult.class);
    }

    public boolean checkExitParticipation(PoliticalParticipationsRequest.SubmitForm dto, Long employeeId, Long participationId) {
        HashMap<String, Object> params = new HashMap<>();
        params.put("employeeId", employeeId);
        params.put("startDate", dto.getStartDate());
        params.put("organizationType", dto.getOrganizationType());
        params.put("organizationName", dto.getOrganizationName());
        params.put("positionTitle", dto.getPositionTitle());
        StringBuilder sql = new StringBuilder("""
                select count(*) from hr_political_participations a
                where a.is_deleted = 'N'
                and a.employee_id = :employeeId
                and (a.start_date >= :startDate OR (:startDate >= a.start_date and (a.end_date >= :startDate OR a.end_date is NULL)))
                and a.organization_type = :organizationType
                and a.position_title = :positionTitle
                and a.organization_name = :organizationName
                """);
        if (dto.getEndDate() != null) {
            sql.append(" and a.start_date <= :endDate");
            params.put("endDate", dto.getEndDate());
        }
        if (participationId != null) {
            sql.append("""
                        and a.start_date not in (
                            select start_date from hr_political_participations a1
                            where a1.participation_id = :participationId
                        )
                    """);
            params.put("participationId", participationId);
        }
        return queryForObject(sql.toString(), params, Integer.class) > 0;
    }

    public void updateParticipation(Long employeeId, PoliticalParticipationsRequest.SubmitForm dto) {
        String sql = """
                    update hr_political_participations sp
                    set sp.end_date = :endDate
                    where nvl(sp.is_deleted, :activeStatus) = :activeStatus
                    and sp.employee_id = :employeeId
                    and sp.start_date < :startDate
                    and (sp.end_date is null or sp.end_date >= :startDate)
                    and sp.organization_type = :organizationType
                    and sp.position_title = :positionTitle
                    and sp.organization_name = :organizationName
                """;
        HashMap<String, Object> params = new HashMap<>();
        params.put("employeeId", employeeId);
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        params.put("startDate", dto.getStartDate());
        params.put("endDate", DateUtils.addDays(dto.getStartDate(), -1));
        params.put("organizationType", dto.getOrganizationType());
        params.put("organizationName", dto.getOrganizationName());
        params.put("positionTitle", dto.getPositionTitle());
        executeSqlDatabase(sql, params);
    }
}
