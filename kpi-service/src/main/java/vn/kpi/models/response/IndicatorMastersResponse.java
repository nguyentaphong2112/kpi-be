/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.kpi.models.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import java.util.Date;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.kpi.constants.BaseConstants;
import vn.kpi.utils.Utils;


/**
 * Lop Response DTO ung voi bang kpi_indicator_masters
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Data
@NoArgsConstructor
@JsonInclude(Include.NON_NULL)
public class IndicatorMastersResponse {

    private Long indicatorMasterId;
    private Long organizationId;
    private Long jobId;
    private String orgTypeId;
    private String statusId;
    private String createdBy;
    private String orgTypeName;
    private String organizationName;
    private String jobName;
    private String status;
    public String getScope() {
        return Utils.join(" - ", jobName, orgTypeName);
    }
    @JsonFormat(pattern = BaseConstants.COMMON_DATETIME_FORMAT, locale = BaseConstants.LOCALE_VN, timezone = BaseConstants.TIMEZONE_VN)
    private Date createdTime;
    private String modifiedBy;
    private String kpiLevel;
    private String kpiLevelName;

    @JsonFormat(pattern = BaseConstants.COMMON_DATETIME_FORMAT, locale = BaseConstants.LOCALE_VN, timezone = BaseConstants.TIMEZONE_VN)
    private Date modifiedTime;


}
