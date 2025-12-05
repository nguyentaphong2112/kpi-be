/*
 * Copyright (C) 2022 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.tax.income.repositories.entity;

import lombok.Data;
import vn.hbtplus.repositories.entity.BaseEntity;


import javax.persistence.*;
import javax.validation.constraints.NotNull;


/**
 * Lop entity ung voi bang pit_income_template_columns
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Data
@Entity
@Table(name = "pit_income_template_columns")
public class IncomeTemplateColumnsEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @NotNull
    @Column(name = "income_template_column_id")
    private Long incomeTemplateColumnId;

    @Column(name = "income_template_id")
    private Long incomeTemplateId;

    @Column(name = "column_code")
    private String columnCode;

    @Column(name = "column_name")
    private String columnName;

    @Column(name = "income_type")
    private String incomeType;

    @Column(name = "order_number")
    private Long orderNumber;

    @Column(name = "is_required")
    private Long isRequired;


}
