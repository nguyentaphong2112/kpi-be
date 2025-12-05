package vn.hbtplus.models.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.hbtplus.models.response.ObjectAttributesResponse;

import java.util.List;

@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BaseCategoryDto {
    @JsonIgnore
    private Long categoryId;
    private String code;
    private String name;
    private String value;
    private String label;

    public BaseCategoryDto(String value, String name) {
        this.name = name;
        this.value = value;
    }

    @Data
    @NoArgsConstructor
    @Schema(name = "BaseCategoryDtoDetailBean")
    public static class DetailBean {
        private Long categoryId;
        private String categoryType;
        private String code;
        private String name;
        private String value;
        private Long orderNumber;

        private List<ObjectAttributesResponse> listAttributes;
    }
}
