/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.models.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.hbtplus.constants.BaseConstants;

import java.util.Date;
import java.util.List;


/**
 * Lop Response DTO ung voi bang sys_categories
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Data
@NoArgsConstructor
@JsonInclude(Include.NON_NULL)
public class CategoryResponse {
    @Data
    @NoArgsConstructor
    @Schema(name = "CategoryResponseSearchResult")
    public static class SearchResult {
        private Long categoryId;
        private String categoryType;
        private String code;
        private String name;
        private String value;
        private Long orderNumber;
        private String createdBy;
        @JsonFormat(pattern = BaseConstants.COMMON_DATETIME_FORMAT, locale = BaseConstants.LOCALE_VN, timezone = BaseConstants.TIMEZONE_VN)
        private Date createdTime;
        private String modifiedBy;

        @JsonFormat(pattern = BaseConstants.COMMON_DATETIME_FORMAT, locale = BaseConstants.LOCALE_VN, timezone = BaseConstants.TIMEZONE_VN)
        private Date modifiedTime;
    }

    @Data
    @NoArgsConstructor
    @Schema(name = "CategoryResponseDetailBean")
    public static class DetailBean {
        private Long categoryId;
        private String categoryType;
        private String code;
        private String name;
        private String value;
        private Long orderNumber;

        private List<AttributeDto> listAttributes;
    }

    @Data
    @NoArgsConstructor
    public static class AttributeDto {
        private String attributeCode;
        private String attributeValue;
        private String dataType;
    }

}
