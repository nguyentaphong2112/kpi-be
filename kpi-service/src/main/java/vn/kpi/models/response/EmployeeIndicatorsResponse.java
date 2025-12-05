/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.kpi.models.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.google.gson.annotations.SerializedName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.kpi.constants.BaseConstants;


/**
 * Lop Response DTO ung voi bang kpi_employee_indicators
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Data
@NoArgsConstructor
@JsonInclude(Include.NON_NULL)
public class EmployeeIndicatorsResponse {
    @Data
    @NoArgsConstructor
    @Schema(name = "EmployeeIndicatorsResponseSearchResult")
    public static class SearchResult {
        private Long employeeIndicatorId;
        private Long indicatorConversionId;
        private Long indicatorId;
        private Long employeeEvaluationId;
        private Double percent;
        private String target;
        private String createdBy;

        @JsonFormat(pattern = BaseConstants.COMMON_DATETIME_FORMAT, locale = BaseConstants.LOCALE_VN, timezone = BaseConstants.TIMEZONE_VN)
        private Date createdTime;
        private String modifiedBy;

        @JsonFormat(pattern = BaseConstants.COMMON_DATETIME_FORMAT, locale = BaseConstants.LOCALE_VN, timezone = BaseConstants.TIMEZONE_VN)
        private Date modifiedTime;
    }


    @Data
    @NoArgsConstructor
    @Schema(name = "EmployeeIndicatorsResponseEmployeeEvaluation")
    public static class EmployeeEvaluation {
        private Long employeeIndicatorId;
        private Long indicatorConversionId;
        private String conversionType;
        private Long indicatorId;
        private Long employeeEvaluationId;
        private String indicatorName;
        private Double percent;
        private String target;
        private String targetStr;
        private String unitName;
        private String periodTypeName;
        private String typeName;
        private String significance;
        private String measurement;
        private String systemInfo;
        private String relatedNames;
        private String scopeNames;
        private String note;
        private Double oldPercent;
        private String result;
        private String resultManage;
        private String ratingType;
        private String listValues;
        private String isFocusReduction;
        private String isWorkPlanningIndex;
        private Long selfPoint;
        private Long managePoint;
        private String isOrg;
        private String resultOrg;
        private String resultManageOrg;
        private String isHead;
        private List<IndicatorConversionsResponse.ConversionDetail> conversions = new ArrayList<>();
    }

    @Data
    @NoArgsConstructor
    @Schema(name = "EmployeeIndicatorsResponseTarget")
    public static class Target {
        @SerializedName("1")
        private String target1;
        @SerializedName("2")
        private String target2;
        @SerializedName("3")
        private String target3;
    }

}
