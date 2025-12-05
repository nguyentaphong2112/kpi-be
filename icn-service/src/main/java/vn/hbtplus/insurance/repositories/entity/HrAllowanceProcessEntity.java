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
 * Lop entity ung voi bang hr_allowance_process
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Data
@Entity
@Table(name = "hr_allowance_process")
public class HrAllowanceProcessEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @NotNull
    @Column(name = "allowance_process_id")
    private Long allowanceProcessId;

    @Column(name = "allowance_type")
    private String allowanceType;

    @Column(name = "employee_id")
    private Long employeeId;

    @Column(name = "factor")
    private Double factor;

    @Column(name = "amount")
    private Long amount;

    @Column(name = "start_date")
    @Temporal(javax.persistence.TemporalType.DATE)
    private Date startDate;

    @Column(name = "end_date")
    @Temporal(javax.persistence.TemporalType.DATE)
    private Date endDate;


}
