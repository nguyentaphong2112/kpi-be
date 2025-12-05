/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.kpi.models.request;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.kpi.models.AttributeRequestDto;
import vn.kpi.models.BaseSearchRequest;
import vn.kpi.utils.StrimDeSerializer;

import javax.validation.constraints.Size;
import java.util.List;

/**
 * Lop DTO ung voi bang hr_emp_types
 *
 * @author tudd
 * @version 1.0
 * @since 1.0
 */

@Data
@NoArgsConstructor
public class EmpTypesRequest extends BaseSearchRequest {

    @Data
    @NoArgsConstructor
    @Schema(name = "EmpTypesRequestSubmitForm")
    public static class SubmitForm {
        @Size(max = 50)
        @JsonDeserialize(using = StrimDeSerializer.class)
        private String code;

        @Size(max = 255)
        @JsonDeserialize(using = StrimDeSerializer.class)
        private String name;

        private Long orderNumber;

        List<AttributeRequestDto> listAttributes;

    }

    @Data
    @NoArgsConstructor
    @Schema(name = "EmpTypesRequestSearchForm")
    public static class SearchForm extends BaseSearchRequest {

        private Long empTypeId;

        @Size(max = 50)
        @JsonDeserialize(using = StrimDeSerializer.class)
        private String code;

        @Size(max = 255)
        @JsonDeserialize(using = StrimDeSerializer.class)
        private String name;

    }
}
