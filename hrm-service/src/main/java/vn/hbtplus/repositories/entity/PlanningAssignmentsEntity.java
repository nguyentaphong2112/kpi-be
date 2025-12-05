/*
 * Copyright (C) 2022 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.repositories.entity;

import lombok.Data;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import java.util.Date;


/**
 * Lop entity ung voi bang hr_planning_assignments
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Data
@Entity
@Table(name = "hr_planning_assignments")
public class PlanningAssignmentsEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "planning_assignment_id")
    private Long planningAssignmentId;

    @Column(name = "employee_id")
    private Long employeeId;

    @Column(name = "start_date")
    @Temporal(javax.persistence.TemporalType.DATE)
    private Date startDate;

    @Column(name = "end_date")
    @Temporal(javax.persistence.TemporalType.DATE)
    private Date endDate;

    @Column(name = "document_no")
    private String documentNo;

    @Column(name = "document_signed_date")
    @Temporal(javax.persistence.TemporalType.DATE)
    private Date documentSignedDate;
    @Column(name = "planning_period_id")
    private String planningPeriodId;
    @Column(name = "position_id")
    private String positionId;
    @Column(name = "end_document_no")
    private String endDocumentNo;
    @Column(name = "end_document_signed_date")
    @Temporal(javax.persistence.TemporalType.DATE)
    private Date endDocumentSignedDate;
    @Column(name = "end_reason_id")
    private String endReasonId;
}
