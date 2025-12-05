package vn.kpi.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import vn.kpi.constants.BaseConstants;
import vn.kpi.exceptions.BaseAppException;
import vn.kpi.models.response.AttachmentFileResponse;
import vn.kpi.repositories.entity.AttachmentFileEntity;
import vn.kpi.repositories.entity.AttachmentLogEntity;
import vn.kpi.repositories.jpa.AttachmentFileRepositoryJPA;
import vn.kpi.repositories.jpa.AttachmentLogRepositoryJPA;
import vn.kpi.services.FileStorageService;
import vn.kpi.utils.ResponseUtils;
import vn.kpi.utils.Utils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;

@Service
@RequiredArgsConstructor
public class FileStorageServiceImpl implements FileStorageService {
    private final AttachmentFileRepositoryJPA attachmentFileRepositoryJPA;
    private final AttachmentLogRepositoryJPA attachmentLogRepositoryJPA;
    @Value("${service.upload.rootFolder}")
    private String folderUpload;
    @Value("${service.upload.deletedFolder}")
    private String folderDeleted;

    @Override
    @Transactional
    public AttachmentFileResponse uploadFile(MultipartFile file, String module, String functionCode, Object metadata) throws IOException {
        List<AttachmentFileResponse> listResponse = this.uploadFileProcess(List.of(file), module, functionCode, metadata);
        AttachmentFileResponse response = new AttachmentFileResponse();
        if (!Utils.isNullOrEmpty(listResponse)) {
            response = listResponse.get(0);
        }
        return response;
    }

    @Override
    @Transactional
    public ResponseEntity<Object> downloadFile(String module, String fileId) throws BaseAppException {
        AttachmentFileEntity entity = attachmentFileRepositoryJPA.getByFileId(fileId);
        if (entity == null) {
            throw new BaseAppException("ERROR_FILE_NOT_EXISTS", "error.downloadFile.fileIdNotExists");
        }
        //luu thong tin nguoi download file
        AttachmentLogEntity logEntity = new AttachmentLogEntity();
        logEntity.setAttachmentFileId(entity.getAttachmentFileId());
        logEntity.setActionType(AttachmentLogEntity.ACTION_TYPE.DOWNLOAD);
        logEntity.setCreatedTime(new Date());
        logEntity.setCreatedBy(Utils.getUserNameLogin());
        attachmentLogRepositoryJPA.save(logEntity);
        return ResponseUtils.getResponseFileEntity(entity.getFilePath(), false);
    }

    @Override
    @Transactional
    public boolean deleteFile(String module, String fileId) throws BaseAppException, IOException {
        AttachmentFileEntity entity = attachmentFileRepositoryJPA.getByFileId(fileId);
        if (entity == null) {
            throw new BaseAppException("ERROR_FILE_NOT_EXISTS", "error.deleteFile.fileIdNotExists");
        }
        String backupPath = getFolderBackup(module);
        File folder = new File(backupPath);
        if (!folder.exists()) {
            folder.mkdirs();
        }
        this.deleteFileProcess(entity, backupPath);

        return false;
    }
    @Override
    @Transactional
    public boolean deleteFileByList(String module, List<String> listFileId) throws BaseAppException, IOException {
        if (Utils.isNullOrEmpty(listFileId)) {
            return false;
        }
        List<AttachmentFileEntity> listEntity = attachmentFileRepositoryJPA.getByListFileId(listFileId);
        Map<String, AttachmentFileEntity> mapData = new HashMap<>();
        for (AttachmentFileEntity entity: listEntity) {
            mapData.put(entity.getFileId(), entity);
        }

        String backupPath = getFolderBackup(module);
        File folder = new File(backupPath);
        if (!folder.exists()) {
            folder.mkdirs();
        }
        for (String fileId: listFileId) {
            AttachmentFileEntity entity = mapData.get(fileId);
            if (entity == null) {
                throw new BaseAppException("ERROR_FILE_NOT_EXISTS", "error.deleteFile.fileIdNotExists");
            }
            this.deleteFileProcess(entity, backupPath);
        }

        return false;
    }


    private void deleteFileProcess(AttachmentFileEntity entity, String backupPath) throws BaseAppException, IOException {
        //copy file sang thu muc delete
        Path sourceFile = Paths.get(entity.getFilePath());
        Path destFile = Paths.get(backupPath + File.separator + entity.getFileId());
        if(!sourceFile.toFile().exists()){
            throw new BaseAppException("ERROR_FILE_NOT_EXISTS", "error.deleteFile.fileNotExists");
        }
        Files.copy(sourceFile, destFile, StandardCopyOption.REPLACE_EXISTING);

        //thuc hien chuyen file vao thu muc delete
        entity.setIsDeleted(BaseConstants.STATUS.DELETED);
        entity.setModifiedTime(new Date());
        entity.setModifiedBy(Utils.getUserNameLogin());
        entity.setFilePathBackup(backupPath + File.separator + entity.getFileId());
        attachmentFileRepositoryJPA.save(entity);


        //luu thong tin nguoi download file
        AttachmentLogEntity logEntity = new AttachmentLogEntity();
        logEntity.setAttachmentFileId(entity.getAttachmentFileId());
        logEntity.setActionType(AttachmentLogEntity.ACTION_TYPE.DELETE);
        logEntity.setCreatedTime(new Date());
        logEntity.setCreatedBy(Utils.getUserNameLogin());
        attachmentLogRepositoryJPA.save(logEntity);

        //xoa file cu di
        Files.delete(sourceFile);
    }

