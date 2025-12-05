/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.models.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.repositories.entity.WorkdayTypesEntity;
import vn.hbtplus.utils.Utils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;


/**
 * Lop Response DTO ung voi bang abs_timekeepings
 *
 * @author tudd
 * @version 1.0
 * @since 1.0
 */

@Data
@NoArgsConstructor
@JsonInclude(Include.NON_NULL)
public class TimekeepingsResponse {


    private Long timekeepingId;
    private Long employeeId;

    @JsonFormat(pattern = BaseConstants.COMMON_DATE_FORMAT, locale = BaseConstants.LOCALE_VN, timezone = BaseConstants.TIMEZONE_VN)
    private Date dateTimekeeping;
    private Long workdayTypeId;
    private Double totalHours;
    private String createdBy;

    @JsonFormat(pattern = BaseConstants.COMMON_DATETIME_FORMAT, locale = BaseConstants.LOCALE_VN, timezone = BaseConstants.TIMEZONE_VN)
    private Date createdTime;
    private String modifiedBy;

    @JsonFormat(pattern = BaseConstants.COMMON_DATETIME_FORMAT, locale = BaseConstants.LOCALE_VN, timezone = BaseConstants.TIMEZONE_VN)
    private Date modifiedTime;

    @Data
    @NoArgsConstructor
    public static class TimekeepingBean {
        private Date dateTimekeeping;
        private String workdayTypeCode;
        private Double totalHours;
        private Long employeeId;

        public String getDisplayWorkdayType(String type) {
            if (WorkdayTypesEntity.TYPE.LAM_THEM.equalsIgnoreCase(type)) {
                return Utils.formatNumber(totalHours);
            } else {
                if (totalHours >= 8) {
                    return workdayTypeCode;
                } else if (totalHours == 4) {
                    return workdayTypeCode + "/2";
                } else {
                    return workdayTypeCode + ":" + Utils.formatNumber(totalHours);
                }
            }
        }
    }

    @Data
    @NoArgsConstructor
    public static class GroupTimekeepingBean {
        private Date dateTimekeeping;
        private String groupName;
        private Long employeeId;
        private Double totalHours;
    }

    @Data
    @NoArgsConstructor
    public static class SearchResult {
        private Long employeeId;
        private String fullName;
        private String employeeCode;
        private String empTypeName;
        private String positionName;
        private String orgName;
        @JsonFormat(pattern = BaseConstants.COMMON_DATE_FORMAT, locale = BaseConstants.LOCALE_VN, timezone = BaseConstants.TIMEZONE_VN)
        private Date startDate;
        @JsonFormat(pattern = BaseConstants.COMMON_DATE_FORMAT, locale = BaseConstants.LOCALE_VN, timezone = BaseConstants.TIMEZONE_VN)
        private Date endDate;
        private Map<String, String> timekeepings;
        private List<CalendarDate> dateList = new ArrayList<>();
    }

    @Data
    @NoArgsConstructor
    public static class CalendarDate {
        private Date date;
        private String day;
        private int dayOfWeek;

        public CalendarDate(Date date) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            this.date = date;
            this.dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
            this.day = Utils.formatDate(date, "dd");
        }
    }
}
