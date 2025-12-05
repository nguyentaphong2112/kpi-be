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

import lombok.Data;
import lombok.NoArgsConstructor;
import vn.hbtplus.constants.BaseConstants;


/**
 * Lop Response DTO ung voi bang exm_questions
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Data
@NoArgsConstructor
@JsonInclude(Include.NON_NULL)
public class QuestionsResponse {

    private Long questionId;
    private String code;
    private String subjectCode;
    private String topicCode;
    private String typeCode;
    private String sectionCode;
    private String levelCode;
    private String skillType;
    private Long questionGroupId;
    private Long orderNumber;
    private Double defaultScore;
    private Double defaultWeight;
    private Long timeSuggestedSeconds;
    private String content;
    private String explanation;
    private String solution;
    private String statusCode;
    private String createdBy;

    @JsonFormat(pattern = BaseConstants.COMMON_DATETIME_FORMAT, locale = BaseConstants.LOCALE_VN, timezone = BaseConstants.TIMEZONE_VN)
    private Date createdTime;
    private String modifiedBy;

    @JsonFormat(pattern = BaseConstants.COMMON_DATETIME_FORMAT, locale = BaseConstants.LOCALE_VN, timezone = BaseConstants.TIMEZONE_VN)
    private Date modifiedTime;

    private String subjectName;
    private String topicName;
    private String typeName;
    private String sectionName;
    private String levelName;
    private String skillTypeName;
    private String statusName;

    private List<QuestionOptionsResponse> options;

}
