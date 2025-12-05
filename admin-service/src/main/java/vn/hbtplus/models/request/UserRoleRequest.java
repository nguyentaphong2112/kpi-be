/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.models.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.hbtplus.models.BaseSearchRequest;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * Lop DTO ung voi bang sys_user_roles
 *
 * @author tudd
 * @version 1.0
 * @since 1.0
 */

@Data
@NoArgsConstructor
public class UserRoleRequest {

    @Data
    @NoArgsConstructor
    @Schema(name = "UserRolesSubmitForm")
    public static class SubmitForm {
        @NotNull
        private Long userId;

        private List<RoleData> roleData;
    }

    @Data
    @NoArgsConstructor
    @Schema(name = "UserRolesRoleData")
    public static class RoleData {
        private Long roleId;
        private List<List<DomainDataBean>> groupDomains;
    }

    @Data
    @NoArgsConstructor
    @Schema(name = "UserRolesDomainDataBean")
    public static class DomainDataBean {
        private String domainType;
        private List<String> domainIds;
    }

    @Data
    @NoArgsConstructor
    @Schema(name = "UserRolesSearchForm")
    public static class SearchForm extends BaseSearchRequest {

        private Long userRoleId;

        private Long userId;

        private Long roleId;

    }
}
