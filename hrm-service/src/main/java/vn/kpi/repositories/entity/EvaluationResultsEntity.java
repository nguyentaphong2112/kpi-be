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


/**
 * Lop entity ung voi bang hr_evaluation_results
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Data
@Entity
@Table(name = "hr_evaluation_results")
public class EvaluationResultsEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "evaluation_result_id")
    private Long evaluationResultId;

    @Column(name = "year")
    private Long year;

    @Column(name = "evaluation_period_id")
    private Long evaluationPeriodId;

    @Column(name = "employee_id")
    private Long employeeId;

    @Column(name = "kpi_point")
    private Double kpiPoint;

    @Column(name = "kpi_result")
    private String kpiResult;

    @Column(name = "note")
    private String note;

    @Column(name = "evaluation_type")
    private String evaluationType;

}
