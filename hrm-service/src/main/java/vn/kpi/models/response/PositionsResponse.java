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


/**
 * Lop Response DTO ung voi bang hr_positions
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Data
@NoArgsConstructor
@JsonInclude(Include.NON_NULL)
public class PositionsResponse {

    @Data
    @NoArgsConstructor
    @Schema(name = "PositionsResponseDetailBean")
    public static class DetailBean {
        private Long positionId;
        private Long organizationId;
        private String name;
        private String orgName;
        private Long jobId;
        private String jobType;
        private Integer quotaNumber;
    }

    @Data
    @NoArgsConstructor
    @Schema(name = "PositionsResponseSearchResult")
    public static class SearchResult {
        private Long positionId;
        private Long jobId;
        private Long organizationId;
        private String name;
        private String orgName;
        private String createdBy;

        @JsonFormat(pattern = BaseConstants.COMMON_DATETIME_FORMAT, locale = BaseConstants.LOCALE_VN, timezone = BaseConstants.TIMEZONE_VN)
        private Date createdTime;
        private String modifiedBy;

        @JsonFormat(pattern = BaseConstants.COMMON_DATETIME_FORMAT, locale = BaseConstants.LOCALE_VN, timezone = BaseConstants.TIMEZONE_VN)
        private Date modifiedTime;

        private Integer quotaNumber;
        private Integer actualNumber;
    }


}
