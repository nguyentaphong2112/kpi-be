package vn.kpi.services;

import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface ImportEmployeeService {
    ResponseEntity<Object> processImport(MultipartFile file) throws Exception;
    ResponseEntity<Object> downloadImportTemplate(boolean isForceUpdate) throws Exception;
    ResponseEntity<Object> processImportUpdate(MultipartFile file) throws IOException;
}
