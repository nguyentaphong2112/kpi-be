/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.models.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import java.util.Date;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.hbtplus.constants.BaseConstants;


/**
 * Lop Response DTO ung voi bang crm_course_lessons
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Data
@NoArgsConstructor
@JsonInclude(Include.NON_NULL)
public class CourseLessonsResponse {
    @Data
    @NoArgsConstructor
    @Schema(name = "CourseLessonsResponseSearchResult")
    public static class SearchResult {
        private Long courseLessonId;
        private String name;
        private Long courseId;
    }

    @Data
    @NoArgsConstructor
    @Schema(name = "CourseLessonsResponseSelected")
    public static class Selected {
        private Long courseLessonId;
        private String name;
    }


}
