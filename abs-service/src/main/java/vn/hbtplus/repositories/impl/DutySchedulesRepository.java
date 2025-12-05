/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.repositories.impl;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.stereotype.Repository;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.constants.Constant;
import vn.hbtplus.models.BaseDataTableDto;
import vn.hbtplus.models.dto.CategoryDto;
import vn.hbtplus.models.request.DutySchedulesRequest;
import vn.hbtplus.models.response.DutySchedulesResponse;
import vn.hbtplus.repositories.BaseRepository;
import vn.hbtplus.repositories.ParameterRepository;
import vn.hbtplus.repositories.entity.DutySchedulesEntity;
import vn.hbtplus.utils.Utils;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Lop repository Impl ung voi bang abs_duty_schedules
 *
 * @author tudd
 * @version 1.0
 * @since 1.0
 */

@Repository
@RequiredArgsConstructor
public class DutySchedulesRepository extends BaseRepository {
    private final ParameterRepository parameterRepository;

    public BaseDataTableDto<DutySchedulesResponse.SearchResult> searchData(DutySchedulesRequest.SearchForm dto) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    a.organization_id,
                    a.name as organization_name
                from hr_organizations a
                where a.is_deleted = 'N'
                and a.organization_id in (:orgConfigIds)
                """);
        HashMap<String, Object> params = new HashMap<>();
        if (dto.getOrganizationId() != null && dto.getOrganizationId() > 0) {
            sql.append(" AND a.path_id like :orgPath ");
            params.put("orgPath", "%/" + dto.getOrganizationId() + "/%");
        }
        sql.append(" and ifnull(a.start_date, :startDate) <= :endDate");
        sql.append(" and ifnull(a.end_date, :endDate) >= :startDate");
        params.put("startDate", dto.getDateValue());
        params.put("endDate", DateUtils.addDays(dto.getDateValue(), 6));
        params.put("orgConfigIds", Utils.stringToListLong(parameterRepository.getConfigValue("DON_VI_TRUC", DateUtils.addDays(dto.getDateValue(), 6), String.class), ","));
        sql.append(" order by a.path_order");
        BaseDataTableDto<DutySchedulesResponse.SearchResult> baseDataTableDto = getListPagination(sql.toString(), params, dto, DutySchedulesResponse.SearchResult.class);
        List<DutySchedulesResponse.SearchResult> listOrgs = baseDataTableDto.getListData();
        List<Long> orgIds = new ArrayList<>();
        listOrgs.forEach(item -> {
            orgIds.add(item.getOrganizationId());
        });
        if (!orgIds.isEmpty()) {
            List<DutySchedulesResponse> listEmps = getListDutySchedules(orgIds, dto.getDateValue(), DateUtils.addDays(dto.getDateValue(), 6));
            Map<Long, List<DutySchedulesResponse.DutyPositionBean>> map = new HashMap<>();
            listEmps.forEach(item -> {
                List<DutySchedulesResponse.DutyPositionBean> positionBeans = map.get(item.getOrganizationId());
                if (positionBeans == null) {
                    map.put(item.getOrganizationId(), new ArrayList<>());
                    map.get(item.getOrganizationId()).add(new DutySchedulesResponse.DutyPositionBean(item));
                } else {
                    boolean exists = false;
                    for (DutySchedulesResponse.DutyPositionBean positionBean : positionBeans) {
                        if (positionBean.getDutyPositionId().equals(item.getDutyPositionId())) {
                            positionBean.addEmployee(item);
                            exists = true;
                        }
                    }
                    if (!exists) {
                        map.get(item.getOrganizationId()).add(new DutySchedulesResponse.DutyPositionBean(item));
                    }
                }
            });
            orgIds.forEach(orgId -> {
                if (Utils.isNullOrEmpty(map.get(orgId))) {
                    //truonng hop chưa có dữ liệu thì add mac dinh cac chuc danh ở tuần cũ
                    List<DutySchedulesResponse.DutyPositionBean> positionBeanList = getPreListDutyPositions(orgId);
                    map.put(orgId, positionBeanList);
                }
            });
            listOrgs.forEach(item -> {
                item.setDutyPositions(map.get(item.getOrganizationId()));
            });
        }


        return baseDataTableDto;
    }


    public BaseDataTableDto<DutySchedulesResponse.SearchResultMonth> searchDataMonth(DutySchedulesRequest.SearchForm dto) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    a.organization_id,
                    a.name as organization_name
                from hr_organizations a
                where a.is_deleted = 'N'
                and a.organization_id in (:orgConfigIds)
                """);
        HashMap<String, Object> params = new HashMap<>();
        if (dto.getOrganizationId() != null && dto.getOrganizationId() > 0) {
            sql.append(" AND a.path_id like :orgPath ");
            params.put("orgPath", "%/" + dto.getOrganizationId() + "/%");
        }
        sql.append(" and ifnull(a.start_date, :startDate) <= :endDate");
        sql.append(" and ifnull(a.end_date, :endDate) >= :startDate");
        params.put("startDate", dto.getMonthValue());
        params.put("endDate", Utils.getLastDayOfMonth(dto.getMonthValue()));
        params.put("orgConfigIds", Utils.stringToListLong(parameterRepository.getConfigValue("DON_VI_TRUC", Utils.getLastDayOfMonth(dto.getMonthValue()), String.class), ","));
        sql.append(" order by a.path_order");
        BaseDataTableDto<DutySchedulesResponse.SearchResultMonth> baseDataTableDto = getListPagination(sql.toString(), params, dto, DutySchedulesResponse.SearchResultMonth.class);
        List<DutySchedulesResponse.SearchResultMonth> listOrgs = baseDataTableDto.getListData();
        List<Long> orgIds = new ArrayList<>();
        listOrgs.forEach(item -> {
            orgIds.add(item.getOrganizationId());
        });
        if (!orgIds.isEmpty()) {
            List<DutySchedulesResponse> listEmps = getListDutySchedules(orgIds, dto.getMonthValue(), Utils.getLastDayOfMonth(dto.getMonthValue()));
            Map<Long, List<DutySchedulesResponse.DutyPositionBeanMonth>> map = new HashMap<>();
            listEmps.forEach(item -> {
                List<DutySchedulesResponse.DutyPositionBeanMonth> positionBeans = map.get(item.getOrganizationId());
                if (positionBeans == null) {
                    map.put(item.getOrganizationId(), new ArrayList<>());
                    map.get(item.getOrganizationId()).add(new DutySchedulesResponse.DutyPositionBeanMonth(item));
                } else {
                    boolean exists = false;
                    for (DutySchedulesResponse.DutyPositionBeanMonth positionBean : positionBeans) {
                        if (positionBean.getDutyPositionId().equals(item.getDutyPositionId())) {
                            positionBean.addEmployee(item);
                            exists = true;
                        }
                    }
                    if (!exists) {
                        map.get(item.getOrganizationId()).add(new DutySchedulesResponse.DutyPositionBeanMonth(item));
                    }
                }
            });
            listOrgs.forEach(item ->
                    item.setDutyPositions(map.get(item.getOrganizationId())));
        }


        return baseDataTableDto;
    }

    private List<DutySchedulesResponse.DutyPositionBean> getPreListDutyPositions(Long orgId) {
        String sql = "select distinct a.duty_position_id," +
                "  sc.name as duty_position_name" +
                "  from abs_duty_schedules a, (" +
                "  select max(date_timekeeping) max_date_timekeeping from abs_duty_schedules a1" +
                "  where a1.is_deleted = 'N'" +
                ") T, sys_categories sc " +
                " where a.organization_id in (:orgId)" +
                " and a.date_timekeeping between DATE_ADD(T.max_date_timekeeping, interval -14 day) and T.max_date_timekeeping" +
                " and sc.value = a.duty_position_id" +
                " and sc.category_type = :codeViTriTruc" +
                " order by sc.order_number";
        HashMap<String, Object> params = new HashMap<>();
        params.put("orgId", orgId);
        params.put("codeViTriTruc", Constant.CATEGORY_CODES.VI_TRI_TRUC);
        return getListData(sql, params, DutySchedulesResponse.DutyPositionBean.class);
    }

    private List<DutySchedulesResponse> getListDutySchedules(List<Long> orgIds, Date startDate, Date endDate) {
        String sql = """
                select a.duty_position_id,
                    a.employee_id,
                    e.employee_code,
                    f_get_last_name(e.employee_code,e.full_name) full_label,
                    e.full_name as employee_name,
                    a.date_timekeeping,
                    sc.name as duty_position_name,
                    a.organization_id
                    FROM abs_duty_schedules a
                    JOIN hr_employees e ON e.employee_id = a.employee_id
                    join sys_categories sc on a.duty_position_id = sc.value and sc.category_type = :typeViTriTruc
                    where a.is_deleted = 'N'
                    and a.organization_id in (:organizationIds)
                    and a.date_timekeeping between :startDate and :endDate
                    order by a.order_number
                """;
        HashMap<String, Object> params = new HashMap<>();
        params.put("startDate", startDate);
        params.put("endDate", endDate);
        params.put("organizationIds", orgIds);
        params.put("typeViTriTruc", Constant.CATEGORY_CODES.VI_TRI_TRUC);
        return getListData(sql, params, DutySchedulesResponse.class);
    }

    public List<Map<String, Object>> getListExport(DutySchedulesRequest.SearchForm dto) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    a.duty_schedule_id,
                    a.duty_position_id,
                    a.employee_id,
                    e.employee_code,
                    e.full_name,
                    a.organization_id,
                    o.org_name,
                    a.date_timekeeping,
                    a.order_number,
                    a.is_deleted,
                    a.created_by,
                    a.created_time,
                    a.modified_by,
                    a.modified_time,
                    a.last_update_time
                """);
        Map<String, Object> params = new HashMap<>();
        addCondition(sql, params, dto);
        return getListData(sql.toString(), params);
    }

    private void addCondition(StringBuilder sql, Map<String, Object> params, DutySchedulesRequest.SearchForm dto) {
        sql.append("""
                    FROM abs_duty_schedules a
                    JOIN hr_employees e ON e.employee_id = a.employee_id
                    LEFT JOIN hr_jobs mj ON mj.job_id = e.job_id
                    JOIN hr_organizations o ON o.organization_id = a.organization_id
                    WHERE IFNULL(a.is_deleted, :activeStatus) = :activeStatus
                """);
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        //QueryUtils.filterGe(dto.getFromDate(), sql, params, "a.from_date", "fromDate");
        //QueryUtils.filterLe(dto.getToDate(), sql, params, "a.from_date", "toDate");
//        sql.append(" ORDER BY mo.display_seq, mo.path_id");
    }


    public List<DutySchedulesResponse.OrgBean> getListOrg(Long organizationId, Date dateValue) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    o.name,
                    o.organization_id
                    FROM hr_organizations o 
                    WHERE IFNULL(o.is_deleted, :activeStatus) = :activeStatus
                    AND o.organization_id IN (:orgConfigIds)
                """);
        HashMap<String, Object> params = new HashMap<>();
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        params.put("orgConfigIds", Utils.stringToListLong(parameterRepository.getConfigValue("DON_VI_TRUC", dateValue, String.class), ","));
        if (organizationId != null && organizationId > 0) {
            sql.append(" AND o.path_id like :orgPath ");
            params.put("orgPath", "%/" + organizationId + "/%");
        }
        sql.append(" order by o.path_order");
        return getListData(sql.toString(), params, DutySchedulesResponse.OrgBean.class);
    }


    public List<Long> getListData(Date startDate, Date endDate, List<Long> listOrganizationId) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    a.duty_schedule_id
                FROM abs_duty_schedules a
                WHERE IFNULL(a.is_deleted, :activeStatus) = :activeStatus
                AND a.organization_id IN (:orgId)
                AND a.date_timekeeping BETWEEN :startDate AND :endDate
                ORDER BY a.organization_id, a.order_number;
                """);
        HashMap<String, Object> params = new HashMap<>();
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        params.put("orgId", listOrganizationId);
        params.put("startDate", startDate);
        params.put("endDate", endDate);
        return getListData(sql.toString(), params, Long.class);
    }

    public Map<String, List<DutySchedulesEntity>> getMapData(Date startDate, Date endDate, List<Long> listOrganizationId) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    a.*
                FROM abs_duty_schedules a
                WHERE IFNULL(a.is_deleted, :activeStatus) = :activeStatus
                AND a.organization_id IN (:orgId)
                AND a.date_timekeeping BETWEEN :startDate AND :endDate
                ORDER BY a.organization_id, a.order_number;
                """);
        HashMap<String, Object> params = new HashMap<>();
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        params.put("orgId", listOrganizationId);
        params.put("startDate", startDate);
        params.put("endDate", endDate);
        List<DutySchedulesEntity> listData = getListData(sql.toString(), params, DutySchedulesEntity.class);
        Map<String, List<DutySchedulesEntity>> result = new HashMap<>();
        for (DutySchedulesEntity entity : listData) {
            String key = entity.getOrganizationId() + "_" + entity.getDutyPositionId() + "_" + Utils.formatDate(entity.getDateTimekeeping());
            result.computeIfAbsent(key, k -> new ArrayList<>());
            result.get(key).add(entity);
        }
        return result;
    }

    public List<CategoryDto> getListCategories(String categoryType) {
        String sql = """
                select value, name
                from sys_categories
                where is_deleted = 'N'
                  and category_type = :categoryType
                  order by ifnull(order_number,:maxInteger), name
                """;
        Map<String, Object> map = new HashMap<>();
        map.put("categoryType", categoryType);
        map.put("maxInteger", Integer.MAX_VALUE);
        return getListData(sql, map, CategoryDto.class);
    }


    public List<DutySchedulesResponse.DetailBean2> getListData(DutySchedulesRequest.SearchForm dto) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    a.duty_position_id,
                    a.organization_id,
                    a.date_timekeeping,
                    a.order_number,
                    GROUP_CONCAT(a.employee_id) AS employee_ids,
                    GROUP_CONCAT(concat(e.employee_code, ' - ', e.full_name)) AS fullNames
                FROM abs_duty_schedules a
                JOIN hr_employees e ON e.employee_id = a.employee_id
                JOIN hr_organizations o ON o.organization_id = a.organization_id
                WHERE IFNULL(a.is_deleted, :activeStatus) = :activeStatus
                AND a.organization_id IN (:orgConfigIds)
                AND a.date_timekeeping BETWEEN :startDate AND :endDate
                """);
        HashMap<String, Object> params = new HashMap<>();
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        params.put("startDate", dto.getDateValue());
        params.put("endDate", dto.getEndDateValue());
        if (!Utils.isNullOrEmpty(dto.getListOrganizationId())) {
            params.put("orgConfigIds", dto.getListOrganizationId());
        } else {
            params.put("orgConfigIds", Utils.stringToListLong(parameterRepository.getConfigValue("DON_VI_TRUC", dto.getDateValue(), String.class), ","));
        }
        if (dto.getOrganizationId() != null && dto.getOrganizationId() > 0) {
            sql.append(" AND o.path_id like :orgPath ");
            params.put("orgPath", "%/" + dto.getOrganizationId() + "/%");
        }
        sql.append("""
                GROUP BY a.duty_position_id, a.organization_id, a.date_timekeeping, a.order_number
                ORDER BY o.path_order, a.duty_position_id, a.date_timekeeping
                """);
        return getListData(sql.toString(), params, DutySchedulesResponse.DetailBean2.class);
    }

    public List<DutySchedulesResponse> getListExportTotal(DutySchedulesRequest.ReportForm dto, boolean exportAll) {
        StringBuilder sql = new StringBuilder("""
                select 
                    org.organization_id,
                	org.name as organizationName,
                	a.date_timekeeping,
                	sc.name dutyPositionName,
                	f_get_last_name(e.employee_code,e.full_name) employeeName
                from abs_duty_schedules a, hr_organizations org,
                	sys_categories sc , hr_employees e
                where a.organization_id = org.organization_id
                and sc.value = a.duty_position_id
                and a.employee_id = e.employee_id
                and sc.category_type = 'ABS_VI_TRI_TRUC'
                and a.date_timekeeping between :startDate and :endDate
                and a.is_deleted = 'N'
                """);
        HashMap<String, Object> params = new HashMap<>();
        if (exportAll) {
            if (dto.getOrganizationId() != null && dto.getOrganizationId() > 0) {
                sql.append(" AND o.path_id like :orgPath ");
                params.put("orgPath", "%/" + dto.getOrganizationId() + "/%");
            }
        } else {
            sql.append(" AND a.organization_id = :orgId ");
            params.put("orgId", dto.getOrganizationId());
        }
        sql.append(" order by org.path_order, sc.order_number, sc.name");
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        params.put("startDate", dto.getFromDate());
        params.put("endDate", dto.getToDate());
        return getListData(sql.toString(), params, DutySchedulesResponse.class);
    }
}
