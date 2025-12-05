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
 * Lop DTO ung voi bang crm_course_lessons
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Data
@NoArgsConstructor
public class CourseLessonsRequest extends BaseSearchRequest {

    @Data
    @NoArgsConstructor
    @Schema(name = "CourseLessonsSubmitForm")
    public static class SubmitForm {
        private Long courseLessonId;

        private String name;

        private Long courseId;

    }

    @Data
    @NoArgsConstructor
    @Schema(name = "CourseLessonsSearchForm")
    public static class SearchForm extends BaseSearchRequest{

        private Long courseLessonId;

        @Size(max = 255)
        @JsonDeserialize(using = StrimDeSerializer.class)
        private String name;

        private Long courseId;

    }
}
