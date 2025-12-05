package vn.kpi.models.response;

import lombok.Data;

@Data
public class AttachmentFileResponse {

    private String fileId;

    private String fileName;

    private String contentType;

    private String fileExtension;

    private Long fileSize;
}
