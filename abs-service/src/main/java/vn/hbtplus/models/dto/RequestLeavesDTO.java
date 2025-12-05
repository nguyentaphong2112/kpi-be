/*
 * Copyright (C) 2022 EcoIT. All rights reserved.
 * EcoIT. Use is subject to license terms.
 */
package vn.hbtplus.models.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import vn.hbtplus.constants.Constant;

import java.util.Date;
import java.util.List;


/**
 * @author ecoIt
 * @since 12/05/2022
 * @version 1.0
 */
@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RequestLeavesDTO {
    private Long requestLeaveId;

    private Long requestId;

    private Long employeeId;

    @DateTimeFormat(pattern = Constant.COMMON_DATE_TIME_FORMAT)
    @JsonFormat(pattern = Constant.COMMON_DATE_TIME_FORMAT, locale = Constant.LOCALE_VN, timezone = Constant.TIMEZONE_VN)
    private Date fromTime;

    @DateTimeFormat(pattern = Constant.COMMON_DATE_TIME_FORMAT)
    @JsonFormat(pattern = Constant.COMMON_DATE_TIME_FORMAT, locale = Constant.LOCALE_VN, timezone = Constant.TIMEZONE_VN)
    private Date toTime;

    @DateTimeFormat(pattern = Constant.COMMON_DATE_FORMAT)
    @JsonFormat(pattern = Constant.COMMON_DATE_FORMAT, locale = Constant.LOCALE_VN, timezone = Constant.TIMEZONE_VN)
    private Date timekeepingDate;

    private Long reasonLeaveId;

    private String partOfTime;

    private String workPlace;
    private String leaveType;
    private Long workdayTypeId;
    private Long timeOffType;
    private String workdayTypeCode;

    private String content;

    private String note;
    private String reasonLeaveName;
    private String reasonLeaveCode;

    private Double totalDays;
    private Double allDays;
    private Long defaultTimeOff;
    private Integer isOverHoliday;
    private Long organizationId;

//    private List<RequestApproversDTO> listApprovers;
    private List<AbsRequestDTO> listAbsRequest;
}
