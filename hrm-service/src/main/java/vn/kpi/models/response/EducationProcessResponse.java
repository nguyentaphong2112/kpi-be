/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.kpi.models.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.kpi.constants.BaseConstants;

import java.util.Date;
import java.util.List;


/**
 * Lop Response DTO ung voi bang hr_education_process
 *
 * @author tudd
 * @version 1.0
 * @since 1.0
 */

@Data
@NoArgsConstructor
@JsonInclude(Include.NON_NULL)
public class EducationProcessResponse {
    @Data
    @NoArgsConstructor
    @Schema(name = "EducationProcessResponseSearchResult")
    public static class SearchResult extends EmpBaseResponse {
        private Long educationProcessId;
        private Long employeeId;
        private String courseName;
        private String trainingMethodId;
        private String trainingMethodName;
        private String trainingMethodPlace;
        private String courseContent;
        private String result;
        private String startDate;
        private String endDate;
    }

    @Data
    @NoArgsConstructor
    @Schema(name = "EducationProcessResponseDetailBean")
    public static class DetailBean extends EmpBaseResponse {
        private Long educationProcessId;
        private Long employeeId;
        private String courseName;
        private String trainingMethodId;
        private String trainingMethodName;
        private String trainingMethodPlace;
        private String courseContent;
        private String result;
        @JsonFormat(pattern = BaseConstants.SHORT_DATE_FORMAT, locale = BaseConstants.LOCALE_VN, timezone = BaseConstants.TIMEZONE_VN)
        private Date startDate;

        @JsonFormat(pattern = BaseConstants.SHORT_DATE_FORMAT, locale = BaseConstants.LOCALE_VN, timezone = BaseConstants.TIMEZONE_VN)
        private Date endDate;
        private List<ObjectAttributesResponse> listAttributes;
    }
}
