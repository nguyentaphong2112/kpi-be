/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.repositories.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.constants.Constant;
import vn.hbtplus.constants.Scope;
import vn.hbtplus.models.BaseDataTableDto;
import vn.hbtplus.models.dto.AbsRequestDTO;
import vn.hbtplus.models.dto.AbsTimekeepingDTO;
import vn.hbtplus.models.dto.EmployeeDto;
import vn.hbtplus.models.dto.LogTimekeepingsDTO;
import vn.hbtplus.models.dto.TimekeepingDTO;
import vn.hbtplus.models.request.TimekeepingsRequest;
import vn.hbtplus.models.response.TimekeepingsResponse;
import vn.hbtplus.repositories.BaseRepository;
import vn.hbtplus.repositories.entity.OvertimeRecordsEntity;
import vn.hbtplus.repositories.entity.RequestsEntity;
import vn.hbtplus.repositories.entity.TimekeepingsEntity;
import vn.hbtplus.services.AuthorizationService;
import vn.hbtplus.utils.QueryUtils;
import vn.hbtplus.utils.Utils;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Lop repository Impl ung voi bang abs_timekeepings
 *
 * @author tudd
 * @version 1.0
 * @since 1.0
 */

@Repository
@RequiredArgsConstructor
public class TimekeepingsRepository extends BaseRepository {


    private final AuthorizationService authorizationService;

    public BaseDataTableDto searchEmployee(TimekeepingsRequest.SearchForm dto) {
        HashMap<String, Object> params = new HashMap<>();

        String sql = getQueryString(dto, params);

        return getListPagination(sql, params, dto, TimekeepingsResponse.SearchResult.class);
    }

    private String getQueryString(TimekeepingsRequest.SearchForm dto, HashMap<String, Object> params) {
        StringBuilder sql = new StringBuilder("""
                with v_work_process as (
                	select
                		ROW_NUMBER() OVER (PARTITION BY employee_id ORDER BY end_date desc, type) AS row_num,
                		T.*
                	from (
                		select
                			 wp.employee_id,
                			 1 type ,
                			 GREATEST(wp.start_date,:startDate) as start_date,
                			 LEAST(ifnull(wp.end_date,:endDate), :endDate) as end_date,
                			 wp.organization_id,
                			 wp.job_id
                			 from hr_work_process wp, hr_document_types dt, hr_organizations org
                			 where wp.is_deleted = 'N'
                			 and wp.start_date <= :endDate
                			 and IFNULL(wp.end_date, :endDate)  >= :startDate
                			 and wp.document_type_id = dt.document_type_id
                			 and org.organization_id = wp.organization_id
                			 and dt.type <> 'OUT'
                			 ${filter_permission}
                		 union all
                			 select
                			 wp.employee_id,
                			 2 type ,
                			 GREATEST(wp.start_date,:startDate) as start_date,
                			 LEAST(ifnull(wp.end_date,:endDate) , :endDate) as end_date,
                			 wp.organization_id,
                			 wp.job_id
                			 from hr_concurrent_process wp, hr_organizations org
                			 where wp.is_deleted = 'N'
                			 and wp.start_date <= :endDate
                			 and IFNULL(wp.end_date, :endDate)  >= :startDate
                			 and org.organization_id = wp.organization_id
                			 ${filter_permission}
                	 ) T
                )
                select e.employee_id, e.employee_code, e.full_name , wp.end_date end_date,
                    (select min(start_date) from v_work_process wp1 where wp1.employee_id = wp.employee_id) as start_date,
                	org.org_name_level_2 as org_name,  jb.name as position_name
                from hr_employees e, v_work_process wp , hr_organizations org ,
                	hr_jobs jb
                where e.employee_id = wp.employee_id
                and wp.organization_id = org.organization_id
                and jb.job_id = wp.job_id
                and wp.row_num = 1
                """);
        List<Long> orgIds = authorizationService.getOrgHasPermission(Scope.VIEW, Constant.RESOURCES.ABS_TIMEKEEPING, Utils.getUserNameLogin());
        String filterPermission = """
                 and exists (
                    select 1 from hr_organizations pmo
                    where pmo.organization_id in (:orgIds)
                    and org.path_id like concat(pmo.path_id, '%')
                )
                """;
        params.put("orgIds", orgIds);

        if (dto.getEmpStatus() != null && !dto.getEmpStatus().isEmpty()) {
            sql.append(" and e.status in (:empStatus)");
            params.put("empStatus", dto.getEmpStatus());
        }
        if (dto.getEmpTypeId() != null && !dto.getEmpTypeId().isEmpty()) {
            sql.append(" and e.emp_type_id in (:empTypeIds)");
            params.put("empTypeIds", dto.getEmpTypeId());
        }
        if (!Utils.isNullOrEmpty(dto.getKeySearch())) {
            sql.append(" and (e.employee_code LIKE :keySearch" +
                       "   or upper(e.full_name) like :keySearch" +
                       "   or upper(e.email) like :keySearch)");
            params.put("keySearch", "%" + dto.getKeySearch().toUpperCase().trim() + "%");
        }
        if (!Utils.isNullObject(dto.getOrganizationId())) {
            sql.append(" and org.path_id LIKE :orgId");
            params.put("orgId", "%/" + dto.getOrganizationId() + "/%");
        }
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        params.put("startDate", dto.getStartDate());
        params.put("endDate", dto.getEndDate());
        QueryUtils.filter(Utils.NVL(dto.getEmployeeCode()).toUpperCase(), sql, params, "e.employee_code");
        QueryUtils.filter(dto.getFullName(), sql, params, "e.full_name");
        sql.append(" ORDER BY org.path_order, e.employee_id");
        return sql.toString().replace("${filter_permission}", filterPermission);
    }


