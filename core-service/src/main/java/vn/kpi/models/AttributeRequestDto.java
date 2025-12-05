package vn.kpi.models;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class AttributeRequestDto {
    private String attributeCode;
    private String attributeValue;
    private String attributeName;
    private String dataType;
}
