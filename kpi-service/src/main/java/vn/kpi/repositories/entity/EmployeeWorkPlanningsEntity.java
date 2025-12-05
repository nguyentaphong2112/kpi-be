/*
 * Copyright (C) 2022 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.kpi.repositories.entity;

import javax.persistence.*;

import lombok.Data;


/**
 * Lop entity ung voi bang kpi_employee_work_plannings
 *
 * @author tudd
 * @version 1.0
 * @since 1.0
 */

@Data
@Entity
@Table(name = "kpi_employee_work_plannings")
public class EmployeeWorkPlanningsEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "employee_work_planning_id")
    private Long employeeWorkPlanningId;

    @Column(name = "employee_evaluation_id")
    private Long employeeEvaluationId;

    @Column(name = "content")
    private String content;

    @Column(name = "name")
    private String name;

    @Column(name = "order_number")
    private Long orderNumber;

    @Column(name = "percent")
    private Double percent;

    @Column(name = "is_selected")
    private String isSelected;

    @Transient
    private String adjustReason;

    @Transient
    private String employeeCode;

    @Transient
    private String fullName;

    @Transient
    private Long employeeId;

    @Transient
    private Double totalPoint;

    @Transient
    private Boolean isExist;

}
