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
 * Lop DTO ung voi bang lms_internship_session_details
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Data
@NoArgsConstructor
public class InternshipSessionDetailsRequest extends BaseSearchRequest {

    @Data
    @NoArgsConstructor
    @Schema(name = "InternshipSessionDetailsSubmitForm")
    public static class SubmitForm {
        private Long internshipSessionDetailId;

        private Long internshipSessionId;

        @Size(max = 20)
        @JsonDeserialize(using = StrimDeSerializer.class)
        private String majorId;

        private Long numOfStudents;

    }

    @Data
    @NoArgsConstructor
    @Schema(name = "InternshipSessionDetailsSearchForm")
    public static class SearchForm extends BaseSearchRequest{

        private Long internshipSessionDetailId;

        private Long internshipSessionId;

        @Size(max = 20)
        @JsonDeserialize(using = StrimDeSerializer.class)
        private String majorId;

        private Long numOfStutdents;

    }
}
