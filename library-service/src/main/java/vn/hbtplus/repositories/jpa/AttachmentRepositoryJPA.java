/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.repositories.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import vn.hbtplus.repositories.entity.AttachmentEntity;

import java.util.List;

/**
 * Lop repository JPA ung voi bang lib_books
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Repository
public interface AttachmentRepositoryJPA extends JpaRepository<AttachmentEntity, Long> {

    @Query("from AttachmentEntity a " +
            " where a.tableName = :tableName and a.functionCode = :fileType and a.objectId = :objectId" +
            " and a.isDeleted = 'N'" +
            " order by a.createdTime desc")
    List<AttachmentEntity> getAttachments(String tableName, String fileType, Long objectId);
}
