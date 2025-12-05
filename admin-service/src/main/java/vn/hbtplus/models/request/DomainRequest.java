package vn.hbtplus.models.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.hbtplus.models.BaseSearchRequest;

@Data
@NoArgsConstructor
public class DomainRequest {
    @Data
    @NoArgsConstructor
    @Schema(name = "DomainRequestSearchForm")
    public static class SearchForm extends BaseSearchRequest {
        private String parentKey;
    }
}