    public List<TimekeepingsResponse.TimekeepingBean> getListTimekeeping(List<Long> empIds, Date startDate, Date endDate, List<String> types) {
        String sql = """
                select 
                	a.employee_id,
                	a.date_timekeeping,
                	GROUP_CONCAT(distinct wt.code ORDER BY a.timekeeping_id SEPARATOR '/') as workday_type_code,
                	sum(a.total_hours) total_hours
                from abs_timekeepings a
                join abs_workday_types wt on a.workday_type_id = wt.workday_type_id
                left join hr_organizations org on a.organization_id = org.organization_id
                join hr_work_process wp on wp.employee_id = a.employee_id 
                    and wp.is_deleted = 'N'
                    and a.date_timekeeping between wp.start_date and ifnull(wp.end_date, :endDate)
                left join hr_organizations wpo on wp.organization_id = wpo.organization_id
                where a.is_deleted = 'N'
                and a.date_timekeeping between :startDate and :endDate
                and a.employee_id in (:empIds)
                and wt.workday_type_id in (
                    select object_id from abs_object_attributes atr 
                    where atr.attribute_code = :typeCode
                    and atr.table_name = 'abs_workday_types'
                    and atr.is_deleted = 'N'
                    and atr.attribute_value in (:types)
                )
                {filter_permission}
                group by a.employee_id,
                	a.date_timekeeping
                """;

        //Lay va check theo du lieu phan quyen
        List<Long> orgIds = authorizationService.getOrgHasPermission(Scope.VIEW, Constant.RESOURCES.ABS_TIMEKEEPING, Utils.getUserNameLogin());
        String filterPermission = """
                 and exists (
                    select 1 from hr_organizations pmo 
                    where pmo.organization_id in (:orgIds)
                    and ifnull(org.path_id, wpo.path_id) like concat(pmo.path_id, '%')
                )
                """;

        HashMap<String, Object> params = new HashMap<>();
        params.put("orgIds", orgIds);
        params.put("startDate", startDate);
        params.put("endDate", endDate);
        params.put("empIds", empIds);
        params.put("types", types);
        params.put("typeCode", "GROUP_TYPE");
        return getListData(sql.replace("{filter_permission}", filterPermission), params, TimekeepingsResponse.TimekeepingBean.class);
    }

