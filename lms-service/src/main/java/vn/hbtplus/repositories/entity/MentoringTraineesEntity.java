/*
 * Copyright (C) 2022 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.repositories.entity;

import lombok.Data;

import javax.persistence.*;
import java.util.Date;


/**
 * Lop entity ung voi bang lms_mentoring_trainees
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Data
@Entity
@Table(name = "lms_mentoring_trainees")
public class MentoringTraineesEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "med_mentoring_trainee_id")
    private Long medMentoringTraineeId;

    @Column(name = "employee_id")
    private Long employeeId;

    @Column(name = "start_date")
    @Temporal(javax.persistence.TemporalType.DATE)
    private Date startDate;

    @Column(name = "end_date")
    @Temporal(javax.persistence.TemporalType.DATE)
    private Date endDate;

    @Column(name = "project_id")
    private String projectId;

    @Column(name = "hospital_id")
    private String hospitalId;

    @Column(name = "total_lessons")
    private Long totalLessons;

    @Column(name = "content")
    private String content;

    @Column(name = "document_no")
    private String documentNo;


}
