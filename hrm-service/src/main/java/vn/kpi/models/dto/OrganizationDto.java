package vn.kpi.models.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class OrganizationDto {
    private String name;
    private Long organizationId;
    private String orgTypeName;
    private Integer pathLevel;
}
