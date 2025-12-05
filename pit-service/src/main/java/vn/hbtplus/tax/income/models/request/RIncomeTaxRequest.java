package vn.hbtplus.tax.income.models.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import vn.hbtplus.constants.BaseConstants;

import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
public class RIncomeTaxRequest {
    @Data
    @NoArgsConstructor
    @Schema(name = "RIncomeTaxRequestReportByIdForm")
    public static class ReportByIdForm {
        @DateTimeFormat(pattern = BaseConstants.COMMON_DATE_FORMAT)
        @JsonFormat(pattern = BaseConstants.COMMON_DATE_FORMAT, locale = BaseConstants.LOCALE_VN, timezone = BaseConstants.TIMEZONE_VN)
        private Date startDate;
        @DateTimeFormat(pattern = BaseConstants.COMMON_DATE_FORMAT)
        @JsonFormat(pattern = BaseConstants.COMMON_DATE_FORMAT, locale = BaseConstants.LOCALE_VN, timezone = BaseConstants.TIMEZONE_VN)
        private Date endDate;
        private String keySearch;
        private String inputType;
        private List<Long> orgIds;
    }
}
