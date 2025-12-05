package vn.hbtplus.models.request;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.hbtplus.models.BaseSearchRequest;
import vn.hbtplus.models.dto.ConfigObjectAttributeDto;
import vn.hbtplus.utils.StrimDeSerializer;

import javax.validation.constraints.Size;
import java.util.List;


@Data
@NoArgsConstructor
public class ConfigObjectAttributeRequest extends BaseSearchRequest {


    @Data
    @NoArgsConstructor
    @Schema(name = "ConfigObjectAttributeRequestSubmitForm")
    public static class SubmitForm {
        private String tableName;
        private String functionCode;
        private String name;
        private String note;
        private List<ConfigObjectAttributeDto.AttributeDto> attributes;

    }


    @Data
    @NoArgsConstructor
    @Schema(name = "ConfigObjectAttributeRequestSearchForm")
    public static class SearchForm extends BaseSearchRequest {

        @Size(max = 255)
        @JsonDeserialize(using = StrimDeSerializer.class)
        private String keySearch;

    }
}
