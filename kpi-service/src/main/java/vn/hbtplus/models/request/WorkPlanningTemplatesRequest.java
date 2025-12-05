package vn.hbtplus.models.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.hbtplus.models.BaseSearchRequest;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.util.List;

@Data
public class WorkPlanningTemplatesRequest {
    @Data
    @NoArgsConstructor
    @Schema(name = "WorkPlanningTemplatesRequestSubmitForm")
    public static class SubmitForm {
//        private Long workPlanningTemplateId;
        @NotBlank
        private String name;
        @NotBlank
        private String content;

        @NotBlank
        private String type;

        @NotBlank
        private String code;
    }

    @Data
    @NoArgsConstructor
    @Schema(name = "WorkPlanningTemplatesRequestPlanningDetail")
    public static class PlanningDetail {
        private Long workPlanningTemplateId;
        private String name;
        private String content;
    }

    @Data
    @NoArgsConstructor
    @Schema(name = "WorkPlanningTemplatesRequestSearchForm")
    public static class SearchForm extends BaseSearchRequest {
        private String name;
        private Long id;
        private List<Long> listId;
        private String type;
        private List<String> listCode;
    }
}
