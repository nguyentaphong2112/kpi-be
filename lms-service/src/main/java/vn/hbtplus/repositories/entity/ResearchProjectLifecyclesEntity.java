/*
 * Copyright (C) 2022 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.repositories.entity;

import lombok.Data;

import javax.persistence.*;
import java.util.Date;


/**
 * Lop entity ung voi bang med_research_project_lifecycles
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Data
@Entity
@Table(name = "lms_research_project_lifecycles")
public class ResearchProjectLifecyclesEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "research_project_lifecycle_id")
    private Long researchProjectLifecycleId;

    @Column(name = "research_project_id")
    private Long researchProjectId;

    @Column(name = "document_no")
    private String documentNo;

    @Column(name = "document_signed_date")
    @Temporal(javax.persistence.TemporalType.DATE)
    private Date documentSignedDate;

    @Column(name = "type")
    private String type;


}
