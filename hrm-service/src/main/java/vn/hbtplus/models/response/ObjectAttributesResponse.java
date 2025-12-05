/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.models.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * Lop Response DTO ung voi bang hr_object_attributes
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Data
@NoArgsConstructor
@JsonInclude(Include.NON_NULL)
public class ObjectAttributesResponse {

    private Long objectAttributeId;
    private String attributeCode;
    private String attributeValue;
    private Long objectId;
    private String tableName;
    private String dataType;


}
