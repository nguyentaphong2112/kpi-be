/*
 * Copyright (C) 2022 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.kpi.repositories.entity;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.GenerationType;
import javax.persistence.Table;
import lombok.Data;

import java.util.Date;
import javax.persistence.Temporal;


/**
 * Lop entity ung voi bang hr_education_certificates
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Data
@Entity
@Table(name = "hr_education_certificates")
public class EducationCertificatesEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "education_certificate_id")
    private Long educationCertificateId;

    @Column(name = "employee_id")
    private Long employeeId;

    @Column(name = "certificate_type_id")
    private String certificateTypeId;

    @Column(name = "certificate_name")
    private String certificateName;

    @Column(name = "certificate_id")
    private String certificateId;

    @Column(name = "issued_place")
    private String issuedPlace;

    @Column(name = "issued_date")
    @Temporal(javax.persistence.TemporalType.DATE)
    private Date issuedDate;

    @Column(name = "expired_date")
    @Temporal(javax.persistence.TemporalType.DATE)
    private Date expiredDate;

    @Column(name = "result")
    private String result;

}
