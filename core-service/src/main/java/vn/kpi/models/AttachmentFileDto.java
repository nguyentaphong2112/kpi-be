package vn.kpi.models;

import lombok.Data;
import lombok.NoArgsConstructor;
import vn.kpi.utils.Utils;

@Data
@NoArgsConstructor
public class AttachmentFileDto {
    private String fileId;

    private String fileName;

    private String contentType;

    private String fileExtension;

    private Long fileSize;

    private Long objectId;
    private Long attachmentId;
    private String fileContent;

    public String getCheckSum() {
        return Utils.getCheckSum(this.attachmentId);
    }
}
