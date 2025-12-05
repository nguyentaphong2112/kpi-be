package vn.kpi.models.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.kpi.constants.BaseConstants;
import vn.kpi.utils.Utils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


@Data
@NoArgsConstructor
public class ConfigObjectAttributeDto {
    private Long configObjectAttributeId;
    private String tableName;
    private String functionCode;
    private String name;
    private String createdBy;
    @JsonFormat(pattern = BaseConstants.COMMON_DATETIME_FORMAT, locale = BaseConstants.LOCALE_VN, timezone = BaseConstants.TIMEZONE_VN)
    private Date createdTime;
    private String modifiedBy;

    @JsonFormat(pattern = BaseConstants.COMMON_DATETIME_FORMAT, locale = BaseConstants.LOCALE_VN, timezone = BaseConstants.TIMEZONE_VN)
    private Date modifiedTime;

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
    @Schema(name ="ConfigObjectAttributeDtoAttributeDto")
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class AttributeDto {
        private String code;
        private String name;
        private String dataType;
        private String urlApi;
        private Integer nzXs;
        private Integer nzLg;
        private boolean required;
    }
}
