package vn.hbtplus.insurance.repositories.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import vn.hbtplus.insurance.repositories.entity.AttachmentEntity;

import java.util.List;

@Repository
public interface AttachmentRepositoryJPA extends JpaRepository<AttachmentEntity, Long> {

    @Query("from AttachmentEntity a " +
            " where a.tableName = :tableName and a.functionCode = :fileType and a.objectId = :objectId" +
            " and a.isDeleted = 'N'" +
            " order by a.createdTime desc")
    List<AttachmentEntity> getAttachments(String tableName, String fileType, Long objectId);
}
