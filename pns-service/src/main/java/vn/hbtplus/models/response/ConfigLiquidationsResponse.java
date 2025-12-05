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


import java.util.Date;


/**
 * Lop Response DTO ung voi bang PNS_CONFIG_LIQUIDATIONS
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Data
@NoArgsConstructor
@JsonInclude(Include.NON_NULL)
public class ConfigLiquidationsResponse {

    private Long configLiquidationId;
    private String configType;
    private String configTypeName;
    private String code;
    private String name;
    private String dataType;
    private Integer displaySeq;
    private Integer isRequired;
    private Long maxValue;
    private Long minValue;
    private Integer maxLength;
    private String createdBy;

    @JsonFormat(pattern = BaseConstants.COMMON_DATE_FORMAT, locale = BaseConstants.LOCALE_VN, timezone = BaseConstants.TIMEZONE_VN)
    private Date createdTime;
    private String lastUpdatedBy;

    @JsonFormat(pattern = BaseConstants.COMMON_DATE_FORMAT, locale = BaseConstants.LOCALE_VN, timezone = BaseConstants.TIMEZONE_VN)
    private Date modifiedTime;
    private Integer flagStatus;

}
