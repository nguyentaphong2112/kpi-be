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

import java.util.Date;
import java.util.List;


/**
 * Lop Response DTO ung voi bang hr_concurrent_process
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Data
@NoArgsConstructor
@JsonInclude(Include.NON_NULL)
public class ConcurrentProcessResponse {

    @Data
    @NoArgsConstructor
    @Schema(name = "ConcurrentProcessResponseSearchResult")
    public static class SearchResult extends EmpBaseResponse {
        private Long concurrentProcessId;
        private Long employeeId;

        @JsonFormat(pattern = BaseConstants.COMMON_DATE_FORMAT, locale = BaseConstants.LOCALE_VN, timezone = BaseConstants.TIMEZONE_VN)
        private Date startDate;

        @JsonFormat(pattern = BaseConstants.COMMON_DATE_FORMAT, locale = BaseConstants.LOCALE_VN, timezone = BaseConstants.TIMEZONE_VN)
        private Date endDate;
        private Long jobId;
        private Long positionId;
        private Long organizationId;
        private String documentNo;

        @JsonFormat(pattern = BaseConstants.COMMON_DATE_FORMAT, locale = BaseConstants.LOCALE_VN, timezone = BaseConstants.TIMEZONE_VN)
        private Date documentSignedDate;

        private String concurrentOrg;
        private String concurrentJob;
    }

    @Data
    @NoArgsConstructor
    @Schema(name = "ConcurrentProcessResponseDetailBean")
    public static class DetailBean extends EmpBaseResponse{
        private Long concurrentProcessId;
        private Long employeeId;

        @JsonFormat(pattern = BaseConstants.COMMON_DATE_FORMAT, locale = BaseConstants.LOCALE_VN, timezone = BaseConstants.TIMEZONE_VN)
        private Date startDate;

        @JsonFormat(pattern = BaseConstants.COMMON_DATE_FORMAT, locale = BaseConstants.LOCALE_VN, timezone = BaseConstants.TIMEZONE_VN)
        private Date endDate;
        private Long jobId;
        private Long positionId;
        private Long organizationId;
        private String documentNo;
        private String concurrentOrg;
        private String concurrentJob;
        private String orgName;
        private String jobName;
        @JsonFormat(pattern = BaseConstants.COMMON_DATE_FORMAT, locale = BaseConstants.LOCALE_VN, timezone = BaseConstants.TIMEZONE_VN)
        private Date documentSignedDate;

        private List<Attachment> attachFileList;
        private List<ObjectAttributesResponse> listAttributes;
    }

}
