/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.models.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.models.request.OrganizationIndicatorsRequest;

import javax.persistence.Column;


/**
 * Lop Response DTO ung voi bang kpi_organization_indicators
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Data
@NoArgsConstructor
@JsonInclude(Include.NON_NULL)
public class OrganizationIndicatorsResponse {
    @Data
    @NoArgsConstructor
    @Schema(name = "OrganizationIndicatorsResponseSearchResult")
    public static class SearchResult {
        private Long organizationIndicatorId;
        private Long indicatorConversionId;
        private Long indicatorId;
        private Long organizationEvaluationId;
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
    @Schema(name = "OrganizationIndicatorsResponseOrganizationEvaluation")
    public static class OrganizationEvaluation {
        private Long organizationIndicatorId;
        private Long indicatorConversionId;
        private Long indicatorId;
        private Long organizationEvaluationId;
        private String indicatorName;
        private Double percent;
        private String target;
        private String type;
        private int level;
        private Boolean isChildren;
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
        private String conversionType;
        private String isFocusReduction;
        private String ratingType;
        private String key;
        private String listValues;
        private String leaderIds;
        private String collaboratorIds;
        private String assignmentNote;
        private String leaderType;
        private String collaboratorType;
        private String isWorkPlanningIndex;
        private Long selfPoint;
        private Long managePoint;
        private String leaderName;
        private String collaboratorName;
        private String statusLevel1;
        private String statusNameLevel1;
        private List<IndicatorConversionsResponse.ConversionDetail> conversions = new ArrayList<>();
    }

}
