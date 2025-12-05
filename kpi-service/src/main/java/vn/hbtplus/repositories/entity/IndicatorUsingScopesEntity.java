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


/**
 * Lop entity ung voi bang kpi_indicator_using_scopes
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Data
@Entity
@Table(name = "kpi_indicator_using_scopes")
public class IndicatorUsingScopesEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "")
    private Long indicatorUsingId;

    @Column(name = "indicator_id")
    private Long indicatorId;

    @Column(name = "organization_id")
    private Long organizationId;

    @Column(name = "position_id")
    private Long positionId;

    @Column(name = "job_id")
    private Long jobId;


}
