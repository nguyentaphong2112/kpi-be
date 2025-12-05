package vn.hbtplus.models.request;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.hbtplus.models.BaseSearchRequest;
import vn.hbtplus.utils.StrimDeSerializer;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class WbMaterialTypeRequest extends BaseSearchRequest {
	@Data
    @NoArgsConstructor
    @Schema(name = "WbMaterialTypeSubmitForm")
    public static class SubmitForm {
        private Long wbMaterialTypeId;

        @Size(max = 50)
        @JsonDeserialize(using = StrimDeSerializer.class)
        private String code;

        @Size(max = 500)
        @JsonDeserialize(using = StrimDeSerializer.class)
        @NotNull(message = "Phải nhập vào tên kiểu loại")
        private String name;

        @Size(max = 255)
        @JsonDeserialize(using = StrimDeSerializer.class)
        @NotNull(message = "Phải nhập vào ký hiệu")
        private String symbol;

        @Size(max = 255)
        @JsonDeserialize(using = StrimDeSerializer.class)
        private String description;
        
        @JsonDeserialize(using = StrimDeSerializer.class)
        private Long parentId;
    }

    @Data
    @NoArgsConstructor
    @Schema(name = "WbMaterialTypeSearchForm")
    public static class SearchForm extends BaseSearchRequest {

        private Long wbMaterialTypeId;

        @Size(max = 50)
        @JsonDeserialize(using = StrimDeSerializer.class)
        private String code;

        @Size(max = 500)
        @JsonDeserialize(using = StrimDeSerializer.class)
        private String name;

        @Size(max = 255)
        @JsonDeserialize(using = StrimDeSerializer.class)
        private String symbol;

    }
}
