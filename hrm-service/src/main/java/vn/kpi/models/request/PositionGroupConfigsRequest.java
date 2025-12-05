/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.kpi.models.request;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import javax.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.kpi.utils.StrimDeSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import vn.kpi.models.BaseSearchRequest;

/**
 * Lop DTO ung voi bang hr_position_group_configs
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Data
@NoArgsConstructor
public class PositionGroupConfigsRequest extends BaseSearchRequest {

    @Data
    @NoArgsConstructor
    @Schema(name = "PositionGroupConfigsSubmitForm")
    public static class SubmitForm {
        private Long positionGroupConfigId;

        private Long positionGroupId;

        @Size(max = 50)
        @JsonDeserialize(using = StrimDeSerializer.class)
        private String orgTypeId;

        private Long organizationId;

        private Long jobId;

    }

    @Data
    @NoArgsConstructor
    @Schema(name = "PositionGroupConfigsSearchForm")
    public static class SearchForm extends BaseSearchRequest{

        private Long positionGroupConfigId;

        private Long positionGroupId;

        @Size(max = 50)
        @JsonDeserialize(using = StrimDeSerializer.class)
        private String orgTypeId;

        private Long organizationId;

        private Long jobId;

    }
}
