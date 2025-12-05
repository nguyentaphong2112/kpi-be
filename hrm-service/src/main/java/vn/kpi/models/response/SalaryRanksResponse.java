/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.kpi.models.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import java.util.Date;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.kpi.constants.BaseConstants;
import vn.kpi.utils.Utils;

import javax.validation.constraints.NotNull;


/**
 * Lop Response DTO ung voi bang hr_salary_ranks
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Data
@NoArgsConstructor
@JsonInclude(Include.NON_NULL)
public class SalaryRanksResponse {
    private Long salaryRankId;
    private String code;
    private String name;
    public String getDisplayName(){
        return Utils.join(" - ", code, name);
    }
    private List<ObjectAttributesResponse> listAttributes;

    @Data
    @NoArgsConstructor
    @Schema(name = "SalaryRanksResponseSearchResult")
    public static class SearchResult {
        private Long salaryRankId;
        private String code;
        private String name;
        private String salaryType;
        private String salaryTypeName;
        private Long orderNumber;

        @JsonFormat(pattern = BaseConstants.COMMON_DATE_FORMAT, locale = BaseConstants.LOCALE_VN, timezone = BaseConstants.TIMEZONE_VN)
        private Date startDate;

        @JsonFormat(pattern = BaseConstants.COMMON_DATE_FORMAT, locale = BaseConstants.LOCALE_VN, timezone = BaseConstants.TIMEZONE_VN)
        private Date endDate;
        private String createdBy;

        @JsonFormat(pattern = BaseConstants.COMMON_DATETIME_FORMAT, locale = BaseConstants.LOCALE_VN, timezone = BaseConstants.TIMEZONE_VN)
        private Date createdTime;
        private String modifiedBy;

        @JsonFormat(pattern = BaseConstants.COMMON_DATETIME_FORMAT, locale = BaseConstants.LOCALE_VN, timezone = BaseConstants.TIMEZONE_VN)
        private Date modifiedTime;
    }

    @Data
    @NoArgsConstructor
    @Schema(name = "SalaryRanksResponseDetailBean")
    public static class DetailBean {
        private Long salaryRankId;
        private String code;
        private String name;
        private String salaryType;
        private Long orderNumber;

        @JsonFormat(pattern = BaseConstants.COMMON_DATE_FORMAT, locale = BaseConstants.LOCALE_VN, timezone = BaseConstants.TIMEZONE_VN)
        private Date startDate;

        @JsonFormat(pattern = BaseConstants.COMMON_DATE_FORMAT, locale = BaseConstants.LOCALE_VN, timezone = BaseConstants.TIMEZONE_VN)
        private Date endDate;

        private List<SalaryGradeDto> grades;
        private List<SalaryJobDto> salaryJobs;
        private List<ObjectAttributesResponse> listAttributes;
    }
    @Data
    @NoArgsConstructor
    @Schema(name = "SalaryRanksResponseSalaryGradeDto")
    public static class SalaryGradeDto {
        private Long salaryGradeId;
        private Long salaryRankId;
        @NotNull
        private String name;
        private Integer duration;
        private Double amount;
        private String note;
        private String gradeName;
        private String salaryRankName;
        private String salaryRankCode;
    }
    @Data
    @NoArgsConstructor
    @Schema(name = "SalaryRanksResponseSalaryJobDto")
    public static class SalaryJobDto {
        private Long jobId;
        private String jobCode;
        private String jobName;
    }


}
