package vn.kpi.models.response;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
public class WorkPlanningTemplatesResponse {
    @Data
    public static class SearchResult extends KpiBaseResponse {
        private Long workPlanningTemplateId;
        private String name;
        private String content;
    }

    @Data
    @NoArgsConstructor
    public static class DetailBean {
        private Long workPlanningTemplateId;
        private String name;
        private String content;
        private String code;
        private String type;
    }

    @Data
    @NoArgsConstructor
    public static class Content {
        private String key;
        private String parentKey;
        private String level;
        private String param;
        private String pathParam;
        private String stepOne;
        private String stepTwo;
        private String fullYear;
        private String note;
        private String unit;
    }
}
