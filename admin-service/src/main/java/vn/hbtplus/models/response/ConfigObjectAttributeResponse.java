package vn.hbtplus.models.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.models.dto.ConfigObjectAttributeDto;
import vn.hbtplus.utils.Utils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ConfigObjectAttributeResponse {
    private Long configObjectAttributeId;
    private String name;
    @JsonIgnore
    private String attributes;

    public List<ConfigObjectAttributeDto.AttributeDto> getListAttributes() {
        if (!Utils.isNullOrEmpty(attributes)) {
            return Utils.fromJsonList(attributes, ConfigObjectAttributeDto.AttributeDto.class);
        }
        return new ArrayList<>();
    }

    @Data
    @NoArgsConstructor
    @Schema(name = "ConfigObjectAttributeResponseDetailBean")
    public static class DetailBean {
        private Long configObjectAttributeId;
        private String tableName;
        private String functionCode;
        private String name;
        private List<ConfigObjectAttributeDto.AttributeDto> attributes;
        private String note;
    }

    @Data
    @NoArgsConstructor
    @Schema(name = "ConfigObjectAttributeResponseListTableName")
    public static class ListTableName {
        private String tableName;

        public ListTableName(String tableName) {
            this.tableName = tableName;
        }
    }

    @Data
    @NoArgsConstructor
    @Schema(name = "ConfigObjectAttributeResponseSearchByTableName")
    public static class SearchByTableName {
        private String tableName;
        private String functionCode;
        private String name;

        @JsonIgnore
        private String attributes;

        public List<ConfigObjectAttributeDto.AttributeDto> getListAttributes() {
            if (!Utils.isNullOrEmpty(attributes)) {
                return Utils.fromJsonList(attributes, ConfigObjectAttributeDto.AttributeDto.class);
            }
            return new ArrayList<>();
        }
    }


    @Data
    @NoArgsConstructor
    @Schema(name = "ConfigObjectAttributeResponseSearchResult")
    public static class SearchResult {
        private Long configObjectAttributeId;
        private String tableName;
        private String functionCode;
        private String name;
        private List<ConfigObjectAttributeDto.AttributeDto> attributes;
        private String createdBy;
        @JsonFormat(pattern = BaseConstants.COMMON_DATETIME_FORMAT, locale = BaseConstants.LOCALE_VN, timezone = BaseConstants.TIMEZONE_VN)
        private Date createdTime;
        private String modifiedBy;

        @JsonFormat(pattern = BaseConstants.COMMON_DATETIME_FORMAT, locale = BaseConstants.LOCALE_VN, timezone = BaseConstants.TIMEZONE_VN)
        private Date modifiedTime;
    }
}
