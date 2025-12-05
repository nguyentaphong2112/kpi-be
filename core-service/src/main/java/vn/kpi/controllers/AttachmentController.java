package vn.kpi.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.kpi.services.AttachmentService;

@RestController
@RequestMapping("")
@RequiredArgsConstructor
public class AttachmentController {

    private final AttachmentService attachmentService;

    @GetMapping(value = "v1/attachment-file/download/{attachmentId}/{checksum}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> downloadAttachment(@PathVariable Long attachmentId, @PathVariable String checksum) throws Exception {
        return attachmentService.downloadAttachment(attachmentId, checksum);
    }
}
