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
 * Lop entity ung voi bang hr_insurance_salary_process
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Data
@Entity
@Table(name = "hr_insurance_salary_process")
public class InsuranceSalaryProcessEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "insurance_salary_process_id")
    private Long insuranceSalaryProcessId;

    @Column(name = "employee_id")
    private Long employeeId;

    @Column(name = "salary_rank_id")
    private Long salaryRankId;

    @Column(name = "salary_grade_id")
    private Long salaryGradeId;

    @Column(name = "percent")
    private Long percent;

    @Column(name = "seniority_percent")
    private Long seniorityPercent;

    @Column(name = "reserve_factor")
    private Double reserveFactor;

    @Column(name = "document_no")
    private String documentNo;

    @Column(name = "document_signed_date")
    @Temporal(javax.persistence.TemporalType.DATE)
    private Date documentSignedDate;

    @Column(name = "start_date")
    @Temporal(javax.persistence.TemporalType.DATE)
    private Date startDate;

    @Column(name = "end_date")
    @Temporal(javax.persistence.TemporalType.DATE)
    private Date endDate;

    @Column(name = "job_salary_id")
    private Long jobSalaryId;

    @Column(name = "increment_date")
    @Temporal(javax.persistence.TemporalType.DATE)
    private Date incrementDate;

    @Column(name = "emp_type_id")
    private Long empTypeId;

    @Column(name = "amount")
    private Double amount;
    @Column(name = "is_early_increased")
    private String isEarlyIncreased;

    @Column(name = "payroll_date")
    @Temporal(javax.persistence.TemporalType.DATE)
    private Date payrollDate;

    @Column(name = "insurance_base_salary")
    private Long insuranceBaseSalary;

    @Column(name = "insurance_factor")
    private Double insuranceFactor;

}
