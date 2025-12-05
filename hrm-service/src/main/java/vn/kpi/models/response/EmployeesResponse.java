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
import org.springframework.format.annotation.DateTimeFormat;
import vn.kpi.constants.BaseConstants;
import vn.kpi.utils.I18n;
import vn.kpi.utils.Utils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 * Lop Response DTO ung voi bang hr_employees
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
    public static class SearchResult extends EmpBaseResponse {
        private Long employeeId;
        private String employeeCode;
        private String fullName;
        private String email;
        private String mobileNumber;
        private Long organizationId;
        private Long positionId;
        private Long jobId;
        private String orgName;
        private String jobName;
        private String genderName;
        private String ethnicName;
        private String religionName;
        private String educationLevelName;
        private String placeOfBirth;
        private String originalAddress;
        private String permanentAddress;
        private String currentAddress;
        private String genderId;
        private String religionId;
        private String ethnicId;
        private String personalEmail;
        private String familyPolicyId;
        private String selfPolicyId;
        private String taxNo;
        private String insuranceNo;
        private String identityNo;
        private String status;
        private Long empTypeId;
        private Long educationLevelId;
        private String label;
        private String maritalStatusName;
        private Integer yearOfBirth;
        private String fullLabel;
    }

    @Data
    @NoArgsConstructor
    @Schema(name = "EmployeesResponseBasicInfo")
    public static class BasicInfo {
        private Long employeeId;
        private String employeeCode;
        private String fullName;
        private String positionTitle;
        private String organizationName;
        private String email;
        private String mobileNumber;
        private String genderId;
        private String religionId;
        private String ethnicId;
        private String maritalStatusId;
        private String personalEmail;
        private String taxNo;
        private String insuranceNo;
        private String originalAddress;
        private String placeOfBirth;

        @JsonFormat(pattern = BaseConstants.COMMON_DATE_FORMAT, locale = BaseConstants.LOCALE_VN, timezone = BaseConstants.TIMEZONE_VN)
        private Date dateOfBirth;
        private List<InfoBean> infoBeans = new ArrayList<>();
        private List<ObjectAttributesResponse> listAttributes;
        private List<ContactAddressesResponse.DetailBean> listContactAddresses;
    }

    @Data
    @NoArgsConstructor
    @Schema(name = "EmployeesResponsePersonalInfo")
    public static class PersonalInfo {
        private Long employeeId;
        private String employeeCode;
        private String fullName;
        private String positionTitle;
        private String organizationName;
        private String email;
        private String statusName;
        private String status;

        private List<InfoBean> infoBeans = new ArrayList<>();
    }

    @Data
    @NoArgsConstructor
    @Schema(name = "EmployeesResponsePoliticalInfo")
    public static class PoliticalInfo {
        private Long employeeId;
        private String partyNumber;
        @DateTimeFormat(pattern = BaseConstants.COMMON_DATE_FORMAT)
        @JsonFormat(pattern = BaseConstants.COMMON_DATE_FORMAT, locale = BaseConstants.LOCALE_VN, timezone = BaseConstants.TIMEZONE_VN)
        private Date partyDate;
        private String partyPlace;
        @DateTimeFormat(pattern = BaseConstants.COMMON_DATE_FORMAT)
        @JsonFormat(pattern = BaseConstants.COMMON_DATE_FORMAT, locale = BaseConstants.LOCALE_VN, timezone = BaseConstants.TIMEZONE_VN)
        private Date partyOfficialDate;
        private String familyPolicyId;
        private String selfPolicyId;
        List<ObjectAttributesResponse> listAttributes;
    }

    @Data
    @NoArgsConstructor
    @Schema(name = "EmployeesResponseInfoBean")
    public static class InfoBean {
        private String infoType;
        private List<InfoDetailBean> details = new ArrayList<>();

        public InfoBean(String infoType) {
            this.infoType = infoType;
        }

        public void addInfo(String code, String label, String value, int colsSpan) {
            details.add(new InfoDetailBean(code, I18n.getMessage(Utils.NVL(label)), value, colsSpan));
        }
    }

    @Data
    @NoArgsConstructor
    @Schema(name = "EmployeesResponseInfoBean")
    public static class InfoDetailBean {
        private String code;
        private String label;
        private String value;
        private int colsSpan;

        public InfoDetailBean(String code, String label, String value, int colsSpan) {
            this.code = code;
            this.label = label;
            this.value = value;
            this.colsSpan = colsSpan;
        }
    }
}
