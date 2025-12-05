/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.kpi.models.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import java.util.Date;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.kpi.constants.BaseConstants;


/**
 * Lop Response DTO ung voi bang kpi_evaluation_periods
 *
 * @author tudd
 * @version 1.0
 * @since 1.0
 */

@Data
@NoArgsConstructor
@JsonInclude(Include.NON_NULL)
public class EvaluationPeriodsResponse {
    @Data
    @NoArgsConstructor
    @Schema(name = "EvaluationPeriodsResponseSearchResult")
    public static class SearchResult extends KpiBaseResponse {
        private Long evaluationPeriodId;
        private Long year;
        private String name;
        private Long evaluationType;
        private String evaluationTypeName;

        @JsonFormat(pattern = BaseConstants.COMMON_DATE_FORMAT, locale = BaseConstants.LOCALE_VN, timezone = BaseConstants.TIMEZONE_VN)
        private Date startDate;

        private String status;

        @JsonFormat(pattern = BaseConstants.COMMON_DATE_FORMAT, locale = BaseConstants.LOCALE_VN, timezone = BaseConstants.TIMEZONE_VN)
        private Date endDate;

    }

    @Data
    @NoArgsConstructor
    @Schema(name = "EvaluationPeriodsResponseDetailBean")
    public static class DetailBean {
        private Long evaluationPeriodId;
        private Long year;
        private String name;
        private String evaluationType;
        @JsonFormat(pattern = BaseConstants.COMMON_DATE_FORMAT, locale = BaseConstants.LOCALE_VN, timezone = BaseConstants.TIMEZONE_VN)
        private Date startDate;
        @JsonFormat(pattern = BaseConstants.COMMON_DATE_FORMAT, locale = BaseConstants.LOCALE_VN, timezone = BaseConstants.TIMEZONE_VN)
        private Date endDate;
        private List<ObjectAttributesResponse> listAttributes;
    }

    @Data
    @NoArgsConstructor
    @Schema(name = "EvaluationPeriodsResponseMaxYear")
    public static class MaxYear {
        private Long year;
    }
}
