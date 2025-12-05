/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.models.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import java.util.Date;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.utils.Utils;


/**
 * Lop Response DTO ung voi bang crm_customer_certificates
 *
 * @author tudd
 * @version 1.0
 * @since 1.0
 */

@Data
@NoArgsConstructor
@JsonInclude(Include.NON_NULL)
public class CustomerCertificatesResponse {

    @Data
    @NoArgsConstructor
    @Schema(name = "CustomerCertificatesResponseSearchResult")
    public static class SearchResult {
        private Long customerCertificateId;
        private Long customerId;
        private String certificateName;
        private String statusId;

        private String fullName;

        private String mobileNumber;

        @JsonIgnore
        private String productDetail;

        public String getProductName(){
            return Utils.isNullOrEmpty(productDetail) ? null : productDetail.split("#")[0];
        }

        @JsonFormat(pattern = BaseConstants.COMMON_DATE_FORMAT, locale = BaseConstants.LOCALE_VN, timezone = BaseConstants.TIMEZONE_VN)
        private Date issuedDate;

        private String approvedBy;

        @JsonFormat(pattern = BaseConstants.COMMON_DATETIME_FORMAT, locale = BaseConstants.LOCALE_VN, timezone = BaseConstants.TIMEZONE_VN)
        private Date approvedDate;

        private String createdBy;

        @JsonFormat(pattern = BaseConstants.COMMON_DATETIME_FORMAT, locale = BaseConstants.LOCALE_VN, timezone = BaseConstants.TIMEZONE_VN)
        private Date createdTime;
        private String modifiedBy;

        @JsonFormat(pattern = BaseConstants.COMMON_DATETIME_FORMAT, locale = BaseConstants.LOCALE_VN, timezone = BaseConstants.TIMEZONE_VN)
        private Date modifiedTime;

    }

    @Data
    @NoArgsConstructor
    @Schema(name = "CustomerCertificatesResponseDetailBean")
    public static class DetailBean {
        private Long customerCertificateId;
        private Long customerId;
        private String certificateId;

        @JsonFormat(pattern = BaseConstants.COMMON_DATE_FORMAT, locale = BaseConstants.LOCALE_VN, timezone = BaseConstants.TIMEZONE_VN)
        private Date issuedDate;

        private String note;
    }

}
