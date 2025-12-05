/*
 * Copyright (C) 2022 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.tax.income.repositories.entity;

import lombok.Data;

import javax.persistence.*;
import java.util.Date;
import java.util.Map;


/**
 * Lop entity ung voi bang pit_income_item_columns
 *
 * @author tudd
 * @version 1.0
 * @since 1.0
 */

@Data
@Entity
@Table(name = "pit_income_item_columns")
public class IncomeItemColumnsEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "income_item_column_id")
    private Long incomeItemColumnId;

    @Column(name = "income_item_master_id")
    private Long incomeItemMasterId;
    @Column(name = "income_item_detail_id")
    private Long incomeItemDetailId;

    @Column(name = "tax_period_date")
    @Temporal(javax.persistence.TemporalType.DATE)
    private Date taxPeriodDate;

    @Column(name = "column_code")
    private String columnCode;

    @Column(name = "column_value")
    private Long columnValue;

    public IncomeItemColumnsEntity() {
    }

    public IncomeItemColumnsEntity(IncomeItemDetailsEntity item, Map.Entry<String, Long> entry) {
        this.setTaxPeriodDate(item.getTaxPeriodDate());
        this.setColumnCode(entry.getKey());
        this.setColumnValue(entry.getValue());
        this.setIncomeItemDetailId(item.getIncomeItemDetailId());
        this.setIncomeItemMasterId(item.getIncomeItemMasterId());
    }
}
