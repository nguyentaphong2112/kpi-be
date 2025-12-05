/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.kpi.models.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import java.util.Date;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.kpi.constants.BaseConstants;


/**
 * Lop Response DTO ung voi bang hr_contract_types
 *
 * @author tudd
 * @version 1.0
 * @since 1.0
 */

@Data
@NoArgsConstructor
@JsonInclude(Include.NON_NULL)
public class ContractTypesResponse {

    @Data
    @NoArgsConstructor
    @Schema(name = "ContractTypesResponseDetailBean")
    public static class DetailBean {
        private Long contractTypeId;
        private String code;
        private String name;
        private Long empTypeId;
        private Integer orderNumber;
        private String classifyCode;
        private List<ObjectAttributesResponse> listAttributes;
    }


    @Data
    @NoArgsConstructor
    @Schema(name = "ContractTypesResponseSearchResult")
    public static class SearchResult {
        private Long contractTypeId;
        private String code;
        private String name;
        private Long empTypeId;
        private String empTypeName;
        private String classifyCode;
        private String createdBy;

        @JsonFormat(pattern = BaseConstants.COMMON_DATETIME_FORMAT, locale = BaseConstants.LOCALE_VN, timezone = BaseConstants.TIMEZONE_VN)
        private Date createdTime;
        private String modifiedBy;

        @JsonFormat(pattern = BaseConstants.COMMON_DATETIME_FORMAT, locale = BaseConstants.LOCALE_VN, timezone = BaseConstants.TIMEZONE_VN)
        private Date modifiedTime;

        private Long orderNumber;
    }

}
