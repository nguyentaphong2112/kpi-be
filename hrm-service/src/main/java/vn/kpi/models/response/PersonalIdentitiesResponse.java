/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.kpi.models.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.kpi.constants.BaseConstants;

import java.util.Date;
import java.util.List;


/**
 * Lop Response DTO ung voi bang hr_personal_identities
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Data
@NoArgsConstructor
@JsonInclude(Include.NON_NULL)
public class PersonalIdentitiesResponse {

    @Data
    @NoArgsConstructor
    @Schema(name = "PersonalIdentitiesResponseSearchResult")
    public static class SearchResult extends EmpBaseResponse{
        private Long personalIdentityId;
        private String identityNo;
        private String identityTypeId;

        @JsonFormat(pattern = BaseConstants.COMMON_DATE_FORMAT, locale = BaseConstants.LOCALE_VN, timezone = BaseConstants.TIMEZONE_VN)
        private Date identityIssueDate;
        private String identityIssuePlace;

        @JsonFormat(pattern = BaseConstants.COMMON_DATE_FORMAT, locale = BaseConstants.LOCALE_VN, timezone = BaseConstants.TIMEZONE_VN)
        private Date expiredDate;
        private String isMain;
        private Long employeeId;
        private String identityTypeName;
    }

    @Data
    @NoArgsConstructor
    @Schema(name = "PersonalIdentitiesResponseDetailBean")
    public static class DetailBean {
        private Long personalIdentityId;
        private String identityNo;
        private String identityTypeId;
        @JsonFormat(pattern = BaseConstants.COMMON_DATE_FORMAT, locale = BaseConstants.LOCALE_VN, timezone = BaseConstants.TIMEZONE_VN)
        private Date identityIssueDate;
        private String identityIssuePlace;

        @JsonFormat(pattern = BaseConstants.COMMON_DATE_FORMAT, locale = BaseConstants.LOCALE_VN, timezone = BaseConstants.TIMEZONE_VN)
        private Date expiredDate;
        private String isMain;
        private Long employeeId;
        List<ObjectAttributesResponse> listAttributes;
    }

}
