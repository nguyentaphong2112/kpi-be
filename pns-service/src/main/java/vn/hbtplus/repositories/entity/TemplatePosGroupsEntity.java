/*
 * Copyright (C) 2022 EcoIT. All rights reserved.
 * EcoIT. Use is subject to license terms.
 */
package vn.hbtplus.repositories.entity;

import lombok.Data;

import javax.persistence.*;
import java.util.Date;


/**
 * Lop entity ung voi bang PNS_TEMPLATE_POS_GROUPS
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Data
@Entity
@Table(name = "pns_template_pos_groups")
public class TemplatePosGroupsEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "template_pos_group_id")
    private Long templatePosGroupId;

    @Column(name = "contract_template_id")
    private Long contractTemplateId;

    @Column(name = "position_group_id")
    private Long positionGroupId;

}
