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
 * Lop Response DTO ung voi bang pit_tax_settlement_details
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Data
@NoArgsConstructor
@JsonInclude(Include.NON_NULL)
public class TaxSettlementDetailsResponse {

    private Long taxSettlementDetailId;
    private Long year;
    private String empCode;
    private String fullName;
    private String taxNo;
    private String personalIdNo;
    private String createdBy;

    @JsonFormat(pattern = BaseConstants.COMMON_DATETIME_FORMAT, locale = BaseConstants.LOCALE_VN, timezone = BaseConstants.TIMEZONE_VN)
    private Date createdTime;
    private String modifiedBy;

    @JsonFormat(pattern = BaseConstants.COMMON_DATETIME_FORMAT, locale = BaseConstants.LOCALE_VN, timezone = BaseConstants.TIMEZONE_VN)
    private Date modifiedTime;

    private Long sumIncomeTaxable;
    private Long sumTaxCollected;
    private Long sumInsuranceDeduction;
    private Long sumTotalIncomeTaxable;
    private Long sumDeduction;
    private Integer countNumOfDependents;
    private Long sumTotalInsuranceDeduction;
    private Long sumTotalTaxCollected;
    private Long sumTotalIncomeTax;
    private Long sumTotalTaxPayed;
    private Integer countEmpIn;
    private Integer countEmpOut;
    private Long sumTotalTaxPayedSubmitted;
    private Long sumTotalTaxPayedNotSubmit;
    private String orgName;
    private Long sumTotalIncomeTaxableIn;
    private Long sumTotalIncomeTaxableOut;


}
