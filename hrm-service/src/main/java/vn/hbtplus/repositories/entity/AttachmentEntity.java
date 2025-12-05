package vn.hbtplus.repositories.entity;

import lombok.Data;

import javax.persistence.*;

@Data
@Entity
@Table(name = "hr_attachments")
public class AttachmentEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "attachment_id")
    private Long attachmentId;

    @Column(name = "file_id")
    private String fileId;
    @Column(name = "file_name")
    private String fileName;
    @Column(name = "object_id")
    private Long objectId;
    @Column(name = "table_name")
    private String tableName;
    @Column(name = "function_code")
    private String functionCode;
}