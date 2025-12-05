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
 * Lop DTO ung voi bang kpi_org_configs
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Data
@NoArgsConstructor
public class OrgConfigsRequest extends BaseSearchRequest {

    @Data
    @NoArgsConstructor
    @Schema(name = "OrgConfigsSubmitForm")
    public static class SubmitForm {
        private Long orgConfigId;

        private Long organizationId;
        private String orgTypeId;

        private Long year;

        @Size(max = 500)
        @JsonDeserialize(using = StrimDeSerializer.class)
        private String note;

        List<AttributeRequestDto> listAttributes;
    }

    @Data
    @NoArgsConstructor
    @Schema(name = "OrgConfigsSearchForm")
    public static class SearchForm extends BaseSearchRequest{

        private Long orgConfigId;

        private Long organizationId;

        private Long year;

        @Size(max = 500)
        @JsonDeserialize(using = StrimDeSerializer.class)
        private String note;

    }
}
