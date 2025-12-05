package vn.hbtplus.models.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class DomainResponse {
    @Data
    @NoArgsConstructor
    @Schema(name = "DomainResponseDomainDto")
    public static class DomainDto {
        private String domainId;
        private String name;
        private String parentName;
    }
    @Data
    @NoArgsConstructor
    @Schema(name = "DomainResponseDefaultDto")
    public static class DefaultDto {
        private String value;
        private String name;
    }
}
