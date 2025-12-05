/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.models.request;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.hbtplus.models.AttributeRequestDto;
import vn.hbtplus.models.BaseSearchRequest;
import vn.hbtplus.utils.StrimDeSerializer;

import javax.validation.constraints.Size;
import java.util.List;

/**
 * Lop DTO ung voi bang hr_jobs
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Data
@NoArgsConstructor
public class JobsRequest extends BaseSearchRequest {

    @Data
    @NoArgsConstructor
    @Schema(name = "JobsSubmitForm")
    public static class SubmitForm {

        @Size(max = 50)
        @JsonDeserialize(using = StrimDeSerializer.class)
        private String code;

        @Size(max = 255)
        @JsonDeserialize(using = StrimDeSerializer.class)
        private String name;

        private Integer orderNumber;

        @Size(max = 255)
        @JsonDeserialize(using = StrimDeSerializer.class)
        private String note;

        @Size(max = 50)
        @JsonDeserialize(using = StrimDeSerializer.class)
        private String jobType;

        List<AttributeRequestDto> listAttributes;

    }

    @Data
    @NoArgsConstructor
    @Schema(name = "JobsSearchForm")
    public static class SearchForm extends BaseSearchRequest{

        private Long jobId;

        @Size(max = 50)
        @JsonDeserialize(using = StrimDeSerializer.class)
        private String code;

        @Size(max = 255)
        @JsonDeserialize(using = StrimDeSerializer.class)
        private String name;

        private List<String> listJobType;

    }
}
