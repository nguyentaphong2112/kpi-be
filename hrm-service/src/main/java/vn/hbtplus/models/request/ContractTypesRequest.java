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
 * Lop DTO ung voi bang hr_contract_types
 *
 * @author tudd
 * @version 1.0
 * @since 1.0
 */

@Data
@NoArgsConstructor
public class ContractTypesRequest extends BaseSearchRequest {

    @Data
    @NoArgsConstructor
    @Schema(name = "ContractTypesRequestSubmitForm")
    public static class SubmitForm {
        @Size(max = 50)
        @JsonDeserialize(using = StrimDeSerializer.class)
        private String code;

        @Size(max = 255)
        @JsonDeserialize(using = StrimDeSerializer.class)
        private String name;

        private Long orderNumber;

        private Long empTypeId;

        private String classifyCode;

        List<AttributeRequestDto> listAttributes;

    }

    @Data
    @NoArgsConstructor
    @Schema(name = "ContractTypesRequestSearchForm")
    public static class SearchForm extends BaseSearchRequest {

        private Long contractTypeId;

        @Size(max = 50)
        @JsonDeserialize(using = StrimDeSerializer.class)
        private String code;

        @Size(max = 255)
        @JsonDeserialize(using = StrimDeSerializer.class)
        private String name;

        private Long orderNumber;

        private Long empTypeId;

    }
}
