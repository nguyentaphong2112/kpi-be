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
 * Lop entity ung voi bang exm_question_options
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Data
@Entity
@Table(name = "exm_question_options")
public class QuestionOptionsEntity extends BaseEntity {

    @Id    @GeneratedValue(strategy = GenerationType.IDENTITY)    @Basic(optional = false)    @Column(name = "question_option_id")    private Long questionOptionId;    @Column(name = "question_id")    private Long questionId;    @Column(name = "option_code")    private String optionCode;    @Column(name = "content")    private String content;    @Column(name = "media_path")    private String mediaPath;    @Column(name = "is_correct")    private String isCorrect;    @Column(name = "explanation")    private String explanation;    @Column(name = "order_number")    private Long orderNumber;

}
