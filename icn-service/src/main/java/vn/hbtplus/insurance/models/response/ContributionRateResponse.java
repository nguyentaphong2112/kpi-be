package vn.hbtplus.insurance.models.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import vn.hbtplus.constants.BaseConstants;

import java.util.Date;

@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ContributionRateResponse {
    private Long contributionRateId;

    private String empTypeCode;
    private Double unitSocialPercent;
    private Double perSocialPercent;
    private Double unitMedicalPercent;
    private Double perMedicalPercent;
    private Double unitUnempPercent;
    private Double perUnempPercent;
    private Double unitUnionPercent;
    private Double perUnionPercent;

    @DateTimeFormat(pattern = BaseConstants.COMMON_DATE_FORMAT)
    @JsonFormat(pattern = BaseConstants.COMMON_DATE_FORMAT, locale = BaseConstants.LOCALE_VN, timezone = BaseConstants.TIMEZONE_VN)
    private Date startDate;
    @DateTimeFormat(pattern = BaseConstants.COMMON_DATE_FORMAT)
    @JsonFormat(pattern = BaseConstants.COMMON_DATE_FORMAT, locale = BaseConstants.LOCALE_VN, timezone = BaseConstants.TIMEZONE_VN)
    private Date endDate;

    private String createdBy;

    @DateTimeFormat(pattern = BaseConstants.COMMON_DATETIME_FORMAT)
    @JsonFormat(pattern = BaseConstants.COMMON_DATETIME_FORMAT, locale = BaseConstants.LOCALE_VN, timezone = BaseConstants.TIMEZONE_VN)
    private Date createdTime;

    private String modifiedBy;
    @DateTimeFormat(pattern = BaseConstants.COMMON_DATETIME_FORMAT)
    @JsonFormat(pattern = BaseConstants.COMMON_DATETIME_FORMAT, locale = BaseConstants.LOCALE_VN, timezone = BaseConstants.TIMEZONE_VN)
    private Date modifiedTime;
}
