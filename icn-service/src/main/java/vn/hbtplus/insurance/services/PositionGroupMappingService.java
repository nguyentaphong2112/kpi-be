package vn.hbtplus.insurance.services;

import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

public interface PositionGroupMappingService {
    ResponseEntity<Object> downloadTemplate() throws Exception;

    void processImport(MultipartFile fileImport) throws Exception;
}
