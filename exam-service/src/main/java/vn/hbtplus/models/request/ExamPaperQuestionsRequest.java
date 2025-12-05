/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.models.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.hbtplus.models.BaseSearchRequest;

/**
 * Lop DTO ung voi bang exm_exam_paper_questions
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Data
@NoArgsConstructor
public class ExamPaperQuestionsRequest extends BaseSearchRequest {

    @Data
    @NoArgsConstructor
    @Schema(name = "ExamPaperQuestionsSubmitForm")
    public static class SubmitForm {
        private Long examPaperQuestionId;

        private Long examPaperId;

        private Long questionId;

        private Long orderNumber;

        private Double score;

        private Double weight;

    }

    @Data
    @NoArgsConstructor
    @Schema(name = "ExamPaperQuestionsSearchForm")
    public static class SearchForm extends BaseSearchRequest{

        private Long examPaperQuestionId;

        private Long examPaperId;

        private Long questionId;

        private Long orderNumber;

        private Double score;

        private Double weight;

    }
}
