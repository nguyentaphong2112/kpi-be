package vn.kpi.models.request;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.kpi.models.BaseSearchRequest;
import vn.kpi.utils.StrimDeSerializer;

import javax.validation.constraints.Size;

public class CategoryAttributeRequest {
    @Data
    @NoArgsConstructor
    @Schema(name = "CategoryAttributeRequestSubmitForm")
    public static class SubmitForm {
        private Long categoryAttributeId;
        private Long categoryId;

        @Size(max = 50)
        @JsonDeserialize(using = StrimDeSerializer.class)
        private String attributeCode;

        @Size(max = 255)
        @JsonDeserialize(using = StrimDeSerializer.class)
        private String attributeValue;

        @Size(max = 50)
        @JsonDeserialize(using = StrimDeSerializer.class)
        private String dataType;

    }
    @Data
    @NoArgsConstructor
    @Schema(name = "CategoryAttributeRequestSearchForm")
    public static class SearchForm extends BaseSearchRequest {

    }
}
