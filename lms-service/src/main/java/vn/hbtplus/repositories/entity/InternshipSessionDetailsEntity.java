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
 * Lop entity ung voi bang lms_internship_session_details
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Data
@Entity
@Table(name = "lms_internship_session_details")
public class InternshipSessionDetailsEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "internship_session_detail_id")
    private Long internshipSessionDetailId;

    @Column(name = "internship_session_id")
    private Long internshipSessionId;

    @Column(name = "major_id")
    private String majorId;
    @Column(name = "organization_id")
    private Long organizationId;
    @Column(name = "num_of_students")
    private Long numOfStudents;


}
