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

import java.util.Date;


/**
 * Lop Response DTO ung voi bang fpn_equipments
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Data
@NoArgsConstructor
@JsonInclude(Include.NON_NULL)
public class EquipmentsResponse {

    private Long equipmentId;
    private String name;
    private String equipmentGroupId;
    private String equipmentTypeId;
    private String equipmentUnitId;
    private Long warningDays;
    private String isSerialChecking;
    private String serialNo;
    private Long unitPrice;
    private String note;
    private String createdBy;
    @JsonFormat(pattern = BaseConstants.COMMON_DATE_FORMAT, locale = BaseConstants.LOCALE_VN, timezone = BaseConstants.TIMEZONE_VN)
    private Date createdTime;
    private String modifiedBy;
    @JsonFormat(pattern = BaseConstants.COMMON_DATE_FORMAT, locale = BaseConstants.LOCALE_VN, timezone = BaseConstants.TIMEZONE_VN)
    private Date modifiedTime;
    private String code;
    private String description;
    private String location;
    private Double inventoryQuantity;
    private String equipmentGroupName;
    private String equipmentTypeName;
    private String equipmentUnitName;
}
