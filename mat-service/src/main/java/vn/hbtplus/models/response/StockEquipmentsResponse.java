package vn.hbtplus.models.response;

import lombok.Data;

@Data
public class StockEquipmentsResponse {
    private Long equipmentId;
    private String equipmentTypeId;
    private String equipmentCode;
    private String equipmentName;
    private String equipmentTypeName;
    private String equipmentUnitName;

    private Double quantity;
    private Double inventoryQuantity;
    private Double unitPrice;
}