    @Override
    public boolean undoDeleteFile(String module, String fileId) throws BaseAppException, IOException {
        AttachmentFileEntity entity = attachmentFileRepositoryJPA.getByFileId(fileId);
        if (entity == null) {
            throw new BaseAppException("ERROR_FILE_NOT_EXISTS", "error.undoDelete.fileIdNotExists");
        }

        Path sourceFile = Paths.get(entity.getFilePathBackup());
        if(!sourceFile.toFile().exists()){
            throw new BaseAppException("ERROR_FILE_NOT_EXISTS", "error.undoDelete.fileNotExists");
        }

        Path destFile = Paths.get(entity.getFilePath());
        Files.copy(sourceFile, destFile, StandardCopyOption.REPLACE_EXISTING);

        //luu thong tin nguoi download file
        AttachmentLogEntity logEntity = new AttachmentLogEntity();
        logEntity.setAttachmentFileId(entity.getAttachmentFileId());
        logEntity.setActionType(AttachmentLogEntity.ACTION_TYPE.UNDO_DELETE);
        logEntity.setCreatedTime(new Date());
        logEntity.setCreatedBy(Utils.getUserNameLogin());
        attachmentLogRepositoryJPA.save(logEntity);

        //active lai file
        entity.setIsDeleted(BaseConstants.STATUS.NOT_DELETED);
        entity.setModifiedTime(new Date());
        entity.setModifiedBy(Utils.getUserNameLogin());
        attachmentFileRepositoryJPA.save(entity);

        //xoa file backup
        Files.delete(sourceFile);

        return true;

    }

    @Override
    public List<AttachmentFileResponse> uploadListFile(List<MultipartFile> files, String module, String functionCode, Object metadata) throws IOException {
        return uploadFileProcess(files, module, functionCode, metadata);
    }

    private String getFolderBackup(String module) {
        return Utils.join(File.separator, folderDeleted, module, Utils.formatDate(new Date(), "yyyy"), Utils.formatDate(new Date(), "MMdd"));
    }

    private String getFolderUpload(String module, String functionCode) {
        return Utils.join(File.separator, folderUpload, module, functionCode, Utils.formatDate(new Date(), "yyyy"), Utils.formatDate(new Date(), "MMdd"));
    }

    private List<AttachmentFileResponse> uploadFileProcess(List<MultipartFile> files, String module, String functionCode, Object metadata) throws IOException {
        if (!Utils.isNullOrEmpty(files)) {
            String folderPath = getFolderUpload(module, functionCode);
            File folder = new File(folderPath);
            if (!folder.exists()) {
                folder.mkdirs();
            }

            List<AttachmentFileEntity> listSave = new ArrayList<>();
            Date curDate = new Date();
            String userName = Utils.getUserNameLogin();
            for (MultipartFile file: files) {
                String fileId = UUID.randomUUID().toString();
                String pathFile = getFolderUpload(module, functionCode) + File.separator + fileId;
                OutputStream outStream = new FileOutputStream(pathFile);
                InputStream inStream = file.getInputStream();
                int bytesRead;
                byte[] buffer = new byte[1024 * 8];
                while ((bytesRead = inStream.read(buffer, 0, 1024 * 8)) != -1) {
                    outStream.write(buffer, 0, bytesRead);
                }
                inStream.close();
                outStream.close();

                //save file
                AttachmentFileEntity entity = new AttachmentFileEntity();
                entity.setFileName(file.getOriginalFilename());
                if (Utils.NVL(file.getOriginalFilename()).contains(".")) {
                    String extension = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf(".") + 1);
                    entity.setFileExtension(extension.toLowerCase());
                } else {
                    entity.setFileExtension("");
                }
                entity.setFileId(fileId);
                entity.setContentType(file.getContentType());
                entity.setFileSize(file.getSize());
                entity.setFilePath(pathFile);
                entity.setCreatedTime(curDate);
                entity.setCreatedBy(userName);
                listSave.add(entity);
            }
            attachmentFileRepositoryJPA.saveAll(listSave);
            return Utils.mapAll(listSave, AttachmentFileResponse.class);
        } else {
            return new ArrayList<>();
        }
    }
}
