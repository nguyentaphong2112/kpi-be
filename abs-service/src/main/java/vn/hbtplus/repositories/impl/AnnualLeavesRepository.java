/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.repositories.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.models.BaseDataTableDto;
import vn.hbtplus.models.dto.WorkProcessDto;
import vn.hbtplus.models.request.AnnualLeavesRequest;
import vn.hbtplus.models.response.AnnualLeavesResponse;
import vn.hbtplus.repositories.BaseRepository;
import vn.hbtplus.repositories.entity.AnnualLeavesEntity;
import vn.hbtplus.repositories.entity.RequestsEntity;
import vn.hbtplus.utils.QueryUtils;
import vn.hbtplus.utils.Utils;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Lop repository Impl ung voi bang abs_annual_leaves
 *
 * @author tudd
 * @version 1.0
 * @since 1.0
 */

@Repository
@RequiredArgsConstructor
public class AnnualLeavesRepository extends BaseRepository {

    public BaseDataTableDto searchData(AnnualLeavesRequest.SearchForm dto) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    a.annual_leave_id,
                    a.year,
                    a.employee_id,
                    e.employee_code,
                    e.full_name as employeeName,
                    a.start_date,
                    a.end_date,
                    a.seniority,
                    o.name as organizationName,
                    mj.name as jobName,
                    a.working_months,
                    a.unpaid_months,
                    a.accident_months,
                    a.sickness_months,
                    a.remain_days,
                    a.annual_days,
                    a.used_days,
                    a.used_last_year_days,
                    (
                        SELECT remain_days
                        FROM abs_annual_leaves b
                        WHERE b.year = a.year - 1
                        AND b.employee_id = a.employee_id
                        AND IFNULL(b.is_deleted, :activeStatus) = :activeStatus
                        LIMIT 1
                    ) AS remainLastYearDays,
                    a.is_deleted,
                    a.created_by,
                    a.created_time,
                    a.modified_by,
                    a.modified_time,
                    a.last_update_time
                """);
        HashMap<String, Object> params = new HashMap<>();
        addCondition(sql, params, dto);
        return getListPagination(sql.toString(), params, dto, AnnualLeavesResponse.SearchResult.class);
    }

    public List<Map<String, Object>> getListExport(AnnualLeavesRequest.SearchForm dto) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    a.annual_leave_id,
                    a.year,
                    a.employee_id,
                    e.employee_code,
                    e.full_name as employeeName,
                    a.start_date,
                    a.end_date,
                    a.seniority,
                    o.name as organizationName,
                    mj.name as jobName,
                    a.working_months,
                    a.unpaid_months,
                    a.accident_months,
                    a.sickness_months,
                    a.remain_days,
                    a.annual_days,
                    a.used_days,
                    a.used_last_year_days,
                    (
                        SELECT remain_days
                        FROM abs_annual_leaves b
                        WHERE b.year = a.year - 1
                        AND b.employee_id = a.employee_id
                        AND IFNULL(b.is_deleted, :activeStatus) = :activeStatus
                        LIMIT 1
                    ) AS remainLastYearDays,
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

    public List<Map<String, Object>> getUsedDaysByEmp(String employeeId) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    et.date_timekeeping,
                    et.total_hours
                    FROM abs_timekeepings et
                    WHERE IFNULL(et.is_deleted, :activeStatus) = :activeStatus
                    AND et.workday_type_id in (
                     select object_id from abs_object_attributes atr
                     where atr.table_name = 'abs_workday_types'
                     and atr.attribute_code = 'LA_CONG_PHEP'
                     and atr.attribute_value = 'Y'
                     and IFNULL(atr.is_deleted, :activeStatus) = :activeStatus
                    ) and et.employee_id = :employeeId
                """);
        Map<String, Object> params = new HashMap<>();
        params.put("employeeId", employeeId);
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        return getListData(sql.toString(), params);
    }

    private void addCondition(StringBuilder sql, Map<String, Object> params, AnnualLeavesRequest.SearchForm dto) {
        sql.append("""
                    FROM abs_annual_leaves a
                    JOIN hr_employees e ON e.employee_id = a.employee_id
                    LEFT JOIN hr_jobs mj ON mj.job_id = e.job_id
                    LEFT JOIN hr_organizations o ON o.organization_id = e.organization_id
                    WHERE IFNULL(a.is_deleted, :activeStatus) = :activeStatus
                """);
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        QueryUtils.filter(dto.getKeySearch(), sql, params, "e.employee_code", "e.full_name");
        QueryUtils.filter(dto.getEmployeeId(), sql, params, "a.employee_id");
        QueryUtils.filter(dto.getYear(), sql, params, "a.year");
        if (dto.getStartDate() != null) {
            sql.append(" and NVL(a.end_date, :startDate) >= :startDate");
            params.put("startDate", dto.getStartDate());
        }
        if (dto.getEndDate() != null) {
            sql.append(" and NVL(a.start_date, :endDate) <= :endDate");
            params.put("endDate", dto.getEndDate());
        }
        sql.append(" order by o.path_order, mj.order_number, e.employee_id");
    }

    public List<WorkProcessDto> getListEmployee(Integer year, List<Long> empIds) {
        StringBuilder sql = new StringBuilder("""
                select wp.employee_id,
                	GREATEST(wp.start_date, :startDate) as start_date,
                	LEAST(IFNULL(wp.end_date, :endDate),:endDate) as end_date
                from hr_work_process wp
                left join hr_document_types dt on wp.document_type_id = dt.document_type_id
                where IFNULL(dt.type,'IN') <> 'OUT'
                and wp.start_date <= :endDate
                and IFNULL(wp.end_date, :endDate) >= :startDate
                and wp.is_deleted = 'N'
                """);
        Map<String, Object> params = new HashMap<>();
        if(!Utils.isNullOrEmpty(empIds)) {
            sql.append(" and wp.employee_id in (:empIds)");
            params.put("empIds", empIds);
        }
        sql.append(" order by wp.employee_id, wp.start_date asc");
        params.put("startDate", Utils.stringToDate("01/01/" + year));
        params.put("endDate", Utils.stringToDate("31/12/" + year));
        return getListData(sql.toString(), params, WorkProcessDto.class);
    }

    public List<AnnualLeavesEntity> getListWorkingMonths(List<Long> empIds, Date startDate, Date endDate) {
        String sql = """
                select
                	cp.employee_id,
                	:startDate as startDate,
                	:endDate as endDate,
                	YEAR(:endDate) as year,
                	sum(months_between(DATE_ADD(LEAST(IFNULL(cp.end_date,:endDate),:endDate), INTERVAL 1 DAY),
                	GREATEST(cp.start_date, :startDate))) workingMonths
                from hr_contract_process cp, hr_contract_types ct
                where cp.classify_code = 'HOP_DONG'
                and cp.is_deleted = 'N'
                and cp.start_date <= :endDate
                and cp.contract_type_id = ct.contract_type_id
                and IFNULL(cp.end_date,:endDate) >= :startDate
                and ct.duoc_huong_phep = 'Y'
                and cp.employee_id in (:empIds)
                and exists (
                	select 1 from hr_contract_process cp1, hr_contract_types ct1
                	where cp1.classify_code = 'HOP_DONG'
                	and cp1.is_deleted = 'N'
                	and cp1.start_date <= :endDate
                	and cp1.contract_type_id = ct1.contract_type_id
                	and IFNULL(cp1.end_date,:endDate) >= :startDate
                	and ct1.trong_danh_sach = 'Y'
                	and cp.employee_id = cp1.employee_id
                )
                and cp.employee_id in (:empIds)
                group by cp.employee_id
                """;
        Map<String, Object> params = new HashMap<>();
        params.put("empIds", empIds);
        params.put("startDate", startDate);
        params.put("endDate", endDate);
        return getListData(sql, params, AnnualLeavesEntity.class);
    }

    public List<AnnualLeavesEntity> getListSeniority(List<Long> empIds, Date startDate, Date endDate) {
        String sql = """
                select e.employee_id, f_get_seniority(e.employee_id, :endDate) seniority
                from hr_employees e
                where e.employee_id in (:empIds)
                """;
        Map<String, Object> params = new HashMap<>();
        params.put("empIds", empIds);
        params.put("startDate", startDate);
        params.put("endDate", endDate);
        return getListData(sql, params, AnnualLeavesEntity.class);
    }
    public List<AnnualLeavesEntity> getListTotalLeaveMonth(List<Long> empIds, Date startDate, Date endDate) {
        String sql = """
                select T.employee_id,
                	sum(case when T.attribute_value = 'KHONG_LUONG' then T.total_months end) as unpaidMonths,
                	sum(case when T.attribute_value = 'TAI_NAN' then T.total_months end) as accidentMonths,
                	sum(case when T.attribute_value = 'NGHI_OM' then T.total_months end) as sicknessMonths
                FROM (
                select 
                	a.employee_id,
                	atr.attribute_value,
                	months_between(DATE_ADD(LEAST(nvl(a.end_time,:endDate),:endDate), INTERVAL 1 DAY),GREATEST(a.start_time,:startDate)) as total_months
                from abs_requests a,
                	abs_reason_types rt,
                	abs_object_attributes atr
                where a.reason_type_id = rt.reason_type_id
                and a.`status` in (:statusPheDuyet)
                and atr.attribute_code = 'NHOM_TINH_PHEP'
                and a.start_time <= :endDate
                and a.end_time >= :startDate
                and atr.object_id = rt.reason_type_id
                and atr.is_deleted = 'N'
                and a.employee_id in (:empIds)
                and a.is_deleted = 'N'
                and atr.table_name = 'abs_reason_types') T
                GROUP BY T.employee_id
                """;
        Map<String, Object> params = new HashMap<>();
        params.put("empIds", empIds);
        params.put("startDate", startDate);
        params.put("endDate", endDate);
        params.put("statusPheDuyet", List.of(RequestsEntity.STATUS.DA_PHE_DUYET));
        return getListData(sql, params, AnnualLeavesEntity.class);
    }

    public void inactiveOldData(List<Long> empIdChange, Integer year) {
        String sql = """
                update abs_annual_leaves a
                set a.is_deleted = 'Y', a.modified_by = :userName, 
                a.modified_time = now()
                where a.year = :year
                and a.employee_id in (:empIdChange)
                """;
        Map<String, Object> params = new HashMap<>();
        params.put("empIdChange", empIdChange);
        params.put("year", year);
        params.put("userName", Utils.getUserNameLogin());
        executeSqlDatabase(sql, params);
    }

    public List<AnnualLeavesEntity> getListAnnualLeave(Integer year, List<Long> empIds) {
        StringBuilder sql = new StringBuilder("""
                select a.* from abs_annual_leaves a
                where a.year = :year
                and a.is_deleted = 'N'
                """);
        Map<String, Object> params = new HashMap<>();
        if(!Utils.isNullOrEmpty(empIds)) {
            sql.append(" and a.employee_id in (:empIds)");
            params.put("empIds", empIds);
        }
        params.put("year", year);
        return getListData(sql.toString(), params, AnnualLeavesEntity.class);
    }

    public void updateLeaveDays(Integer year, List<Long> empIds) {
        StringBuilder sql = new StringBuilder("""
                update abs_annual_leaves a
                	left join abs_annual_leaves a1 on a1.year = :preYear and a1.end_date = :endPreYear and a1.employee_id = a.employee_id and a1.is_deleted = 'N'
                	left join (
                		select T.annual_leave_id, sum(T.nghi_phep_quy_1) nghi_phep_quy_1,
                			sum(T.nghi_phep_ca_nam) nghi_phep_ca_nam
                		from (
                		select
                		 a2.annual_leave_id,
                		 case when et.date_timekeeping between :startYear and :endMarch
                				then et.total_hours
                		 end nghi_phep_quy_1,
                		 et.total_hours as nghi_phep_ca_nam
                		from abs_timekeepings et, abs_annual_leaves a2
                		where et.date_timekeeping between :startYear and :endYear
                		and et.date_timekeeping between a2.start_date and a2.end_date 
                		and et.is_deleted = 'N'
                		and et.employee_id = a2.employee_id
                		and a2.year = :year
                		and a2.is_deleted = 'N'
                		and et.workday_type_id in (
                		 select object_id from abs_object_attributes atr
                		 where atr.table_name = 'abs_workday_types'
                		 and atr.attribute_code = 'LA_CONG_PHEP'
                		 and atr.attribute_value = 'Y'
                		 and atr.is_deleted = 'N'
                		) ) T
                		GROUP by T.annual_leave_id
                	) p on a.annual_leave_id = p.annual_leave_id
                set
                	a.used_last_year_days = LEAST(ifnull(a1.remain_days,0), ifnull(p.nghi_phep_quy_1,0)),
                	a.used_days = (ifnull(nghi_phep_ca_nam,0) - LEAST(ifnull(a1.remain_days,0), ifnull(p.nghi_phep_quy_1,0))),
                	a.remain_days = (
                	case
                		when now() > :endMarch
                		then a.total_annual_days - (ifnull(nghi_phep_ca_nam,0) - LEAST(ifnull(a1.remain_days,0), ifnull(p.nghi_phep_quy_1,0)))
                		else a.total_annual_days + ifnull(a1.remain_days,0) - ifnull(nghi_phep_ca_nam,0)
                	end )
                where a.year = :year
                and a.is_deleted = 'N' 
                """);
        Map<String, Object> params = new HashMap<>();
        params.put("year", year);
        params.put("preYear", year - 1);
        params.put("endPreYear", Utils.stringToDate("31/12/" + (year - 1)));
        params.put("startYear", Utils.stringToDate("01/01/" + year));
        params.put("endYear", Utils.stringToDate("31/12/" + year));
        params.put("endMarch", Utils.stringToDate("31/03/" + year));
        if(!Utils.isNullOrEmpty(empIds)) {
            sql.append(" and a.employee_id in (:empIds)");
            params.put("empIds", empIds);
        }
        executeSqlDatabase(sql.toString(), params);
    }
}
