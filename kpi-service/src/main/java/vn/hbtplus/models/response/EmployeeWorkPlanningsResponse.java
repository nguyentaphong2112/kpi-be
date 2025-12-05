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

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.models.BaseSearchRequest;
import vn.hbtplus.repositories.entity.EmployeeWorkPlanningsEntity;


/**
 * Lop Response DTO ung voi bang kpi_employee_work_plannings
 *
 * @author tudd
 * @version 1.0
 * @since 1.0
 */

@Data
@NoArgsConstructor
@JsonInclude(Include.NON_NULL)
public class EmployeeWorkPlanningsResponse {

    @Data
    @NoArgsConstructor
    @Schema(name = "EmployeeWorkPlanningsResponseSearchForm")
    public static class SearchForm {
        private Long employeeWorkPlanningId;
        private Long employeeEvaluationId;
        private String content;
        private String createdBy;

        @JsonFormat(pattern = BaseConstants.COMMON_DATETIME_FORMAT, locale = BaseConstants.LOCALE_VN, timezone = BaseConstants.TIMEZONE_VN)
        private Date createdTime;
        private String modifiedBy;

        @JsonFormat(pattern = BaseConstants.COMMON_DATETIME_FORMAT, locale = BaseConstants.LOCALE_VN, timezone = BaseConstants.TIMEZONE_VN)
        private Date modifiedTime;
        private String name;
        private Long orderNumber;
    }

    @Data
    @NoArgsConstructor
    @Schema(name = "EmployeeWorkPlanningsResponseContent")
    public static class Content {
        private String key;
        private String parentKey;
        private String level;
        private String param;
        private String stepOne;
        private String stepTwo;
        private String fullYear;
        private String note;
        private String unit;
        private String result;
        private String resultManage;
        private String selfPoint;
        private String managePoint;
        private Boolean isNumber;
        private List<Long> listIdRelated;
    }

    @Data
    @NoArgsConstructor
    @Schema(name = "EmployeeWorkPlanningsResponseWorkPlanningData")
    public static class WorkPlanningData {
        List<EmployeeWorkPlanningsEntity> listData;
        List<Long> empEvaluationIds;
    }
}
