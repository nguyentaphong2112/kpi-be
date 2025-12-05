package vn.kpi.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.kpi.exceptions.BaseAppException;
import vn.kpi.models.Attachment;
import vn.kpi.models.AttachmentFileDto;
import vn.kpi.services.AttachmentService;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AttachmentServiceImpl implements AttachmentService {
//    private final AttachmentRepositoryJPA attachmentRepositoryJPA;
//    private final AttachmentRepository attachmentRepository;
//    private final FileStorageFeignClient storageFeignClient;
//    private final HttpServletRequest request;

    @Override
    public void inactiveAttachment(String tableName, String fileType, Long objectId) {
//        List<AttachmentEntity> attachmentEntities = attachmentRepositoryJPA.getAttachments(tableName, fileType, objectId);
//        if(!Utils.isNullOrEmpty(attachmentEntities)){
//            attachmentEntities.stream().forEach(item -> {
//                item.setIsDeleted(BaseConstants.STATUS.DELETED);
//                item.setModifiedBy(Utils.getUserNameLogin());
//                item.setModifiedTime(new Date());
//                attachmentRepositoryJPA.save(item);
//                //xoa file tren he thong
//
//            });
//        }
    }

    @Override
    public void saveAttachment(String tableName, String fileType, Long objectId, AttachmentFileDto fileId) {
//        AttachmentEntity attachmentEntity = new AttachmentEntity();
//        attachmentEntity.setFileId(fileId.getFileId());
//        attachmentEntity.setFileName(fileId.getFileName());
//        attachmentEntity.setFunctionCode(fileType);
//        attachmentEntity.setTableName(tableName);
//        attachmentEntity.setObjectId(objectId);
//        attachmentEntity.setIsDeleted(BaseConstants.STATUS.NOT_DELETED);
//        attachmentEntity.setCreatedBy(Utils.getUserNameLogin());
//        attachmentEntity.setCreatedTime(new Date());
//        attachmentRepositoryJPA.save(attachmentEntity);

    }

    @Override
    public Attachment getAttachmentEntity(String tableName, String fileType, Long objectId) {
//        List<Attachment> attachments = attachmentRepository.getAttachments(tableName, fileType, objectId);
//        return Utils.isNullOrEmpty(attachments) ? null : attachments.get(0);
        return null;
    }

    @Override
    public List<Attachment> getAttachmentEntities(String tableName, String fileType, Long objectId) {
//        return attachmentRepository.getAttachments(tableName, fileType, objectId);
        return new ArrayList<>();
    }

    @Override
    public ResponseEntity<Object> downloadAttachment(Long attachmentId, String checksum) throws BaseAppException, IOException {
//        validateChecksum(attachmentId, checksum);
//        Optional<AttachmentEntity> optional = attachmentRepositoryJPA.findById(attachmentId);
//        if (optional.isEmpty() || optional.get().isDeleted()) {
//            throw new RecordNotExistsException(attachmentId, AttachmentEntity.class);
//        }
//        AttachmentEntity attachmentEntity = optional.get();
//
//        byte[] fileContent = storageFeignClient.downloadFile(Utils.getRequestHeader(request), Constant.ATTACHMENT.MODULE, attachmentEntity.getFileId());
//        return ResponseUtils.getResponseFileEntity(fileContent, attachmentEntity.getFileName());
        return null;
    }

    @Override
    public InputStream downloadFileInputStream(Long attachmentId) throws BaseAppException {
//        Optional<AttachmentEntity> optional = attachmentRepositoryJPA.findById(attachmentId);
//        if (optional.isEmpty() || optional.get().isDeleted()) {
//            throw new RecordNotExistsException(attachmentId, AttachmentEntity.class);
//        }
//        AttachmentEntity attachmentEntity = optional.get();
//
//        byte[] fileContent = storageFeignClient.downloadFile(Utils.getRequestHeader(request), Constant.ATTACHMENT.MODULE, attachmentEntity.getFileId());
//        return new ByteArrayInputStream(fileContent);
        return null;
    }

    @Override
    @Transactional(readOnly = true)
    public List<AttachmentFileDto> getAttachmentList(String tableName, String fileType, List<Long> objIds) {
//        return attachmentRepository.getAttachments(tableName, fileType, objIds);
        return new ArrayList<>();
    }

    @Override
    public List<Attachment> getAttachmentListByObjectId(String tableName, String fileType, Long objId) {
        return null;
    }
}
