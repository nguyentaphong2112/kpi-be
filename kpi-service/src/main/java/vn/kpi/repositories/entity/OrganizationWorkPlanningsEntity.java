/*
 * Copyright (C) 2022 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.kpi.repositories.entity;

import javax.persistence.*;

import lombok.Data;


/**
 * Lop entity ung voi bang kpi_organization_work_plannings
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Data
@Entity
@Table(name = "kpi_organization_work_plannings")
public class OrganizationWorkPlanningsEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "organization_work_planning_id")
    private Long organizationWorkPlanningId;

    @Column(name = "organization_evaluation_id")
    private Long organizationEvaluationId;

    @Column(name = "content")
    private String content;

    @Transient
    private String adjustReason;
    @Transient
    private String status;

    @Transient
    private Long organizationId;


}
