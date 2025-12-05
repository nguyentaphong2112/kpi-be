/*
 * Copyright (C) 2022 EcoIT. All rights reserved.
 * EcoIT. Use is subject to license terms.
 */
package vn.hbtplus.tax.personal.repositories.entity;

import lombok.Data;
import vn.hbtplus.repositories.entity.BaseEntity;

import javax.persistence.*;
import java.util.Date;


/**
 * Lop entity ung voi bang HR_DEPENDENT_PERSONS
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Data
@Entity
@Table(name = "hr_dependent_persons")
public class HrDependentPersonsEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "dependent_person_id")
    private Long dependentPersonId;

    @Column(name = "employee_id")
    private Long employeeId;

    @Column(name = "family_relationship_id")
    private Long familyRelationshipId;

    @Column(name = "from_date")
    @Temporal(TemporalType.DATE)
    private Date fromDate;

    @Column(name = "to_date")
    @Temporal(TemporalType.DATE)
    private Date toDate;

    @Column(name = "tax_organization_id")
    private Long taxOrganizationId;

    @Column(name = "province_code")
    private String provinceCode;

    @Column(name = "district_code")
    private String districtCode;

    @Column(name = "ward_code")
    private String wardCode;

    @Column(name = "code_no")
    private String codeNo;

    @Column(name = "book_no")
    private String bookNo;

    @Column(name = "personal_id")
    private String personalId;

    @Column(name = "passport_no")
    private String passportNo;

    @Column(name = "nation_code")
    private String nationCode;

    @Column(name = "note")
    private String note;

    @Column(name = "dependent_person_code")
    private String dependentPersonCode;

    @Transient
    private String employeeCode;
    @Transient
    private String taxNo;
    @Transient
    private String strFromDate;
    @Transient
    private String strToDate;


}
