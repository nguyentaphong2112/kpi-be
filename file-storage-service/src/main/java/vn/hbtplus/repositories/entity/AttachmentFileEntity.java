/*
 * Copyright (C) 2022 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.repositories.entity;

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
@Table(name = "sys_attachment_files")
public class AttachmentFileEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "attachment_file_id")
    private Long attachmentFileId;

    @Column(name = "file_id")
    private String fileId;
    @Column(name = "file_name")
    private String fileName;
    @Column(name = "content_type")
    private String contentType;
    @Column(name = "file_extension")
    private String fileExtension;
    @Column(name = "file_size")
    private Long fileSize;
    @Column(name = "file_path")
    private String filePath;
    @Column(name = "file_path_backup")
    private String filePathBackup;


}