    public List<EmployeeDto> getListEmployee(TimekeepingsRequest.SearchForm dto) {
        HashMap<String, Object> params = new HashMap<>();
        String sql = getQueryString(dto, params);
        return getListData(sql, params, EmployeeDto.class);
    }

    public List<TimekeepingsResponse.GroupTimekeepingBean> getListGroupTimekeeping(List<Long> empIds, Date startDate, Date endDate, String type) {
        String sql = """
                WITH v_group_workday_types as (
                    select 
                    	wt.workday_type_id,
                    	sc.`name`,
                    	1 as percent
                    from sys_categories sc ,
                    	abs_workday_types wt
                    where sc.category_type = 'ABS_PHAN_LOAI_CONG'
                    and exists (
                    	select 1 from abs_object_attributes a
                    	where a.table_name = 'abs_workday_types'
                    	and a.attribute_code = 'PHAN_LOAI_CONG'
                    	and a.object_id = wt.workday_type_id
                    	and a.is_deleted = 'N'
                    	and FIND_IN_SET(sc.value, a.attribute_value) > 0	
                    )
                    union all
                    select 
                    	wt.workday_type_id,
                    	sc.`name`,
                    	0.5 as percent
                    from sys_categories sc ,
                    	abs_workday_types wt
                    where sc.category_type = 'ABS_PHAN_LOAI_CONG'
                    and exists (
                    	select 1 from abs_object_attributes a
                    	where a.table_name = 'abs_workday_types'
                    	and a.attribute_code = 'PHAN_LOAI_NUA_NGAY_CONG'
                    	and a.object_id = wt.workday_type_id
                    	and a.is_deleted = 'N'
                    	and FIND_IN_SET(sc.value, a.attribute_value) > 0	
                    )
                )
                select 
                	a.employee_id,
                	gr.name as group_name,
                	sum(a.total_hours * gr.percent) total_hours
                from abs_timekeepings a
                join abs_workday_types wt on a.workday_type_id = wt.workday_type_id
                join v_group_workday_types gr on gr.workday_type_id = wt.workday_type_id
                left join hr_organizations org on a.organization_id = org.organization_id
                join hr_work_process wp on wp.employee_id = a.employee_id 
                    and wp.is_deleted = 'N'
                    and a.date_timekeeping between wp.start_date and ifnull(wp.end_date, :endDate)
                left join hr_organizations wpo on wp.organization_id = wpo.organization_id
                where a.is_deleted = 'N'
                and a.date_timekeeping between :startDate and :endDate
                and a.employee_id in (:empIds)
                {filter_permission}
                and wt.workday_type_id in (
                     select object_id from abs_object_attributes
                     where table_name = 'abs_workday_types'
                     and attribute_code = :typeCode
                     and is_deleted = 'N'
                     and attribute_value = :type 
                ) 
                group by a.employee_id,gr.name
                """;
        HashMap<String, Object> params = new HashMap<>();
        params.put("startDate", startDate);
        params.put("endDate", endDate);
        params.put("empIds", empIds);
        params.put("type", type);
        params.put("typeCode", "GROUP_TYPE");
        List<Long> orgIds = authorizationService.getOrgHasPermission(Scope.VIEW, Constant.RESOURCES.ABS_TIMEKEEPING, Utils.getUserNameLogin());
        String filterPermission = """
                 and exists (
                    select 1 from hr_organizations pmo 
                    where pmo.organization_id in (:orgIds)
                    and ifnull(org.path_id, wpo.path_id) like concat(pmo.path_id, '%')
                )
                """;
        params.put("orgIds", orgIds);
        return getListData(sql.replace("{filter_permission}", filterPermission), params, TimekeepingsResponse.GroupTimekeepingBean.class);
    }

