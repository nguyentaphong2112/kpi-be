/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.kpi.models.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * Lop Response DTO ung voi bang hr_contact_addresses
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Data
@NoArgsConstructor
@JsonInclude(Include.NON_NULL)
public class ContactAddressesResponse {

    @Data
    @NoArgsConstructor
    @Schema(name = "ContactAddressesResponseDetailBean")
    public static class DetailBean {
        private Long contactAddressId;
        private Long employeeId;
        private String provinceId;
        private String districtId;
        private String wardId;
        private String villageAddress;
        private String addressType;
    }
}
