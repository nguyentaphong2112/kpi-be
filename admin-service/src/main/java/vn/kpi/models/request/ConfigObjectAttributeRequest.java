package vn.kpi.models.request;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.kpi.models.BaseSearchRequest;
import vn.kpi.models.dto.ConfigObjectAttributeDto;
import vn.kpi.utils.StrimDeSerializer;

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
