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
import vn.kpi.models.Attachment;

import java.util.Date;
import java.util.List;


/**
 * Lop Response DTO ung voi bang hr_contract_process
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Data
@NoArgsConstructor
@JsonInclude(Include.NON_NULL)
public class ContractProcessResponse {

    @Data
    @NoArgsConstructor
    @Schema(name = "ContractProcessResponseSearchResult")
    public static class SearchResult extends EmpBaseResponse {
        private Long contractProcessId;
        private Long employeeId;
        private Long empTypeId;
        private Long contractTypeId;

        @JsonFormat(pattern = BaseConstants.COMMON_DATE_FORMAT, locale = BaseConstants.LOCALE_VN, timezone = BaseConstants.TIMEZONE_VN)
        private Date startDate;

        @JsonFormat(pattern = BaseConstants.COMMON_DATE_FORMAT, locale = BaseConstants.LOCALE_VN, timezone = BaseConstants.TIMEZONE_VN)
        private Date endDate;
        private String documentNo;
        private String contractTypeName;

        @JsonFormat(pattern = BaseConstants.COMMON_DATE_FORMAT, locale = BaseConstants.LOCALE_VN, timezone = BaseConstants.TIMEZONE_VN)
        private Date documentSignedDate;

        private String classifyCode;
        private List<ObjectAttributesResponse> listAttributes;

    }


    @Data
    @NoArgsConstructor
    @Schema(name = "ContractProcessDetailBean")
    public static class DetailBean {
        private Long contractProcessId;
        private Long employeeId;
        private Long empTypeId;
        private Long contractTypeId;

        @JsonFormat(pattern = BaseConstants.COMMON_DATE_FORMAT, locale = BaseConstants.LOCALE_VN, timezone = BaseConstants.TIMEZONE_VN)
        private Date startDate;

        @JsonFormat(pattern = BaseConstants.COMMON_DATE_FORMAT, locale = BaseConstants.LOCALE_VN, timezone = BaseConstants.TIMEZONE_VN)
        private Date endDate;
        private String documentNo;
        private String empTypeName;
        private String contractTypeName;

        @JsonFormat(pattern = BaseConstants.COMMON_DATE_FORMAT, locale = BaseConstants.LOCALE_VN, timezone = BaseConstants.TIMEZONE_VN)
        private Date documentSignedDate;

        private String classifyCode;
        private List<Attachment> attachFileList;
        List<ObjectAttributesResponse> listAttributes;
    }

}
