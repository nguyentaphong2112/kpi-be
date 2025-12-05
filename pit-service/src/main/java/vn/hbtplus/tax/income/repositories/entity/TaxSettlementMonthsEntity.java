/*
 * Copyright (C) 2022 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.tax.income.repositories.entity;

import lombok.Data;

import javax.persistence.*;


/**
 * Lop entity ung voi bang pit_tax_settlement_details
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Data
@Entity
@Table(name = "pit_tax_settlement_months")
public class TaxSettlementMonthsEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "tax_settlement_month_id")
    private Long taxSettlementMonthId;

    @Column(name = "tax_settlement_detail_id")
    private Long taxSettlementDetailId;
    @Column(name = "tax_settlement_master_id")
    private Long taxSettlementMasterId;

    @Column(name = "year")
    private Integer year;
    @Column(name = "month")
    private Integer month;

    @Column(name = "tax_collected")
    private Long taxCollected;
    @Column(name = "income_taxable")
    private Long incomeTaxable;

    @Column(name = "insurance_deduction")
    private Long insuranceDeduction;


}
