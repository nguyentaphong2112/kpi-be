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
 * Lop entity ung voi bang hr_concurrent_process
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Data
@Entity
@Table(name = "hr_concurrent_process")
public class ConcurrentProcessEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "concurrent_process_id")
    private Long concurrentProcessId;

    @Column(name = "employee_id")
    private Long employeeId;

    @Column(name = "start_date")
    @Temporal(javax.persistence.TemporalType.DATE)
    private Date startDate;

    @Column(name = "end_date")
    @Temporal(javax.persistence.TemporalType.DATE)
    private Date endDate;

    @Column(name = "job_id")
    private Long jobId;

    @Column(name = "position_id")
    private Long positionId;

    @Column(name = "organization_id")
    private Long organizationId;

    @Column(name = "document_no")
    private String documentNo;

    @Column(name = "document_signed_date")
    private Date documentSignedDate;
    @Column(name = "percentage_id")
    private String percentageId;


}
