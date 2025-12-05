/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.kpi.models.request;

import lombok.Data;
import lombok.NoArgsConstructor;
import io.swagger.v3.oas.annotations.media.Schema;
import vn.kpi.models.BaseSearchRequest;

/**
 * Lop DTO ung voi bang kpi_indicator_using_scopes
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Data
@NoArgsConstructor
public class IndicatorUsingScopesRequest extends BaseSearchRequest {

    @Data
    @NoArgsConstructor
    @Schema(name = "KpiIndicatorUsingScopesSubmitForm")
    public static class SubmitForm {
        private Long indicatorUsingId;

        private Long indicatorId;

        private Long organizationId;

        private Long positionId;

        private Long jobId;

    }

    @Data
    @NoArgsConstructor
    @Schema(name = "KpiIndicatorUsingScopesSearchForm")
    public static class SearchForm extends BaseSearchRequest{

        private Long indicatorUsingId;

        private Long indicatorId;

        private Long organizationId;

        private Long positionId;

        private Long jobId;

    }
}
