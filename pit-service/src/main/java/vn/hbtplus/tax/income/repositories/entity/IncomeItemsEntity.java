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
 * Lop entity ung voi bang pit_income_items
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Data
@Entity
@Table(name = "pit_income_items")
public class IncomeItemsEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "income_item_id")
    private Long incomeItemId;

    @Column(name = "income_template_id")
    private Long incomeTemplateId;

    @Column(name = "code")
    private String code;

    @Column(name = "name")
    private String name;

    @Column(name = "salary_period_date")
    @Temporal(javax.persistence.TemporalType.DATE)
    private Date salaryPeriodDate;


}
