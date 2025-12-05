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
import vn.hbtplus.models.dto.CategoryDto;


/**
 * Lop Response DTO ung voi bang sys_warning_configs
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Data
@NoArgsConstructor
@JsonInclude(Include.NON_NULL)
public class WarningConfigsResponse {

    private Long warningConfigId;
    private String title;
    private String resource;
    private String backgroundColor;
    private String icon;
    private String apiUri;
    private String urlViewDetail;
    private String sqlQuery;
    private String isMustPositive;
    private Long orderNumber;

    private String createdBy;
    @JsonFormat(pattern = BaseConstants.COMMON_DATETIME_FORMAT, locale = BaseConstants.LOCALE_VN, timezone = BaseConstants.TIMEZONE_VN)
    private Date createdTime;
    private String modifiedBy;

    @JsonFormat(pattern = BaseConstants.COMMON_DATETIME_FORMAT, locale = BaseConstants.LOCALE_VN, timezone = BaseConstants.TIMEZONE_VN)
    private Date modifiedTime;
    private Boolean isPopup;
    private Boolean isShowExcel;
    private List<ObjectAttributesResponse> listAttributes;
    private List<CategoryDto> listColumnTable;

}
