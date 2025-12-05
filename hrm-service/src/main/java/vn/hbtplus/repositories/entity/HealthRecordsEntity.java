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
 * Lop entity ung voi bang hr_health_records
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Data
@Entity
@Table(name = "hr_health_records")
public class HealthRecordsEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "health_record_id")
    private Long healthRecordId;

    @Column(name = "employee_id")
    private Long employeeId;

    @Column(name = "examination_period_id")
    private String examinationPeriodId;

    @Column(name = "examination_date")
    @Temporal(javax.persistence.TemporalType.DATE)
    private Date examinationDate;

    @Column(name = "result_id")
    private String resultId;

    @Column(name = "disease_ids")
    private String diseaseIds;

    @Column(name = "patient_id")
    private String patientId;
    @Column(name = "doctor_conclusion")
    private String doctorConclusion;


}
