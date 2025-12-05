package vn.kpi.models.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;
import vn.kpi.constants.BaseConstants;
import vn.kpi.models.BaseSearchRequest;

import javax.validation.constraints.NotBlank;
import java.util.Date;
import java.util.List;

public class FeedbackRequest {
    @Data
    @NoArgsConstructor
    public static class SearchForm extends BaseSearchRequest {
        private String keySearch;
        private String functionCode;
        private Long objectId;
        private String status;
        private String type;

        @DateTimeFormat(pattern = BaseConstants.COMMON_DATE_FORMAT)
        @JsonFormat(pattern = BaseConstants.COMMON_DATE_FORMAT, locale = BaseConstants.LOCALE_VN, timezone = BaseConstants.TIMEZONE_VN)
        private Date startDate;

        @DateTimeFormat(pattern = BaseConstants.COMMON_DATE_FORMAT)
        @JsonFormat(pattern = BaseConstants.COMMON_DATE_FORMAT, locale = BaseConstants.LOCALE_VN, timezone = BaseConstants.TIMEZONE_VN)
        private Date endDate;
    }
    @Data
    @NoArgsConstructor
    public static class SubmitForm {

        private String functionCode;
        @NotBlank
        private String content;
        private Long objectId;
        private String status;

        private String type;

        List<MultipartFile> attachments;
    }
}
