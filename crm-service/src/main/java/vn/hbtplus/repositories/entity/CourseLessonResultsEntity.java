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
 * Lop entity ung voi bang crm_course_lesson_results
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Data
@Entity
@Table(name = "crm_course_lesson_results")
public class CourseLessonResultsEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "course_lesson_result_id")
    private Long courseLessonResultId;

    @Column(name = "course_lesson_id")
    private Long courseLessonId;

    @Column(name = "trainee_id")
    private Long traineeId;

    @Column(name = "point")
    private Double point;

    @Column(name = "note")
    private String note;

    @Column(name = "status_id")
    private String statusId;


}
