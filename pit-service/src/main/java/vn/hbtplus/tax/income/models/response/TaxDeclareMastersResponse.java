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
 * Lop Response DTO ung voi bang pit_tax_declare_masters
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Data
@NoArgsConstructor
@JsonInclude(Include.NON_NULL)
public class TaxDeclareMastersResponse {

    private Long taxDeclareMasterId;

    @JsonFormat(pattern = BaseConstants.SHORT_DATE_FORMAT, locale = BaseConstants.LOCALE_VN, timezone = BaseConstants.TIMEZONE_VN)
    private Date taxPeriodDate;
    private Long totalIncome;
    private Long totalIncomeTaxable;
    private Long totalIncomeFreeTax;
    private Long totalInsuranceDeduction;
    private Long totalOtherDeduction;
    private Long totalIncomeTax;
    private Long totalTaxCollected;
    private Long totalTaxPayable;
    private Long totalMonthRetroTax;
    private Long totalDeduction;
    private Long totalTaxpayers;
    private String createdBy;

    @JsonFormat(pattern = BaseConstants.COMMON_DATETIME_FORMAT, locale = BaseConstants.LOCALE_VN, timezone = BaseConstants.TIMEZONE_VN)
    private Date createdTime;
    private String modifiedBy;

    @JsonFormat(pattern = BaseConstants.COMMON_DATETIME_FORMAT, locale = BaseConstants.LOCALE_VN, timezone = BaseConstants.TIMEZONE_VN)
    private Date modifiedTime;

    private String strTaxPeriodDate;
    private String strCreatedTime;
    private String statusName;
    private String status;
    private String inputType;

}
