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
import vn.hbtplus.models.dto.OrgDTO;

import java.util.Date;
import java.util.List;


/**
 * Lop Response DTO ung voi bang PNS_CONFIG_SEQ_CONTRACTS
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Data
@NoArgsConstructor
@JsonInclude(Include.NON_NULL)
public class ConfigSeqContractsResponse {

    private Long configSeqContractId;
    private Long organizationId;
    private String orgGroup;
    private Long positionGroupId;
    private String documentNo;
    private String note;
    private Integer flagStatus;
    private String createdBy;

    @JsonFormat(pattern = BaseConstants.COMMON_DATE_FORMAT, locale = BaseConstants.LOCALE_VN, timezone = BaseConstants.TIMEZONE_VN)
    private Date createdTime;
    private String lastUpdatedBy;

    @JsonFormat(pattern = BaseConstants.COMMON_DATE_FORMAT, locale = BaseConstants.LOCALE_VN, timezone = BaseConstants.TIMEZONE_VN)
    private Date modifiedTime;

    @JsonFormat(pattern = BaseConstants.COMMON_DATE_FORMAT, locale = BaseConstants.LOCALE_VN, timezone = BaseConstants.TIMEZONE_VN)
    private Date fromDate;

    @JsonFormat(pattern = BaseConstants.COMMON_DATE_FORMAT, locale = BaseConstants.LOCALE_VN, timezone = BaseConstants.TIMEZONE_VN)
    private Date toDate;

    private String orgName;

    private String contractTypeNameStr;

    private List<String> contractTypeName;

    private String positionGroupName;

    private String empTypeName;

    private String state;

    private String empTypeCode;

    private List<Long> contractTypeIds;

    private OrgDTO org;

}
