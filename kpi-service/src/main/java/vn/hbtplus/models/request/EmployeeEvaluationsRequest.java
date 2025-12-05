/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.models.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.util.Date;
import java.util.List;
import javax.persistence.Column;
import javax.validation.constraints.*;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.models.AttributeRequestDto;
import vn.hbtplus.models.response.EmployeeEvaluationsResponse;
import vn.hbtplus.utils.StrimDeSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import vn.hbtplus.models.BaseSearchRequest;

/**
 * Lop DTO ung voi bang kpi_employee_evaluations
 *
 * @author tudd
 * @version 1.0
 * @since 1.0
 */

@Data
@NoArgsConstructor
public class EmployeeEvaluationsRequest extends BaseSearchRequest {

    @Data
    @NoArgsConstructor
    @Schema(name = "EmployeeEvaluationsRequestSubmitForm")
    public static class SubmitForm {
        private Long employeeEvaluationId;

        @NotEmpty
        private List<EmployeeIndicatorsRequest.SubmitForm> employeeIndicatorList;
        private String isEvaluate;
        private String isEvaluateManage;
        private String adjustReason;
        private String finalResultId;
        private Double selfTotalPoint;
        private Double managerTotalPoint;
        private Double finalPoint;
        private List<EmployeeWorkPlanningsRequest.SubmitForm> workPlanningList;
        List<AttributeRequestDto> listAttributes;

    }

    @Data
    @NoArgsConstructor
    @Schema(name = "EmployeeEvaluationsRequestEmpSummarySubmitForm")
    public static class EmpSummarySubmitForm {
        private Long employeeEvaluationId;
        private String finalResultId;
        private Double finalPoint;
        private String resultId;

    }

    @Data
    @NoArgsConstructor
    @Schema(name = "EmployeeEvaluationsRequestSearchForm")
    public static class SearchForm extends BaseSearchRequest {

        private String keySearch;

        private Long employeeEvaluationId;

        private Long evaluationPeriodId;

        private Long organizationId;

        private Long employeeId;
        private Long year;
        @Size(max = 9)
        @JsonDeserialize(using = StrimDeSerializer.class)
        private String status;

        private List<String> statusList;

        private String isEvaluation;
        private String isSynthetic;
        private List<Long> listId;

    }

    @Data
    @NoArgsConstructor
    @Schema(name = "EmployeeEvaluationsRequestIndicatorSubmitForm")
    public static class IndicatorSubmitForm {
        @NotEmpty
        private List<EmployeeIndicatorsRequest.SubmitForm> employeeIndicatorList;
        private String isEvaluate;
        private String isEvaluateManage;
        private String adjustReason;
        private Double selfTotalPoint;
        private Double managerTotalPoint;
    }

    @Data
    @NoArgsConstructor
    @Schema(name = "EmployeeEvaluationsRequestEvaluate")
    public static class Evaluate {
        private List<EmployeeEvaluationsResponse.EmpBean> listData;
        private List<Total> listSum;
    }

    @Data
    @NoArgsConstructor
    @Schema(name = "EmployeeEvaluationsRequestTotal")
    public static class Total {
        private Long employeeEvaluationId;
        private Double totalSelfPoint;
        private Double totalManagePoint;
    }

    @Data
    @NoArgsConstructor
    @Schema(name = "EmployeeEvaluationsRequestReview")
    public static class Review {
        private List<Long> ids;
        @JsonDeserialize(using = StrimDeSerializer.class)
        private String comment;
    }
}
