package vn.hbtplus.models.request;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class StockEquipmentsRequest {
    private Long equipmentId;

    private Double quantity;

    private Long unitPrice;

    private Double total;

    private Double inventoryQuantity;
}
