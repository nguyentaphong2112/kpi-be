package vn.hbtplus.models.request;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.hbtplus.models.AttributeRequestDto;
import vn.hbtplus.models.BaseSearchRequest;
import vn.hbtplus.utils.StrimDeSerializer;

import javax.validation.constraints.Size;
import java.util.List;

@Data
@NoArgsConstructor
public class ConfigPageRequest  {
    @Data
    @NoArgsConstructor
    @Schema(name = "ConfigParameterRequestSubmitForm")
    public static class SubmitForm {
        private String url;
        private String reportCodes;
        private String type;
        List<AttributeRequestDto> listAttributes;

    }


    @Data
    @NoArgsConstructor
    @Schema(name = "ConfigPageRequestSearchForm")
    public static class SearchForm extends BaseSearchRequest {
        @Size(max = 255)
        @JsonDeserialize(using = StrimDeSerializer.class)
        private String keySearch;
    }
}
