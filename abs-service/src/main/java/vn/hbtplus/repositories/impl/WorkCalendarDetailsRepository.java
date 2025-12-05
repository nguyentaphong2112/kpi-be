/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.repositories.impl;

import org.apache.commons.lang3.time.DateUtils;
import org.springframework.stereotype.Repository;
import lombok.RequiredArgsConstructor;
import vn.hbtplus.models.BaseDataTableDto;
import vn.hbtplus.models.dto.WorkCalendarDetailsDTO;
import vn.hbtplus.repositories.BaseRepository;
import vn.hbtplus.models.request.WorkCalendarDetailsRequest;
import vn.hbtplus.models.response.WorkCalendarDetailsResponse;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.repositories.entity.WorkCalendarDetailsEntity;
import vn.hbtplus.repositories.entity.WorkCalendarsEntity;
import vn.hbtplus.repositories.jpa.WorkCalendarDetailsRepositoryJPA;
import vn.hbtplus.utils.Utils;

import java.util.*;

/**
 * Lop repository Impl ung voi bang abs_work_calendar_details
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Repository
@RequiredArgsConstructor
public class WorkCalendarDetailsRepository extends BaseRepository {

    private final WorkCalendarDetailsRepositoryJPA workCalendarDetailsJPA;

    public BaseDataTableDto searchData(WorkCalendarDetailsRequest.SearchForm dto) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    a.work_calendar_detail_id,
                    a.work_calendar_id,
                    a.date_timekeeping,
                    a.workday_time_id,
                    a.description,
                    a.is_deleted,
                    a.created_by,
                    a.created_time,
                    a.modified_by,
                    a.modified_time,
                    a.last_update_time
                """);
        HashMap<String, Object> params = new HashMap<>();
        addCondition(sql, params, dto);
        return getListPagination(sql.toString(), params, dto, WorkCalendarDetailsResponse.class);
    }

    public List<Map<String, Object>> getListExport(WorkCalendarDetailsRequest.SearchForm dto) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    a.work_calendar_detail_id,
                    a.work_calendar_id,
                    a.date_timekeeping,
                    a.workday_time_id,
                    a.description,
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

    private void addCondition(StringBuilder sql, Map<String, Object> params, WorkCalendarDetailsRequest.SearchForm dto) {
        sql.append("""
            FROM abs_work_calendar_details a
            
            
            WHERE IFNULL(a.is_deleted, :activeStatus) = :activeStatus
        """);
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        //QueryUtils.filterGe(dto.getFromDate(), sql, params, "a.from_date", "fromDate");
        //QueryUtils.filterLe(dto.getToDate(), sql, params, "a.from_date", "toDate");
        sql.append(" ORDER BY mo.display_seq, mo.path_id");
    }


    /**
     * getSearchRequests
     * @param workCalendarDetailsDTO
     * @return
     */
    public List<WorkCalendarDetailsDTO> getSearchRequests(WorkCalendarDetailsDTO workCalendarDetailsDTO) {
        HashMap<String, Object> params = new HashMap<>();
        String sql = """
                SELECT
                	wc.work_calendar_detail_id as workCalendarDetailId ,
                	wc.work_calendar_id as workCalendarId ,
                	wc.workday_time_id as workdayTimeId,
                	(select sc.code from sys_categories sc where sc.value = wc.workday_time_id and sc.category_type = :workdayTime) as workdayTime,
                	wc.date_timekeeping as dateTimekeeping ,
                	wc.description as description
                FROM
                	abs_work_calendar_details wc
                WHERE 1=1
                	and	wc.work_calendar_id = :workCalendarId
                	and YEAR(wc.date_timekeeping) = :year
                """;
//        params.put("fromDate", Utils.getFirstDay(1, absWorkCalendarDetailsDTO.getYear()));
//        params.put("toDate", Utils.getLastDayOfMonth(12, absWorkCalendarDetailsDTO.getYear()));
        params.put("workCalendarId", workCalendarDetailsDTO.getWorkCalendarId());
        params.put("year", workCalendarDetailsDTO.getYear());
        params.put("workdayTime",BaseConstants.CATEGORY_CODES.LICH_LAM_VIEC);
        return getListData(sql, params, WorkCalendarDetailsDTO.class);
    }

    /**
     * reInsertWorkCalendarDetails
     * @param entity
     * @throws Exception
     */
    public void reInsertWorkCalendarDetails(WorkCalendarsEntity entity, Integer year ){
        deleteByWorkCalendars(entity.getWorkCalendarId() , year);
        List<WorkCalendarDetailsEntity> listInsert = generateDetails(entity, year);
        insertBatch(WorkCalendarDetailsEntity.class, listInsert, Utils.getUserNameLogin());
    }

    /**
     * deleteByWorkCalendars
     *
     * @param workCalendarId
     * @return
     */
    public int deleteByWorkCalendars(Long workCalendarId , Integer year) {
        String sql = """
                DELETE
                FROM
                	abs_work_calendar_details
                WHERE
                	1 = 1
                	AND work_calendar_id = :workCalendarId
                	AND YEAR(date_timekeeping) = :year
                """;
        HashMap<String, Object> params = new HashMap<>();
        params.put("workCalendarId", workCalendarId);
        params.put("year", year);
        return executeSqlDatabase(sql, params);
    }


    /**
     * generateDetails
     * @param entity
     * @return
     * @throws Exception
     */
    private List<WorkCalendarDetailsEntity> generateDetails(WorkCalendarsEntity entity ,Integer year) {
        List<WorkCalendarDetailsEntity> returnList = new ArrayList<>();
        Date startDay = Utils.stringToDate(String.format("01/01/%s", year));
        Date endDay = Utils.stringToDate(String.format("31/12/%s", year));
        Map<String, String> mapWorkdayTimeId = new HashMap<>();
        String defaultHolidayDate = entity.getDefaultHolidayDate();
        if (!Utils.isNullObject(defaultHolidayDate)) {
            String[] days = defaultHolidayDate.split(",");
            for (String day: days) {
                Date date = Utils.stringToDate(String.format("%s/%s", day.trim(), year));
                if (date == null) {
                    continue;
                }
                String key = Utils.formatDate(date);
                mapWorkdayTimeId.put(key, BaseConstants.WorkdayTime.NB.getValue());
            }
        }

        calculateWorkdayType(year, mapWorkdayTimeId, entity);
        while (!endDay.before(startDay)) {
            WorkCalendarDetailsEntity detail = new WorkCalendarDetailsEntity();
            String key = Utils.formatDate(startDay);
            detail.setDateTimekeeping(startDay);
            detail.setWorkCalendarId(entity.getWorkCalendarId());
            detail.setWorkdayTimeId(String.valueOf(mapWorkdayTimeId.get(key)));
            returnList.add(detail);
            startDay = DateUtils.addDays(startDay, 1);
        }
        return returnList;
    }


    /**
     * calculateWorkdayType
     * @param year
     * @param mapWorkdayTime
     * @param entity
     * @throws Exception
     */
    private void calculateWorkdayType(Integer year, Map<String, String> mapWorkdayTime,
                                      WorkCalendarsEntity entity) {

        Date startDay = Utils.stringToDate(String.format("01/01/%s", year));
        Date endDay = Utils.stringToDate(String.format("31/12/%s", year));
        Calendar cal = Calendar.getInstance();
        Map<String, String> workdayTimeMap = loadDataBySysCategory(false, BaseConstants.CATEGORY_CODES.LICH_LAM_VIEC);
        while (!endDay.before(startDay)) {
            String key = Utils.formatDate(startDay);
            if (mapWorkdayTime.containsKey(key)) {
                startDay = DateUtils.addDays(startDay, 1);
                continue;
            }

            cal.setTime(startDay);
            int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
            String workdayTime = "";
            switch (dayOfWeek) {
                case Calendar.MONDAY:
                    workdayTime = entity.getMonWorkTimeId();
                    break;
                case Calendar.TUESDAY:
                    workdayTime = entity.getTueWorkTimeId();
                    break;
                case Calendar.WEDNESDAY:
                    workdayTime = entity.getWedWorkTimeId();
                    break;
                case Calendar.THURSDAY:
                    workdayTime = entity.getThuWorkTimeId();
                    break;
                case Calendar.FRIDAY:
                    workdayTime = entity.getFriWorkTimeId();
                    break;
                case Calendar.SATURDAY:
                    workdayTime = entity.getSatWorkTimeId();
                    break;
                case Calendar.SUNDAY:
                    workdayTime = entity.getSunWorkTimeId();
                    break;
                default:
                    break;
            }
            for (Map.Entry<String, String> entry : workdayTimeMap.entrySet()) {
                if (entry.getValue().equals(workdayTime)) {
                    mapWorkdayTime.put(key, entry.getValue());
                }
            }

            startDay = DateUtils.addDays(startDay, 1);
        }
    }



    public Map<String, String> loadDataBySysCategory(Boolean isValueAndName, String categoryType) {
        String sql = isValueAndName
                ? "SELECT name, value FROM sys_categories WHERE category_type = :categoryType"
                : "SELECT code, value FROM sys_categories WHERE category_type = :categoryType";

        Map<String, Object> params = new HashMap<>();
        params.put("categoryType", categoryType);

        return jdbcTemplate.query(sql, params, rs -> {
            Map<String, String> result = new HashMap<>();
            while (rs.next()) {
                String key = isValueAndName
                        ? rs.getString("name").toLowerCase()
                        : rs.getString("code").toLowerCase();
                String value = rs.getString("value");
                result.put(key, value);
            }
            return result;
        });
    }


    public WorkCalendarDetailsDTO getWorkCalendar(Date timekeepingDate) {
        String sql = """
                select a.workday_time_id,
                	sct.attribute_value workday_type_id,
                	sct1.attribute_value total_hours
                	from abs_work_calendar_details a,
                	sys_categories sc
                	left join sys_category_attributes sct on sct.category_id = sc.category_id and sct.attribute_code = 'KY_HIEU_CHAM_CONG'
                	left join sys_category_attributes sct1 on sct1.category_id = sc.category_id and sct1.attribute_code = 'SO_GIO_CHAM_CONG'
                where a.date_timekeeping = :timekeepingDate
                and a.is_deleted = 'N'
                and sc.category_type = 'LICH_LAM_VIEC'
                and sc.`value` = a.workday_time_id
                limit 1
                """;
        Map<String, Object> params = new HashMap<>();
        params.put("timekeepingDate", timekeepingDate);

        return queryForObject(sql, params, WorkCalendarDetailsDTO.class);
    }
}
