package vn.kpi.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import vn.kpi.annotations.HasPermission;
import vn.kpi.annotations.UserLogActivity;
import vn.kpi.constants.BaseConstants;
import vn.kpi.constants.Scope;
import vn.kpi.exceptions.BaseAppException;
import vn.kpi.models.dto.AttachmentFileRequest;
import vn.kpi.models.response.AttachmentFileResponse;
import vn.kpi.models.response.BaseResponseEntity;
import vn.kpi.models.response.ListResponseEntity;
import vn.kpi.services.FileStorageService;
import vn.kpi.utils.ResponseUtils;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping(BaseConstants.REQUEST_MAPPING_PREFIX)
@RequiredArgsConstructor
public class FileStorageController {
    private final FileStorageService fileStorageService;

    @PostMapping(value = "/v1/upload-file/{module}",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    @UserLogActivity
    public BaseResponseEntity<AttachmentFileResponse> uploadFile(@PathVariable String module,
                                                                 AttachmentFileRequest.SubmitForm form
                                         ) throws IOException {
        return ResponseUtils.ok(fileStorageService.uploadFile(form.getFile(), module, form.getFunctionCode(), form.getMetadata()));
    }

    @PostMapping(value = "/v1/upload-file/list/{module}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public ListResponseEntity<AttachmentFileResponse> uploadListFile(@RequestPart(value = "files") List<MultipartFile> files,
                                                                     @PathVariable String module,
                                                                     @RequestPart(value = "functionCode") String functionCode,
                                                                     @RequestPart(value = "metadata", required = false) Object metadata
    ) throws IOException {
        return ResponseUtils.ok(fileStorageService.uploadListFile(files, module, functionCode, metadata));
    }

    @GetMapping(value = "/v1/download-file/{module}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public ResponseEntity<Object> downloadFile(@PathVariable String module, @RequestParam String fileId) throws BaseAppException {
        return fileStorageService.downloadFile(module, fileId);
    }

    @DeleteMapping(value = "/v1/delete-file/{module}/{fileId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public BaseResponseEntity<Boolean> deleteFile(@PathVariable String module, @PathVariable String fileId) throws BaseAppException, IOException {
        return ResponseUtils.ok(fileStorageService.deleteFile(module, fileId));
    }

    @DeleteMapping(value = "/v1/delete-file/{module}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public BaseResponseEntity<Boolean> deleteFileByList(@PathVariable String module, @RequestParam List<String> listFileId) throws BaseAppException, IOException {
        return ResponseUtils.ok(fileStorageService.deleteFileByList(module, listFileId));
    }

    @PutMapping(value = "/v1/undo-delete-file/{module}/{fileId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public BaseResponseEntity<Boolean> undoDeleteFile(@PathVariable String module, @PathVariable String fileId) throws BaseAppException, IOException {
        return ResponseUtils.ok(fileStorageService.undoDeleteFile(module, fileId));
    }
}
