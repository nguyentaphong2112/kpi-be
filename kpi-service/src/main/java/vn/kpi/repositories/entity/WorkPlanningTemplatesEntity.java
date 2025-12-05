/*
 * Copyright (C) 2022 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.kpi.repositories.entity;

import lombok.Data;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;


/**
 * Lop entity ung voi bang kpi_indicators
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Data
@Entity
@Table(name = "kpi_work_planning_templates")
public class WorkPlanningTemplatesEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "work_planning_template_id")
    private Long workPlanningTemplateId;

    @Column(name = "name")
    private String name;

    @Column(name = "content")
    private String content;

    @Column(name = "type")
    private String type;

    @Column(name = "code")
    private String code;



}
