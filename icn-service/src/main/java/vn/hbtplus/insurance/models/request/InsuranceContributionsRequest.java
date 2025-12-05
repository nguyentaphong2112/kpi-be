/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.insurance.models.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.models.BaseSearchRequest;
import vn.hbtplus.utils.StrimDeSerializer;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Date;
import java.util.List;

/**
 * Lop DTO ung voi bang icn_insurance_contributions
 *
 * @author tudd
 * @version 1.0
 * @since 1.0
 */

@Data
@NoArgsConstructor
public class InsuranceContributionsRequest extends BaseSearchRequest {

    @Data
    @NoArgsConstructor
    public static class SubmitForm {
        private Long insuranceContributionId;

        @DateTimeFormat(pattern = BaseConstants.COMMON_DATE_FORMAT)
        @JsonFormat(pattern = BaseConstants.COMMON_DATE_FORMAT, locale = BaseConstants.LOCALE_VN, timezone = BaseConstants.TIMEZONE_VN)
        private Date periodDate;

        private Long employeeId;

        @Size(max = 50)
        @JsonDeserialize(using = StrimDeSerializer.class)
        private String empTypeCode;

        @Size(max = 200)
        @JsonDeserialize(using = StrimDeSerializer.class)
        private String labourType;

        private Long jobId;

        private Long orgId;


        @Size(max = 255)
        @JsonDeserialize(using = StrimDeSerializer.class)
        private String insuranceAgency;

        @Size(max = 20)
        @JsonDeserialize(using = StrimDeSerializer.class)
        private String type;

    }

    @Data
    @NoArgsConstructor
    @Schema(name = "InsuranceContributionsRequest_ReportForm")
    public static class ReportForm {
        @Schema(
                description = "Type of period (MONTH, QUARTER, YEAR)",
                allowableValues = {"MONTH", "QUARTER", "YEAR"},
                example = "MONTH",
                required = true
        )
        private String periodType;
        private int year;
        private int startYear;
        private int endYear;
        private int quarter;
        private List<String> listLocationJoin;
        private List<String> listStatus;
        @DateTimeFormat(pattern = BaseConstants.COMMON_DATE_FORMAT)
        @JsonFormat(pattern = BaseConstants.COMMON_DATE_FORMAT, locale = BaseConstants.LOCALE_VN, timezone = BaseConstants.TIMEZONE_VN)
        private Date startDate;
        @DateTimeFormat(pattern = BaseConstants.COMMON_DATE_FORMAT)
        @JsonFormat(pattern = BaseConstants.COMMON_DATE_FORMAT, locale = BaseConstants.LOCALE_VN, timezone = BaseConstants.TIMEZONE_VN)
        private Date endDate;
    }

    @Data
    @NoArgsConstructor
    @Schema(name = "InsuranceContributionsRequest_RetroMedicalForm")
    public static class RetroMedicalForm {
        @NotBlank
        private String empCodes;
        @DateTimeFormat(pattern = BaseConstants.COMMON_DATE_FORMAT)
        @JsonFormat(pattern = BaseConstants.COMMON_DATE_FORMAT, locale = BaseConstants.LOCALE_VN, timezone = BaseConstants.TIMEZONE_VN)
        @NotNull
        private Date toPeriodDate;
        @DateTimeFormat(pattern = BaseConstants.COMMON_DATE_FORMAT)
        @JsonFormat(pattern = BaseConstants.COMMON_DATE_FORMAT, locale = BaseConstants.LOCALE_VN, timezone = BaseConstants.TIMEZONE_VN)
        @NotNull
        private Date fromPeriodDate;
        private String isIndividuals;
        private String isUnitPayed;
    }

    @Data
    @NoArgsConstructor
    public static class SearchForm extends BaseSearchRequest {

        private Long insuranceContributionId;
        private Integer isPreview;

        @DateTimeFormat(pattern = BaseConstants.COMMON_DATE_FORMAT)
        @JsonFormat(pattern = BaseConstants.COMMON_DATE_FORMAT, locale = BaseConstants.LOCALE_VN, timezone = BaseConstants.TIMEZONE_VN)
        private Date periodDate;

        private Long employeeId;

        @Size(max = 50)
        @JsonDeserialize(using = StrimDeSerializer.class)
        private String empTypeCode;

        @Size(max = 200)
        @JsonDeserialize(using = StrimDeSerializer.class)
        private String labourType;

        private Long jobId;

        private Long orgId;

        private List<String> status;

        @Size(max = 255)
        @JsonDeserialize(using = StrimDeSerializer.class)
        private String reason;

        @Size(max = 1000)
        @JsonDeserialize(using = StrimDeSerializer.class)
        private String note;

        @Size(max = 255)
        @JsonDeserialize(using = StrimDeSerializer.class)
        private String leaveReason;

        private Double maternityTimekeeping;

        @Size(max = 255)
        @JsonDeserialize(using = StrimDeSerializer.class)
        private String insuranceAgency;

        private List<String> type;

        private List<String> classify;
    }


}
