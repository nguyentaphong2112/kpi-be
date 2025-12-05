package vn.hbtplus.models.request;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.hbtplus.models.BaseSearchRequest;
import vn.hbtplus.models.dto.CategoryTypeDto;
import vn.hbtplus.utils.StrimDeSerializer;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.List;

@Data
@NoArgsConstructor
public class CategoryTypeRequest extends BaseSearchRequest {

    @Data
    @NoArgsConstructor
    @Schema(name = "CategoryTypeRequestSearchForm")
    public static class SearchForm extends BaseSearchRequest {

        @Size(max = 50)
        @JsonDeserialize(using = StrimDeSerializer.class)
        private String code;

        @Size(max = 255)
        @JsonDeserialize(using = StrimDeSerializer.class)
        private String name;

    }

    @Data
    @NoArgsConstructor
    @Schema(name = "CategoryTypeRequestSubmitForm")
    public static class SubmitForm {
        @Size(max = 50)
        @JsonDeserialize(using = StrimDeSerializer.class)
        private String code;

        @Size(max = 255)
        @JsonDeserialize(using = StrimDeSerializer.class)
        @NotBlank
        private String name;

        @Size(max = 50)
        @JsonDeserialize(using = StrimDeSerializer.class)
        private String isAutoIncrease;

        private Long orderNumber;

        private String groupType;

        List<CategoryTypeDto.AttributeDto> attributes;

    }
}
