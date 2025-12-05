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
import vn.hbtplus.models.Attachment;

import java.util.Date;
import java.util.List;


/**
 * Lop Response DTO ung voi bang crm_employees
 *
 * @author tudd
 * @version 1.0
 * @since 1.0
 */

@Data
@NoArgsConstructor
@JsonInclude(Include.NON_NULL)
public class EmployeesResponse {

    @Data
    @NoArgsConstructor
    @Schema(name = "EmployeesResponseSearchResult")
    public static class SearchResult {
        private Long employeeId;
        private String fullName;
        @JsonFormat(pattern = BaseConstants.COMMON_DATE_FORMAT, locale = BaseConstants.LOCALE_VN, timezone = BaseConstants.TIMEZONE_VN)
        private Date dateOfBirth;
        private String mobileNumber;


        private String loginName;


        private String genderId;


        private String email;


        private String zaloAccount;


        private String positionTitleId;


        private String departmentId;


        private Long managerId;


        private String jobRankId;


        private String provinceId;


        private String districtId;


        private String wardId;


        private String villageAddress;


        private String bankAccount;


        private String bankName;


        private String bankBranch;


        private String status;


        private String personalIdNo;

        private String taxNo;

        private String insuranceNo;
        private String createdBy;

        @JsonFormat(pattern = BaseConstants.COMMON_DATETIME_FORMAT, locale = BaseConstants.LOCALE_VN, timezone = BaseConstants.TIMEZONE_VN)
        private Date createdTime;
        private String modifiedBy;

        @JsonFormat(pattern = BaseConstants.COMMON_DATETIME_FORMAT, locale = BaseConstants.LOCALE_VN, timezone = BaseConstants.TIMEZONE_VN)
        private Date modifiedTime;

        private String positionTitleName;
        private String departmentName;
        private String researchTurn;
        private String exportTurn;
        private String researchTime;
        private String exportTime;
        private String managerName;
    }
    @Data
    @NoArgsConstructor
    @Schema(name = "EmployeesResponseDetailBean")
    public static class DetailBean extends SearchResult {
        private List<ObjectAttributesResponse> listAttributes;
        private List<FamilyRelationshipsResponse> familyRelationships;
        private List<ProfileAttachment> profileAttachments;
    }
    @Data
    @NoArgsConstructor
    @Schema(name = "EmployeesResponseDetailBean")
    public static class ProfileAttachment {
        private Long employeeProfileId;
        private String attachmentType;
        private String attachmentTypeName;
        private List<Attachment> attachFileList;

    }


}
