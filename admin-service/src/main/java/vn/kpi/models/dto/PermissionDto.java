package vn.kpi.models.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class PermissionDto {
    private String scope;
    private String resourceCode;
}
