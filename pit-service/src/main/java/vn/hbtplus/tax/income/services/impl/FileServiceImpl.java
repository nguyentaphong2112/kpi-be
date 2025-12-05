package vn.hbtplus.tax.income.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.feigns.FileStorageFeignClient;
import vn.hbtplus.models.AttachmentFileDto;
import vn.hbtplus.models.BaseResponse;
import vn.hbtplus.tax.income.repositories.entity.AttachmentEntity;
import vn.hbtplus.tax.income.repositories.impl.AttachmentRepository;
import vn.hbtplus.tax.income.repositories.jpa.AttachmentRepositoryJPA;
import vn.hbtplus.tax.income.services.FileService;
import vn.hbtplus.utils.Utils;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FileServiceImpl implements FileService {
    private final FileStorageFeignClient storageFeignClient;
    private final HttpServletRequest request;
    private final AttachmentRepositoryJPA attachmentRepositoryJPA;
    private final AttachmentRepository attachmentRepository;
    @Override
    public void uploadFile(MultipartFile file, Long objectId, String tableName, String fileType, String module) {
        if (file == null) {
            return;
        }
        BaseResponse<AttachmentFileDto> response = storageFeignClient.uploadFile(Utils.getRequestHeader(request), file, module, fileType, null);
        AttachmentFileDto fileResponse = response.getData();
        if (fileResponse != null) {
            AttachmentEntity entity = new AttachmentEntity();
            entity.setFileId(fileResponse.getFileId());
            entity.setFileName(fileResponse.getFileName());
            entity.setFunctionCode(fileType);
            entity.setTableName(tableName);
            entity.setObjectId(objectId);
            entity.setIsDeleted(BaseConstants.STATUS.NOT_DELETED);
            entity.setCreatedBy(Utils.getUserNameLogin());
            entity.setCreatedTime(new Date());
            attachmentRepositoryJPA.save(entity);
        }
    }

    @Override
    public void uploadFiles(List<MultipartFile> listFile, Long objectId, String tableName, String fileType, String module) {
        if (Utils.isNullOrEmpty(listFile)) {
            return;
        }
        BaseResponse<List<AttachmentFileDto>> response = storageFeignClient.uploadListFile(Utils.getRequestHeader(request), listFile, module, fileType, null);
        List<AttachmentFileDto> listResponse = response.getData();

        if (!Utils.isNullOrEmpty(listResponse)) {
            List<AttachmentEntity> listSave = new ArrayList<>();
            for (AttachmentFileDto attachmentFileDto: listResponse) {
                AttachmentEntity attachmentEntity = new AttachmentEntity();
                attachmentEntity.setFileId(attachmentFileDto.getFileId());
                attachmentEntity.setFileName(attachmentFileDto.getFileName());
                attachmentEntity.setFunctionCode(fileType);
                attachmentEntity.setTableName(tableName);
                attachmentEntity.setObjectId(objectId);
                attachmentEntity.setIsDeleted(BaseConstants.STATUS.NOT_DELETED);
                attachmentEntity.setCreatedBy(Utils.getUserNameLogin());
                attachmentEntity.setCreatedTime(new Date());
                listSave.add(attachmentEntity);
            }
            attachmentRepositoryJPA.saveAll(listSave);
        }
    }

    @Override
    public void deActiveFile(List<Long> listObjId, String tableName, String fileType) {
        attachmentRepository.deleteByListObjId(listObjId, tableName, fileType);
    }

    @Override
    public void deActiveFileByAttachmentId(List<Long> listId, String tableName, String fileType) {
        if (!Utils.isNullOrEmpty(listId)) {
            for (Long id : listId) {
                deActiveFileByAttachmentId(id, tableName, fileType);
            }
        }
    }

    @Override
    public void deActiveFileByAttachmentId(Long id, String tableName, String fileType) {
        List<AttachmentEntity> listDocument = attachmentRepository.findByProperties(
                AttachmentEntity.class,
                "attachmentId", id,
                "functionCode", fileType,
                "tableName", tableName
        );

        List<Long> listId = listDocument.stream().map(AttachmentEntity::getAttachmentId).toList();
        attachmentRepository.deActiveObjectByListId(AttachmentEntity.class, listId);
    }
}
