/*
 * Copyright (C) 2022 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.kpi.repositories.entity;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.GenerationType;
import javax.persistence.Table;

import lombok.Data;

import java.util.Date;
import javax.persistence.Temporal;


/**
 * Lop entity ung voi bang kpi_evaluation_periods
 *
 * @author tudd
 * @version 1.0
 * @since 1.0
 */

@Data
@Entity
@Table(name = "kpi_evaluation_periods")
public class EvaluationPeriodsEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "evaluation_period_id")
    private Long evaluationPeriodId;

    @Column(name = "year")
    private Integer year;

    @Column(name = "name")
    private String name;

    @Column(name = "evaluation_type")
    private Long evaluationType;

    @Column(name = "status")
    private String status;

    @Column(name = "start_date")
    @Temporal(javax.persistence.TemporalType.DATE)
    private Date startDate;

    @Column(name = "end_date")
    @Temporal(javax.persistence.TemporalType.DATE)
    private Date endDate;


}
