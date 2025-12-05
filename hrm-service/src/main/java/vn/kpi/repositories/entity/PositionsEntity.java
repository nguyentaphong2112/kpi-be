/*
 * Copyright (C) 2022 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.kpi.repositories.entity;

import lombok.Data;

import javax.persistence.*;


/**
 * Lop entity ung voi bang hr_positions
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Data
@Entity
@Table(name = "hr_positions")
public class PositionsEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "position_id")
    private Long positionId;

    @Column(name = "job_id")
    private Long jobId;

    @Column(name = "organization_id")
    private Long organizationId;

    @Column(name = "name")
    private String name;

    @Column(name = "quota_number")
    private Integer quotaNumber;


}
