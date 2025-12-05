/*
 * Copyright (C) 2022 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.kpi.repositories.entity;

import lombok.Data;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;


/**
 * Lop entity ung voi bang kpi_indicators
 *
 * @author tudd
 * @version 1.0
 * @since 1.0
 */

@Data
@Entity
@Table(name = "hr_organizations")
public class OrganizationEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "organization_id")
    private Long organizationId;

    @Column(name = "name")
    private String name;

    @Column(name = "parent_id")
    private Long parentId;

    @Column(name = "org_level_manage")
    private Long orgLevelManage;

    @Column(name = "org_type_id")
    private Long orgTypeId;

}
