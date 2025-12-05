package vn.hbtplus.tax.income.models.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class AttachmentFileResponse {
    private Long attachmentFileId;
    private String fileName;
    private Long objectId;
    private String type;
}
