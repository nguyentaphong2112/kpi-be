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
 * Lop entity ung voi bang abs_work_calendar_details
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Data
@Entity
@Table(name = "abs_work_calendar_details")
public class WorkCalendarDetailsEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "work_calendar_detail_id")
    private Long workCalendarDetailId;

    @Column(name = "work_calendar_id")
    private Long workCalendarId;

    @Column(name = "date_timekeeping")
    @Temporal(javax.persistence.TemporalType.DATE)
    private Date dateTimekeeping;

    @Column(name = "workday_time_id")
    private String workdayTimeId;

    @Column(name = "description")
    private String description;


}
