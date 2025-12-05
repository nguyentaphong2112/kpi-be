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
 * Lop entity ung voi bang hr_salary_grades
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Data
@Entity
@Table(name = "hr_salary_grades")
public class SalaryGradesEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "salary_grade_id")
    private Long salaryGradeId;

    @Column(name = "name")
    private String name;

    @Column(name = "salary_rank_id")
    private Long salaryRankId;

    @Column(name = "amount")
    private Double amount;

    @Column(name = "note")
    private String note;

    @Column(name = "duration")
    private Integer duration;
    @Column(name = "seniority_percent")
    private Integer seniorityPercent;


}
