/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.models.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import java.util.Date;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.hbtplus.constants.BaseConstants;


/**
 * Lop Response DTO ung voi bang crm_training_programs
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Data
@NoArgsConstructor
@JsonInclude(Include.NON_NULL)
public class TrainingProgramsResponse {

    @Data
    @NoArgsConstructor
    @Schema(name = "TrainingProgramsResponseSearchResult")
    public static class SearchResult {
        private Long trainingProgramId;
        private String title;
        private String lessons;
        private String createdBy;

        @JsonFormat(pattern = BaseConstants.COMMON_DATETIME_FORMAT, locale = BaseConstants.LOCALE_VN, timezone = BaseConstants.TIMEZONE_VN)
        private Date createdTime;
        private String modifiedBy;

        @JsonFormat(pattern = BaseConstants.COMMON_DATETIME_FORMAT, locale = BaseConstants.LOCALE_VN, timezone = BaseConstants.TIMEZONE_VN)
        private Date modifiedTime;
    }

    @Data
    @NoArgsConstructor
    @Schema(name = "TrainingProgramsResponseDetailBean")
    public static class DetailBean {
        private Long trainingProgramId;
        private String title;
        private String lessons;
        private List<ObjectAttributesResponse> listAttributes;
    }

    @Data
    @NoArgsConstructor
    @Schema(name = "TrainingProgramsResponseDataSelected")
    public static class DataSelected {
        private Long trainingProgramId;
        private String title;
        private String lessons;
    }

}
