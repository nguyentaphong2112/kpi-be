/*
 * Copyright (C) 2022 EcoIT. All rights reserved.
 * EcoIT. Use is subject to license terms.
 */
package vn.hbtplus.tax.personal.models.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import javax.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.hbtplus.models.BaseSearchRequest;
import vn.hbtplus.utils.StrimDeSerializer;

/**
 * Lop DTO ung voi bang PTX_LOG_ACTIONS
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Data
@NoArgsConstructor
@JsonInclude(Include.NON_NULL)
public class LogActionsDTO extends BaseSearchRequest {

    private Long logActionId;

    @NotNull
    private Long objectId;

    @Size(max = 50)
    @JsonDeserialize(using = StrimDeSerializer.class)
    @NotBlank
    private String objectType;

    @Size(max = 1000)
    @JsonDeserialize(using = StrimDeSerializer.class)
    private String content;

    @Min(value = 0, message = "{startRecord.validate.min}")
    private Integer startRecord;

    @Min(value = 1, message = "{pageSize.validate.min}")
    private Integer pageSize;


}
