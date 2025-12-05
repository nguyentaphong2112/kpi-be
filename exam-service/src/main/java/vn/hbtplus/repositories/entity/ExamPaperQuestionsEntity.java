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
 * Lop entity ung voi bang exm_exam_paper_questions
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Data
@Entity
@Table(name = "exm_exam_paper_questions")
public class ExamPaperQuestionsEntity extends BaseEntity {

    @Id    @GeneratedValue(strategy = GenerationType.IDENTITY)    @Basic(optional = false)    @Column(name = "exam_paper_question_id")    private Long examPaperQuestionId;    @Column(name = "exam_paper_id")    private Long examPaperId;    @Column(name = "question_id")    private Long questionId;    @Column(name = "order_number")    private Long orderNumber;    @Column(name = "score")    private Double score;    @Column(name = "weight")    private Double weight;

}
