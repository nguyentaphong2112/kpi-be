/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.models.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.models.dto.AttachmentDto;

import java.util.Date;
import java.util.List;


/**
 * Lop Response DTO ung voi bang hr_worked_histories
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Data
@NoArgsConstructor
@JsonInclude(Include.NON_NULL)
public class WorkedHistoriesResponse {

    @Data
    @NoArgsConstructor
    @Schema(name = "WorkedHistoriesResponseSearchResult")
    public static class SearchResult extends EmpBaseResponse {
        private Long workedHistoryId;
        private Long employeeId;

        @JsonFormat(pattern = BaseConstants.SHORT_DATE_FORMAT, locale = BaseConstants.LOCALE_VN, timezone = BaseConstants.TIMEZONE_VN)
        private Date startDate;

        @JsonFormat(pattern = BaseConstants.SHORT_DATE_FORMAT, locale = BaseConstants.LOCALE_VN, timezone = BaseConstants.TIMEZONE_VN)
        private Date endDate;
        private String job;
        private String companyName;
        private String referenceName;
        private String referenceJob;
    }

    @Data
    @NoArgsConstructor
    @Schema(name = "WorkedHistoriesResponseDetailBean")
    public static class DetailBean extends EmpBaseResponse{
        private Long workedHistoryId;
        private Long employeeId;

        @JsonFormat(pattern = BaseConstants.SHORT_DATE_FORMAT, locale = BaseConstants.LOCALE_VN, timezone = BaseConstants.TIMEZONE_VN)
        private Date startDate;

        @JsonFormat(pattern = BaseConstants.SHORT_DATE_FORMAT, locale = BaseConstants.LOCALE_VN, timezone = BaseConstants.TIMEZONE_VN)
        private Date endDate;
        private String job;
        private String companyName;
        private String referenceName;
        private String referenceJob;
        private String employeeName;
        private String employeeCode;

        private List<ObjectAttributesResponse> listAttributes;
    }
}
