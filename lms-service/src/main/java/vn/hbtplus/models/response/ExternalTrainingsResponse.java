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

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.utils.StrimDeSerializer;

import javax.validation.constraints.Size;


/**
 * Lop Response DTO ung voi bang lms_external_trainings
 *
 * @author tudd
 * @version 1.0
 * @since 1.0
 */

@Data
@NoArgsConstructor
@JsonInclude(Include.NON_NULL)
public class ExternalTrainingsResponse {
    @Data
    @NoArgsConstructor
    @Schema(name = "ExternalTrainingsResponseSearchResult")
    public static class SearchResult {
        private Long externalTrainingId;
        private String typeId;
        private String typeName;
        private String fullName;
        private String genderId;
        private String genderName;
        private String yearOfBirth;
        private String mobileNumber;
        private String personalIdNo;
        private String address;
        private String organizationAddress;

        @JsonFormat(pattern = BaseConstants.COMMON_DATE_FORMAT, locale = BaseConstants.LOCALE_VN, timezone = BaseConstants.TIMEZONE_VN)
        private Date startDate;

        @JsonFormat(pattern = BaseConstants.COMMON_DATE_FORMAT, locale = BaseConstants.LOCALE_VN, timezone = BaseConstants.TIMEZONE_VN)
        private Date endDate;

        private String trainningTypeName;

        private String trainingMajorName;

        private String content;

        private String organizationName;

        private String mentorName;

        private String admissionResults;

        private String graduatedResults;

        private Long numberOfLessons;

        private String tuitionFeeStatusName;

        @JsonFormat(pattern = BaseConstants.COMMON_DATE_FORMAT, locale = BaseConstants.LOCALE_VN, timezone = BaseConstants.TIMEZONE_VN)
        private Date certificateDate;

        private String certificateNo;
        private String createdBy;

        @JsonFormat(pattern = BaseConstants.COMMON_DATETIME_FORMAT, locale = BaseConstants.LOCALE_VN, timezone = BaseConstants.TIMEZONE_VN)
        private Date createdTime;
        private String modifiedBy;

        @JsonFormat(pattern = BaseConstants.COMMON_DATETIME_FORMAT, locale = BaseConstants.LOCALE_VN, timezone = BaseConstants.TIMEZONE_VN)
        private Date modifiedTime;
    }

    @Data
    @NoArgsConstructor
    @Schema(name = "ExternalTrainingsResponseDetail")
    public static class Detail {
        private Long externalTrainingId;
        private String typeId;
        private String fullName;
        private String genderId;
        private String yearOfBirth;
        private String mobileNumber;
        private String personalIdNo;
        private String address;
        private String organizationAddress;

       @JsonFormat(pattern = BaseConstants.COMMON_DATE_FORMAT, locale = BaseConstants.LOCALE_VN, timezone = BaseConstants.TIMEZONE_VN)
        private Date startDate;

       @JsonFormat(pattern = BaseConstants.COMMON_DATE_FORMAT, locale = BaseConstants.LOCALE_VN, timezone = BaseConstants.TIMEZONE_VN)
        private Date endDate;

        private String trainningTypeId;

        private String trainingMajorId;

        private String content;

        private Long organizationId;

        private Long mentorId;

        private String admissionResults;

        private String graduatedResults;

        private Long numberOfLessons;

        @JsonFormat(pattern = BaseConstants.COMMON_DATE_FORMAT, locale = BaseConstants.LOCALE_VN, timezone = BaseConstants.TIMEZONE_VN)
        private Date certificateDate;

        private String certificateNo;
        private String tuitionFeeStatusId;
        private List<ObjectAttributesResponse> listAttributes;
    }

}
