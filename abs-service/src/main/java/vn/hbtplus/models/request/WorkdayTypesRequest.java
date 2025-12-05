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
 * Lop DTO ung voi bang abs_workday_types
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Data
@NoArgsConstructor
public class WorkdayTypesRequest extends BaseSearchRequest {

    @Data
    @NoArgsConstructor
    @Schema(name = "WorkdayTypesSubmitForm")
    public static class SubmitForm {
        private Long workdayTypeId;

        @Size(max = 255)
        @JsonDeserialize(using = StrimDeSerializer.class)
        private String code;

        @Size(max = 255)
        @JsonDeserialize(using = StrimDeSerializer.class)
        private String name;

        List<AttributeRequestDto> listAttributes;

    }

    @Data
    @NoArgsConstructor
    @Schema(name = "WorkdayTypesSearchForm")
    public static class SearchForm extends BaseSearchRequest{

        private Long workdayTypeId;

        @Size(max = 255)
        @JsonDeserialize(using = StrimDeSerializer.class)
        private String code;

        @Size(max = 255)
        @JsonDeserialize(using = StrimDeSerializer.class)
        private String name;

    }
}
