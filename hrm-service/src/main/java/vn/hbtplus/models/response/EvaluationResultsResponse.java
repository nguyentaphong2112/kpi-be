/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.models.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import java.util.Date;
import java.util.List;

import lombok.Data;
import lombok.NoArgsConstructor;
import vn.hbtplus.constants.BaseConstants;


/**
 * Lop Response DTO ung voi bang hr_evaluation_results
 *
 * @author tudd
 * @version 1.0
 * @since 1.0
 */

@Data
@NoArgsConstructor
@JsonInclude(Include.NON_NULL)
public class EvaluationResultsResponse {
    @Data
    @NoArgsConstructor
    @JsonInclude(Include.NON_NULL)
    public static class SearchResult extends EmpBaseResponse {
        private Long evaluationResultId;
        private Long year;
        private Long evaluationPeriodId;
        private String evaluationPeriodName;
        private Long employeeId;
        private Double kpiPoint;
        private String kpiResult;
        private String note;
        private String createdBy;

        @JsonFormat(pattern = BaseConstants.COMMON_DATETIME_FORMAT, locale = BaseConstants.LOCALE_VN, timezone = BaseConstants.TIMEZONE_VN)
        private Date createdTime;
        private String modifiedBy;

        @JsonFormat(pattern = BaseConstants.COMMON_DATETIME_FORMAT, locale = BaseConstants.LOCALE_VN, timezone = BaseConstants.TIMEZONE_VN)
        private Date modifiedTime;

        private String evaluationTypeName;

        List<ObjectAttributesResponse> listAttributes;
    }

    @Data
    @NoArgsConstructor
    @JsonInclude(Include.NON_NULL)
    public static class DetailBean {
        private Long evaluationResultId;
        private Long year;
        private Long evaluationPeriodId;
        private String evaluationPeriodName;
        private Long employeeId;
        private Double kpiPoint;
        private String kpiResult;
        private String note;
        private String evaluationType;

        List<ObjectAttributesResponse> listAttributes;
    }

    @Data
    @NoArgsConstructor
    @JsonInclude(Include.NON_NULL)
    public static class EvaluationPeriods {
        private Long evaluationPeriodId;
        private String value;
        private Integer year;
        private String name;
    }

}
