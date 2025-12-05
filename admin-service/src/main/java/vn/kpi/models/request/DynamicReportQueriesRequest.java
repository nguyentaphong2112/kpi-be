/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.kpi.models.request;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.kpi.models.BaseSearchRequest;
import vn.kpi.utils.StrimDeSerializer;

import javax.validation.constraints.Size;

/**
 * Lop DTO ung voi bang sys_dynamic_report_queries
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Data
@NoArgsConstructor
public class DynamicReportQueriesRequest extends BaseSearchRequest {

    @Data
    @NoArgsConstructor
    @Schema(name = "DynamicReportQueriesSubmitForm")
    public static class SubmitForm {
        private Long dynamicReportQueryId;

        private Long dynamicReportId;

        private Long orderNumber;

        @Size(max = 2000)
        @JsonDeserialize(using = StrimDeSerializer.class)
        private String sqlQuery;

    }

    @Data
    @NoArgsConstructor
    @Schema(name = "DynamicReportQueriesSearchForm")
    public static class SearchForm extends BaseSearchRequest{

        private Long dynamicReportQueryId;

        private Long dynamicReportId;

        private Long orderNumber;

        @Size(max = 2000)
        @JsonDeserialize(using = StrimDeSerializer.class)
        private String sqlQuery;

    }
}
