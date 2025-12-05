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
import org.nfunk.jep.function.Str;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.models.Attachment;
import vn.hbtplus.models.dto.EmployeesDTO;
import vn.hbtplus.models.dto.OrgDTO;

import java.util.Date;


/**
 * Lop Response DTO ung voi bang PNS_CONFIG_APPROVALS
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Data
@NoArgsConstructor
@JsonInclude(Include.NON_NULL)
public class ConfigApprovalsResponse {

    private Long configApprovalId;
    private Long organizationId;
    private String documentNo;
    private Long jobApproverId;
    private Long approverId;
    private Long empPgrId;

    @JsonFormat(pattern = BaseConstants.COMMON_DATE_FORMAT, locale = BaseConstants.LOCALE_VN, timezone = BaseConstants.TIMEZONE_VN)
    private Date fromDate;

    @JsonFormat(pattern = BaseConstants.COMMON_DATE_FORMAT, locale = BaseConstants.LOCALE_VN, timezone = BaseConstants.TIMEZONE_VN)
    private Date toDate;
    private String note;
    private Integer flagStatus;
    private String type;
    private String typeStr;
    private String orgName;
    private String approverName;
    private String jobApproverName;
    private String empPgrName;
    private OrgDTO org;
    private EmployeesDTO approver;
    private Attachment file;
    private String orgGroup;
    private String branchType;
}
