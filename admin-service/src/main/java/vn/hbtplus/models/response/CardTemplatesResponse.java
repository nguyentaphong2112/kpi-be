/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.models.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.models.Attachment;
import vn.hbtplus.models.dto.CardTemplateParameterDto;
import vn.hbtplus.utils.Utils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 * Lop Response DTO ung voi bang sys_card_templates
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Data
@NoArgsConstructor
@JsonInclude(Include.NON_NULL)
public class CardTemplatesResponse {
    @Data
    @NoArgsConstructor
    @Schema(name = "CardTemplatesResponseSearchResult")
    public static class SearchResult {
        private Long cardTemplateId;
        private String templateType;
        private String templateTypeName;
        private String title;
        private String defaultParameters;
        private String parameters;
        private String isApplyAll;
        private String isApplyAllName;

        private String createdBy;
        @JsonFormat(pattern = BaseConstants.COMMON_DATETIME_FORMAT, locale = BaseConstants.LOCALE_VN, timezone = BaseConstants.TIMEZONE_VN)
        private Date createdTime;
        private String modifiedBy;

        @JsonFormat(pattern = BaseConstants.COMMON_DATETIME_FORMAT, locale = BaseConstants.LOCALE_VN, timezone = BaseConstants.TIMEZONE_VN)
        private Date modifiedTime;
    }

    @Data
    @NoArgsConstructor
    @Schema(name = "CardTemplatesResponseDetailBean")
    public static class DetailBean {
        private Long cardTemplateId;
        private String templateType;
        private String title;
        private String defaultParameters;
        private String parameters;
        private String isApplyAll;
        private List<Attachment> attachFileList;

        public List<CardTemplateParameterDto> getListParameter() {
            if (!Utils.isNullOrEmpty(parameters)) {
                return Utils.fromJsonList(parameters, CardTemplateParameterDto.class);
            } else {
                return new ArrayList<>();
            }
        }

        public List<CardTemplateParameterDto> getListDefaultParameter() {
            if (!Utils.isNullOrEmpty(defaultParameters)) {
                return Utils.fromJsonList(defaultParameters, CardTemplateParameterDto.class);
            } else {
                return new ArrayList<>();
            }
        }
    }
}
