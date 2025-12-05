/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.models.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.hbtplus.models.dto.UserRoleDomainDto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


/**
 * Lop Response DTO ung voi bang sys_user_roles
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Data
@NoArgsConstructor
@JsonInclude(Include.NON_NULL)
public class UserRoleResponse implements Serializable {

    private Long roleId;
    private String roleName;

    private List<List<DomainDataBean>> groupDomains = new ArrayList<>();

    public void addDomain(UserRoleDomainDto dto) {
        List<DomainDataBean> domainDataBeans = groupDomains.get(groupDomains.size()-1);
        for (DomainDataBean bean : domainDataBeans){
            if(bean.getDomainType().equalsIgnoreCase(dto.getDomainType())){
                bean.getDomains().add(new DomainDto(dto.getDomainId(), dto.getDomainName()));
                return;
            }
        }
        DomainDataBean domainDataBean = new DomainDataBean();
        domainDataBean.setDomainType(dto.getDomainType());
        domainDataBean.domains = new ArrayList<>();
        domainDataBean.domains.add(new DomainDto(dto.getDomainId(), dto.getDomainName()));

        domainDataBeans.add(domainDataBean);
    }

    @Data
    @NoArgsConstructor
    @Schema(name = "UserRoleResponseDomainDataBean")
    public static class DomainDataBean {
        private String domainType;
        private List<DomainDto> domains;
    }

    @Data
    @NoArgsConstructor
    @Schema(name = "UserRoleResponseDomainDto")
    public static class DomainDto {
        private String domainId;
        private String domainName;

        public DomainDto(String domainId, String domainName) {
            this.domainId = domainId;
            this.domainName = domainName;
        }
    }
}
