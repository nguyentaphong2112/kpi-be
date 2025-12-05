/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.repositories.impl;

import org.springframework.stereotype.Repository;
import lombok.RequiredArgsConstructor;
import vn.hbtplus.models.BaseDataTableDto;
import vn.hbtplus.repositories.BaseRepository;
import vn.hbtplus.models.request.WorkCalendarsRequest;
import vn.hbtplus.models.response.WorkCalendarsResponse;
import vn.hbtplus.constants.BaseConstants;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Lop repository Impl ung voi bang abs_work_calendars
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Repository
@RequiredArgsConstructor
public class WorkCalendarsRepository extends BaseRepository {

    public BaseDataTableDto searchData(WorkCalendarsRequest.SearchForm dto) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    a.work_calendar_id,
                    a.name,
                    a.mon_work_time_id,
                    a.tue_work_time_id,
                    a.wed_work_time_id,
                    a.thu_work_time_id,
                    a.fri_work_time_id,
                    a.sat_work_time_id,
                    a.sun_work_time_id,
                    a.default_holiday_date,
                    a.start_date,
                    a.end_date,
                    a.is_deleted,
                    a.created_by,
                    a.created_time,
                    a.modified_by,
                    a.modified_time,
                    a.last_update_time
                """);
        HashMap<String, Object> params = new HashMap<>();
        addCondition(sql, params, dto);
        return getListPagination(sql.toString(), params, dto, WorkCalendarsResponse.class);
    }

    public List<Map<String, Object>> getListExport(WorkCalendarsRequest.SearchForm dto) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    a.work_calendar_id,
                    a.name,
                    a.mon_work_time_id,
                    a.tue_work_time_id,
                    a.wed_work_time_id,
                    a.thu_work_time_id,
                    a.fri_work_time_id,
                    a.sat_work_time_id,
                    a.sun_work_time_id,
                    a.default_holiday_date,
                    a.start_date,
                    a.end_date,
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

    private void addCondition(StringBuilder sql, Map<String, Object> params, WorkCalendarsRequest.SearchForm dto) {
        sql.append("""
            FROM abs_work_calendars a
            WHERE IFNULL(a.is_deleted, :activeStatus) = :activeStatus
        """);
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        //QueryUtils.filterGe(dto.getFromDate(), sql, params, "a.from_date", "fromDate");
        //QueryUtils.filterLe(dto.getToDate(), sql, params, "a.from_date", "toDate");
        //sql.append(" ORDER BY mo.display_seq, mo.path_id");
    }
}
