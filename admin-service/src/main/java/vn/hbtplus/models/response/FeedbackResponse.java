package vn.hbtplus.models.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.models.Attachment;

import java.util.Date;
import java.util.List;


public class FeedbackResponse {
    @Data
    @NoArgsConstructor
    public static class SearchResult {
        private Long feedbackId;
        private String content;
        private String statusName;
        private List<CommentBean> comments;
        private String orgName;
        private String jobName;
        private String status;
        private String employeeCode;
        private String fullName;
        private String type;
        private String typeName;
        @JsonFormat(pattern = BaseConstants.COMMON_DATETIME_FORMAT, locale = BaseConstants.LOCALE_VN, timezone = BaseConstants.TIMEZONE_VN)
        private Date createdTime;
        private String createdBy;
        private List<Attachment> attachFileList;
    }

    @Data
    @NoArgsConstructor
    public static class DetailBean {
        private String content;
        private String statusName;
        private List<CommentBean> comments;
        @JsonFormat(pattern = BaseConstants.COMMON_DATETIME_FORMAT, locale = BaseConstants.LOCALE_VN, timezone = BaseConstants.TIMEZONE_VN)
        private Date createdTime;
        private String orgName;
        private String jobName;
        private String status;
        private String employeeCode;
        private String fullName;
        private String loginName;
        private String type;
        private String typeName;
        private List<Attachment> attachFileList;
    }

    @Data
    @NoArgsConstructor
    public static class CommentBean {
        private String content;
        private String userName;
        @JsonFormat(pattern = BaseConstants.COMMON_DATETIME_FORMAT, locale = BaseConstants.LOCALE_VN, timezone = BaseConstants.TIMEZONE_VN)
        private Date createdTime;
        private String createdBy;
    }
}
