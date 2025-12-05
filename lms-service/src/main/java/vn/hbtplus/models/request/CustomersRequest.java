/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.models.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.hbtplus.models.BaseSearchRequest;

/**
 * Lop DTO ung voi bang crm_customers
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Data
@NoArgsConstructor
public class CustomersRequest extends BaseSearchRequest {

    @Data
    @NoArgsConstructor
    @Schema(name = "CustomersReportForm")
    public static class ReportForm {
        private Long startDate;
        private Long endDate;
        private Long organizationId;
        private String keySearch;

    }
}