    public void deleteOldData(Long employeeId, Date dateTimekeeping) {
        List<Long> orgIds = authorizationService.getOrgHasPermission(Scope.VIEW, Constant.RESOURCES.ABS_TIMEKEEPING, Utils.getUserNameLogin());
        String sql = """
                update abs_timekeepings a                
                join abs_workday_types wt on a.workday_type_id = wt.workday_type_id
                left join hr_organizations org on a.organization_id = org.organization_id
                join hr_work_process wp on wp.employee_id = a.employee_id 
                    and wp.is_deleted = 'N'
                    and :dateTimekeeping between wp.start_date and ifnull(wp.end_date, :dateTimekeeping)
                left join hr_organizations wpo on wp.organization_id = wpo.organization_id
                set a.is_deleted = 'Y',
                    a.modified_time = now(),
                    a.modified_by = :userName
                where a.is_deleted = 'N'
                and a.date_timekeeping = :dateTimekeeping
                and a.employee_id = :employeeId
                and exists (
                    select 1 from hr_organizations pmo 
                    where pmo.organization_id in (:orgIds)
                    and ifnull(org.path_id, wpo.path_id) like concat(pmo.path_id, '%')
                )
                """;
        HashMap<String, Object> params = new HashMap<>();
        params.put("employeeId", employeeId);
        params.put("dateTimekeeping", dateTimekeeping);
        params.put("userName", Utils.getUserNameLogin());
        params.put("orgIds", orgIds);
        executeSqlDatabase(sql, params);
    }

    public Long getOrganizationOfDate(Long employeeId, Date dateTimekeeping) {
        List<Long> orgIds = authorizationService.getOrgHasPermission(Scope.VIEW, Constant.RESOURCES.ABS_TIMEKEEPING, Utils.getUserNameLogin());
        String sql = """
                select pmo.organization_id 
                from hr_organizations pmo
                where pmo.organization_id in (:orgIds)
                and exists (
                    select 1 from hr_work_process wp, hr_document_types dt, hr_organizations org
                    where wp.organization_id = org.organization_id
                    and wp.document_type_id = dt.document_type_id
                    and wp.employee_id = :employeeId
                    and wp.is_deleted = 'N'
                    and org.path_id like concat(pmo.path_id, '%')
                    and :dateTimekeeping between wp.start_date and ifnull(wp.end_date, :dateTimekeeping)
                    and dt.type <> 'OUT'
                )
                order by pmo.path_level desc
                limit 1
                """;
        HashMap<String, Object> params = new HashMap<>();
        params.put("orgIds", orgIds);
        params.put("dateTimekeeping", dateTimekeeping);
        params.put("employeeId", employeeId);
        Long organizationId = queryForObject(sql, params, Long.class);
        if (organizationId != null) {
            return null;
        }

        //Lay don vi kiem nhiem
        sql = """
                select pmo.organization_id 
                from hr_organizations pmo
                where pmo.organization_id in (:orgIds)
                and exists (
                    select 1 from hr_concurrent_process wp, hr_organizations org
                    where wp.organization_id = org.organization_id
                    and wp.employee_id = :employeeId
                    and wp.is_deleted = 'N'
                    and org.path_id like concat(pmo.path_id, '%')
                    and :dateTimekeeping between wp.start_date and ifnull(wp.end_date, :dateTimekeeping)
                )
                order by pmo.path_level desc
                limit 1
                """;
        organizationId = queryForObject(sql, params, Long.class);

        return organizationId == null ? -1 : organizationId;
    }

    public List<Long> getListEmployeeId(Date timekeepingDate, List<Long> empIds) {
        String sql = """
                             select wp.employee_id
                             from hr_work_process wp, hr_document_types dt
                             where wp.is_deleted = 'N'
                             and :timekeepingDate between wp.start_date and ifnull(wp.end_date, :timekeepingDate)
                             and wp.document_type_id = dt.document_type_id
                             and dt.type <> 'OUT'
                             """
                     + (Utils.isNullOrEmpty(empIds) ? "" : " and wp.employee_id in (:empIds)");
        sql += " order by wp.employee_id";
        Map<String, Object> params = new HashMap<>();
        params.put("timekeepingDate", timekeepingDate);
        if(!Utils.isNullOrEmpty(empIds)) {
            params.put("empIds", empIds);
        }
        return getListData(sql, params, Long.class);
    }

