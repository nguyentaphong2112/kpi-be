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
 * Lop entity ung voi bang lms_external_trainings
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Data
@Entity
@Table(name = "lms_external_trainings")
public class ExternalTrainingsEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "external_training_id")
    private Long externalTrainingId;

    @Column(name = "type_id")
    private String typeId;

    @Column(name = "full_name")
    private String fullName;

    @Column(name = "gender_id")
    private String genderId;

    @Column(name = "year_of_birth")
    private String yearOfBirth;

    @Column(name = "mobile_number")
    private String mobileNumber;

    @Column(name = "personal_id_no")
    private String personalIdNo;

    @Column(name = "address")
    private String address;

    @Column(name = "organization_address")
    private String organizationAddress;

    @Column(name = "start_date")
    @Temporal(javax.persistence.TemporalType.DATE)
    private Date startDate;

    @Column(name = "end_date")
    @Temporal(javax.persistence.TemporalType.DATE)
    private Date endDate;

    @Column(name = "trainning_type_id")
    private String trainningTypeId;

    @Column(name = "training_major_id")
    private String trainingMajorId;

    @Column(name = "content")
    private String content;

    @Column(name = "organization_id")
    private Long organizationId;

    @Column(name = "mentor_id")
    private Long mentorId;

    @Column(name = "admission_results")
    private String admissionResults;

    @Column(name = "graduated_results")
    private String graduatedResults;

    @Column(name = "number_of_lessons")
    private Long numberOfLessons;

    @Column(name = "tuition_fee_status_id")
    private String tuitionFeeStatusId;

    @Column(name = "certificate_no")
    private String certificateNo;

    @Column(name = "certificate_date")
    @Temporal(javax.persistence.TemporalType.DATE)
    private Date certificateDate;




}
