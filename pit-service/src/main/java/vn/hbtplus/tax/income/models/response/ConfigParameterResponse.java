package vn.hbtplus.tax.income.models.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.hbtplus.utils.Utils;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ConfigParameterResponse {
    private Long configParameterId;
    private String configGroup;
    private String configGroupName;
    private String configPeriodType;
    @JsonIgnore
    private String configColumns;
    private List<ConfigColumn> columns;

    public List<ConfigColumn> getColumns() {

        if (!Utils.isNullOrEmpty(configColumns)) {
            return Utils.fromJsonList(configColumns, ConfigColumn.class);
        }
        return columns;
    }
    @Data
    @NoArgsConstructor
    public static class ConfigColumn {
        private String configCode;
        private String configName;
        private String dataType;
        private boolean required;
    }
}



