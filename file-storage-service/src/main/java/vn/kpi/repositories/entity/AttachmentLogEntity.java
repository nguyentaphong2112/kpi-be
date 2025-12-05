/*
 * Copyright (C) 2022 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.kpi.repositories.entity;

import lombok.Data;

import javax.persistence.*;


/**
 * Lop entity ung voi bang sys_user_roles
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Data
@Entity
@Table(name = "sys_attachment_logs")
public class AttachmentLogEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "attachment_log_id")
    private Long attachmentLogId;

    @Column(name = "attachment_file_id")
    private Long attachmentFileId;
    @Column(name = "action_type")
    private String actionType;

    public interface ACTION_TYPE {
        String DOWNLOAD = "DOWNLOAD";
        String DELETE = "DELETE";
        String UNDO_DELETE = "UNDO_DELETE";
    }
}
