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
 * Lop entity ung voi bang hr_worked_histories
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Data
@Entity
@Table(name = "hr_worked_histories")
public class WorkedHistoriesEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "worked_history_id")
    private Long workedHistoryId;

    @Column(name = "employee_id")
    private Long employeeId;

    @Column(name = "start_date")
    @Temporal(javax.persistence.TemporalType.DATE)
    private Date startDate;

    @Column(name = "end_date")
    @Temporal(javax.persistence.TemporalType.DATE)
    private Date endDate;

    @Column(name = "job")
    private String job;

    @Column(name = "company_name")
    private String companyName;

    @Column(name = "reference_name")
    private String referenceName;

    @Column(name = "reference_job")
    private String referenceJob;

}
