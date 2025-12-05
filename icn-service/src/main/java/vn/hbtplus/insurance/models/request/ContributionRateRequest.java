package vn.hbtplus.insurance.models.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.databinds.TrimStringDeSerializer;
import vn.hbtplus.models.BaseSearchRequest;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.List;


@Data
@NoArgsConstructor
public class ContributionRateRequest {
    @Data
    @NoArgsConstructor
    @Schema(name = "ContributionRateRequestSubmitForm")
    public static class SubmitForm {
        private Long contributionRateId;

        @JsonDeserialize(using = TrimStringDeSerializer.class)
        @NotBlank(message = "empTypeCode must not be null")
        private String empTypeCode;

        @NotNull(message = "unitSocialPercent must not be null")
        @Min(value = 0l)
        @Max(value = 100l)
        private Double unitSocialPercent;

        @NotNull
        @Min(value = 0l)
        @Max(value = 100l)
        private Double perSocialPercent;

        @NotNull
        @Min(value = 0l)
        @Max(value = 100l)
        private Double unitMedicalPercent;
        @NotNull
        @Min(value = 0l)
        @Max(value = 100l)
        private Double perMedicalPercent;
        @NotNull
        @Min(value = 0l)
        @Max(value = 100l)
        private Double unitUnempPercent;
        @NotNull
        @Min(value = 0l)
        @Max(value = 100l)
        private Double perUnempPercent;
        @NotNull
        @Min(value = 0l)
        @Max(value = 100l)
        private Double unitUnionPercent;
        @NotNull
        @Min(value = 0l)
        @Max(value = 100l)
        private Double perUnionPercent;

        @DateTimeFormat(pattern = BaseConstants.COMMON_DATE_FORMAT)
        @JsonFormat(pattern = BaseConstants.COMMON_DATE_FORMAT, locale = BaseConstants.LOCALE_VN, timezone = BaseConstants.TIMEZONE_VN)
        private Date startDate;

        @DateTimeFormat(pattern = BaseConstants.COMMON_DATE_FORMAT)
        @JsonFormat(pattern = BaseConstants.COMMON_DATE_FORMAT, locale = BaseConstants.LOCALE_VN, timezone = BaseConstants.TIMEZONE_VN)
        private Date endDate;
    }

    @Data
    @NoArgsConstructor
    @Schema(name = "ContributionRateRequestSearchForm")
    public class SearchForm extends BaseSearchRequest {
        private List<String> empTypeCode;

        @DateTimeFormat(pattern = BaseConstants.COMMON_DATE_FORMAT)
        @JsonFormat(pattern = BaseConstants.COMMON_DATE_FORMAT, locale = BaseConstants.LOCALE_VN, timezone = BaseConstants.TIMEZONE_VN)
        private Date startDate;

        @DateTimeFormat(pattern = BaseConstants.COMMON_DATE_FORMAT)
        @JsonFormat(pattern = BaseConstants.COMMON_DATE_FORMAT, locale = BaseConstants.LOCALE_VN, timezone = BaseConstants.TIMEZONE_VN)
        private Date endDate;
    }
}
