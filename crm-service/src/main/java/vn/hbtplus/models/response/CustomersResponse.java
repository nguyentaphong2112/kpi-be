/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.models.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.constants.Constant;
import vn.hbtplus.utils.Utils;

import java.util.Date;
import java.util.List;


/**
 * Lop Response DTO ung voi bang crm_customers
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Data
@NoArgsConstructor
@JsonInclude(Include.NON_NULL)
public class CustomersResponse {

    @Data
    @NoArgsConstructor
    @Schema(name = "CustomersResponseSearchResult")
    public static class SearchResult {
        private Long customerId;
        private String fullName;
        private String mobileNumber;
        private String createdBy;

        @JsonFormat(pattern = BaseConstants.COMMON_DATETIME_FORMAT, locale = BaseConstants.LOCALE_VN, timezone = BaseConstants.TIMEZONE_VN)
        private Date createdTime;
        private String modifiedBy;

        @JsonFormat(pattern = BaseConstants.COMMON_DATETIME_FORMAT, locale = BaseConstants.LOCALE_VN, timezone = BaseConstants.TIMEZONE_VN)
        private Date modifiedTime;

        private String loginName;
        private String genderId;

        @JsonFormat(pattern = BaseConstants.COMMON_DATE_FORMAT, locale = BaseConstants.LOCALE_VN, timezone = BaseConstants.TIMEZONE_VN)
        private Date dateOfBirth;
        private String email;
        private String zaloAccount;
        private Long introducerId;
        private Long receiverId;
        private Long userTakeCareId;
        private String job;
        private String departmentName;
        private String provinceId;
        private String districtId;
        private String wardId;
        private String villageAddress;
        private String bankAccount;
        private String bankName;
        private String bankBranch;
        private String status;
        private String userTakeCareName;
        private String introducerName;
        private String receiverName;
        private String wardName;
        private String districtName;
        private String provinceName;
        private String fullAddress;
        private String genderName;
        private Long totalAmount;
        private Long paidAmount;
        private Long totalOrderAmount;
        private Long referralFee;
        private Long careFee;
        private Long welfareFee;
        private String statusName;
        private String isStatusChild;
        @JsonIgnore
        private String productDetail;

        public String getPhoneAndName(){
            return mobileNumber + " - " + fullName;
        }

        public String getProductName(){
            return Utils.isNullOrEmpty(productDetail) ? null : productDetail.split("#")[0];
        }
        public Long getProductPrice(){
            return Utils.isNullOrEmpty(productDetail) ? null : Double.valueOf(productDetail.split("#")[1]).longValue();
        }
        public Long getOwedAmount (){
            return Utils.NVL(totalOrderAmount) - Utils.NVL(paidAmount);
        }
    }

    @Data
    @NoArgsConstructor
    @Schema(name = "CustomersResponseDetailBean")
    public static class DetailBean {
        private Long customerId;
        private String fullName;
        private String mobileNumber;
        private String createdBy;

        @JsonFormat(pattern = BaseConstants.COMMON_DATETIME_FORMAT, locale = BaseConstants.LOCALE_VN, timezone = BaseConstants.TIMEZONE_VN)
        private Date createdTime;
        private String modifiedBy;

        @JsonFormat(pattern = BaseConstants.COMMON_DATETIME_FORMAT, locale = BaseConstants.LOCALE_VN, timezone = BaseConstants.TIMEZONE_VN)
        private Date modifiedTime;

        private String loginName;
        private String genderId;

        @JsonFormat(pattern = BaseConstants.COMMON_DATE_FORMAT, locale = BaseConstants.LOCALE_VN, timezone = BaseConstants.TIMEZONE_VN)
        private Date dateOfBirth;
        private String email;
        private String zaloAccount;
        private Long introducerId;
        private Long receiverId;
        private Long userTakeCareId;
        private String job;
        private String departmentName;
        private String provinceId;
        private String districtId;
        private String wardId;
        private String villageAddress;
        private String bankAccount;
        private String bankName;
        private String bankBranch;
        private String status;
        private String userTakeCareName;
        private String wardName;
        private String districtName;
        private String provinceName;
        private String fullAddress;
        private String genderName;
        List<FamilyRelationshipsResponse> listFamilyRelationship;
        private List<ObjectAttributesResponse> listAttributes;
    }


    @Data
    @NoArgsConstructor
    @Schema(name = "CustomersResponseDataSelected")
    public static class DataSelected {
        private Long customerId;
        private String fullName;
        private String mobileNumber;

        @DateTimeFormat(pattern = BaseConstants.COMMON_DATE_FORMAT)
        @JsonFormat(pattern = BaseConstants.COMMON_DATE_FORMAT, locale = BaseConstants.LOCALE_VN, timezone = BaseConstants.TIMEZONE_VN)
        private Date dateOfBirth;
    }

}
