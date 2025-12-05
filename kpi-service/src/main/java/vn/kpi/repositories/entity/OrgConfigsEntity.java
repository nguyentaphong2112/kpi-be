/*
 * Copyright (C) 2022 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.kpi.repositories.entity;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.GenerationType;
import javax.persistence.Table;

import lombok.Data;


/**
 * Lop entity ung voi bang kpi_org_configs
 *
 * @author tudd
 * @version 1.0
 * @since 1.0
 */

@Data
@Entity
@Table(name = "kpi_org_configs")
public class OrgConfigsEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "org_config_id")
    private Long orgConfigId;

    @Column(name = "organization_id")
    private Long organizationId;

    @Column(name = "year")
    private Long year;

    @Column(name = "org_type_id")
    private String orgTypeId;

    @Column(name = "note")
    private String note;


}
