package vn.kpi.models.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CategoryDto {
    @JsonIgnore
    private Long categoryId;
    private String value;
    private String label;
    private String code;
    private String name;
    private String orderNumber;
    private String attributeValue;

    private Map<String, Object> attributes;
}
