/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.models.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.util.Date;
import javax.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.utils.StrimDeSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import vn.hbtplus.models.BaseSearchRequest;

/**
 * Lop DTO ung voi bang crm_course_trainees
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Data
@NoArgsConstructor
public class CourseTraineesRequest extends BaseSearchRequest {

    @Data
    @NoArgsConstructor
    @Schema(name = "CourseTraineesSubmitForm")
    public static class SubmitForm {
        private Long courseTraineeId;

        private Long courseId;

        private Long traineeId;
        private String traineeName;

        private Long instructorId;
        private String instructorName;

    }

    @Data
    @NoArgsConstructor
    @Schema(name = "CourseTraineesSearchForm")
    public static class SearchForm extends BaseSearchRequest{

        private Long courseTraineeId;

        private Long courseId;

        private Long traineeId;

        private Long instructorId;

    }
}
