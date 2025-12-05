package vn.hbtplus.models.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.hbtplus.utils.Utils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
public class CategoryTypeDto implements Serializable {
    private Long categoryTypeId;
    private String name;
    private String code;
    private String isAutoIncrease;

    @JsonIgnore
    private String attributes;

    public List<AttributeDto> getListAttributes(){
        if(!Utils.isNullOrEmpty(attributes)){
            return Utils.fromJsonList(attributes, AttributeDto.class);
        }
        return new ArrayList<>();
    }

    @Data
    @NoArgsConstructor
    @Schema(name ="CategoryTypeDtoAttributeDto")
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class AttributeDto {
        private String code;
        private String name;
        private String dataType;
        private String urlApi;
        private Boolean isRequired;
    }
}


