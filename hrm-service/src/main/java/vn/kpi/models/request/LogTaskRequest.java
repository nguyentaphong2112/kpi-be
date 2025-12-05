package vn.kpi.models.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import vn.kpi.constants.BaseConstants;
import vn.kpi.models.BaseSearchRequest;
import vn.kpi.utils.StrimDeSerializer;

import javax.validation.constraints.Size;
import java.util.Date;


public class LogTaskRequest {
    @Data
    @NoArgsConstructor
    @Schema(name = "logTaskSubmitForm")
    public static class SubmitForm {

        private Long logTaskId;

        @Size(max = 255)
        @JsonDeserialize(using = StrimDeSerializer.class)
        private String projectCode;

        private String name;

        @DateTimeFormat(pattern = BaseConstants.COMMON_DATE_FORMAT)
        @JsonFormat(pattern = BaseConstants.COMMON_DATE_FORMAT, locale = BaseConstants.LOCALE_VN, timezone = BaseConstants.TIMEZONE_VN)
        private Date logDate;

        private String description;

        private Long totalHouse;

    }

    @Data
    @NoArgsConstructor
    @Schema(name = "logTaskSearchForm")
    public static class SearchForm extends BaseSearchRequest {

        private Long logTaskId;

        @Size(max = 50)
        @JsonDeserialize(using = StrimDeSerializer.class)
        private String projectCode;

        @Size(max = 255)
        @JsonDeserialize(using = StrimDeSerializer.class)
        private String name;

        @DateTimeFormat(pattern = BaseConstants.COMMON_DATE_FORMAT)
        @JsonFormat(pattern = BaseConstants.COMMON_DATE_FORMAT, locale = BaseConstants.LOCALE_VN, timezone = BaseConstants.TIMEZONE_VN)
        private Date toDate;

        @DateTimeFormat(pattern = BaseConstants.COMMON_DATE_FORMAT)
        @JsonFormat(pattern = BaseConstants.COMMON_DATE_FORMAT, locale = BaseConstants.LOCALE_VN, timezone = BaseConstants.TIMEZONE_VN)
        private Date fromDate;

        private String description;

        private Long totalHouse;
        private String createdBy;

    }
}
