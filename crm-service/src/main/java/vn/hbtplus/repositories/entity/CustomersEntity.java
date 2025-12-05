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
 * Lop entity ung voi bang crm_customers
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Data
@Entity
@Table(name = "crm_customers")
public class CustomersEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "customer_id")
    private Long customerId;

    @Column(name = "full_name")
    private String fullName;

    @Column(name = "mobile_number")
    private String mobileNumber;


    @Column(name = "login_name")
    private String loginName;

    @Column(name = "gender_id")
    private String genderId;

    @Column(name = "date_of_birth")
    @Temporal(javax.persistence.TemporalType.DATE)
    private Date dateOfBirth;

    @Column(name = "email")
    private String email;

    @Column(name = "zalo_account")
    private String zaloAccount;

    @Column(name = "introducer_id")
    private Long introducerId;

    @Column(name = "receiver_id")
    private Long receiverId;

    @Column(name = "user_take_care_id")
    private Long userTakeCareId;

    @Column(name = "job")
    private String job;

    @Column(name = "department_name")
    private String departmentName;

    @Column(name = "province_id")
    private String provinceId;

    @Column(name = "district_id")
    private String districtId;

    @Column(name = "ward_id")
    private String wardId;

    @Column(name = "village_address")
    private String villageAddress;

    @Column(name = "full_address")
    private String fullAddress;

    @Column(name = "bank_account")
    private String bankAccount;

    @Column(name = "bank_name")
    private String bankName;

    @Column(name = "bank_branch")
    private String bankBranch;

    @Column(name = "status")
    private String status;

}
