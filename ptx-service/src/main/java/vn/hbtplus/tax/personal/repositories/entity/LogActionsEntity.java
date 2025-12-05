/*
 * Copyright (C) 2022 EcoIT. All rights reserved.
 * EcoIT. Use is subject to license terms.
 */
package vn.hbtplus.tax.personal.repositories.entity;

import lombok.Data;
import vn.hbtplus.repositories.entity.BaseEntity;

import javax.persistence.*;
import java.util.Date;


/**
 * Lop entity ung voi bang PTX_LOG_ACTIONS
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Data
@Entity
@Table(name = "ptx_log_actions")
public class LogActionsEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "log_action_id")
    private Long logActionId;

    @Column(name = "object_id")
    private Long objectId;

    @Column(name = "object_type")
    private String objectType;

    @Column(name = "content")
    private String content;

}
