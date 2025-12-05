/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.kpi.models.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.kpi.utils.Utils;

import java.util.ArrayList;
import java.util.List;


/**
 * Lop Response DTO ung voi bang kpi_indicator_conversions
 *
 * @author tudd
 * @version 1.0
 * @since 1.0
 */

@Data
@NoArgsConstructor
@JsonInclude(Include.NON_NULL)
public class IndicatorConversionsResponse {
    @Data
    @NoArgsConstructor
    @Schema(name = "IndicatorConversionsResponseSearchResult")
    public static class SearchResult extends KpiBaseResponse {
        private Long indicatorConversionId;
        private String orgTypeId;
        private Long jobId;
        private String orgTypeName;
        private String organizationName;
        private String organizationId;
        private String jobName;
        private String status;

        public String getScope() {
            return Utils.join(" - ", jobName, orgTypeName);
        }
    }

    @Data
    @NoArgsConstructor
    @Schema(name = "IndicatorConversionsResponseIndicator")
    public static class Indicator {
        private Long indicatorId;
        private Long indicatorConversionId;
        private String indicatorName;
        private String unitName;
        private String periodTypeName;
        private String typeName;
        private String significance;
        private String measurement;
        private String systemInfo;
        private String status;
        private String note;
        private String relatedNames;
        private String scopeNames;
        private String conversionType;
        private String ratingType;
        private String listValues;
        private String isRequired;
        private String isRequiredName;
        private String target;
        private List<ConversionDetail> conversions = new ArrayList<>();

    }

    @Data
    @NoArgsConstructor
    @Schema(name = "IndicatorConversionsResponseIndicators")
    public static class Indicators {
        private String orgTypeName;
        private String organizationName;
        private String jobName;
        private String kpiLevelName;
        private String kpiLevel;
        private List<Indicator> indicatorConversion = new ArrayList<>();
    }

    @Data
    @NoArgsConstructor
    @Schema(name = "IndicatorConversionsResponseOrganization")
    public static class Organization {
        private String organizationId;
        private String name;
        private String orgTypeId;
        private String orgTypeName;
        private String jobId;
        private String jobName;
    }


    @Data
    @NoArgsConstructor
    @Schema(name = "IndicatorConversionsResponseConversionDetail")
    public static class ConversionDetail {
        private String resultId;
        private String expression;
        private String minValue;
        private String maxValue;
        private String minComparison;
        private String maxComparison;
        private String note;
    }

    @Data
    @NoArgsConstructor
    @Schema(name = "IndicatorConversionsResponseDetailBean")
    public static class DetailBean {
        private Long indicatorId;
        private Long indicatorConversionId;
        private String unitId;
        private String periodType;
        private String indicatorType;
        private String indicatorName;
        private String note;
        private String unitName;
        private String periodTypeName;
        private String significance;
        private String measurement;
        private String systemInfo;
        private String typeName;
        private String conversionType;
        private String ratingType;
        private String listValues;
        private String isRequired;
        private List<ConversionDetail> conversions = new ArrayList<>();
    }


}
