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
import java.util.List;


/**
 * Lop Response DTO ung voi bang crm_orders
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Data
@NoArgsConstructor
@JsonInclude(Include.NON_NULL)
public class OrdersResponse {

    private Long orderId;
    private String orderNo;
    private Long customerId;

    private Long introducerId;

    private Long caregiverId;

    @JsonFormat(pattern = BaseConstants.COMMON_DATE_FORMAT, locale = BaseConstants.LOCALE_VN, timezone = BaseConstants.TIMEZONE_VN)
    private Date orderDate;
    private Long totalAmount;
    private Long collectedAmount;
    private Long remainingAmount;
    private Long discountAmount;
    private String discountCode;
    private Long finalAmount;
    private String createdBy;

    @JsonFormat(pattern = BaseConstants.COMMON_DATETIME_FORMAT, locale = BaseConstants.LOCALE_VN, timezone = BaseConstants.TIMEZONE_VN)
    private Date createdTime;
    private String modifiedBy;

    @JsonFormat(pattern = BaseConstants.COMMON_DATETIME_FORMAT, locale = BaseConstants.LOCALE_VN, timezone = BaseConstants.TIMEZONE_VN)
    private Date modifiedTime;

    List<OrderDetailsResponse> orderDetails;
    private String fullName;
    private String mobileNumber;
    private Long taxAmount;
    private Long referralFee;
    private Long careFee;
    private Long welfareFee;
    private Long taxRate;
    private Long saleStaffId;
    private String provinceId;
    private String districtId;
    private String wardId;
    private String villageAddress;
    private List<ObjectAttributesResponse> listAttributes;
    private List<PaymentsResponse> payments;
}
