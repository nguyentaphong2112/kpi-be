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

import java.util.Date;
import java.util.List;


/**
 * Lop Response DTO ung voi bang hr_award_process
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Data
@NoArgsConstructor
@JsonInclude(Include.NON_NULL)
public class AwardProcessResponse {

    @Data
    @NoArgsConstructor
    @Schema(name = "AwardProcessResponseSearchResult")
    public static class SearchResult extends EmpBaseResponse {
        private Long awardProcessId;
        private Long employeeId;
        private String awardFormId;
        private String awardFormName;
        private Long awardYear;
        private String documentNo;

        @JsonFormat(pattern = BaseConstants.COMMON_DATE_FORMAT, locale = BaseConstants.LOCALE_VN, timezone = BaseConstants.TIMEZONE_VN)
        private Date documentSignedDate;
        List<ObjectAttributesResponse> listAttributes;
    }

    @Data
    @NoArgsConstructor
    @Schema(name = "AwardProcessResponseDetailBean")
    public static class DetailBean {
        private Long awardProcessId;
        private Long employeeId;
        private String awardFormId;
        private String awardFormName;
        private Long awardYear;
        private String documentNo;

        @JsonFormat(pattern = BaseConstants.COMMON_DATE_FORMAT, locale = BaseConstants.LOCALE_VN, timezone = BaseConstants.TIMEZONE_VN)
        private Date documentSignedDate;

        List<ObjectAttributesResponse> listAttributes;
    }

}
