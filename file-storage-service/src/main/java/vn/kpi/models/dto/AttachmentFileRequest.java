package vn.kpi.models.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotNull;

@Data
@NoArgsConstructor
public class AttachmentFileRequest {
    @Data
    @NoArgsConstructor
    @Schema(name = "AttachmentRequestSubmitForm")
    @AllArgsConstructor
    @Builder
    public static class SubmitForm {
        @NotNull
        @JsonIgnore
        private MultipartFile file;
        @NotNull
        private String functionCode;
        private Object metadata;
    }
}
