package vn.hbtplus.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ReportConfigDto {
    private Long dynamicReportId;
    private String code;
    private String name;
    private String reportType;

    private List<Parameter> parametersResponseList;

    private List<SQLQuery> queryResponseList;

    private List<AttachmentFileDto> attachmentFileList;

    @Data
    @NoArgsConstructor
    public static class Parameter {
        private Long orderNumber;
        private String appendQuery;
        private String name;
        private String dataType;
        private String title;
    }

    @Data
    @NoArgsConstructor
    public static class SQLQuery {
        private Long orderNumber;
        private String sqlQuery;
    }
}
