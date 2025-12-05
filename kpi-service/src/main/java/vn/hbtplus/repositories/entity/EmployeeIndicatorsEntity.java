/*
 * Copyright (C) 2022 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.repositories.entity;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.GenerationType;
import javax.persistence.Table;
import lombok.Data;
import javax.validation.constraints.NotNull;
import java.util.Date;
import javax.persistence.Temporal;


/**
 * Lop entity ung voi bang kpi_employee_indicators
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Data
@Entity
@Table(name = "kpi_employee_indicators")
public class EmployeeIndicatorsEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "employee_indicator_id")
    private Long employeeIndicatorId;

    @Column(name = "indicator_conversion_id")
    private Long indicatorConversionId;

    @Column(name = "indicator_id")
    private Long indicatorId;

    @Column(name = "employee_evaluation_id")
    private Long employeeEvaluationId;

    @Column(name = "percent")
    private Double percent;

    @Column(name = "target")
    private String target;

    @Column(name = "status")
    private String status;

    @Column(name = "old_percent")
    private Double oldPercent;

    @Column(name = "result")
    private String result;

    @Column(name = "result_manage")
    private String resultManage;

    @Column(name = "self_point")
    private Long selfPoint;

    @Column(name = "manage_point")
    private Long managePoint;

}
