/*
 * Copyright (C) 2022 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.tax.income.repositories.entity;

import lombok.Data;
import vn.hbtplus.repositories.entity.BaseEntity;


import javax.persistence.*;
import java.util.Date;


/**
 * Lop entity ung voi bang pit_income_item_masters
 *
 * @author tudd
 * @version 1.0
 * @since 1.0
 */

@Data
@Entity
@Table(name = "pit_income_item_masters")
public class IncomeItemMastersEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "income_item_master_id")
    private Long incomeItemMasterId;

    @Column(name = "income_item_id")
    private Long incomeItemId;

    @Column(name = "tax_period_date")
    @Temporal(javax.persistence.TemporalType.DATE)
    private Date taxPeriodDate;

    @Column(name = "is_tax_calculated")
    private Integer isTaxCalculated;

    @Column(name = "status")
    private String status;

    @Column(name = "input_times")
    private Integer inputTimes;

    @Column(name = "total_income")
    private Long totalIncome;

    @Column(name = "total_insurance_deduction")
    private Long totalInsuranceDeduction;

    @Column(name = "total_income_taxable")
    private Long totalIncomeTaxable;

    @Column(name = "total_income_free_tax")
    private Long totalIncomeFreeTax;

    @Column(name = "total_income_tax")
    private Long totalIncomeTax;

    @Column(name = "total_received")
    private Long totalReceived;
    @Column(name = "total_month_retro_tax")
    private Long totalMonthRetroTax;

    @Column(name = "total_year_retro_tax")
    private Long totalYearRetroTax;
    @Column(name = "last_update_time")
    private Date lastUpdateTime;

    @Column(name = "tax_cal_by")
    private String taxCalBy;

    @Column(name = "tax_date")
    private Date taxDate;

    public interface STATUS {
        String DU_THAO = "DU_THAO";
        String DA_TINH_THUE = "DA_TINH_THUE";
        String DA_CHOT = "DA_CHOT";
        String DA_KE_KHAI = "DA_KE_KHAI";
    }

}
