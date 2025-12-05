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
 * Lop DTO ung voi bang stk_transferring_equipments
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Data
@NoArgsConstructor
public class TransferringEquipmentsRequest extends BaseSearchRequest {

    @Data
    @NoArgsConstructor
    @Schema(name = "TransferringEquipmentsSubmitForm")
    public static class SubmitForm {
        private Long transferringEquipmentId;

        private Long equipmentId;

        private Double quantity;

        private Double unitPrice;

        private Long transferringShipmentId;

    }

    @Data
    @NoArgsConstructor
    @Schema(name = "TransferringEquipmentsSearchForm")
    public static class SearchForm extends BaseSearchRequest{

        private Long transferringEquipmentId;

        private Long equipmentId;

        private Double quantity;

        private Double unitPrice;

        private Long transferringShipmentId;

    }
}
