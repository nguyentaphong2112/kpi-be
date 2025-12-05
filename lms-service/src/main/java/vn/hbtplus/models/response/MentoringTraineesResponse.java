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
 * Lop Response DTO ung voi bang lms_mentoring_trainees
 *
 * @author tudd
 * @version 1.0
 * @since 1.0
 */

@Data
@NoArgsConstructor
@JsonInclude(Include.NON_NULL)
public class MentoringTraineesResponse {

    @Data
    @NoArgsConstructor
    @Schema(name = "MentoringTraineesResponseSearchResult")
    public static class SearchResult extends MedBaseResponse {

        private Long medMentoringTraineeId;
        private Long employeeId;
        private String employeeCode;
        private String employeeName;
        private String projectName;
        private String hospitalName;

        @JsonFormat(pattern = BaseConstants.COMMON_DATE_FORMAT, locale = BaseConstants.LOCALE_VN, timezone = BaseConstants.TIMEZONE_VN)
        private Date startDate;

        @JsonFormat(pattern = BaseConstants.COMMON_DATE_FORMAT, locale = BaseConstants.LOCALE_VN, timezone = BaseConstants.TIMEZONE_VN)
        private Date endDate;
        private String projectId;
        private String hospitalId;
        private Long totalLessons;
        private String content;
        private String documentNo;
    }

    @Data
    @NoArgsConstructor
    @Schema(name = "MentoringTraineesResponseSearchResult")
    public static class DetailBean extends MedBaseResponse {
        private Long medMentoringTraineeId;
        private Long employeeId;
        @JsonFormat(pattern = BaseConstants.COMMON_DATE_FORMAT, locale = BaseConstants.LOCALE_VN, timezone = BaseConstants.TIMEZONE_VN)
        private Date startDate;
        @JsonFormat(pattern = BaseConstants.COMMON_DATE_FORMAT, locale = BaseConstants.LOCALE_VN, timezone = BaseConstants.TIMEZONE_VN)
        private Date endDate;
        private String projectId;
        private String hospitalId;
        private Long totalLessons;
        private String content;
        private String documentNo;
        private List<ObjectAttributesResponse> listAttributes;
        List<Attachment> attachFileList;
    }
}
