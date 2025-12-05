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
import javax.validation.constraints.NotNull;
import java.util.Date;
import javax.persistence.Temporal;


/**
 * Lop entity ung voi bang crm_log_actions
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Data
@Entity
@Table(name = "crm_log_actions")
public class LogActionsEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "log_action_id")
    private Long logActionId;

    @Column(name = "action")
    private String action;

    @Column(name = "action_name")
    private String actionName;

    @Column(name = "obj_type")
    private String objType;

    @Column(name = "obj_id")
    private Long objId;

    @Column(name = "data_before")
    private String dataBefore;

    @Column(name = "data_after")
    private String dataAfter;

    @Column(name = "obj_name")
    private String objName;

}
