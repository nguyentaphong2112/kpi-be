/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.models.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.util.Date;
import javax.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.utils.StrimDeSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import vn.hbtplus.models.BaseSearchRequest;

/**
 * Lop DTO ung voi bang kpi_organization_work_plannings
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Data
@NoArgsConstructor
public class OrganizationWorkPlanningsRequest extends BaseSearchRequest {

    @Data
    @NoArgsConstructor
    @Schema(name = "OrganizationWorkPlanningsRequestSubmitForm")
    public static class SubmitForm {
        private Long organizationWorkPlanningId;

        private Long empManagerId;

        private Long organizationEvaluationId;
        private String isEvaluate;
        @NotBlank
        private String content;

        private String adjustReason;

    }

    @Data
    @NoArgsConstructor
    @Schema(name = "OrganizationWorkPlanningsRequestSearchForm")
    public static class SearchForm extends BaseSearchRequest{

        private Long organizationWorkPlanningId;

        private Long organizationEvaluationId;

        @NotBlank
        private String content;

    }

    @Data
    @NoArgsConstructor
    @Schema(name = "OrganizationWorkPlanningsRequestStatus")
    public static class Status extends BaseSearchRequest {
        private Long organizationWorkPlanningId;
        @JsonDeserialize(using = StrimDeSerializer.class)
        private String status;
        @JsonDeserialize(using = StrimDeSerializer.class)
        private String content;
        @JsonDeserialize(using = StrimDeSerializer.class)
        private String reason;
        @JsonDeserialize(using = StrimDeSerializer.class)
        private String reasonRequest;
    }
}
