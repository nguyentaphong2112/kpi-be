package vn.hbtplus.services;

import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
import vn.hbtplus.exceptions.BaseAppException;
import vn.hbtplus.models.response.AttachmentFileResponse;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

public interface FileStorageService {
    AttachmentFileResponse uploadFile(MultipartFile file, String module, String functionCode, Object metadata) throws IOException;

    ResponseEntity<Object> downloadFile(String module, String fileId) throws BaseAppException;

    boolean deleteFile(String module, String fileId) throws BaseAppException, IOException;
    boolean deleteFileByList(String module, List<String> fileId) throws BaseAppException, IOException;

    boolean undoDeleteFile(String module, String fileId) throws BaseAppException, IOException;

    List<AttachmentFileResponse> uploadListFile(List<MultipartFile> files, String module, String functionCode, Object metadata) throws IOException;
}
