/*
 * Copyright (C) 2022 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.insurance.repositories.entity;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import vn.hbtplus.repositories.entity.BaseEntity;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Date;


/**
 * Lop entity ung voi bang hr_work_process
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Data
@Entity
@Slf4j
@Table(name = "hr_work_process")
public class HrWorkProcessEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @NotNull
    @Column(name = "work_process_id")
    private Long workProcessId;

    @Column(name = "employee_id")
    private Long employeeId;

    @Column(name = "start_date")
    @Temporal(javax.persistence.TemporalType.DATE)
    private Date startDate;

    @Column(name = "end_date")
    @Temporal(javax.persistence.TemporalType.DATE)
    private Date endDate;

    @Column(name = "org_id")
    private Long orgId;

    @Column(name = "job_id")
    private Long jobId;

    @Column(name = "document_type_id")
    private Long documentTypeId;

    @Column(name = "decision_no")
    private String decisionNo;


}
