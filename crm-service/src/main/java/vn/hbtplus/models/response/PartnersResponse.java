/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.models.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import java.util.Date;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.hbtplus.constants.BaseConstants;


/**
 * Lop Response DTO ung voi bang crm_partners
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Data
@NoArgsConstructor
@JsonInclude(Include.NON_NULL)
public class PartnersResponse {

    @Data
    @NoArgsConstructor
    @Schema(name = "PartnersResponseSearchResult")
    public static class SearchResult {
        private Long partnerId;
        private String fullName;

        @JsonFormat(pattern = BaseConstants.COMMON_DATE_FORMAT, locale = BaseConstants.LOCALE_VN, timezone = BaseConstants.TIMEZONE_VN)
        private Date dateOfBirth;
        private String mobileNumber;
        private String zaloAccount;
        private String email;
        private String partnerType;
        private String partnerTypeName;
        private String currentAddress;
        private String job;
        private String departmentName;
        private String provinceId;
        private String provinceName;
        private String districtId;
        private String districtName;
        private String wardId;
        private String wardName;
        private String villageAddress;
        private String bankAccount;
        private String bankName;
        private String bankBranch;
        private String genderId;
        private String createdBy;

        @JsonFormat(pattern = BaseConstants.COMMON_DATETIME_FORMAT, locale = BaseConstants.LOCALE_VN, timezone = BaseConstants.TIMEZONE_VN)
        private Date createdTime;
        private String modifiedBy;

        @JsonFormat(pattern = BaseConstants.COMMON_DATETIME_FORMAT, locale = BaseConstants.LOCALE_VN, timezone = BaseConstants.TIMEZONE_VN)
        private Date modifiedTime;
    }

    @Data
    @NoArgsConstructor
    @Schema(name = "PartnersResponseDetailBean")
    public static class DetailBean {
        private Long partnerId;
        private String fullName;

        @JsonFormat(pattern = BaseConstants.COMMON_DATE_FORMAT, locale = BaseConstants.LOCALE_VN, timezone = BaseConstants.TIMEZONE_VN)
        private Date dateOfBirth;
        private String mobileNumber;
        private String zaloAccount;
        private String email;
        private String partnerType;
        private String currentAddress;
        private String job;
        private String departmentName;
        private String provinceId;
        private String districtId;
        private String wardId;
        private String villageAddress;
        private String bankAccount;
        private String bankName;
        private String bankBranch;
        private String genderId;
        private String createdBy;

        @JsonFormat(pattern = BaseConstants.COMMON_DATETIME_FORMAT, locale = BaseConstants.LOCALE_VN, timezone = BaseConstants.TIMEZONE_VN)
        private Date createdTime;
        private String modifiedBy;

        @JsonFormat(pattern = BaseConstants.COMMON_DATETIME_FORMAT, locale = BaseConstants.LOCALE_VN, timezone = BaseConstants.TIMEZONE_VN)
        private Date modifiedTime;

        private List<ObjectAttributesResponse> listAttributes;
    }

}
