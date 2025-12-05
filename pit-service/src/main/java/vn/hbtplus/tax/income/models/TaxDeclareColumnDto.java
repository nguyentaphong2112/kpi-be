package vn.hbtplus.tax.income.models;

import lombok.Data;

import java.util.Arrays;
import java.util.List;

@Data
public class TaxDeclareColumnDto {
    private String columnCode;
    private String columnName;
    private List<String> incomeTypes;

    public TaxDeclareColumnDto() {
    }

    public TaxDeclareColumnDto(String columnCode, String columnName, String ... incomeTypes) {
        this.columnCode = columnCode;
        this.columnName = columnName;
        this.incomeTypes = Arrays.asList(incomeTypes);
    }
}
