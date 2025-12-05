package vn.hbtplus.feigns;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;
import vn.hbtplus.models.AttachmentFileDto;
import vn.hbtplus.models.BaseResponse;

import java.util.List;

@FeignClient(value = "file-storage-client", url = "${service.properties.file-client-url:default}")
public interface FileStorageFeignClient {

    String BREAKER_NAME = "fileStorage";

    @Retry(name = "personInfoRetry")
    @CircuitBreaker(name = BREAKER_NAME + "uploadFile", fallbackMethod = "retryUploadFile")
    @PostMapping(value = "/v1/upload-file/{module}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    BaseResponse<AttachmentFileDto> uploadFile(@RequestHeader HttpHeaders httpHeaders, @RequestPart(value = "file") MultipartFile file,
                                               @PathVariable String module,
                                               @RequestPart(value = "functionCode") String functionCode,
                                               @RequestPart(value = "metadata", required = false) Object metadata
    );

    @Retry(name = "personInfoRetry")
    @CircuitBreaker(name = BREAKER_NAME + "uploadFile", fallbackMethod = "retryUploadFile")
    @PostMapping(value = "/v1/upload-file/list/{module}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    BaseResponse<List<AttachmentFileDto>> uploadListFile(@RequestHeader HttpHeaders httpHeaders, @RequestPart(value = "files") List<MultipartFile> files,
                                                         @PathVariable String module,
                                                         @RequestPart(value = "functionCode") String functionCode,
                                                         @RequestPart(value = "metadata", required = false) Object metadata
    );

    @Retry(name = "personInfoRetry")
    @CircuitBreaker(name = BREAKER_NAME + "uploadFile", fallbackMethod = "retryUploadFile")
    @GetMapping(value = "/v1/download-file/{module}", produces = MediaType.APPLICATION_JSON_VALUE)
    byte[] downloadFile(@RequestHeader HttpHeaders httpHeaders, @PathVariable String module, @RequestParam String fileId);
}
