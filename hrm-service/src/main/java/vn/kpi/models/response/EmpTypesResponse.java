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
 * Lop Response DTO ung voi bang hr_emp_types
 *
 * @author tudd
 * @version 1.0
 * @since 1.0
 */

@Data
@NoArgsConstructor
@JsonInclude(Include.NON_NULL)
public class EmpTypesResponse {


    @Data
    @NoArgsConstructor
    @Schema(name = "EmpTypesResponseDetailBean")
    public static class DetailBean {
        private Long empTypeId;
        private String code;
        private String name;
        private String value;
        private Long orderNumber;
        private List<ObjectAttributesResponse> listAttributes;
    }


    @Data
    @NoArgsConstructor
    @Schema(name = "EmpTypesResponseSearchResult")
    public static class SearchResult {
        private Long empTypeId;
        private String code;
        private String name;
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
    @Schema(name = "EmpTypesResponseEmpTypeList")
    public static class EmpTypeList {
        private Long empTypeId;
        private String code;
        private String name;
    }


}
