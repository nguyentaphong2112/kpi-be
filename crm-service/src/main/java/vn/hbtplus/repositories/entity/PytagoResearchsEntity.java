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
 * Lop entity ung voi bang crm_pytago_researchs
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Data
@Entity
@Table(name = "crm_pytago_researchs")
public class PytagoResearchsEntity extends BaseEntity {

    @Id    @GeneratedValue(strategy = GenerationType.IDENTITY)    @Basic(optional = false)    @Column(name = "pytago_research_id")    private Long pytagoResearchId;    @Column(name = "full_name")    private String fullName;    @Column(name = "date_of_birth")    @Temporal(javax.persistence.TemporalType.DATE)    private Date dateOfBirth;    @Column(name = "parent_name")    private String parentName;    @Column(name = "mobile_number")    private String mobileNumber;    @Column(name = "email")    private String email;    @Column(name = "current_address")    private String currentAddress;    @Column(name = "type")    private String type;

}
