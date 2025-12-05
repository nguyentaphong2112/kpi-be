package vn.hbtplus.tax.income.models;

import lombok.Data;

@Data
public class IncomeItemColumnDto {
    private String columnCode;
    private Long columnValue;
    private Long incomeItemDetailId;
    private String incomeType;
    private Long incomeItemMasterId;
}
