package vn.hbtplus.models.dto;

import lombok.Data;

@Data
public class UserRoleDomainDto {
    private Long roleId;
    private String roleName;
    private String domainType;
    private String domainId;
    private String domainName;
    private String keyOrder;
}
