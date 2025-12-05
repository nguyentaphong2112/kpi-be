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
 * Lop Response DTO ung voi bang hr_education_promotions
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Data
@NoArgsConstructor
@JsonInclude(Include.NON_NULL)
public class EducationPromotionsResponse {

    @Data
    @NoArgsConstructor
    @Schema(name = "EducationPromotionsResponseSearchResult")
    public static class SearchResult extends EmpBaseResponse{
        private Long educationPromotionId;
        private Long employeeId;
        private Long issuedYear;
        private String issuedPlace;
        private String promotionRankId;
        private String promotionRankName;
    }

    @Data
    @NoArgsConstructor
    @Schema(name = "EducationPromotionsResponseDetailBean")
    public static class DetailBean extends EmpBaseResponse{
        private Long educationPromotionId;
        private Long employeeId;
        private Long issuedYear;
        private String issuedPlace;
        private String promotionRankId;
        private String promotionRankName;
        private String createdBy;

        @JsonFormat(pattern = BaseConstants.COMMON_DATETIME_FORMAT, locale = BaseConstants.LOCALE_VN, timezone = BaseConstants.TIMEZONE_VN)
        private Date createdTime;
        private String modifiedBy;

        @JsonFormat(pattern = BaseConstants.COMMON_DATETIME_FORMAT, locale = BaseConstants.LOCALE_VN, timezone = BaseConstants.TIMEZONE_VN)
        private Date modifiedTime;
        private List<ObjectAttributesResponse> listAttributes;
    }
}
