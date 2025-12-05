package vn.hbtplus.models.request;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.hbtplus.models.BaseSearchRequest;
import vn.hbtplus.utils.StrimDeSerializer;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class WbMaterialRequest extends BaseSearchRequest {
	@Data
    @NoArgsConstructor
    @Schema(name = "WbMaterialSubmitForm")
    public static class SubmitForm {
        private Long wbMaterialId;

        @Size(max = 50)
        @JsonDeserialize(using = StrimDeSerializer.class)
        @NotNull(message = "Phải nhập vào mã vật tư")
        private String code;

        @Size(max = 500)
        @JsonDeserialize(using = StrimDeSerializer.class)
        @NotNull(message = "Phải nhập vào tên vật tư")
        private String name;

        @Size(max = 255)
        @JsonDeserialize(using = StrimDeSerializer.class)
        private String barcode;

        @Size(max = 255)
        @JsonDeserialize(using = StrimDeSerializer.class)
        private String seri;
        
        @Size(max = 255)
        @JsonDeserialize(using = StrimDeSerializer.class)
        private String number;
        
        @JsonDeserialize(using = StrimDeSerializer.class)
        @NotNull(message = "Phải nhập vào loại vật tư")
        private Long wbMaterialTypeId;
        
        @JsonDeserialize(using = StrimDeSerializer.class)
        @NotNull(message = "Phải nhập vào đơn vị tính")
        private Long unitId;
        
        private Long countryId;
        
        private Long companyId;
        
        private String description;
    }

    @Data
    @NoArgsConstructor
    @Schema(name = "WbMaterialSearchForm")
    public static class SearchForm extends BaseSearchRequest{

        @Size(max = 50)
        @JsonDeserialize(using = StrimDeSerializer.class)
        private String code;

        @Size(max = 500)
        @JsonDeserialize(using = StrimDeSerializer.class)
        private String name;

        private String wbMaterialTypeId;

    }
}
