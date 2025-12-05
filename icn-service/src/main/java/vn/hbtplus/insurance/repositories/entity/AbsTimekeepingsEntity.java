/*
 * Copyright (C) 2022 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.insurance.repositories.entity;

import lombok.Data;
import vn.hbtplus.repositories.entity.BaseEntity;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Date;


/**
 * Lop entity ung voi bang hr_timekeepings
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Data
@Entity
@Table(name = "abs_timekeepings")
public class AbsTimekeepingsEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @NotNull
    @Column(name = "timekeeping_id")
    private Long timekeepingId;

    @Column(name = "employee_id")
    private Long employeeId;

    @Column(name = "date_timekeeping")
    @Temporal(javax.persistence.TemporalType.DATE)
    private Date dateTimekeeping;

    @Column(name = "workday_type_id")
    private Long workdayTypeId;

    @Column(name = "total_hours")
    private Double totalHours;


}
