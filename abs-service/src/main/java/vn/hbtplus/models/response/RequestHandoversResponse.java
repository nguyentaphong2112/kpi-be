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
 * Lop Response DTO ung voi bang abs_reason_types
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Data
@NoArgsConstructor
@JsonInclude(Include.NON_NULL)
public class RequestHandoversResponse {

    private Long requestHandoverId;
    
    private Long requestId;
    
    private String status;
   
    private Long employeeId;

    private Long empHandoverId;

    private Long orderNumber;

    private String content;


}
