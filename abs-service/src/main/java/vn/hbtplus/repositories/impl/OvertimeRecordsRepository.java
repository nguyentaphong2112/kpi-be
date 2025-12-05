/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.repositories.impl;

import org.springframework.stereotype.Repository;
import lombok.RequiredArgsConstructor;
import vn.hbtplus.constants.Constant;
import vn.hbtplus.models.BaseDataTableDto;
import vn.hbtplus.models.dto.CategoryDto;
import vn.hbtplus.models.dto.EmployeeDto;
import vn.hbtplus.repositories.BaseRepository;
import vn.hbtplus.models.request.OvertimeRecordsRequest;
import vn.hbtplus.models.response.OvertimeRecordsResponse;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.utils.QueryUtils;
import vn.hbtplus.utils.Utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Lop repository Impl ung voi bang abs_overtime_records
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Repository
@RequiredArgsConstructor
public class OvertimeRecordsRepository extends BaseRepository {

    public BaseDataTableDto searchData(OvertimeRecordsRequest.SearchForm dto) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    a.overtime_record_id,
                    a.employee_id,
                    e.employee_code,
                    e.full_name,
                    a.date_timekeeping,
                    a.start_time,
                    a.end_time,
                    a.total_hours,
                    a.overtime_type_id,
                    a.content,
                    (SELECT syc.name FROM sys_categories syc where syc.value =  a.overtime_type_id and syc.category_type = 'ABS_LOAI_DK_LAM_THEM') overtimeTypeName,
                    mj.name as job_name,
                    o.full_name as organization_name,
                    a.is_deleted,
                    a.created_by,
                    a.created_time,
                    a.modified_by,
                    a.modified_time,
                    a.last_update_time
                """);
        HashMap<String, Object> params = new HashMap<>();
        addCondition(sql, params, dto);
        return getListPagination(sql.toString(), params, dto, OvertimeRecordsResponse.class);
    }

    public List<Map<String, Object>> getListExport(OvertimeRecordsRequest.SearchForm dto) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    a.overtime_record_id,
                    a.employee_id,
                    e.employee_code,
                    e.full_name,
                    a.date_timekeeping,
                    a.start_time,
                    a.end_time,
                    a.total_hours,
                    a.overtime_type_id,
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

    public List<CategoryDto> getOvertimeTypes() {
        String sql = """
                select value, name
                from sys_categories
                where is_deleted = 'N'
                  and category_type = :categoryType
                  order by ifnull(order_number,:maxInteger), name
                  """;
        Map<String, Object> map = new HashMap<>();
        map.put("categoryType", Constant.CATEGORY_CODES.ABS_LOAI_DK_LAM_THEM);
        map.put("maxInteger", Integer.MAX_VALUE);
        return getListData(sql, map, CategoryDto.class);
    }

    public Map<String, EmployeeDto> getMapEmpById(List<String> empCodeList) {
        Map<String, EmployeeDto> mapEmp = new HashMap<>();
        if (Utils.isNullOrEmpty(empCodeList)) {
            return mapEmp;
        }

        String sql = """
                select employee_id, employee_code, full_name
                from hr_employees e
                where ifnull(e.is_deleted, :isDeleted) = :isDeleted
                    and e.employee_id in (:empCodeList)
                """;
        Map<String, Object> params = new HashMap<>();
        params.put("isDeleted", BaseConstants.STATUS.NOT_DELETED);
        params.put("empCodeList", empCodeList);

        List<EmployeeDto> empResponseList = getListData(sql, params, EmployeeDto.class);
        empResponseList.forEach(item -> mapEmp.put(item.getEmployeeId().toString().toLowerCase(), item));

        return mapEmp;
    }

    private void addCondition(StringBuilder sql, Map<String, Object> params, OvertimeRecordsRequest.SearchForm dto) {
        sql.append("""
            FROM abs_overtime_records a
            JOIN hr_employees e ON e.employee_id = a.employee_id
            LEFT JOIN hr_jobs mj ON mj.job_id = e.job_id
            JOIN hr_organizations o ON o.organization_id = e.organization_id
            WHERE IFNULL(a.is_deleted, :activeStatus) = :activeStatus
        """);
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        QueryUtils.filter(dto.getKeySearch(), sql, params, "e.employee_code","e.full_name");
        QueryUtils.filterGe(dto.getStartTime(), sql, params, "a.date_timekeeping", "fromDate");
        QueryUtils.filterLe(dto.getEndTime(), sql, params, "a.date_timekeeping", "toDate");

    }
}
