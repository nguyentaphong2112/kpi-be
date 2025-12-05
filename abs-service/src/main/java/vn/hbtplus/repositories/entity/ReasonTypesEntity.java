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
 * Lop entity ung voi bang abs_reason_types
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Data
@Entity
@Table(name = "abs_reason_types")
public class ReasonTypesEntity extends BaseEntity {

    @Id    @GeneratedValue(strategy = GenerationType.IDENTITY)    @Basic(optional = false)    @Column(name = "reason_type_id")    private Long reasonTypeId;    @Column(name = "code")    private String code;    @Column(name = "name")    private String name;    @Column(name = "workday_type_id")    private Long workdayTypeId;    @Column(name = "default_time_off")    private Long defaultTimeOff;    @Column(name = "default_time_off_type")    private String defaultTimeOffType;    @Column(name = "max_time_off")    private Long maxTimeOff;    @Column(name = "year_max_time_off_type")    private String yearMaxTimeOffType;    @Column(name = "max_time_off_type")    private String maxTimeOffType;    @Column(name = "year_max_time_off")    @Temporal(javax.persistence.TemporalType.DATE)    private Date yearMaxTimeOff;    @Column(name = "is_over_holiday")    private String isOverHoliday;

}
