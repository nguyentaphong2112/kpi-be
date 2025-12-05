/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.models.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.models.Attachment;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 * Lop Response DTO ung voi bang hr_position_salary_process
 *
 * @author tudd
 * @version 1.0
 * @since 1.0
 */

@Data
@NoArgsConstructor
@JsonInclude(Include.NON_NULL)
public class PositionSalaryProcessResponse {

    @Data
    @NoArgsConstructor
    @Schema(name = "PositionSalaryProcessResponseSearchResult")
    public static class SearchResult extends EmpBaseResponse {
        private Long positionSalaryProcessId;
        private Long employeeId;
        private Long salaryGradeId;

        @JsonFormat(pattern = BaseConstants.COMMON_DATE_FORMAT, locale = BaseConstants.LOCALE_VN, timezone = BaseConstants.TIMEZONE_VN)
        private Date startDate;

        @JsonFormat(pattern = BaseConstants.COMMON_DATE_FORMAT, locale = BaseConstants.LOCALE_VN, timezone = BaseConstants.TIMEZONE_VN)
        private Date endDate;
        private String documentNo;

        @JsonFormat(pattern = BaseConstants.COMMON_DATE_FORMAT, locale = BaseConstants.LOCALE_VN, timezone = BaseConstants.TIMEZONE_VN)
        private Date documentSignedDate;
        private String createdBy;

        @JsonFormat(pattern = BaseConstants.COMMON_DATETIME_FORMAT, locale = BaseConstants.LOCALE_VN, timezone = BaseConstants.TIMEZONE_VN)
        private Date createdTime;
        private String modifiedBy;

        @JsonFormat(pattern = BaseConstants.COMMON_DATETIME_FORMAT, locale = BaseConstants.LOCALE_VN, timezone = BaseConstants.TIMEZONE_VN)
        private Date modifiedTime;
        private List<ObjectAttributesResponse> listAttributes;

        private String salaryGradeName;
        private String salaryRankName;
        private String salaryRankCode;
        private Double salaryAmount;
        private Double percent;
        private String salaryTypeName;
        private String salaryJobName;
    }

    @Data
    @NoArgsConstructor
    @Schema(name = "PositionSalaryProcessResponseDetailBean")
    public static class DetailBean {
        private Long positionSalaryProcessId;
        private Long employeeId;
        private Long salaryGradeId;
        private String salaryGradeName;
        private Long salaryRankId;
        private String salaryRankName;
        private Double salaryAmount;
        private Long percent;

        @JsonFormat(pattern = BaseConstants.COMMON_DATE_FORMAT, locale = BaseConstants.LOCALE_VN, timezone = BaseConstants.TIMEZONE_VN)
        private Date startDate;

        @JsonFormat(pattern = BaseConstants.COMMON_DATE_FORMAT, locale = BaseConstants.LOCALE_VN, timezone = BaseConstants.TIMEZONE_VN)
        private Date endDate;
        private String documentNo;
        private String salaryType;
        private Long jobId;

        @JsonFormat(pattern = BaseConstants.COMMON_DATE_FORMAT, locale = BaseConstants.LOCALE_VN, timezone = BaseConstants.TIMEZONE_VN)
        private Date documentSignedDate;
        private List<ObjectAttributesResponse> listAttributes;
        List<Attachment> attachFileList;

    }

    @Data
    @NoArgsConstructor
    @Schema(name = "PositionSalaryProcessResponseDetailBeanV2")
    public static class DetailBeanV2 {
        private Long positionSalaryProcessId;
        private Long employeeId;
        @JsonFormat(pattern = BaseConstants.COMMON_DATE_FORMAT, locale = BaseConstants.LOCALE_VN, timezone = BaseConstants.TIMEZONE_VN)
        private Date startDate;

        @JsonFormat(pattern = BaseConstants.COMMON_DATE_FORMAT, locale = BaseConstants.LOCALE_VN, timezone = BaseConstants.TIMEZONE_VN)
        private Date endDate;

        private List<ObjectAttributesResponse> listAttributes;
        List<Attachment> attachFileList;

        private List<FormData> formData = new ArrayList<>();

    }

    @Data
    @NoArgsConstructor
    @Schema(name = "PositionSalaryProcessResponseDetailBeanV2")
    public static class FormData {
        private Long salaryGradeId;
        private String salaryGradeName;
        private Long salaryRankId;
        private String salaryRankName;
        private Double salaryAmount;
        private Long percent;
        private String salaryType;
        private Long jobId;
    }
}
