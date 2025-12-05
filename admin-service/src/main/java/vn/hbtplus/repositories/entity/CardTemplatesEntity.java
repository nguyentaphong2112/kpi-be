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
 * Lop entity ung voi bang sys_card_templates
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Data
@Entity
@Table(name = "sys_card_templates")
public class CardTemplatesEntity extends BaseEntity {

    @Id    @GeneratedValue(strategy = GenerationType.IDENTITY)    @Basic(optional = false)    @Column(name = "card_template_id")    private Long cardTemplateId;    @Column(name = "template_type")    private String templateType;    @Column(name = "title")    private String title;    @Column(name = "default_parameters")    private String defaultParameters;    @Column(name = "parameters")    private String parameters;    @Column(name = "is_apply_all")    private String isApplyAll;

}
