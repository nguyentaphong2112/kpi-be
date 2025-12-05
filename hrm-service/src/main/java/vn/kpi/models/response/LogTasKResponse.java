package vn.kpi.models.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import vn.kpi.constants.BaseConstants;

import java.util.Date;

public class LogTasKResponse {
    @Data
    @NoArgsConstructor
    @Schema(name = "logTasksResponseDetailBean")
    public static class DetailBean {
        private Long logTaskId;
        private String projectCode;
        private String name;
        @DateTimeFormat(pattern = BaseConstants.COMMON_DATE_FORMAT)
        @JsonFormat(pattern = BaseConstants.COMMON_DATE_FORMAT, locale = BaseConstants.LOCALE_VN, timezone = BaseConstants.TIMEZONE_VN)        private String logDate;
        private String description;
        private Long totalHouse;
    }

    @Data
    @NoArgsConstructor
    @Schema(name = "logTasksResponseSearchResult")
    public static class SearchResult {
        private Long logTaskId;
        private String projectCode;
        private String name;
        private Date logDate;
        private String description;
        private Long totalHouse;
        private String createdBy;

        @DateTimeFormat(pattern = BaseConstants.COMMON_DATE_FORMAT)
        @JsonFormat(pattern = BaseConstants.COMMON_DATE_FORMAT, locale = BaseConstants.LOCALE_VN, timezone = BaseConstants.TIMEZONE_VN)
        private Date createdTime;
        private String modifiedBy;

        @DateTimeFormat(pattern = BaseConstants.COMMON_DATE_FORMAT)
        @JsonFormat(pattern = BaseConstants.COMMON_DATE_FORMAT, locale = BaseConstants.LOCALE_VN, timezone = BaseConstants.TIMEZONE_VN)
        private Date modifiedTime;
    }

    @Data
    @NoArgsConstructor
    @Schema(name = "logTasksResponseUnloggedUser")
    public static class UnloggedUser {
        private String employeeCode;
        private Long telegramChatId;
        private String fullName;
    }
}
