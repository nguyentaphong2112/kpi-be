package vn.hbtplus.models.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.hbtplus.models.BaseSearchRequest;

import java.util.List;

@Data
@NoArgsConstructor
public class ConfigParameterRequest {
    @Data
    @NoArgsConstructor
    @Schema(name = "ConfigParameterRequestSubmitForm")
    public static class SubmitForm {
        private Long configParameterId;
        private String configGroup;
        private String configGroupName;
        private String moduleCode;
        private String configPeriodType;
        private List<ColumnDto> configColumns;

    }

    @Data
    @NoArgsConstructor
    @Schema(name = "ConfigParameterRequestColumnDto")
    public static class ColumnDto {
        private String configCode;
        private String configName;
        private String dataType;
        private boolean required;
        private String urlLoadData;
    }

    @Data
    @NoArgsConstructor
    @Schema(name = "ConfigParameterRequestSearchForm")
    public static class SearchForm extends BaseSearchRequest {

    }
}
