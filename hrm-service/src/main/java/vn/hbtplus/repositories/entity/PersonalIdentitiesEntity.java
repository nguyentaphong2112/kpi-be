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
 * Lop entity ung voi bang hr_personal_identities
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Data
@Entity
@Table(name = "hr_personal_identities")
public class PersonalIdentitiesEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "personal_identity_id")
    private Long personalIdentityId;

    @Column(name = "identity_no")
    private String identityNo;

    @Column(name = "identity_type_id")
    private String identityTypeId;

    @Column(name = "identity_issue_date")
    @Temporal(javax.persistence.TemporalType.DATE)
    private Date identityIssueDate;

    @Column(name = "identity_issue_place")
    private String identityIssuePlace;

    @Column(name = "expired_date")
    @Temporal(javax.persistence.TemporalType.DATE)
    private Date expiredDate;

    @Column(name = "is_main")
    private String isMain;

    @Column(name = "employee_id")
    private Long employeeId;

}
