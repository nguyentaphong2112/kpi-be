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
import vn.hbtplus.constants.BaseConstants;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Date;


/**
 * @author ecoIt
 * @since 11/05/2022
 * @version 1.0
 */
@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class WorkCalendarsDTO {

    private Long workCalendarId;
    @NotNull
    @Size(max = 200)
    private String name;
    @NotNull
    private String monWorkTime;
    @NotNull
    private String tueWorkTime;
    @NotNull
    private String wedWorkTime;
    @NotNull
    private String thuWorkTime;
    @NotNull
    private String friWorkTime;
    @NotNull
    private String satWorkTime;
    @NotNull
    private String sunWorkTime;
    private String defaultHolidayDate;

    @DateTimeFormat(pattern = BaseConstants.COMMON_DATE_FORMAT)
    @JsonFormat(pattern = BaseConstants.COMMON_DATE_FORMAT, locale = BaseConstants.LOCALE_VN, timezone = BaseConstants.TIMEZONE_VN)
    private Date startDate;

    @DateTimeFormat(pattern = BaseConstants.COMMON_DATE_FORMAT)
    @JsonFormat(pattern = BaseConstants.COMMON_DATE_FORMAT, locale = BaseConstants.LOCALE_VN, timezone = BaseConstants.TIMEZONE_VN)
    private Date endDate;

//    private List<WorkCalendarOrgsDTO> listWorkCalendarOrgs;
}
