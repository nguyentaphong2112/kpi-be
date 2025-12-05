/*
 * Copyright (C) 2022 EcoIT. All rights reserved.
 * EcoIT. Use is subject to license terms.
 */
package vn.hbtplus.models.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.hbtplus.models.BaseSearchRequest;
import vn.hbtplus.utils.StrimDeSerializer;

import javax.validation.constraints.Min;
import javax.validation.constraints.Size;

/**
 * Lop DTO ung voi bang PNS_TEMPLATE_PARAMS
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Data
@NoArgsConstructor
@JsonInclude(Include.NON_NULL)
public class TemplateParamsDTO extends BaseSearchRequest {

    private Long templateParamId;

    @Size(max = 200)
    @JsonDeserialize(using = StrimDeSerializer.class)
    private String code;

    @Size(max = 200)
    @JsonDeserialize(using = StrimDeSerializer.class)
    private String name;

    @Size(max = 500)
    @JsonDeserialize(using = StrimDeSerializer.class)
    private String defaultValue;

    @Min(value = 0, message = "{startRecord.validate.min}")
    private Integer startRecord;

    @Min(value = 1, message = "{pageSize.validate.min}")
    private Integer pageSize;


}
