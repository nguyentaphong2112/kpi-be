/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.kpi.models.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.kpi.constants.BaseConstants;
import vn.kpi.utils.StrimDeSerializer;
import vn.kpi.utils.Utils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 * Lop Response DTO ung voi bang sys_user_bookmarks
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Data
@NoArgsConstructor
@JsonInclude(Include.NON_NULL)
public class UserBookmarksResponse {

    @Data
    @NoArgsConstructor
    @JsonInclude(Include.NON_NULL)
    @Schema(name = "UserBookmarksResponseDetailBean")
    public static class DetailBean {
        private Long userBookmarkId;
        private String loginName;
        private String name;
        private String bookmarkType;
        private String createdBy;

        @JsonFormat(pattern = BaseConstants.COMMON_DATETIME_FORMAT, locale = BaseConstants.LOCALE_VN, timezone = BaseConstants.TIMEZONE_VN)
        private Date createdTime;
        private String modifiedBy;

        @JsonFormat(pattern = BaseConstants.COMMON_DATETIME_FORMAT, locale = BaseConstants.LOCALE_VN, timezone = BaseConstants.TIMEZONE_VN)
        private Date modifiedTime;

        @JsonIgnore
        private String options;

        public List<OptionsDTO> getListOptions(){
            if(!Utils.isNullOrEmpty(options)){
                return Utils.fromJsonList(options, OptionsDTO.class);
            }
            return new ArrayList<>();
        }
    }

    @Data
    @NoArgsConstructor
    @Schema(name = "UserBookmarksResponseOptionsDTO")
    public static class OptionsDTO {
        @JsonDeserialize(using = StrimDeSerializer.class)
        private String code;
        private List<?> values;
        @JsonDeserialize(using = StrimDeSerializer.class)
        private String valueFrom;
        @JsonDeserialize(using = StrimDeSerializer.class)
        private String valueTo;
    }

}
