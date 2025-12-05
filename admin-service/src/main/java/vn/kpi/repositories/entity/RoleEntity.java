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
 * Lop entity ung voi bang sys_roles
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Data
@Entity
@Table(name = "sys_roles")
public class RoleEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "role_id")
    private Long roleId;

    @Column(name = "code")
    private String code;
    @Column(name = "note")
    private String note;

    @Column(name = "name")
    private String name;

    @Column(name = "default_domain_type")
    private String defaultDomainType;

    @Column(name = "default_domain_value")
    private String defaultDomainValue;


}
