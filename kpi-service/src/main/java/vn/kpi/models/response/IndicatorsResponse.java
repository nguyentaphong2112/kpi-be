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

import java.util.ArrayList;
import java.util.List;


/**
 * Lop Response DTO ung voi bang kpi_indicators
 *
 * @author tudd
 * @version 1.0
 * @since 1.0
 */

@Data
@NoArgsConstructor
@JsonInclude(Include.NON_NULL)
public class IndicatorsResponse {

    @Data
    @NoArgsConstructor
    @Schema(name = "IndicatorsResponseSearchResult")
    public static class SearchResult extends KpiBaseResponse {
        private Long indicatorId;
        private String name;
        private String unitId;
        private String type;
        private String periodType;
        private String significance;
        private String measurement;
        private String systemInfo;
        private String unitName;
        private String periodTypeName;
        private String typeName;
        private String note;
        private String relatedNames;
        private String scopeNames;
        private String ratingType;
        private String listValues;
        private List<Long> orgIds;
        private List<Long> indicatorIds;
    }


    @Data
    @NoArgsConstructor
    @Schema(name = "IndicatorsResponseDetailList")
    public static class DetailList extends KpiBaseResponse {
        private Long indicatorId;
        private String name;
        private String unitId;
        private String type;
        private String periodType;
        private String significance;
        private String measurement;
        private String systemInfo;
        private String unitName;
        private String periodTypeName;
        private String typeName;
        private String note;
        private Long indicatorConversionId;
        List<IndicatorConversionsResponse.ConversionDetail> conversions = new ArrayList<>();
    }

    @Data
    @NoArgsConstructor
    @Schema(name = "IndicatorsResponseDataList")
    public static class DataList extends KpiBaseResponse {
        private Long indicatorId;
        private String name;
        private String unitId;
        private String type;
        private String periodType;
        private String ratingType;
    }
}
