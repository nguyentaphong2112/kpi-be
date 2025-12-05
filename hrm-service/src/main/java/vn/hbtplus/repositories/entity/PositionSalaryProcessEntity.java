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
 * Lop entity ung voi bang hr_position_salary_process
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Data
@Entity
@Table(name = "hr_position_salary_process")
public class PositionSalaryProcessEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "position_salary_process_id")
    private Long positionSalaryProcessId;

    @Column(name = "employee_id")
    private Long employeeId;

    @Column(name = "salary_grade_id")
    private Long salaryGradeId;

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

    @Column(name = "salary_rank_id")
    private Long salaryRankId;

    @Column(name = "percent")
    private Long percent;

    @Column(name = "salary_type")
    private String salaryType;

    @Column(name = "job_id")
    private Long jobId;
    @Column(name = "order_number")
    private Integer orderNumber;
}
