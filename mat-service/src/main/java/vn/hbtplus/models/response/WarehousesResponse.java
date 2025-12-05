/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.models.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.hbtplus.constants.BaseConstants;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 * Lop Response DTO ung voi bang mat_warehouses
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Data
@NoArgsConstructor
@JsonInclude(Include.NON_NULL)
public class WarehousesResponse {

    private Long warehouseId;
    private String code;
    private String name;
    private Long parentId;
    private String parentName;
    private String address;
    private Long departmentId;
    private String departmentName;
    private String type;
    private String typeName;
    private String statusId;
    private String managerName;
    private String note;
    private List<warehouseEmployeeDTO> listEmployee;
    private List<warehouseEquipmentDTO> listEquipment;
    private List<warehouseIncomingShipmentDTO> listIncomingShipment;
    private List<warehouseOutgoingShipmentDTO> listOutgoingShipment;
    private List<warehouseInventoryAdjustmentDTO> listInventoryAdjustment;
    private String createdBy;

    @JsonFormat(pattern = BaseConstants.COMMON_DATETIME_FORMAT, locale = BaseConstants.LOCALE_VN, timezone = BaseConstants.TIMEZONE_VN)
    private Date createdTime;
    private String modifiedBy;

    @JsonFormat(pattern = BaseConstants.COMMON_DATETIME_FORMAT, locale = BaseConstants.LOCALE_VN, timezone = BaseConstants.TIMEZONE_VN)
    private Date modifiedTime;

    private List<WarehousesResponse> children;

    public void addChild(WarehousesResponse item) {
        if (children == null) {
            children = new ArrayList<>();
        }
        children.add(item);
    }


    public Long getKey() {
        return warehouseId;
    }


    @Data
    @NoArgsConstructor
    public static class warehouseEmployeeDTO {
        private Long employeeId;
        private String fullName;
        private String employeeCode;
        private String email;
        private String mobileNumber;
        private String organizationName;
        private String positionName;
        private Boolean isManager;
        private Boolean hasApproveImport;

        private Boolean hasApproveTransfer;

        private Boolean hasApproveExport;

        private Boolean hasApproveAdjustment;

    }

    @Data
    @NoArgsConstructor
    public static class warehouseEquipmentDTO {
        private Long warehouseEquipmentId;
        private String code;
        private String name;
        private Long equipmentId;
        private Long warehouseId;
        private String equipmentTypeName;
        private String equipmentUnitName;
        private Double quantity;
        private Double unitPrice;

    }

    @Data
    @NoArgsConstructor
    public static class warehouseIncomingShipmentDTO {
        private Long incomingShipmentId;
        private String pickingNo;
        @JsonFormat(pattern = BaseConstants.COMMON_DATE_FORMAT, locale = BaseConstants.LOCALE_VN, timezone = BaseConstants.TIMEZONE_VN)
        private Date incomingDate;
        private String pickingEmployeeName;
        private String statusId;
        private String statusName;
        private String type;
        private String typeName;
        private String approvedBy;
        private String approvedName;
        @JsonFormat(pattern = BaseConstants.COMMON_DATE_FORMAT, locale = BaseConstants.LOCALE_VN, timezone = BaseConstants.TIMEZONE_VN)
        private Date approvedTime;
    }

    @Data
    @NoArgsConstructor
    public static class warehouseOutgoingShipmentDTO {
        private Long outgoingShipmentId;
        private String pickingNo;
        @JsonFormat(pattern = BaseConstants.COMMON_DATE_FORMAT, locale = BaseConstants.LOCALE_VN, timezone = BaseConstants.TIMEZONE_VN)
        private Date outgoingDate;
        private String pickingEmployeeName;
        private String statusId;
        private String statusName;
        private String type;
        private String typeName;
        private String approvedBy;
        private String approvedName;
        @JsonFormat(pattern = BaseConstants.COMMON_DATE_FORMAT, locale = BaseConstants.LOCALE_VN, timezone = BaseConstants.TIMEZONE_VN)
        private Date approvedTime;
    }

    @Data
    @NoArgsConstructor
    public static class warehouseInventoryAdjustmentDTO {
        private Long inventoryAdjustmentId;
        private String inventoryAdjustmentNo;
        private String type;
        private String typeName;
        private String statusId;
        private String statusName;
        @JsonFormat(pattern = BaseConstants.COMMON_DATE_FORMAT, locale = BaseConstants.LOCALE_VN, timezone = BaseConstants.TIMEZONE_VN)
        private Date startDate;
        @JsonFormat(pattern = BaseConstants.COMMON_DATE_FORMAT, locale = BaseConstants.LOCALE_VN, timezone = BaseConstants.TIMEZONE_VN)
        private Date endDate;
        private String note;
        private String approvedName;
        @JsonFormat(pattern = BaseConstants.COMMON_DATE_FORMAT, locale = BaseConstants.LOCALE_VN, timezone = BaseConstants.TIMEZONE_VN)
        private Date approvedTime;
    }




}
