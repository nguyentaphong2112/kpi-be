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
import lombok.NoArgsConstructor;
import vn.hbtplus.constants.BaseConstants;

import javax.persistence.Transient;
import javax.validation.constraints.NotNull;
import java.util.Date;
import javax.persistence.Temporal;


/**
 * Lop entity ung voi bang abs_timekeepings
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Data
@Entity
@Table(name = "abs_timekeepings")
@NoArgsConstructor
public class TimekeepingsEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "timekeeping_id")
    private Long timekeepingId;

    @Column(name = "employee_id")
    private Long employeeId;

    @Column(name = "date_timekeeping")
    @Temporal(javax.persistence.TemporalType.DATE)
    private Date dateTimekeeping;

    @Column(name = "workday_type_id")
    private Long workdayTypeId;
    @Column(name = "organization_id")
    private Long organizationId;

    @Column(name = "total_hours")
    private Double totalHours;

    @Transient
    private String workdayTypeCode;

    public TimekeepingsEntity(Long employeeId, Date dateTimekeeping, Long organizationId) {
        this.employeeId = employeeId;
        this.dateTimekeeping = dateTimekeeping;
        this.organizationId = organizationId;
        this.setIsDeleted(BaseConstants.STATUS.NOT_DELETED);
        this.setCreatedTime(new Date());
    }
}
