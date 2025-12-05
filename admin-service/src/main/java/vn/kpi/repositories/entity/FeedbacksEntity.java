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
@Table(name = "sys_feedbacks")
public class FeedbacksEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "feedback_id")
    private Long feedbackId;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "content")
    private String content;

    @Column(name = "object_id")
    private Long objectId;

    @Column(name = "function_code")
    private String functionCode;

    @Column(name = "status")
    private String status;

    @Column(name = "type")
    private String type;

    public interface STATUS {
        String NEW = "NEW";
        String CLOSED = "CLOSED";
        String PROCESSING = "PROCESSING";
    }
}
