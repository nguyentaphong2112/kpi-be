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
 * Lop Response DTO ung voi bang pit_tax_declare_details
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Data
@NoArgsConstructor
@JsonInclude(Include.NON_NULL)
public class TaxDeclareDetailsResponse {

    private Long taxDeclareDetailId;

    @JsonFormat(pattern = BaseConstants.COMMON_DATE_FORMAT, locale = BaseConstants.LOCALE_VN, timezone = BaseConstants.TIMEZONE_VN)
    private Date taxPeriodDate;
    private Long taxDeclareMasterId;
    private String empCode;
    private String fullName;
    private String taxNo;
    private String workOrgName;
    private String personalIdNo;
    private String empTypeCode;
    private String taxMethod;
    private Long incomeTaxable;
    private Long incomeFreeTax;
    private Integer numOfDependents;
    private Long dependentDeduction;
    private Long insuranceDeduction;
    private Long otherDeduction;
    private Long incomeAmount;
    private String note;
    private String createdBy;

    @JsonFormat(pattern = BaseConstants.COMMON_DATETIME_FORMAT, locale = BaseConstants.LOCALE_VN, timezone = BaseConstants.TIMEZONE_VN)
    private Date createdTime;
    private String modifiedBy;

    @JsonFormat(pattern = BaseConstants.COMMON_DATETIME_FORMAT, locale = BaseConstants.LOCALE_VN, timezone = BaseConstants.TIMEZONE_VN)
    private Date modifiedTime;
    private Long incomeTax;
    private Long monthRetroTax;
    private Long taxPayable;
    private Long taxCollected;
    private Long declareOrgId;
    private String posName;
    private String orgName;
    private String orgCode;

    private Integer countEmp;
    private Long countIncomeTaxable;
    private Long countDeduction;
    private Long countIncomeTax;
    private Long countTaxCollected;
    private Long countMonthRetroTax;

}
