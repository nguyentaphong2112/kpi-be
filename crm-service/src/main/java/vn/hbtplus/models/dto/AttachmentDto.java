package vn.hbtplus.models.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.hbtplus.models.Attachment;
import vn.hbtplus.utils.Utils;

@Data
@NoArgsConstructor
public class AttachmentDto implements Attachment {
    private Long attachmentId;
    private String fileName;
    private String fileId;
    private String fileContent;

    @Override
    public String getCheckSum() {
        return Utils.getCheckSum(this.attachmentId);
    }
}
