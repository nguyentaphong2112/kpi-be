/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.models.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import java.util.Date;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.hbtplus.constants.BaseConstants;


/**
 * Lop Response DTO ung voi bang exm_exam_papers
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Data
@NoArgsConstructor
@JsonInclude(Include.NON_NULL)
public class ExamPapersResponse {

    private Long examPaperId;    private String code;    private String name;    private String subjectCode;    private String topicCode;    private String description;    private Long totalQuestions;    private Double totalScore;    private Long durationMinutes;    private String difficultyDistribution;    private String skillDistribution;    private String randomOrder;    private String randomOptionOrder;    private String generationMode;    private String generationStrategy;    private String statusCode;    private String createdBy;    @JsonFormat(pattern = BaseConstants.COMMON_DATETIME_FORMAT, locale = BaseConstants.LOCALE_VN, timezone = BaseConstants.TIMEZONE_VN)    private Date createdTime;    private String modifiedBy;    @JsonFormat(pattern = BaseConstants.COMMON_DATETIME_FORMAT, locale = BaseConstants.LOCALE_VN, timezone = BaseConstants.TIMEZONE_VN)    private Date modifiedTime;

}
