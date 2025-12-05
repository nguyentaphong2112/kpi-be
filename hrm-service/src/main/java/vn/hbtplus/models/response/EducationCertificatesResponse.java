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
 * Lop Response DTO ung voi bang hr_education_certificates
 *
 * @author tudd
 * @version 1.0
 * @since 1.0
 */

@Data
@NoArgsConstructor
@JsonInclude(Include.NON_NULL)
public class EducationCertificatesResponse {

    @Data
    @NoArgsConstructor
    @Schema(name = "EducationCertificatesResponseSearchResult")
    public static class SearchResult extends EmpBaseResponse {
        private Long educationCertificateId;
        private Long employeeId;
        private String certificateTypeId;
        private String certificateTypeName;
        private String certificateName;
        private String certificateId;
        private String issuedPlace;

        @JsonFormat(pattern = BaseConstants.COMMON_DATE_FORMAT, locale = BaseConstants.LOCALE_VN, timezone = BaseConstants.TIMEZONE_VN)
        private Date issuedDate;

        @JsonFormat(pattern = BaseConstants.COMMON_DATE_FORMAT, locale = BaseConstants.LOCALE_VN, timezone = BaseConstants.TIMEZONE_VN)
        private Date expiredDate;

        private String result;
    }

    @Data
    @NoArgsConstructor
    @Schema(name = "EducationCertificatesResponseDetailBean")
    public static class DetailBean extends EmpBaseResponse{
        private Long educationCertificateId;
        private Long employeeId;
        private String certificateTypeId;
        private String certificateTypeName;
        private String certificateName;
        private String certificateId;
        private String issuedPlace;
        private String result;

        @JsonFormat(pattern = BaseConstants.COMMON_DATE_FORMAT, locale = BaseConstants.LOCALE_VN, timezone = BaseConstants.TIMEZONE_VN)
        private Date issuedDate;

        @JsonFormat(pattern = BaseConstants.COMMON_DATE_FORMAT, locale = BaseConstants.LOCALE_VN, timezone = BaseConstants.TIMEZONE_VN)
        private Date expiredDate;
        private List<ObjectAttributesResponse> listAttributes;
        List<Attachment> attachFileList;

    }

    @Data
    @NoArgsConstructor
    public static class AttributeDto {
        private String attributeCode;
        private String attributeValue;
        private String dataType;
    }

}
