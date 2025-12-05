/*
 * Copyright (C) 2022 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.kpi.repositories.entity;

import lombok.Data;

import javax.persistence.*;


/**
 * Lop entity ung voi bang hr_position_groups
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Data
@Entity
@Table(name = "hr_position_groups")
public class PositionGroupsEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "position_group_id")
    private Long positionGroupId;

    @Column(name = "group_type_id")
    private String groupTypeId;

    @Column(name = "code")
    private String code;

    @Column(name = "name")
    private String name;


}
