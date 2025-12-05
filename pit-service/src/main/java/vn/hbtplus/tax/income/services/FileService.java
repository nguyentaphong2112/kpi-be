package vn.hbtplus.tax.income.services;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface FileService {
    void uploadFile(MultipartFile file, Long objectId, String tableName, String fileType, String module);
    void uploadFiles(List<MultipartFile> listFile, Long objectId, String tableName, String fileType, String module);
    void deActiveFile(List<Long> listObjId, String tableName, String fileType);
    void deActiveFileByAttachmentId(List<Long> listAttachmentId, String tableName, String fileType);
    void deActiveFileByAttachmentId(Long listAttachmentId, String tableName, String fileType);

}
