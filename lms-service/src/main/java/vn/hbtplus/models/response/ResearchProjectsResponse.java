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
import java.util.Map;


/**
 * Lop Response DTO ung voi bang lms_research_projects
 *
 * @author tudd
 * @version 1.0
 * @since 1.0
 */

@Data
@NoArgsConstructor
@JsonInclude(Include.NON_NULL)
public class ResearchProjectsResponse {
    @Data
    @NoArgsConstructor
    @Schema(name = "ResearchProjectsResponseSearchResult")
    public static class SearchResult {
        private Long researchProjectId;
        private String title;
        private String content;
        private String target;
        private String projectTypeId;
        private Long organizationId;
        private String researchLevelId;
        private String researchTopicId;
        private int duration;
        private Long estimatedBudget;
        private String statusId;
        private String createdBy;
        private String status;
        private String organization;
        private String memberName;
        @JsonFormat(pattern = BaseConstants.COMMON_DATETIME_FORMAT, locale = BaseConstants.LOCALE_VN, timezone = BaseConstants.TIMEZONE_VN)
        private Date createdTime;
        private String modifiedBy;
        private Long researchProjectMemberId;
        private String projectManager;
        @JsonFormat(pattern = BaseConstants.COMMON_DATETIME_FORMAT, locale = BaseConstants.LOCALE_VN, timezone = BaseConstants.TIMEZONE_VN)
        private Date modifiedTime;

    }

    @Data
    @NoArgsConstructor
    @Schema(name = "ResearchProjectsResponseDetailBean")
    public static class DetailBean {
        private Long researchProjectId;
        private String title;
        private String content;
        private String target;
        private String projectTypeId;
        private Long organizationId;
        private String researchLevelId;
        private String researchTopicId;
        private Double duration;
        private Double estimatedBudget;
        private String statusId;
        private List<Attachment> listFileAttachments;
        private List<Member> listMembers;
        private List<ObjectAttributesResponse> listAttributes;
        private Map<String, Lifecycle> lifecycles;

    }

    @Data
    @NoArgsConstructor
    @Schema(name = "ResearchProjectsResponseMember")
    public static class Member {
        private Long employeeId;
        private String employeeCode;
        private String employeeName;
        private String roleId;
        private String note;

    }

    @Data
    @NoArgsConstructor
    @Schema(name = "ResearchProjectsResponseLifecycle")
    public static class Lifecycle {
        private String documentNo;
        @JsonFormat(pattern = BaseConstants.COMMON_DATE_FORMAT, locale = BaseConstants.LOCALE_VN, timezone = BaseConstants.TIMEZONE_VN)
        private Date documentSignedDate;
        private List<ObjectAttributesResponse> listAttributes;
        private List<Member> listMembers;
        private List<Attachment> listFileAttachments;
    }
}
