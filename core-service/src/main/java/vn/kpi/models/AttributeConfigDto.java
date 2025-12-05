package vn.kpi.models;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class AttributeConfigDto {
    private String code;
    private String name;
    private boolean required;
    private String dataType;
    private String urlApi;
    private Integer nzXs;
    private Integer nzLg;
}