    public List<AbsRequestDTO> getListRequestLeave(Date timekeepingDate, List<Long> empIds) {
        String sql = """
                 select 
                 a.employee_id,
                 rs.workday_type_id,
                 TIMESTAMPDIFF(SECOND, GREATEST(a.start_time, :startDay), LEAST(a.end_time, :endDay))/3600 as total_hours
                from abs_requests a, abs_reason_types rs
                where a.reason_type_id = rs.reason_type_id
                and a.is_deleted = 'N'
                and a.`status` in (:statusPheduyet)
                and :dateTimekeeping between DATE(a.start_time) and a.end_time
                """;
        sql += (Utils.isNullOrEmpty(empIds) ? "" : "and a.employee_id in (:empIds)");
        sql += "group by a.employee_id, rs.workday_type_id";
        Map<String, Object> params = new HashMap<>();
        params.put("dateTimekeeping", timekeepingDate);
        params.put("startDay", Utils.stringToDate(Utils.formatDate(timekeepingDate) + " 8:00", "dd/MM/yyyy HH:mm"));
        params.put("endDay", Utils.stringToDate(Utils.formatDate(timekeepingDate) + " 17:30", "dd/MM/yyyy HH:mm"));
        params.put("statusPheduyet", List.of(RequestsEntity.STATUS.DA_PHE_DUYET));
        if(!Utils.isNullOrEmpty(empIds)) {
            params.put("empIds", empIds);
        }
        return getListData(sql, params, AbsRequestDTO.class);
    }

    public void inactiveTimekeeping(Date timekeepingDate, String type, String reason) {
        inactiveTimekeeping(null, timekeepingDate, type, reason);
    }

    public void inactiveTimekeeping(List<Long> empIds, Date timekeepingDate, String type, String reason) {
        String sql = """
                             update abs_timekeepings a, abs_workday_types wt
                             set a.is_deleted = 'Y',
                                 a.modified_time = now(),
                                 a.modified_by = :userName,
                                 a.note = concat(a.note, '; ', :reason)
                             where a.is_deleted = 'N'
                             and a.date_timekeeping = :dateTimekeeping
                             and a.workday_type_id = wt.workday_type_id
                             and wt.workday_type_id in (
                                 select object_id from abs_object_attributes
                                 where table_name = 'abs_workday_types'
                                 and attribute_code = :typeCode
                                 and is_deleted = 'N'
                                 and attribute_value = :type 
                             ) 
                             """
                     + (Utils.isNullOrEmpty(empIds) ? "" : "and a.employee_id in (:empIds)");
        Map<String, Object> params = new HashMap<>();
        params.put("dateTimekeeping", timekeepingDate);
        params.put("userName", Utils.getUserNameLogin());
        params.put("typeCode", "GROUP_TYPE");
        params.put("type", type);
        params.put("reason", reason);
        if (!Utils.isNullOrEmpty(empIds)) {
            params.put("empIds", empIds);
        }
        executeSqlDatabase(sql, params);
    }

