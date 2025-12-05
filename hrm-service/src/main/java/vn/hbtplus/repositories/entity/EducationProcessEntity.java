/*
 * Copyright (C) 2022 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.repositories.entity;

import javax.persistence.*;

import lombok.Data;
import javax.validation.constraints.NotNull;
import java.util.Date;


/**
 * Lop entity ung voi bang hr_education_process
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Data
@Entity
@Table(name = "hr_education_process")
public class EducationProcessEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "education_process_id")
    private Long educationProcessId;

    @Column(name = "employee_id")
    private Long employeeId;

    @Column(name = "course_name")
    private String courseName;

    @Column(name = "training_method_id")
    private String trainingMethodId;

    @Column(name = "course_content")
    private String courseContent;

    @Column(name = "result")
    private String result;

    @Column(name = "training_method_place")
    private String trainingMethodPlace;

    @Column(name = "start_date")
    @Temporal(TemporalType.DATE)
    private Date startDate;

    @Column(name = "end_date")
    @Temporal(TemporalType.DATE)
    private Date endDate;

}
