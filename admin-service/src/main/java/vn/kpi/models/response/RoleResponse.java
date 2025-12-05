/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.kpi.models.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.kpi.constants.BaseConstants;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 * Lop Response DTO ung voi bang sys_roles
 *
 * @author tudd
 * @version 1.0
 * @since 1.0
 */

@Data
@NoArgsConstructor
@JsonInclude(Include.NON_NULL)
public class RoleResponse {
    @Data
    @NoArgsConstructor
    @JsonInclude(Include.NON_NULL)
    @Schema(name = "RoleResponseSearchResult")
    public static class SearchResult {
        private Long roleId;
        private String code;
        private String name;
        private String defaultDomainType;
        private String defaultDomainValue;
        private String createdBy;

        @JsonFormat(pattern = BaseConstants.COMMON_DATETIME_FORMAT, locale = BaseConstants.LOCALE_VN, timezone = BaseConstants.TIMEZONE_VN)
        private Date createdTime;
        private String modifiedBy;

        @JsonFormat(pattern = BaseConstants.COMMON_DATETIME_FORMAT, locale = BaseConstants.LOCALE_VN, timezone = BaseConstants.TIMEZONE_VN)
        private Date modifiedTime;
    }

    @Data
    @NoArgsConstructor
    @JsonInclude(Include.NON_NULL)
    @Schema(name = "RoleResponseDetailBean")
    public static class DetailBean {
        private Long roleId;
        private String code;
        private String name;
        private String note;
        private String defaultDomainType;
        private String defaultDomainValue;
    }

    @Data
    @NoArgsConstructor
    @JsonInclude(Include.NON_NULL)
    @Schema(name = "RoleResponseTreeDto")
    public static class TreeDto {
        private String nodeId;
        private String name;
        private String parentId;
        private String code;
        @JsonIgnore
        private String isMenu;
        private List<TreeDto> children;

        public void addChild(TreeDto item) {
            if (children == null) {
                children = new ArrayList<>();
            }
            children.add(item);
        }

        public void addChild(List<TreeDto> item) {
            if (children == null) {
                children = item;
                return;
            }
            if (item == null) {
                return;
            }
            children.addAll(item);
        }

        public void addChild(List<TreeDto> item, boolean first) {
            if (children == null) {
                children = item;
                return;
            }
            if (item == null) {
                return;
            }
            if (first) {
                children.addAll(0, item);
            } else {
                children.addAll(item);
            }
        }
    }
}
