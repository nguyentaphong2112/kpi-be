package vn.hbtplus.models.bean;

import lombok.Data;
import lombok.NoArgsConstructor;
import vn.hbtplus.utils.Utils;

import java.util.Date;

@Data
@NoArgsConstructor
public class StkEquipmentBean implements Comparable<StkEquipmentBean> {
    private Long departmentId;
    private Long warehouseId;
    private Long equipmentTypeId;
    private Long equipmentUnitId;
    private String departmentName;
    private String warehouseName;
    private String equipmentTypeName;
    private String equipmentUnitName;
    private String equipmentName;
    private String equipmentCode;
    private Long equipmentId;
    private Double quantity;
    private Date pickingDate;
    private Date approvedTime;
    private String pickingNo;
    private String type;
    private String typeName;
    private String isIncoming;
    private Double amountMoney;

    public String getKey() {
        return Utils.NVL(this.departmentId)
                + "-" + Utils.NVL(this.warehouseId)
                + "-" + Utils.NVL(this.equipmentTypeId)
                + "-" + Utils.NVL(this.equipmentUnitId)
                + "-" + Utils.NVL(this.equipmentName);
    }

    public void add(StkEquipmentBean item) {
        this.quantity = Utils.NVL(this.quantity) + Utils.NVL(item.getQuantity());
        this.amountMoney = Utils.NVL(this.amountMoney) + Utils.NVL(item.getAmountMoney());
    }

    public void remove(StkEquipmentBean item) {
        this.quantity = Utils.NVL(this.quantity) - Utils.NVL(item.getQuantity());
        this.amountMoney = Utils.NVL(this.amountMoney) - Utils.NVL(item.getAmountMoney());
    }

    // Override compareTo để so sánh theo độ tuổi
    @Override
    public int compareTo(StkEquipmentBean other) {
        if (this.pickingDate == null && other.pickingDate == null) {
            return 0;
        } else if (this.pickingDate == null) {
            return 1;
        } else if (other.pickingDate == null) {
            return -1;
        } else if (this.pickingDate.compareTo(other.pickingDate) == 0) {
            if (this.approvedTime == null && other.approvedTime == null) {
                return 0;
            } else if (this.approvedTime == null) {
                return 1;
            } else if (other.approvedTime == null) {
                return -1;
            }
            return this.approvedTime.compareTo(other.approvedTime);
        } else {
            return this.pickingDate.compareTo(other.pickingDate);
        }
    }
}
