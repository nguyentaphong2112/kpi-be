/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.models.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


/**
 * Lop Response DTO ung voi bang hr_bank_accounts
 *
 * @author tudd
 * @version 1.0
 * @since 1.0
 */

@Data
@NoArgsConstructor
@JsonInclude(Include.NON_NULL)
public class BankAccountsResponse {
    @Data
    @NoArgsConstructor
    @Schema(name = "BankAccountsResponseSearchResult")
    public static class SearchResult extends EmpBaseResponse {
        private Long bankAccountId;
        private Long employeeId;
        private String accountNo;
        private String bankId;
        private String bankName;
        private String bankBranch;
        private String isMain;
        private String accountTypeId;
        private String accountTypeName;
    }

    @Data
    @NoArgsConstructor
    @Schema(name = "BankAccountsResponseDetailBean")
    public static class DetailBean {
        private Long bankAccountId;
        private Long employeeId;
        private String accountNo;
        private String bankId;
        private String bankCode;
        private String bankBranch;
        private String accountTypeId;
        private String isMain;
        List<ObjectAttributesResponse> listAttributes;
    }

}
