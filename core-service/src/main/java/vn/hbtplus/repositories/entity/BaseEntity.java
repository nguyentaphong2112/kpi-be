package vn.hbtplus.repositories.entity;

import lombok.Data;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import vn.hbtplus.constants.BaseConstants;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.util.Date;

@Data
@MappedSuperclass
public class BaseEntity {
    @Column(name = "is_deleted", columnDefinition = "VARCHAR(1) DEFAULT 'N'")
    private String isDeleted = BaseConstants.STATUS.NOT_DELETED;

    @Column(name = "created_by", updatable = false)
    @CreatedBy
    private String createdBy;

    @Column(name = "created_time", updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    @CreatedDate
    private Date createdTime;

    @LastModifiedBy
    @Column(name = "modified_by", insertable = false)
    private String modifiedBy;

    @LastModifiedDate
    @Column(name = "modified_time", insertable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date modifiedTime;

    public boolean isDeleted(){
        return "Y".equalsIgnoreCase(isDeleted);
    }
}
