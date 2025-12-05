/*
 * Copyright (C) 2022 EcoIT. All rights reserved.
 * EcoIT. Use is subject to license terms.
 */
package vn.hbtplus.models.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.hbtplus.models.BaseSearchRequest;

import javax.validation.constraints.Min;

/**
 * Lop DTO ung voi bang PNS_CONFIG_SEQ_DETAILS
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Data
@NoArgsConstructor
@JsonInclude(Include.NON_NULL)
public class ConfigSeqDetailsDTO extends BaseSearchRequest {

    private Long configSeqDetailId;

    private Long configSeqContractId;

    private Long contractTypeId;

    private Integer orderNumber;

    @Min(value = 0, message = "{startRecord.validate.min}")
    private Integer startRecord;

    @Min(value = 1, message = "{pageSize.validate.min}")
    private Integer pageSize;


}
