package vn.hbtplus.insurance.models.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.hbtplus.models.BaseSearchRequest;

@Data
@NoArgsConstructor
public class ConfigParameterRequest {
    @Data
    @NoArgsConstructor
    @Schema(name = "ConfigParameterRequestSubmitForm")
    public static class SubmitForm {
        private String configGroup;
        private String configGroupName;
        private String configPeriodType;
        private String configColumns;
        private String moduleCode;
    }
    @Data
    @NoArgsConstructor
    @Schema(name = "ConfigParameterRequestSearchForm")
    public static class SearchForm extends BaseSearchRequest {

    }
}
