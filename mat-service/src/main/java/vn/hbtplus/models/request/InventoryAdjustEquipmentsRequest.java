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
 * Lop DTO ung voi bang stk_inventory_adjust_equipments
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Data
@NoArgsConstructor
public class InventoryAdjustEquipmentsRequest extends BaseSearchRequest {

    @Data
    @NoArgsConstructor
    @Schema(name = "InventoryAdjustEquipmentsSubmitForm")
    public static class SubmitForm {
        private Long inventoryAdjustEquipmentId;

        private Long equipmentId;

        private Double quantity;

        private Double unitPrice;

        private Long inventoryAjustmentId;

    }

    @Data
    @NoArgsConstructor
    @Schema(name = "InventoryAdjustEquipmentsSearchForm")
    public static class SearchForm extends BaseSearchRequest{

        private Long inventoryAdjustEquipmentId;

        private Long equipmentId;

        private Double quantity;

        private Double unitPrice;

        private Long inventoryAjustmentId;

    }
}
