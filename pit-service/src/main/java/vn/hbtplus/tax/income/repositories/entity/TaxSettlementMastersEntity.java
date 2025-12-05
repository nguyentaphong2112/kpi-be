/*
 * Copyright (C) 2022 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.tax.income.repositories.entity;

import lombok.Data;
import vn.hbtplus.repositories.entity.BaseEntity;


import javax.persistence.*;


/**
 * Lop entity ung voi bang pit_tax_settlement_masters
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Data
@Entity
@Table(name = "pit_tax_settlement_masters")
public class TaxSettlementMastersEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "tax_settlement_master_id")
    private Long taxSettlementMasterId;

    @Column(name = "year")
    private Integer year;

    @Column(name = "input_type")
    private String inputType;

    @Column(name = "status")
    private String status;
    @Column(name = "tax_declare_master_ids")
    private String taxDeclareMasterIds;
    @Column(name = "total_taxpayers")
    private Long totalTaxpayers;
    @Column(name = "total_income_taxable")
    private Long totalIncomeTaxable;
    @Column(name = "total_insurance_deduction")
    private Long totalInsuranceDeduction;
    @Column(name = "total_tax_collected")
    private Long totalTaxCollected;

}
