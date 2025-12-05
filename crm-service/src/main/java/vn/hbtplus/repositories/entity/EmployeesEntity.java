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
 * Lop entity ung voi bang crm_employees
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Data
@Entity
@Table(name = "crm_employees")
public class EmployeesEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "employee_id")
    private Long employeeId;

    @Column(name = "full_name")
    private String fullName;

    @Column(name = "date_of_birth")
    @Temporal(javax.persistence.TemporalType.DATE)
    private Date dateOfBirth;

    @Column(name = "mobile_number")
    private String mobileNumber;


    @Column(name = "login_name")
    private String loginName;

    @Column(name = "gender_id")
    private String genderId;

    @Column(name = "email")
    private String email;

    @Column(name = "zalo_account")
    private String zaloAccount;

    @Column(name = "position_title_id")
    private String positionTitleId;

    @Column(name = "department_id")
    private String departmentId;

    @Column(name = "manager_id")
    private Long managerId;

    @Column(name = "job_rank_id")
    private String jobRankId;

    @Column(name = "province_id")
    private String provinceId;

    @Column(name = "district_id")
    private String districtId;

    @Column(name = "ward_id")
    private String wardId;

    @Column(name = "village_address")
    private String villageAddress;

    @Column(name = "bank_account")
    private String bankAccount;

    @Column(name = "bank_name")
    private String bankName;

    @Column(name = "bank_branch")
    private String bankBranch;

    @Column(name = "status")
    private String status;

    @Column(name = "personal_id_no")
    private String personalIdNo;

    @Column(name = "tax_no")
    private String taxNo;

    @Column(name = "insurance_no")
    private String insuranceNo;

}
