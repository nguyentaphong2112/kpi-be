package vn.hbtplus.models.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.models.dto.CategoryTypeDto;
import vn.hbtplus.utils.Utils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CategoryTypeResponse {
    @Data
    @NoArgsConstructor
    @Schema(name = "CategoryTypeResponseSearchResult")
    public static class SearchResult {
        private Long categoryTypeId;
        private String code;
        private String name;
        private Long orderNumber;
        private String createdBy;
        private String attributes;
        private String isAutoIncrease;
        private String isAutoIncreaseName;

        @JsonFormat(pattern = BaseConstants.COMMON_DATETIME_FORMAT, locale = BaseConstants.LOCALE_VN, timezone = BaseConstants.TIMEZONE_VN)
        private Date createdTime;
        private String modifiedBy;

        @JsonFormat(pattern = BaseConstants.COMMON_DATETIME_FORMAT, locale = BaseConstants.LOCALE_VN, timezone = BaseConstants.TIMEZONE_VN)
        private Date modifiedTime;
    }

    @Data
    @NoArgsConstructor
    @Schema(name = "CategoryTypeResponseDetailBean")
    public static class DetailBean {
        private Long categoryTypeId;
        private String code;
        private String name;
        private Long orderNumber;
        private String attributes;
        private String isAutoIncrease;
        private String groupType;
        private List<CategoryTypeDto.AttributeDto> listAttributes;
    }
}
