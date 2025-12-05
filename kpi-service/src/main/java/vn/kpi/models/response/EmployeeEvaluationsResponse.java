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

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.kpi.constants.BaseConstants;


/**
 * Lop Response DTO ung voi bang kpi_employee_evaluations
 *
 * @author tudd
 * @version 1.0
 * @since 1.0
 */

@Data
@NoArgsConstructor
@JsonInclude(Include.NON_NULL)
public class EmployeeEvaluationsResponse extends KpiBaseResponse {
    @Data
    @NoArgsConstructor
    @Schema(name = "EmployeeEvaluationsResponseSearchResult")
    public static class SearchResult extends KpiBaseResponse {
        private Long employeeEvaluationId;
        private Long evaluationPeriodId;
        private String evaluationPeriodName;
        private Long employeeId;
        private String employeeName;
        private String employeeCode;
        private String orgName;
        private String jobName;
        private String status;
        private String resultId;
        private String finalResultId;
        private String isConcurrent;
        private Double selfTotalPoint;
        private Double managerTotalPoint;
        private Double finalPoint;
        private String approvedBy;
        private String orgReviewName;
        @JsonFormat(pattern = BaseConstants.COMMON_DATETIME_FORMAT, locale = BaseConstants.LOCALE_VN, timezone = BaseConstants.TIMEZONE_VN)
        private Date approvedTime;
        private String reason;
        private String reasonRequest;
        private String reasonManageRequest;
        private String managerGrade;
        private List<ObjectAttributesResponse> listAttributes;
    }

    @Data
    @NoArgsConstructor
    @Schema(name = "EmployeeEvaluationsResponseDetailBean")
    public static class DetailBean {
        private List<EmployeeIndicatorsResponse.EmployeeEvaluation> listData;
        private String adjustReason;
    }

    @Data
    @NoArgsConstructor
    @Schema(name = "EmployeeEvaluationsResponseEvaluateBeanResult")
    public static class EvaluateBeanResult {
        private List<EvaluateBean> listData;
        private List<EmpData> listEmp;
    }

    @Data
    @NoArgsConstructor
    @Schema(name = "EmployeeEvaluationsResponseEvaluateBean")
    public static class EvaluateBean {
        private Long indicatorConversionId;
        private String conversionType;
        private Long indicatorId;
        private Long employeeEvaluationId;
        private String indicatorName;
        private Double percent;
        private String target;
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
        private String ratingType;
        private String listValues;
        private String isFocusReduction;
        private List<IndicatorConversionsResponse.ConversionDetail> conversions = new ArrayList<>();
        private List<EmpBean> listEmp;
    }

    @Data
    @NoArgsConstructor
    @Schema(name = "EmployeeEvaluationsResponseEmpBean")
    public static class EmpBean {
        private Long employeeEvaluationId;
        private Long employeeIndicatorId;
        private Long employeeId;
        private String result;
        private String resultManage;
        private Long selfPoint;
        private Long managePoint;
        private String status;
        private String employeeName;
        private Long evaluationPeriodYear;
        private String jobName;
        private Double selfTotalPoint;
        private Double managerTotalPoint;
        private String isHeadLv2;
        private Long evaluationPeriodId;
    }

    @Data
    @NoArgsConstructor
    @Schema(name = "EmployeeEvaluationsResponseEmpData")
    public static class EmpData {
        private String employeeName;
        private Long employeeId;
    }


    @Data
    @NoArgsConstructor
    @Schema(name = "EmployeeEvaluationsResponseValidate")
    public static class Validate {
        private boolean adjust;
        private boolean adjustKHCT;
        private List<ObjectAttributesResponse> listAttributes;
    }

    @Data
    @NoArgsConstructor
    @Schema(name = "EmployeeEvaluationsResponseErrorData")
    public static class ErrorData {
        private String employeeName;
        private String employeeCode;
        private Double beforeResult;
        private Double afterResult;
    }

}
