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
import vn.hbtplus.models.request.CourseLessonsRequest;
import vn.hbtplus.models.request.CourseTraineesRequest;


/**
 * Lop Response DTO ung voi bang crm_courses
 *
 * @author tudd
 * @version 1.0
 * @since 1.0
 */

@Data
@NoArgsConstructor
@JsonInclude(Include.NON_NULL)
public class CoursesResponse {


    @Data
    @NoArgsConstructor
    @Schema(name = "CoursesResponseSearchResult")
    public static class SearchResult {
        private Long courseId;
        private Long courseTraineeId;
        private Long trainingProgramId;

        @JsonFormat(pattern = BaseConstants.COMMON_DATE_FORMAT, locale = BaseConstants.LOCALE_VN, timezone = BaseConstants.TIMEZONE_VN)
        private Date startDate;

        @JsonFormat(pattern = BaseConstants.COMMON_DATE_FORMAT, locale = BaseConstants.LOCALE_VN, timezone = BaseConstants.TIMEZONE_VN)
        private Date endDate;
        private String name;
        private String courseName;
        private String mobileNumber;
        @JsonFormat(pattern = BaseConstants.COMMON_DATE_FORMAT, locale = BaseConstants.LOCALE_VN, timezone = BaseConstants.TIMEZONE_VN)
        private Date dateOfBirth;
        private String createdBy;

        @JsonFormat(pattern = BaseConstants.COMMON_DATETIME_FORMAT, locale = BaseConstants.LOCALE_VN, timezone = BaseConstants.TIMEZONE_VN)
        private Date createdTime;
        private String modifiedBy;

        @JsonFormat(pattern = BaseConstants.COMMON_DATETIME_FORMAT, locale = BaseConstants.LOCALE_VN, timezone = BaseConstants.TIMEZONE_VN)
        private Date modifiedTime;
        private Long rank;
        private String completionRate;
        private Double totalPoint;
        private String dateRange;
        private String instructorName;
        private Long traineeId;
        private String phoneNumberInstructor;
    }

    @Data
    @NoArgsConstructor
    @Schema(name = "CoursesResponseDetailBean")
    public static class DetailBean {
        private Long courseId;
        private Long trainingProgramId;

        @JsonFormat(pattern = BaseConstants.COMMON_DATE_FORMAT, locale = BaseConstants.LOCALE_VN, timezone = BaseConstants.TIMEZONE_VN)
        private Date startDate;

        @JsonFormat(pattern = BaseConstants.COMMON_DATE_FORMAT, locale = BaseConstants.LOCALE_VN, timezone = BaseConstants.TIMEZONE_VN)
        private Date endDate;
        private String name;

        private List<CourseLessonsRequest.SubmitForm> lessons;

        private List<CourseTraineesRequest.SubmitForm> listCoursesTrainees;

        private List<ObjectAttributesResponse> listAttributes;
    }

    @Data
    @NoArgsConstructor
    @Schema(name = "CoursesResponseUserDataSelected")
    public static class UserDataSelected {
        private Long userId;
        private String fullName;
    }


    @Data
    @NoArgsConstructor
    @Schema(name = "CoursesResponseDataSelected")
    public static class DataSelected {
        private Long courseId;
        private String name;
    }


    @Data
    @NoArgsConstructor
    @Schema(name = "CoursesResponseStatusData")
    public static class StatusData {
        private String value;
        private String name;
    }

}
