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
import vn.hbtplus.tax.income.constants.Constant;

import java.util.Date;


/**
 * Lop Response DTO ung voi bang pit_income_item_masters
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Data
@NoArgsConstructor
@JsonInclude(Include.NON_NULL)
public class IncomeItemMastersResponse {

    private Long incomeItemMasterId;
    private Long incomeItemId;

    @JsonFormat(pattern = Constant.SHORT_FORMAT_DATE, locale = BaseConstants.LOCALE_VN, timezone = BaseConstants.TIMEZONE_VN)
    private Date taxPeriodDate;
    @JsonFormat(pattern = Constant.SHORT_FORMAT_DATE, locale = BaseConstants.LOCALE_VN, timezone = BaseConstants.TIMEZONE_VN)
    private Date salaryPeriodDate;
    private Long isTaxCalculated;
    private String status;
    private Long inputTimes;
    private String createdBy;

    @JsonFormat(pattern = BaseConstants.COMMON_DATETIME_FORMAT, locale = BaseConstants.LOCALE_VN, timezone = BaseConstants.TIMEZONE_VN)
    private Date createdTime;
    private String modifiedBy;

    @JsonFormat(pattern = BaseConstants.COMMON_DATETIME_FORMAT, locale = BaseConstants.LOCALE_VN, timezone = BaseConstants.TIMEZONE_VN)
    private Date modifiedTime;

    private String itemName;
    private String typeName;
    private String itemCode;
    private String statusName;
    private Long totalIncome;
    private Long totalInsuranceDeduction;
    private Long totalIncomeTaxable;
    private Long totalIncomeFreeTax;
    private Long totalIncomeTax;
    private Long totalReceived;
    private Long totalMonthRetroTax;
    private Long totalYearRetroTax;
    private String taxCalBy;
    @JsonFormat(pattern = BaseConstants.COMMON_DATETIME_FORMAT, locale = BaseConstants.LOCALE_VN, timezone = BaseConstants.TIMEZONE_VN)
    private Date taxDate;
}
