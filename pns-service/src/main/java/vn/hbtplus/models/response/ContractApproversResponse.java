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
import vn.hbtplus.repositories.entity.ContractApproversEntity;


import java.util.Date;
import java.util.List;


/**
 * Lop Response DTO ung voi bang PNS_CONTRACT_APPROVERS
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Data
@NoArgsConstructor
@JsonInclude(Include.NON_NULL)
public class ContractApproversResponse {

    private Integer type;

    private Long contractApproverId;
    private Long contractApproverId1;
    private Long contractApproverId2;
    private Long contractApproverId3;
    private Long contractProposalId;
    private Long approverId;
    private Integer approverLevel;
    private Long contractTypeId;
    private String note;
    private Integer isLiquidation;
    private Integer status;
    private Integer liquidationStatusOfNext; // trạng thái đánh giá của ông cấp trên
    private Integer flagStatus;
    private String createdBy;

    private Long employeeId;
    private String employeeCode;
    private String empName;
    private Long positionId;
    private String positionName;
    private Long orgId;
    private String orgName;

    private Long curContractTypeId;
    private String curContractTypeName;

    private Long contractByLawId;
    private String contractByLawName;

    private Integer isDisciplined;
    private String disciplinedName;
    private Long kpiPoint;

    private String rankCode;
    private String rankName;

    private Long signerId;
    private String signerName;

    private String signerPosition;
    private String delegacyNo;

    private Long directManagerId;
    private String directManagerName;
    private Integer dmIsLiquidation;  // Quản lý trực tiếp
    private String dmLiquidationName;
    private Long dmContractTypeId;
    private String dmContractTypeName;
    private Integer dmCursorCurrent;
    private Integer dmApproverLevel;

    private Long approvalLevelPersonId;
    private String approvalLevelPersonName;
    private Integer alIsLiquidation;  // Cấp phê duyệt
    private String alLiquidationName;
    private Long alContractTypeId;
    private String alContractTypeName;
    private Integer alCursorCurrent;
    private Integer alApproverLevel;

    private Long changeApprovalLevelId;
    private String changeApprovalLevelName;
    private Integer calIsLiquidation;  // Cấp phê duyệt thay đổi
    private String calLiquidationName;
    private Long calContractTypeId;
    private String calContractTypeName;
    private Integer calCursorCurrent;
    private Integer calApproverLevel;


    @JsonFormat(pattern = BaseConstants.COMMON_DATE_FORMAT, locale = BaseConstants.LOCALE_VN, timezone = BaseConstants.TIMEZONE_VN)
    private Date createdTime;
    private String lastUpdatedBy;

    @JsonFormat(pattern = BaseConstants.COMMON_DATE_FORMAT, locale = BaseConstants.LOCALE_VN, timezone = BaseConstants.TIMEZONE_VN)
    private Date modifiedTime;
    private Integer cursorCurrent;


    @JsonFormat(pattern = BaseConstants.COMMON_DATE_FORMAT, locale = BaseConstants.LOCALE_VN, timezone = BaseConstants.TIMEZONE_VN)
    private Date fromDate;

    @JsonFormat(pattern = BaseConstants.COMMON_DATE_FORMAT, locale = BaseConstants.LOCALE_VN, timezone = BaseConstants.TIMEZONE_VN)
    private Date toDate;

    private ContractProposalsResponse contractProposalsResponse;
    private ContractEvaluationsResponse contractEvaluationsResponse;
    private String curContractNumber;
    private List<ContractApproversEntity> listContractApproverEntity;
}
