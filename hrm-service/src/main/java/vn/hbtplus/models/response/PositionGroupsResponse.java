/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.models.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.utils.Utils;


/**
 * Lop Response DTO ung voi bang hr_position_groups
 *
 * @author tudd
 * @version 1.0
 * @since 1.0
 */

@Data
@NoArgsConstructor
@JsonInclude(Include.NON_NULL)
public class PositionGroupsResponse {

    private Long positionGroupId;
    private String groupTypeId;
    private String code;
    private String name;
    private String createdBy;
    private String groupTypeName;

    @JsonFormat(pattern = BaseConstants.COMMON_DATETIME_FORMAT, locale = BaseConstants.LOCALE_VN, timezone = BaseConstants.TIMEZONE_VN)
    private Date createdTime;
    private String modifiedBy;

    @JsonFormat(pattern = BaseConstants.COMMON_DATETIME_FORMAT, locale = BaseConstants.LOCALE_VN, timezone = BaseConstants.TIMEZONE_VN)
    private Date modifiedTime;

    @Data
    @NoArgsConstructor
    @JsonInclude(Include.NON_NULL)
    @Schema(name = "PositionGroupsResponseDetailBean")
    public static class DetailBean {
        private Long positionGroupId;
        private String groupTypeId;
        private String code;
        private String name;

        private List<ObjectAttributesResponse> listAttributes;
        private List<ConfigDto> configs = new ArrayList<>();
    }

    @Data
    @NoArgsConstructor
    @JsonInclude(Include.NON_NULL)
    @Schema(name = "PositionGroupsResponseConfigDto")
    public static class ConfigDto {
        private String orgTypeId;
        private String orgTypeName;
        private Long organizationId;
        private String organizationName;
        private List<JobBean> jobs;
        @JsonIgnore
        private Long jobId;
        @JsonIgnore
        private String jobName;

        public void add(ConfigDto dto) {
            jobs.add(new JobBean(dto.getJobId(), dto.getJobName()));
        }

        public ConfigDto(ConfigDto dto) {
            Utils.copyProperties(dto, this);
            this.jobs = new ArrayList<>();
            this.jobs.add(new JobBean(dto.getJobId(), dto.getJobName()));
        }
    }

    @Data
    @NoArgsConstructor
    @JsonInclude(Include.NON_NULL)
    @Schema(name = "PositionGroupsResponseJob")
    public static class JobBean {
        private Long jobId;
        private String jobName;

        public JobBean(Long jobId, String jobName) {
            this.jobId = jobId;
            this.jobName = jobName;
        }
    }
}
