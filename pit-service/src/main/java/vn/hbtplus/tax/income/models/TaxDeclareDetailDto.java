package vn.hbtplus.tax.income.models;

import lombok.Data;

@Data
public class TaxDeclareDetailDto {
    private String empCode;
    private String fullName;
    private String taxNo;
    private String personalIdNo;
    private Long incomeTaxable;
    private Long insuranceDeduction;
    private Long otherDeduction;
    private Long incomeFreeTax;
    private Long incomeTax;
    private Long taxCollected;
    private Long monthRetroTax;

    public void add(TaxDeclareDetailDto dto){

    }
}
