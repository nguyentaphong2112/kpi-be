/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.tax.income.models.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.hbtplus.constants.BaseConstants;

import java.util.Date;


/**
 * Lop Response DTO ung voi bang pit_income_item_details
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Data
@NoArgsConstructor
@JsonInclude(Include.NON_NULL)
public class IncomeItemDetailsResponse {

    private Long incomeItemDetailId;
    private Long incomeItemMasterId;

    @JsonFormat(pattern = BaseConstants.COMMON_DATE_FORMAT, locale = BaseConstants.LOCALE_VN, timezone = BaseConstants.TIMEZONE_VN)
    private Date taxPeriodDate;
    private String empCode;
    private String fullName;
    private String taxNo;
    private String extraCode;
    private String personalIdNo;
    private String personalIdPlace;

    @JsonFormat(pattern = BaseConstants.COMMON_DATE_FORMAT, locale = BaseConstants.LOCALE_VN, timezone = BaseConstants.TIMEZONE_VN)
    private Date personalIdDate;
    private Long incomeTaxable;
    private Long incomeFreeTax;
    private Long incomeTax;
    private Long monthRetroTax;
    private Long yearRetroTax;
    private Long orderNumber;
    private String note;


}
