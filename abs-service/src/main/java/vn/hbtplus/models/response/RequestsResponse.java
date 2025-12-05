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

import lombok.Data;
import lombok.NoArgsConstructor;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.models.Attachment;
import vn.hbtplus.models.dto.AbsRequestDTO;
import vn.hbtplus.models.dto.RequestApproversDTO;
import vn.hbtplus.models.dto.RequestHandoversDTO;


/**
 * Lop Response DTO ung voi bang abs_requests
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Data
@NoArgsConstructor
@JsonInclude(Include.NON_NULL)
public class RequestsResponse {

    private Long requestId;
    private Long employeeId;
    private String employeeCode;
    private String fullName;
    private String empTypeName;
    private String jobName;
    private String organizationName;
    private String status;
    private String statusName;
    private String reasonTypeName;

    @JsonFormat(pattern = "dd/MM/yyyy HH:mm", locale = BaseConstants.LOCALE_VN, timezone = BaseConstants.TIMEZONE_VN)
    private Date startTime;

    @JsonFormat(pattern = "dd/MM/yyyy HH:mm", locale = BaseConstants.LOCALE_VN, timezone = BaseConstants.TIMEZONE_VN)
    private Date endTime;
    private Long reasonTypeId;
    private String note;
    private String reason;
    private String requestNo;
    private String createdBy;

    @JsonFormat(pattern = BaseConstants.COMMON_DATETIME_FORMAT, locale = BaseConstants.LOCALE_VN, timezone = BaseConstants.TIMEZONE_VN)
    private Date createdTime;
    private String modifiedBy;

    @JsonFormat(pattern = BaseConstants.COMMON_DATETIME_FORMAT, locale = BaseConstants.LOCALE_VN, timezone = BaseConstants.TIMEZONE_VN)
    private Date modifiedTime;

    private List<AbsRequestDTO> listAbsRequest;

    private List<Attachment> fileRequest;

}
