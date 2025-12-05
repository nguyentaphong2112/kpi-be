/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.tax.personal.models.response;

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
 * Lop Response DTO ung voi bang hr_family_relationships
 *
 * @author tudd
 * @version 1.0
 * @since 1.0
 */

@Data
@NoArgsConstructor
@JsonInclude(Include.NON_NULL)
public class FamilyRelationshipsResponse {

    @Data
    @NoArgsConstructor
    @Schema(name = "FamilyRelationshipsResponseSearchResult")
    public static class SearchResult extends EmpBaseResponse {
        private Long familyRelationshipId;
        private Long employeeId;
        private String relationTypeId;
        private String familyRelationshipName;
        private String relationStatusId;
        private String policyTypeId;

        private String dateOfBirthStr;
        private String job;
        private String organizationAddress;
        private String currentAddress;
        private String personalIdNo;
        private String mobileNumber;
        private String relationTypeName;
        private String relationStatusName;
        private String policyTypeName;

    }

    @Data
    @NoArgsConstructor
    @Schema(name = "FamilyRelationshipsResponseDetailBean")
    public static class DetailBean extends EmpBaseResponse {
        private Long familyRelationshipId;
        private Long employeeId;
        private String relationTypeId;
        private String familyRelationshipName;
        private String relationStatusId;
        private String policyTypeId;
        private String relationTypeName;
        private String relationStatusName;
        private String policyTypeName;
        private String isForeign;
        @JsonFormat(pattern = BaseConstants.COMMON_DATE_FORMAT, locale = BaseConstants.LOCALE_VN, timezone = BaseConstants.TIMEZONE_VN)
        private Date dateOfBirth;
        private String dateOfBirthStr;
        private String job;
        private String organizationAddress;
        private String currentAddress;
        private String personalIdNo;
        private String mobileNumber;
        private String fullName;
        private String employeeName;
        private String employeeCode;
        private String typeDateOfBirth;
        private List<ObjectAttributesResponse> listAttributes;
    }

}
