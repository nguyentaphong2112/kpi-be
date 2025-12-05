package vn.hbtplus.insurance.models.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.hbtplus.models.Attachment;
import vn.hbtplus.utils.PlainTextEncoder;
import vn.hbtplus.utils.Utils;

import java.util.Date;

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
