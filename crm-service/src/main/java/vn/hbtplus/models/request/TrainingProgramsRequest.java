/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.models.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.util.Date;
import java.util.List;
import javax.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.models.AttributeRequestDto;
import vn.hbtplus.utils.StrimDeSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import vn.hbtplus.models.BaseSearchRequest;

/**
 * Lop DTO ung voi bang crm_training_programs
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Data
@NoArgsConstructor
public class TrainingProgramsRequest extends BaseSearchRequest {

    @Data
    @NoArgsConstructor
    @Schema(name = "TrainingProgramsSubmitForm")
    public static class SubmitForm {
        private Long trainingProgramId;

        @Size(max = 255)
        @JsonDeserialize(using = StrimDeSerializer.class)
        private String title;

        @JsonDeserialize(using = StrimDeSerializer.class)
        private String lessons;

        private List<AttributeRequestDto> listAttributes;

    }

    @Data
    @NoArgsConstructor
    @Schema(name = "TrainingProgramsSearchForm")
    public static class SearchForm extends BaseSearchRequest{

        private Long trainingProgramId;

        @Size(max = 255)
        @JsonDeserialize(using = StrimDeSerializer.class)
        private String title;


        @JsonDeserialize(using = StrimDeSerializer.class)
        private String lessons;

        private List<AttributeRequestDto> listAttributes;

    }
}
