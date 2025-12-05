/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.models.request;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.hbtplus.models.BaseSearchRequest;
import vn.hbtplus.utils.StrimDeSerializer;

import javax.validation.constraints.Size;

/**
 * Lop DTO ung voi bang stk_warehouse_managers
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Data
@NoArgsConstructor
public class WarehouseManagersRequest extends BaseSearchRequest {

    @Data
    @NoArgsConstructor
    @Schema(name = "WarehouseManagersSubmitForm")
    public static class SubmitForm {
        private Long warehouseManagerId;

        private Long employeeId;

        private Long warehouseId;

        @Size(max = 9)
        @JsonDeserialize(using = StrimDeSerializer.class)
        private String roleId;

    }

    @Data
    @NoArgsConstructor
    @Schema(name = "WarehouseManagersSearchForm")
    public static class SearchForm extends BaseSearchRequest{

        private Long warehouseManagerId;

        private Long employeeId;

        private Long warehouseId;

        @Size(max = 9)
        @JsonDeserialize(using = StrimDeSerializer.class)
        private String roleId;

    }
}
