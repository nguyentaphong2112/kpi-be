package vn.kpi.models.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.kpi.constants.BaseConstants;
import vn.kpi.models.ReportConfigDto;

import java.util.Date;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ConfigPageResponse {

    @Data
    @NoArgsConstructor
    @Schema(name = "ConfigPageSearchResult")
    public static class ExtractBean {
        private List<ReportConfigDto> reportConfigs;
        private List<ConfigParameterResponse> configParameters;
        private List<ConfigObjectAttributeResponse> configObjectAttributes;
        private List<ConfigMappingsResponse> configMappings;
    }

    @Data
    @NoArgsConstructor
    @Schema(name = "ConfigPageSearchResult")
    public static class SearchResult {
        private Long configPageId;
        private String url;
        private String reportCodes;
        private String type;

        private String createdBy;

        @JsonFormat(pattern = BaseConstants.COMMON_DATETIME_FORMAT, locale = BaseConstants.LOCALE_VN, timezone = BaseConstants.TIMEZONE_VN)
        private Date createdTime;
        private String modifiedBy;

        @JsonFormat(pattern = BaseConstants.COMMON_DATETIME_FORMAT, locale = BaseConstants.LOCALE_VN, timezone = BaseConstants.TIMEZONE_VN)
        private Date modifiedTime;


    }

    @Data
    @NoArgsConstructor
    @Schema(name = "ConfigPageDetail")
    public static class Detail {
        private Long configPageId;
        private String url;
        private String reportCodes;
        private String type;

        private String createdBy;

        @JsonFormat(pattern = BaseConstants.COMMON_DATETIME_FORMAT, locale = BaseConstants.LOCALE_VN, timezone = BaseConstants.TIMEZONE_VN)
        private Date createdTime;
        private String modifiedBy;

        @JsonFormat(pattern = BaseConstants.COMMON_DATETIME_FORMAT, locale = BaseConstants.LOCALE_VN, timezone = BaseConstants.TIMEZONE_VN)
        private Date modifiedTime;

        private List<ObjectAttributesResponse> listAttributes;

    }


}
