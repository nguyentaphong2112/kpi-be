package vn.hbtplus.tax.income.models;

import lombok.Data;
import vn.hbtplus.utils.Utils;

import java.util.Date;

@Data
public class IncomeItemDetailsDto implements Cloneable{
    private Long incomeItemDetailId;
    private Long incomeItemMasterId;
    private String empCode;
    private String taxMethod;
    private Integer numOfDependents;
    private String extraCode;
    private String taxNo;
    private String personalIdNo;
    private Date personalIdDate;
    private String personalIdPlace;
    private String fullName;
    private String declareOrgName;
    private String note;
    private Long incomeTax;
    private Long incomeAmount;
    private Long insuranceDeduction;
    private Long monthRetroTax;
    private Long yearRetroTax;
    private Long incomeTax10;
    private Long incomeTax20;
    private Long incomeTaxable;
    private Long minIncomeDeduct;


    public void add(IncomeItemDetailsDto item) {
        this.incomeTax = Utils.NVL(this.incomeTax) + Utils.NVL(item.incomeTax);
        this.incomeAmount = Utils.NVL(this.incomeAmount) + Utils.NVL(item.incomeAmount);
        this.insuranceDeduction = Utils.NVL(this.insuranceDeduction) + Utils.NVL(item.insuranceDeduction);
        this.monthRetroTax = Utils.NVL(this.monthRetroTax) + Utils.NVL(item.monthRetroTax);
        this.yearRetroTax = Utils.NVL(this.yearRetroTax) + Utils.NVL(item.yearRetroTax);
        this.incomeTax10 = Utils.NVL(this.incomeTax10) + Utils.NVL(item.incomeTax10);
        this.incomeTax20 = Utils.NVL(this.incomeTax20) + Utils.NVL(item.incomeTax20);
        this.incomeTaxable = Utils.NVL(this.incomeTaxable) + Utils.NVL(item.incomeTaxable);
        this.minIncomeDeduct = Utils.NVL(this.minIncomeDeduct) + Utils.NVL(item.minIncomeDeduct);

    }

    public Long getIncomeHasTaxes() {
        return Utils.NVL(incomeTaxable)
                - Utils.NVL(insuranceDeduction)
                - Utils.NVL(minIncomeDeduct);
    }

    public Long getIncomeReceived() {
        return Utils.NVL(incomeAmount)
                - Utils.NVL(insuranceDeduction)
                - Utils.NVL(incomeTax)
                - Utils.NVL(monthRetroTax)
                - Utils.NVL(yearRetroTax);
    }

    @Override
    public IncomeItemDetailsDto clone() throws CloneNotSupportedException {
        return (IncomeItemDetailsDto) super.clone();
    }
}
