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
 * Lop entity ung voi bang exm_exam_papers
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Data
@Entity
@Table(name = "exm_exam_papers")
public class ExamPapersEntity extends BaseEntity {

    @Id    @GeneratedValue(strategy = GenerationType.IDENTITY)    @Basic(optional = false)    @Column(name = "exam_paper_id")    private Long examPaperId;    @Column(name = "code")    private String code;    @Column(name = "name")    private String name;    @Column(name = "subject_code")    private String subjectCode;    @Column(name = "topic_code")    private String topicCode;    @Column(name = "description")    private String description;    @Column(name = "total_questions")    private Long totalQuestions;    @Column(name = "total_score")    private Double totalScore;    @Column(name = "duration_minutes")    private Long durationMinutes;    @Column(name = "difficulty_distribution")    private String difficultyDistribution;    @Column(name = "skill_distribution")    private String skillDistribution;    @Column(name = "random_order")    private String randomOrder;    @Column(name = "random_option_order")    private String randomOptionOrder;    @Column(name = "generation_mode")    private String generationMode;    @Column(name = "generation_strategy")    private String generationStrategy;    @Column(name = "status_code")    private String statusCode;

}
