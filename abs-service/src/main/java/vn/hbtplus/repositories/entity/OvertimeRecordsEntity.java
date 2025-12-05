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

import javax.persistence.Transient;
import javax.validation.constraints.NotNull;
import java.util.Date;
import javax.persistence.Temporal;


/**
 * Lop entity ung voi bang abs_overtime_records
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Data
@Entity
@Table(name = "abs_overtime_records")
public class OvertimeRecordsEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "overtime_record_id")
    private Long overtimeRecordId;

    @Column(name = "employee_id")
    private Long employeeId;

    @Column(name = "date_timekeeping")
    @Temporal(javax.persistence.TemporalType.DATE)
    private Date dateTimekeeping;

    @Column(name = "start_time")
    private Date startTime;

    @Column(name = "end_time")
    private Date endTime;

    @Column(name = "total_hours")
    private Double totalHours;

    @Column(name = "overtime_type_id")
    private String overtimeTypeId;

    @Column(name = "content")
    private String content;

    @Transient
    private Long workdayTypeId;


}
