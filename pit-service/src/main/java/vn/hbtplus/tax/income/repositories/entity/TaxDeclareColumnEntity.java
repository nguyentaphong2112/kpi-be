/*
 * Copyright (C) 2022 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.tax.income.repositories.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.Date;
import java.util.Map;


/**
 * Lop entity ung voi bang pit_tax_declare_columns
 *
 * @author tudd
 * @version 1.0
 * @since 1.0
 */

@Data
@Entity
@Table(name = "pit_tax_declare_columns")
@NoArgsConstructor
public class TaxDeclareColumnEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "tax_declare_column_id")
    private Long taxDeclareColumnId;

    @Column(name = "tax_declare_master_id")
    private Long taxDeclareMasterId;

    @Column(name = "tax_declare_detail_id")
    private Long taxDeclareDetailId;

    @Column(name = "tax_period_date")
    @Temporal(TemporalType.DATE)
    private Date taxPeriodDate;

    @Column(name = "column_code")
    private String columnCode;

    @Column(name = "column_value")
    private Long columnValue;

    public TaxDeclareColumnEntity(TaxDeclareDetailsEntity item, Map.Entry<String, Long> entry) {
        this.taxPeriodDate = item.getTaxPeriodDate();
        this.taxDeclareMasterId = item.getTaxDeclareMasterId();
        this.taxDeclareDetailId = item.getTaxDeclareDetailId();
        this.columnCode = entry.getKey();
        this.columnValue = entry.getValue();
    }
    public TaxDeclareColumnEntity(TaxDeclareDetailsEntity item, String columnCode, Long columnValue) {
        this.taxPeriodDate = item.getTaxPeriodDate();
        this.taxDeclareMasterId = item.getTaxDeclareMasterId();
        this.taxDeclareDetailId = item.getTaxDeclareDetailId();
        this.columnCode = columnCode;
        this.columnValue = columnValue;
    }
}
