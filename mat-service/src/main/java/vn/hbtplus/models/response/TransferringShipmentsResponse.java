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
import vn.hbtplus.models.AttachmentFileDto;
import vn.hbtplus.models.dto.AttachmentDto;

import java.util.Date;
import java.util.List;


/**
 * Lop Response DTO ung voi bang stk_transfering_shipments
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Data
@NoArgsConstructor
@JsonInclude(Include.NON_NULL)
public class TransferringShipmentsResponse {

    private Long transferringShipmentId;
    private Long warehouseId;

    @JsonFormat(pattern = BaseConstants.COMMON_DATE_FORMAT, locale = BaseConstants.LOCALE_VN, timezone = BaseConstants.TIMEZONE_VN)
    private Date transferringDate;
    private Long receivedWarehouseId;
    private String receivedWarehouseName;
    private Long transferredEmployeeId;
    private String transferredWarehouseName;
    private String approvedBy;
    private String approvedName;
    private Long approvedId;
    private String approvedNote;

    @JsonFormat(pattern = BaseConstants.COMMON_DATE_FORMAT, locale = BaseConstants.LOCALE_VN, timezone = BaseConstants.TIMEZONE_VN)
    private Date approvedTime;
    private String createdBy;

    @JsonFormat(pattern = BaseConstants.COMMON_DATETIME_FORMAT, locale = BaseConstants.LOCALE_VN, timezone = BaseConstants.TIMEZONE_VN)
    private Date createdTime;
    private String modifiedBy;

    @JsonFormat(pattern = BaseConstants.COMMON_DATETIME_FORMAT, locale = BaseConstants.LOCALE_VN, timezone = BaseConstants.TIMEZONE_VN)
    private Date modifiedTime;

    private String pickingNo;
    private String statusId;
    private String statusName;
    private String name;
    private Long receivedEmployeeId;
    private String receivedEmployeeFullName;
    private String receivedEmployeeCode;
    private Long createdEmployeeId;
    private String createdEmployeeFullName;
    private String createdEmployeeCode;
    private String note;
    private String transferredEmployeeFullName;
    private String transferredEmployeeCode;
    private String incomingPickingNo;
    private String outgoingPickingNo;
    private List<TransferringEquipmentsResponse> listEquipments;
    private List<AttachmentFileDto> files;
    private String hasApproveTransfer;
}
