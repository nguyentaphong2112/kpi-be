/*
 * Copyright (C) 2022 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.repositories.entity;

import lombok.Data;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import java.util.Date;


/**
 * Lop entity ung voi bang hr_employees
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Data
@Entity
@Table(name = "hr_employees")
public class EmployeesEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "employee_id")
    private Long employeeId;

    @Column(name = "employee_code")
    private String employeeCode;

    @Column(name = "full_name")
    private String fullName;

    @Column(name = "email")
    private String email;

    @Column(name = "mobile_number")
    private String mobileNumber;

    @Column(name = "organization_id")
    private Long organizationId;

    @Column(name = "position_id")
    private Long positionId;

    @Column(name = "job_id")
    private Long jobId;

    @Column(name = "alias_name")
    private String aliasName;

    @Column(name = "date_of_birth")
    @Temporal(javax.persistence.TemporalType.DATE)
    private Date dateOfBirth;

    @Column(name = "gender_id")
    private String genderId;

    @Column(name = "religion_id")
    private String religionId;

    @Column(name = "ethnic_id")
    private String ethnicId;

    @Column(name = "marital_status_id")
    private String maritalStatusId;

    @Column(name = "personal_email")
    private String personalEmail;

    @Column(name = "place_of_birth")
    private String placeOfBirth;

    @Column(name = "original_address")
    private String originalAddress;

    @Column(name = "permanent_address")
    private String permanentAddress;

    @Column(name = "current_address")
    private String currentAddress;



    @Column(name = "tax_no")
    private String taxNo;

    @Column(name = "insurance_no")
    private String insuranceNo;

    @Column(name = "status")
    private Integer status;

    @Column(name = "emp_type_id")
    private Long empTypeId;

    @Column(name = "education_level_id")
    private Long educationLevelId;

    @Column(name = "family_policy_id")
    private String familyPolicyId;

    @Column(name = "self_policy_id")
    private String selfPolicyId;

    @Column(name = "party_date")
    private Date partyDate;
    @Column(name = "party_official_date")
    private Date partyOfficialDate;

    @Column(name = "party_place")
    private String partyPlace;

    @Column(name = "party_number")
    private String partyNumber;

    public interface STATUS {
        String DANG_LAM_VIEC = "DANG_LAM_VIEC";
    }
}
