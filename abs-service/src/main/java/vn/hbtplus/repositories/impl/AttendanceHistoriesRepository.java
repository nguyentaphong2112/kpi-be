/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.repositories.impl;

import org.springframework.stereotype.Repository;
import lombok.RequiredArgsConstructor;
import vn.hbtplus.models.BaseDataTableDto;
import vn.hbtplus.repositories.BaseRepository;
import vn.hbtplus.models.request.AttendanceHistoriesRequest;
import vn.hbtplus.models.response.AttendanceHistoriesResponse;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.utils.QueryUtils;
import vn.hbtplus.utils.Utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Lop repository Impl ung voi bang abs_attendance_histories
 *
 * @author tudd
 * @version 1.0
 * @since 1.0
 */

@Repository
@RequiredArgsConstructor
public class AttendanceHistoriesRepository extends BaseRepository {

    public BaseDataTableDto searchData(AttendanceHistoriesRequest.SearchForm dto) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    a.attendance_history_id,
                    a.employee_id,
                    e.employee_code,
                    e.full_name as employee_name,
                    a.check_in_time,
                    a.check_out_time,
                    a.is_valid,
                    CASE
                        WHEN a.is_valid = 'Y' THEN 'Hợp lệ'
                        ELSE 'Không hợp lệ'
                    END AS validName,
                    cd.calendar_date date_timekeeping,
                    a.valid_check_in_time,
                    a.valid_check_out_time,
                    a.status_id,
                    CASE
                        WHEN a.status_id = 'CHO_PHE_DUYET' THEN 'Chờ phê duyệt'
                        WHEN a.status_id = 'PHE_DUYET' THEN 'Đã phê duyệt'
                        WHEN a.status_id = 'TU_CHOI' THEN 'Đã từ chối'
                    END AS statusName,
                    (SELECT syc.name FROM sys_categories syc where syc.value =  a.reason_id and syc.category_type = 'ABS_LY_DO_DC_CHAM_CONG') reasonName,
                    a.is_deleted,
                    a.created_by,
                    a.created_time,
                    a.modified_by,
                    a.modified_time,
                    a.last_update_time
                """);
        HashMap<String, Object> params = new HashMap<>();
        addCondition(sql, params, dto);
        return getListPagination(sql.toString(), params, dto, AttendanceHistoriesResponse.class);
    }

    public BaseDataTableDto searchDataByCurrentUser(AttendanceHistoriesRequest.SearchForm dto) {
        StringBuilder sql = new StringBuilder("""
                with v_sys_categories as (
                    select sc.`value`, IFNULL(sct.attribute_value,sc.`name`) as name  from sys_categories sc
                    left join sys_category_attributes sct on sc.category_id = sct.category_id and sct.attribute_code = 'TEN_HIEN_THI' and sct.is_deleted = 'N'
                    where sc.category_type = 'LICH_LAM_VIEC'
                )
                SELECT
                    a.attendance_history_id,
                    a.employee_id,
                    e.employee_code,
                    e.full_name as employee_name,
                    a.check_in_time,
                    a.check_out_time,
                    a.is_valid,
                    CASE
                        WHEN a.is_valid = 'Y' THEN 'Hợp lệ'
                        ELSE 'Không hợp lệ'
                    END AS validName,
                    cd.calendar_date date_timekeeping,
                    a.valid_check_in_time,
                    a.valid_check_out_time,
                    a.status_id,
                    (
                        select sc.name from v_sys_categories sc, 
                        abs_work_calendar_details wd 
                        where sc.value = wd.workday_time_id
                        and wd.is_deleted = 'N'
                        and wd.date_timekeeping = cd.calendar_date
                        limit 1
                    ) workScheduleName,
                    CASE
                        WHEN a.status_id = 'CHO_PHE_DUYET' THEN 'Chờ phê duyệt'
                        WHEN a.status_id = 'PHE_DUYET' THEN 'Đã phê duyệt'
                        WHEN a.status_id = 'TU_CHOI' THEN 'Đã từ chối'
                    END AS statusName,
                    (SELECT syc.name FROM sys_categories syc where syc.value =  a.reason_id and syc.category_type = 'ABS_LY_DO_DC_CHAM_CONG') reasonName,
                    a.is_deleted,
                    a.created_by,
                    a.created_time,
                    a.modified_by,
                    a.modified_time,
                    a.last_update_time
                """);
        HashMap<String, Object> params = new HashMap<>();
        addCondition(sql, params, dto);
        return getListPagination(sql.toString(), params, dto, AttendanceHistoriesResponse.class);
    }

    public List<Map<String, Object>> getListExport(AttendanceHistoriesRequest.SearchForm dto) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    a.attendance_history_id,
                    a.employee_id,
                    e.employee_code,
                    e.full_name,
                    a.check_in_time,
                    a.check_out_time,
                    a.is_valid,
                    a.date_timekeeping,
                    a.valid_check_in_time,
                    a.valid_check_out_time,
                    a.status_id,
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

    public BaseDataTableDto getLogData(AttendanceHistoriesRequest.SearchForm dto) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    a.attendance_log_id,
                    a.log_time,
                    a.address,
                    a.type
                    FROM abs_attendance_logs a
                    WHERE a.employee_id = :employeeId
                    and a.log_time >= :startTime
                    and a.log_time <= :endTime
                """);
        HashMap<String, Object> params = new HashMap<>();
        params.put("employeeId", this.getEmployeeIdByEmpCode(Utils.getUserEmpCode()));
        String dateTimeStr = Utils.formatDate(dto.getDateTimekeeping(), "yyyy-MM-dd");
        String startTime = dateTimeStr + " 00:00:00";
        String endTime = dateTimeStr + " 23:59:59";
        params.put("startTime", startTime);
        params.put("endTime", endTime);

        return getListPagination(sql.toString(), params, dto, AttendanceHistoriesResponse.AttendanceLogResponse.class);
    }

    private void addCondition(StringBuilder sql, Map<String, Object> params, AttendanceHistoriesRequest.SearchForm dto) {
        sql.append("""
                    FROM sys_calendars cd 
                    join hr_employees e on 1=1 
                    left join abs_attendance_histories a 
                        ON e.employee_id = a.employee_id and a.date_timekeeping = cd.calendar_date and a.is_deleted = 'N'
                    LEFT JOIN hr_jobs mj ON mj.job_id = e.job_id
                    LEFT JOIN hr_organizations o ON o.organization_id = e.organization_id
                    WHERE 1=1
                """);
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        sql.append(" and e.employee_id = :employeeId");
        params.put("employeeId", this.getEmployeeIdByEmpCode(Utils.getUserEmpCode()));
        QueryUtils.filter(dto.getKeySearch(), sql, params, "e.employee_code", "e.full_name");
        QueryUtils.filterGe(dto.getStartDate(), sql, params, "cd.calendar_date", "startDate");
        QueryUtils.filterLe(dto.getEndDate(), sql, params, "cd.calendar_date", "endDate");
        sql.append(" ORDER BY cd.calendar_date desc, a.employee_id");
    }

    public Long getEmployeeIdByEmpCode(String employeeCode) {
        String sql = "SELECT he.employee_id FROM hr_employees he" +
                     " WHERE he.is_deleted = :isDeleted " +
                     "AND employee_code = :employeeCode";
        Map map = new HashMap<>();
        map.put("isDeleted", BaseConstants.STATUS.NOT_DELETED);
        map.put("employeeCode", employeeCode);
        return queryForObject(sql, map, Long.class);
    }
}