    public List<TimekeepingsEntity> getListTimekeeping(List<Long> employeeIds, Date timekeepingDate, String type) {
        StringBuilder sql = new StringBuilder("""
                select 
                	a.employee_id,
                	a.workday_type_id,
                	a.date_timekeeping,
                	wt.code as workday_type_code,
                	sum(a.total_hours) as total_hours
                from abs_timekeepings a,
                	abs_workday_types wt 
                where a.date_timekeeping = :dateTimekeeping
                and a.workday_type_id = wt.workday_type_id
                and a.is_deleted = 'N'
                and wt.workday_type_id in (
                	 select object_id from abs_object_attributes
                	 where table_name = 'abs_workday_types'
                	 and attribute_code = :typeCode
                	 and is_deleted = 'N'
                	 and attribute_value = :type
                )
                group by a.employee_id,
                	a.workday_type_id,
                	a.date_timekeeping,
                	wt.code
                """);
        Map<String, Object> params = new HashMap<>();
        if (!Utils.isNullOrEmpty(employeeIds)) {
            sql.append(" and a.employee_id in (:empIds)");
            params.put("empIds", employeeIds);
        }
        params.put("dateTimekeeping", timekeepingDate);
        params.put("typeCode", "GROUP_TYPE");
        params.put("type", type);
        return getListData(sql.toString(), params, TimekeepingsEntity.class);
    }

    public void insertLogTimekeeping(List<Long> empIdChanges, Map<Long, List<TimekeepingsEntity>> mapOldTimekeepings,
                                     Map<Long, List<TimekeepingsEntity>> mapNewTimekeepings,
                                     Date dateTimekeeping,
                                     String type) {
        String sql = """
                insert into abs_timekeeping_logs (employee_id, date_timekeeping, data_before, data_after, type, created_by, created_time)
                values (:employeeId, :dateTimekeeping, :dataBefore, :dataAfter, :type, :userName, now())
                """;
        List listParams = new ArrayList<>();
        String userName = Utils.getUserNameLogin();
        for (Long empId : empIdChanges) {
            List<TimekeepingsEntity> timekeepingsOld = mapOldTimekeepings.get(empId);
            List<TimekeepingsEntity> timekeepingsNew = mapNewTimekeepings.get(empId);
            String dataBefore = getDataLog(timekeepingsOld);
            String dataAfter = getDataLog(timekeepingsNew);
            if (Utils.isNullOrEmpty(dataBefore) && Utils.isNullOrEmpty(dataAfter)) {
                continue;
            }
            Map<String, Object> params = new HashMap<>();
            params.put("employeeId", empId);
            params.put("dateTimekeeping", dateTimekeeping);
            params.put("dataBefore", dataBefore);
            params.put("dataAfter", dataAfter);
            params.put("type", type);
            params.put("userName", userName);
            listParams.add(params);
        }
        executeBatch(sql, listParams);
    }

    private String getDataLog(List<TimekeepingsEntity> timekeepings) {
        if (Utils.isNullOrEmpty(timekeepings)) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        for (TimekeepingsEntity timekeeping : timekeepings) {
            sb.append(";");
            sb.append(timekeeping.getWorkdayTypeCode());
            if (timekeeping.getTotalHours().equals(4d)) {
                sb.append("/2");
            } else if (!timekeeping.getTotalHours().equals(8d)) {
                sb.append(":" + Utils.formatNumber(timekeeping.getTotalHours()));
            }
        }
        return sb.toString().substring(1);
    }

    public List<TimekeepingsEntity> getListOvertime(List<Long> empIds, Date timekeepingDate) {
        String sql = """
                select 
                	sct.attribute_value workday_type_id,
                	et.date_timekeeping,
                	et.employee_id,
                	et.total_hours
                from abs_overtime_records et,
                sys_category_attributes sct, sys_categories sc
                where sc.category_id = sct.category_id
                and sct.is_deleted = 'N'
                and et.is_deleted = 'N'
                and et.overtime_type_id = sc.`value`
                and sct.attribute_code = 'KY_HIEU_CHAM_CONG'
                and sc.category_type = 'ABS_LOAI_DK_LAM_THEM'                
                """;
        Map params = new HashMap<>();
        params.put("timekeepingDate", timekeepingDate);
        if (!Utils.isNullOrEmpty(empIds)) {
            sql += " and et.employee_id in (:empIds)";
            params.put("empIds", empIds);
        }
        return getListData(sql, params, TimekeepingsEntity.class);
    }
}
