package vn.hbtplus.insurance.models.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.models.BaseSearchRequest;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
public class ParameterRequest extends BaseSearchRequest {
    @Data
    @NoArgsConstructor
    @Schema(name = "ParameterRequestSubmitForm")
    public static class SubmitForm {
        @DateTimeFormat(pattern = BaseConstants.COMMON_DATE_FORMAT)
        @JsonFormat(pattern = BaseConstants.COMMON_DATE_FORMAT, locale = BaseConstants.LOCALE_VN, timezone = BaseConstants.TIMEZONE_VN)
        @NotNull
        private Date startDate;
        @DateTimeFormat(pattern = BaseConstants.COMMON_DATE_FORMAT)
        @JsonFormat(pattern = BaseConstants.COMMON_DATE_FORMAT, locale = BaseConstants.LOCALE_VN, timezone = BaseConstants.TIMEZONE_VN)
        private Date endDate;
        private String configGroup;
        @NotEmpty
        private List<Column> columns;
        @Data
        @AllArgsConstructor
        @NoArgsConstructor
        public static class Column {
            private String configCode;
            private String configValue;
        }
    }
    @Data
    @NoArgsConstructor
    public static class SearchForm extends BaseSearchRequest{

    }
}
