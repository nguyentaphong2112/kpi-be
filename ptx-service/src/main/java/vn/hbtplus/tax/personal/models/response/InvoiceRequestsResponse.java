/*
 * Copyright (C) 2022 EcoIT. All rights reserved.
 * EcoIT. Use is subject to license terms.
 */
package vn.hbtplus.tax.personal.models.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import java.util.Date;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.hbtplus.constants.BaseConstants;

/**
 * Lop Response DTO ung voi bang PTX_INVOICE_REQUESTS
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Data
@NoArgsConstructor
@JsonInclude(Include.NON_NULL)
public class InvoiceRequestsResponse extends EmployeeInfoResponse{

    private Long invoiceRequestId;
    private Integer year;
    private Long employeeId;
    private String employeeCode;
    private String taxNo;
    private String idNo;
    private String reason;
    private Integer status;
    private String email;
    private Integer flagStatus;
    private String createdBy;
    private String note;

    @JsonFormat(pattern = BaseConstants.COMMON_DATE_FORMAT, locale = BaseConstants.LOCALE_VN, timezone = BaseConstants.TIMEZONE_VN)
    private Date createdTime;
    private String modifiedBy;

    @JsonFormat(pattern = BaseConstants.COMMON_DATE_FORMAT, locale = BaseConstants.LOCALE_VN, timezone = BaseConstants.TIMEZONE_VN)
    private Date modifiedTime;

    private String orgManageName;
    private Long orgId;
    private Long orgManageId;
    private String fullName;
    private Integer invoiceStatus;


}
