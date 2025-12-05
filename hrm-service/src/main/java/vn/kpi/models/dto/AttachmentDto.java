package vn.kpi.models.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.kpi.models.Attachment;
import vn.kpi.utils.Utils;

@Data
@NoArgsConstructor
public class AttachmentDto implements Attachment {
    private Long attachmentId;
    private String fileName;
    @JsonIgnore
    private String fileId;

    @Override
    public String getCheckSum() {
        return Utils.getCheckSum(this.attachmentId);
    }
}
