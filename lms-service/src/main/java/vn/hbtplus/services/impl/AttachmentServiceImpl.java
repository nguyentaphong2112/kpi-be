package vn.hbtplus.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.constants.Constant;
import vn.hbtplus.exceptions.BaseAppException;
import vn.hbtplus.exceptions.RecordNotExistsException;
import vn.hbtplus.feigns.FileStorageFeignClient;
import vn.hbtplus.models.Attachment;
import vn.hbtplus.models.AttachmentFileDto;
import vn.hbtplus.repositories.entity.AttachmentEntity;
import vn.hbtplus.repositories.impl.AttachmentRepository;
import vn.hbtplus.repositories.jpa.AttachmentRepositoryJPA;
import vn.hbtplus.services.AttachmentService;
import vn.hbtplus.utils.ResponseUtils;
import vn.hbtplus.utils.Utils;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AttachmentServiceImpl implements AttachmentService {
    private final AttachmentRepositoryJPA attachmentRepositoryJPA;
    private final AttachmentRepository attachmentRepository;
    private final FileStorageFeignClient storageFeignClient;
    private final HttpServletRequest request;

    @Override
    public void inactiveAttachment(String tableName, String fileType, Long objectId) {
        List<AttachmentEntity> attachmentEntities = attachmentRepositoryJPA.getAttachments(tableName, fileType, objectId);
        if(!Utils.isNullOrEmpty(attachmentEntities)){
            attachmentEntities.stream().forEach(item -> {
                item.setIsDeleted(BaseConstants.STATUS.DELETED);
                item.setModifiedBy(Utils.getUserNameLogin());
                item.setModifiedTime(new Date());
                attachmentRepositoryJPA.save(item);
                //xoa file tren he thong

            });
        }
    }

    @Override
    public void saveAttachment(String tableName, String fileType, Long objectId, AttachmentFileDto fileId) {
        AttachmentEntity attachmentEntity = new AttachmentEntity();
        attachmentEntity.setFileId(fileId.getFileId());
        attachmentEntity.setFileName(fileId.getFileName());
        attachmentEntity.setFunctionCode(fileType);
        attachmentEntity.setTableName(tableName);
        attachmentEntity.setObjectId(objectId);
        attachmentEntity.setIsDeleted(BaseConstants.STATUS.NOT_DELETED);
        attachmentEntity.setCreatedBy(Utils.getUserNameLogin());
        attachmentEntity.setCreatedTime(new Date());
        attachmentRepositoryJPA.save(attachmentEntity);

    }

    @Override
    public Attachment getAttachmentEntity(String tableName, String fileType, Long objectId) {
        List<Attachment> attachments = attachmentRepository.getAttachments(tableName, fileType, objectId);
        return Utils.isNullOrEmpty(attachments) ? null : attachments.get(0);
    }

    @Override
    public List<Attachment> getAttachmentEntities(String tableName, String fileType, Long objectId) {
        return attachmentRepository.getAttachments(tableName, fileType, objectId);
    }

    @Override
    public List<AttachmentFileDto> getAttachmentList(String tableName, String fileType, List<Long> objIds) {
        return null;
    }

    @Override
    public List<Attachment> getAttachmentListByObjectId(String tableName, String fileType, Long objId) {
        return null;
    }

    @Override
    public ResponseEntity<Object> downloadAttachment(Long attachmentId, String checksum) throws BaseAppException, IOException {
        validateChecksum(attachmentId, checksum);
        Optional<AttachmentEntity> optional = attachmentRepositoryJPA.findById(attachmentId);
        if (optional.isEmpty() || optional.get().isDeleted()) {
            throw new RecordNotExistsException(attachmentId, AttachmentEntity.class);
        }
        AttachmentEntity attachmentEntity = optional.get();

        byte[] fileContent = storageFeignClient.downloadFile(Utils.getRequestHeader(request), Constant.ATTACHMENT.MODULE, attachmentEntity.getFileId());
        return ResponseUtils.getResponseFileEntity(fileContent, attachmentEntity.getFileName());
    }

    @Override
    public InputStream downloadFileInputStream(Long fileId) throws BaseAppException, IOException {
        return null;
    }
}
