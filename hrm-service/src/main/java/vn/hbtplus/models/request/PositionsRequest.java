/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.models.request;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.util.List;
import javax.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.hbtplus.utils.StrimDeSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import vn.hbtplus.models.BaseSearchRequest;

/**
 * Lop DTO ung voi bang hr_positions
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Data
@NoArgsConstructor
public class PositionsRequest extends BaseSearchRequest {

    @Data
    @NoArgsConstructor
    @Schema(name = "PositionsSubmitForm")
    public static class SubmitForm {
        private Long positionId;

        private List<Long> jobIds;

        private Long organizationId;

        private String jobType;

        @Size(max = 255)
        @JsonDeserialize(using = StrimDeSerializer.class)
        private String name;

        private Integer quotaNumber;

    }

    @Data
    @NoArgsConstructor
    @Schema(name = "PositionsSearchForm")
    public static class SearchForm extends BaseSearchRequest{

        private Long positionId;

        private Long jobId;

        private Long organizationId;
        private String jobType;

        private List<Long> listJobIds;

        @Size(max = 255)
        @JsonDeserialize(using = StrimDeSerializer.class)
        private String name;

    }
}
