/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.tax.income.models.request;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.hbtplus.models.BaseSearchRequest;
import vn.hbtplus.utils.StrimDeSerializer;

import javax.validation.constraints.Size;
import java.util.List;

/**
 * Lop DTO ung voi bang pit_tax_settlement_masters
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Data
@NoArgsConstructor
public class TaxSettlementMastersRequest extends BaseSearchRequest {

    @Data
    @NoArgsConstructor
    @Schema(name = "TaxSettlementMastersSubmitForm")
    public static class SubmitForm {
        private Long taxSettlementMasterId;

        private Long year;

        @Size(max = 9)
        @JsonDeserialize(using = StrimDeSerializer.class)
        private String inputType;

    }

    @Data
    @NoArgsConstructor
    @Schema(name = "TaxSettlementMastersSearchForm")
    public static class SearchForm extends BaseSearchRequest{

        private Long taxSettlementMasterId;

        private Long year;

        @Size(max = 9)
        @JsonDeserialize(using = StrimDeSerializer.class)
        private String inputType;

    }

    @Data
    @NoArgsConstructor
    @Schema(name = "TaxSettlementMastersCalculate")
    public static class CalculateForm {
        List<MonthDto> months;
    }

    @Data
    public static class MonthDto {
        int month;
        String inputType;
    }
}
