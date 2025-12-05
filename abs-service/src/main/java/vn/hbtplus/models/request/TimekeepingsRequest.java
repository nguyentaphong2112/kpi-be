/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.models.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.util.Date;
import java.util.List;
import javax.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.constants.Constant;
import vn.hbtplus.utils.StrimDeSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import vn.hbtplus.models.BaseSearchRequest;

/**
 * Lop DTO ung voi bang abs_timekeepings
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Data
@NoArgsConstructor
public class TimekeepingsRequest extends BaseSearchRequest {

    @Data
    @NoArgsConstructor
    @Schema(name = "TimekeepingsSubmitForm")
    public static class SubmitForm {
        private Long employeeId;
        @DateTimeFormat(pattern = BaseConstants.COMMON_DATE_FORMAT)
        @JsonFormat(pattern = BaseConstants.COMMON_DATE_FORMAT, locale = BaseConstants.LOCALE_VN, timezone = BaseConstants.TIMEZONE_VN)
        private Date dateTimekeeping;
        private String workdayType;
    }

    @Data
    @NoArgsConstructor
    @Schema(name = "TimekeepingsSearchForm")
    public static class SearchForm extends BaseSearchRequest{

        private Long timekeepingId;

        private Long employeeId;

        @DateTimeFormat(pattern = BaseConstants.COMMON_DATE_FORMAT)
        @JsonFormat(pattern = BaseConstants.COMMON_DATE_FORMAT, locale = BaseConstants.LOCALE_VN, timezone = BaseConstants.TIMEZONE_VN)
        private Date dateTimekeeping;

        private Long workdayTypeId;

        private Double totalHours;

        @DateTimeFormat(pattern = Constant.COMMON_DATE_FORMAT)
        @JsonFormat(pattern = Constant.COMMON_DATE_FORMAT, locale = Constant.LOCALE_VN, timezone = Constant.TIMEZONE_VN)
        @NotNull
        private Date startDate;

        @DateTimeFormat(pattern = Constant.COMMON_DATE_FORMAT)
        @JsonFormat(pattern = Constant.COMMON_DATE_FORMAT, locale = Constant.LOCALE_VN, timezone = Constant.TIMEZONE_VN)
        @NotNull
        private Date endDate;

        @DateTimeFormat(pattern = Constant.COMMON_DATE_TIME_FORMAT)
        @JsonFormat(pattern = Constant.COMMON_DATE_TIME_FORMAT, locale = Constant.LOCALE_VN, timezone = Constant.TIMEZONE_VN)
        private Date fromTime;

        @DateTimeFormat(pattern = Constant.COMMON_DATE_TIME_FORMAT)
        @JsonFormat(pattern = Constant.COMMON_DATE_TIME_FORMAT, locale = Constant.LOCALE_VN, timezone = Constant.TIMEZONE_VN)
        private Date toTime;

        @DateTimeFormat(pattern = Constant.COMMON_DATE_TIME_FORMAT)
        @JsonFormat(pattern = Constant.COMMON_DATE_TIME_FORMAT, locale = Constant.LOCALE_VN, timezone = Constant.TIMEZONE_VN)
        private Date reportTime;

        private Long organizationId;
        private List<Long> empStatus;
        private String employeeCode;
        private List<Long> empTypeId;
        private String fullName;


    }
}
