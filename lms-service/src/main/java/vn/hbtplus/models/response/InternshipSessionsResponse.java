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
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.models.Attachment;
import vn.hbtplus.models.request.InternshipSessionsRequest;
import vn.hbtplus.utils.StrimDeSerializer;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;


/**
 * Lop Response DTO ung voi bang lms_internship_sessions
 *
 * @author tudd
 * @version 1.0
 * @since 1.0
 */

@Data
@NoArgsConstructor
@JsonInclude(Include.NON_NULL)
public class InternshipSessionsResponse {

    @Data
    @NoArgsConstructor
    @Schema(name = "InternshipSessionsResponseSearchResult")
    public static class SearchResult {
        private Long internshipSessionId;
        private String universityId;
        private String universityName;
        private String sessionName;

        @JsonFormat(pattern = BaseConstants.COMMON_DATE_FORMAT, locale = BaseConstants.LOCALE_VN, timezone = BaseConstants.TIMEZONE_VN)
        private Date startDate;

        @JsonFormat(pattern = BaseConstants.COMMON_DATE_FORMAT, locale = BaseConstants.LOCALE_VN, timezone = BaseConstants.TIMEZONE_VN)
        private Date endDate;
        private Long totalStudents;
        private String createdBy;

        @JsonFormat(pattern = BaseConstants.COMMON_DATETIME_FORMAT, locale = BaseConstants.LOCALE_VN, timezone = BaseConstants.TIMEZONE_VN)
        private Date createdTime;
        private String modifiedBy;

        @JsonFormat(pattern = BaseConstants.COMMON_DATETIME_FORMAT, locale = BaseConstants.LOCALE_VN, timezone = BaseConstants.TIMEZONE_VN)
        private Date modifiedTime;
    }

    @Data
    @NoArgsConstructor
    @Schema(name = "InternshipSessionsResponseSearchResult")
    public static class Detail {
        private Long internshipSessionId;
        private String universityId;
        private String universityName;
        private String sessionName;

        @JsonFormat(pattern = BaseConstants.COMMON_DATE_FORMAT, locale = BaseConstants.LOCALE_VN, timezone = BaseConstants.TIMEZONE_VN)
        private Date startDate;

        @JsonFormat(pattern = BaseConstants.COMMON_DATE_FORMAT, locale = BaseConstants.LOCALE_VN, timezone = BaseConstants.TIMEZONE_VN)
        private Date endDate;
        private Long totalStudents;
        private String createdBy;

        @JsonFormat(pattern = BaseConstants.COMMON_DATETIME_FORMAT, locale = BaseConstants.LOCALE_VN, timezone = BaseConstants.TIMEZONE_VN)
        private Date createdTime;
        private String modifiedBy;

        @JsonFormat(pattern = BaseConstants.COMMON_DATETIME_FORMAT, locale = BaseConstants.LOCALE_VN, timezone = BaseConstants.TIMEZONE_VN)
        private Date modifiedTime;

        private List<Attachment> attachFileList;
        private List<ObjectAttributesResponse> listAttributes;
        private List<DetailResponse> details;
    }
    @Data
    @NoArgsConstructor
    @Schema(name = "InternshipSessionsResponseDetailResponse")
    public static class DetailResponse {

        @Size(max = 20)
        @JsonDeserialize(using = StrimDeSerializer.class)
        private String majorId;
        private String majorName;
        private Long numOfStudents;
        private Long organizationId;
        private String organizationName;
    }
}
