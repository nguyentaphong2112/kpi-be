/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.kpi.models.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.util.Date;
import java.util.List;
import javax.validation.constraints.*;

import lombok.Data;
import lombok.NoArgsConstructor;
import vn.kpi.constants.BaseConstants;
import vn.kpi.utils.StrimDeSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import vn.kpi.models.BaseSearchRequest;

/**
 * Lop DTO ung voi bang kpi_organization_evaluations
 *
 * @author tudd
 * @version 1.0
 * @since 1.0
 */

@Data
@NoArgsConstructor
public class OrganizationEvaluationsRequest extends BaseSearchRequest {

    @Data
    @NoArgsConstructor
    @Schema(name = "OrganizationEvaluationsRequestSubmitForm")
    public static class SubmitForm {
        private Long organizationEvaluationId;

        private List<OrganizationIndicatorsRequest.SubmitForm> organizationIndicatorList;
        private String isEvaluate;
        private Long empManagerId;
        private String isEvaluateManage;
        private String adjustReason;
        private Double selfTotalPoint;
        private Double managerTotalPoint;
        private Boolean isLevel1;
//        private List<OrganizationWorkPlanningsRequest.SubmitForm> workPlanningList;

    }

    @Data
    @NoArgsConstructor
    @Schema(name = "OrganizationEvaluationsRequestSearchForm")
    public static class SearchForm extends BaseSearchRequest {

        private String keySearch;

        private Long organizationEvaluationId;

        private Long organizationId;

        private Long evaluationPeriodId;

        private Long year;
        @Size(max = 9)
        @JsonDeserialize(using = StrimDeSerializer.class)
        private String status;

        private List<String> groupCodes;

        private String groupCode;

        private List<String> statusList;
        private List<String> orgTypeIdList;
        private Long empManagerId;
        private String approvedBy;
        @JsonFormat(pattern = BaseConstants.COMMON_DATETIME_FORMAT, locale = BaseConstants.LOCALE_VN, timezone = BaseConstants.TIMEZONE_VN)
        private Date approvedTime;

        private String isEvaluation;
        private String isSynthetic;
        private List<Long> listId;

        private List<String> orgIdList;

        private String type;

        private String level;

        private List<Long> tableColumns;

        List<String> orgIds;
    }

    @Data
    @NoArgsConstructor
    @Schema(name = "OrganizationEvaluationsRequestOrgSummarySubmitForm")
    public static class OrgSummarySubmitForm {
        private Long organizationEvaluationId;
        private String finalResultId;
        private Double finalPoint;
        private String resultId;

    }

    @Data
    @NoArgsConstructor
    @Schema(name = "OrganizationEvaluationsRequestRejectDto")
    public static class RejectDto {
        private List<Long> listId;
        private List<OrganizationIndicatorsRequest.SubmitForm> listData;
        private Long id;
        private String isConfirm;
        private String rejectReason;
    }

    @Data
    @NoArgsConstructor
    @Schema(name = "OrganizationEvaluationsRequestIndicatorSubmitForm")
    public static class IndicatorSubmitForm {
        private List<OrganizationIndicatorsRequest.SubmitForm> organizationIndicatorList;
        private String isEvaluate;
        private Long empManagerId;
        private String isEvaluateManage;
        private String adjustReason;
        private Double selfTotalPoint;
        private Double managerTotalPoint;
    }

    @Data
    @NoArgsConstructor
    @Schema(name = "OrganizationEvaluationsRequestWorkPlanningSubmitForm")
    public static class WorkPlanningSubmitForm {
        private Long organizationWorkPlanningId;

        private Long organizationEvaluationId;

        @NotBlank
        private String content;
    }

    @Data
    @NoArgsConstructor
    @Schema(name = "OrganizationEvaluationsRequestReview")
    public static class Review {
        private List<Long> ids;
        @JsonDeserialize(using = StrimDeSerializer.class)
        private String comment;
    }

    @Data
    @NoArgsConstructor
    @Schema(name = "OrganizationEvaluationsRequestOrgParent")
    public static class OrgParent extends BaseSearchRequest {
        private Long orgParentId;
        private Long evaluationPeriodId;
    }
}
