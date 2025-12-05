/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.models.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.utils.StrimDeSerializer;
import vn.hbtplus.utils.Utils;

import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Lop Response DTO ung voi bang abs_duty_schedules
 *
 * @author tudd
 * @version 1.0
 * @since 1.0
 */

@Data
@NoArgsConstructor
@JsonInclude(Include.NON_NULL)
public class DutySchedulesResponse {

    private Long organizationId;
    private String organizationName;
    private String dutyPositionId;
    private String dutyPositionName;
    private Long employeeId;
    private String employeeCode;
    private String employeeName;
    private Date dateTimekeeping;
    private String fullLabel;


    @Data
    @NoArgsConstructor
    @Schema(name = "DutySchedulesResponseSearchResult")
    public static class SearchResult {
        private Long organizationId;
        private String organizationName;
        private List<DutyPositionBean> dutyPositions;
    }

    @Data
    @NoArgsConstructor
    @Schema(name = "DutySchedulesResponseSearchResultMonth")
    public static class SearchResultMonth {
        private Long organizationId;
        private String organizationName;
        private List<DutyPositionBeanMonth> dutyPositions;
    }

    @Data
    @NoArgsConstructor
    @Schema(name = "DutySchedulesResponseDutyPositionBean")
    public static class DutyPositionBean {
        private String dutyPositionId;
        private String dutyPositionName;
        private Map<Integer, List<EmployeeBean>> mapEmployeeBeans = new HashMap<>();

        public DutyPositionBean(DutySchedulesResponse item) {
            this.dutyPositionId = item.getDutyPositionId();
            this.dutyPositionName = item.getDutyPositionName();
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(item.getDateTimekeeping());
            Integer date = calendar.get(Calendar.DAY_OF_WEEK);
            if (mapEmployeeBeans.get(date) == null) {
                this.mapEmployeeBeans.put(date, new ArrayList<>());
            }
            this.mapEmployeeBeans.get(date).add(new EmployeeBean(item));
        }

        public void addEmployee(DutySchedulesResponse item) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(item.getDateTimekeeping());
            Integer date = calendar.get(Calendar.DAY_OF_WEEK);
            if (mapEmployeeBeans.get(date) == null) {
                this.mapEmployeeBeans.put(date, new ArrayList<>());
            }
            this.mapEmployeeBeans.get(date).add(new EmployeeBean(item));
        }

        public List<EmployeeBean> getEmployeeId0() {
            return mapEmployeeBeans.get(Calendar.MONDAY);
        }

        public List<EmployeeBean> getEmployeeId1() {
            return mapEmployeeBeans.get(Calendar.TUESDAY);
        }

        public List<EmployeeBean> getEmployeeId2() {
            return mapEmployeeBeans.get(Calendar.WEDNESDAY);
        }

        public List<EmployeeBean> getEmployeeId3() {
            return mapEmployeeBeans.get(Calendar.THURSDAY);
        }

        public List<EmployeeBean> getEmployeeId4() {
            return mapEmployeeBeans.get(Calendar.FRIDAY);
        }

        public List<EmployeeBean> getEmployeeId5() {
            return mapEmployeeBeans.get(Calendar.SATURDAY);
        }

        public List<EmployeeBean> getEmployeeId6() {
            return mapEmployeeBeans.get(Calendar.SUNDAY);
        }

    }


    @Data
    @NoArgsConstructor
    @Schema(name = "DutySchedulesResponseDutyPositionBeanMonth")
    public static class DutyPositionBeanMonth {
        private String dutyPositionId;
        private String dutyPositionName;
        private Map<Integer, List<EmployeeBean>> mapEmployeeBeans = new HashMap<>();

        public DutyPositionBeanMonth(DutySchedulesResponse item) {
            this.dutyPositionId = item.getDutyPositionId();
            this.dutyPositionName = item.getDutyPositionName();
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(item.getDateTimekeeping());
            Integer date = calendar.get(Calendar.DAY_OF_MONTH);
            if (mapEmployeeBeans.get(date) == null) {
                this.mapEmployeeBeans.put(date, new ArrayList<>());
            }
            this.mapEmployeeBeans.get(date).add(new EmployeeBean(item));
        }

        public void addEmployee(DutySchedulesResponse item) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(item.getDateTimekeeping());
            Integer date = calendar.get(Calendar.DAY_OF_MONTH);
            if (mapEmployeeBeans.get(date) == null) {
                this.mapEmployeeBeans.put(date, new ArrayList<>());
            }
            this.mapEmployeeBeans.get(date).add(new EmployeeBean(item));
        }

    }

    @Data
    @NoArgsConstructor
    @Schema(name = "DutySchedulesResponseDetailBean")
    public static class DetailBean {

        @Size(max = 20)
        @JsonDeserialize(using = StrimDeSerializer.class)
        private String dutyPositionId;
        private List<EmployeeBean> employeeId0;
        private List<EmployeeBean> employeeId1;
        private List<EmployeeBean> employeeId2;
        private List<EmployeeBean> employeeId3;
        private List<EmployeeBean> employeeId4;
        private List<EmployeeBean> employeeId5;
        private List<EmployeeBean> employeeId6;

        private Long organizationId;

        @DateTimeFormat(pattern = BaseConstants.COMMON_DATE_FORMAT)
        @JsonFormat(pattern = BaseConstants.COMMON_DATE_FORMAT, locale = BaseConstants.LOCALE_VN, timezone = BaseConstants.TIMEZONE_VN)
        private Date dateValue;

        private Long orderNumber;
    }


    @Data
    @NoArgsConstructor
    @Schema(name = "DutySchedulesResponseDetailBean2")
    public static class DetailBean2 {

        @Size(max = 20)
        @JsonDeserialize(using = StrimDeSerializer.class)
        private String dutyPositionId;
        private List<Long> employeeIds;
        private List<String> fullNames;
        private Long organizationId;

        @DateTimeFormat(pattern = BaseConstants.COMMON_DATE_FORMAT)
        @JsonFormat(pattern = BaseConstants.COMMON_DATE_FORMAT, locale = BaseConstants.LOCALE_VN, timezone = BaseConstants.TIMEZONE_VN)
        private Date dateTimekeeping;

        private Long orderNumber;
    }


    @Data
    @NoArgsConstructor
    @Schema(name = "DutySchedulesResponseListBoxBean")
    public static class ListBoxBean {
        String value;
        String name;
    }

    @Data
    @NoArgsConstructor
    @Schema(name = "DutySchedulesResponseOrgBean")
    public static class OrgBean {
        String organizationId;
        String name;
    }


    @Data
    @NoArgsConstructor
    @Schema(name = "DutySchedulesResponseEmployeeBean")
    public static class EmployeeBean {
        Long employeeId;
        String label;
        String fullLabel;

        public EmployeeBean(DutySchedulesResponse item) {
            this.employeeId = item.getEmployeeId();
            this.label = Utils.join("-", item.getEmployeeCode(), item.getEmployeeName());
            this.fullLabel = item.getFullLabel();
        }
    }

}
