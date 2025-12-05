/*
 * Copyright (C) 2022 EcoIT. All rights reserved.
 * EcoIT. Use is subject to license terms.
 */
package vn.hbtplus.models.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.models.Attachment;

import java.util.Date;
import java.util.List;


/**
 * Lop Response DTO ung voi bang PNS_CONTRACT_EVALUATIONS
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Data
@NoArgsConstructor
@JsonInclude(Include.NON_NULL)
public class ContractEvaluationsResponse {

    private Long contractEvaluationId;
    private Long contractProposalId;
    private Long employeeId;
    private Integer isDisciplined;
    private String disciplinedNote;
    private Double kpiPoint;
    private String rankCode;
    private String rankName;
    private String note;
    private Integer flagStatus;
    private String createdBy;

    @JsonFormat(pattern = BaseConstants.COMMON_DATE_FORMAT, locale = BaseConstants.LOCALE_VN, timezone = BaseConstants.TIMEZONE_VN)
    private Date createdTime;
    private String lastUpdatedBy;

    @JsonFormat(pattern = BaseConstants.COMMON_DATE_FORMAT, locale = BaseConstants.LOCALE_VN, timezone = BaseConstants.TIMEZONE_VN)
    private Date modifiedTime;

    private List<Attachment> attachFileList;

}
