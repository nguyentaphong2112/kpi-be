/*
 * Copyright (C) 2022 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.tax.income.repositories.entity;

import lombok.Data;

import vn.hbtplus.repositories.entity.BaseEntity;
import vn.hbtplus.utils.Utils;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;


/**
 * Lop entity ung voi bang pit_tax_settlement_details
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Data
@Entity
@Table(name = "pit_tax_settlement_details")
public class TaxSettlementDetailsEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "tax_settlement_detail_id")
    private Long taxSettlementDetailId;
    @Column(name = "tax_settlement_master_id")
    private Long taxSettlementMasterId;

    @Column(name = "year")
    private Integer year;

    @Column(name = "emp_code")
    private String empCode;
    @Column(name = "emp_type_code")
    private String empTypeCode;
    @Column(name = "working_status")
    private String workingStatus;

    @Column(name = "full_name")
    private String fullName;

    @Column(name = "tax_no")
    private String taxNo;

    @Column(name = "personal_id_no")
    private String personalIdNo;
    @Column(name = "num_of_dependents")
    private Integer numOfDependents;
    @Column(name = "self_deduction")
    private Long selfDeduction;
    @Column(name = "dependent_deduction")
    private Long dependentDeduction;

    @Column(name = "income_taxable")
    private Long incomeTaxable;
    @Column(name = "insurance_deduction")
    private Long insuranceDeduction;
    @Column(name = "tax_collected")
    private Long taxCollected;


    @Column(name = "total_income_taxable")
    private Long totalIncomeTaxable;
    @Column(name = "total_insurance_deduction")
    private Long totalInsuranceDeduction;
    @Column(name = "total_income_tax")
    private Long totalIncomeTax;
    @Column(name = "total_tax_collected")
    private Long totalTaxCollected;
    @Column(name = "total_tax_payed")
    private Long totalTaxPayed;
    @Column(name = "note")
    private String note;
    @Column(name = "declare_org_id")
    private Long declareOrgId;
    @Column(name = "work_org_id")
    private Long workOrgId;
    @Column(name = "report_form")
    private String reportForm;
    @Transient
    private List<TaxSettlementMonthsEntity> monthValues= new ArrayList<>();
    @Transient
    private Integer month;
    @Transient
    private Long taxDeclareMasterId;

    public void initMonthValues(){
        TaxSettlementMonthsEntity monthsEntity = new TaxSettlementMonthsEntity();
        monthsEntity.setMonth(this.getMonth());
        monthsEntity.setTaxCollected(this.getTotalTaxCollected());
        monthsEntity.setIncomeTaxable(this.getTotalIncomeTaxable());
        monthsEntity.setInsuranceDeduction(this.getTotalInsuranceDeduction());
        monthValues.add(monthsEntity);
    }

    public void add(TaxSettlementDetailsEntity item) {
        boolean exists = false;
        for (TaxSettlementMonthsEntity monthsEntity : monthValues){
            if(monthsEntity.getMonth().equals(item.getMonth())){
                monthsEntity.setTaxCollected(Utils.NVL(item.getTotalTaxCollected()) + Utils.NVL(monthsEntity.getTaxCollected()));
                monthsEntity.setIncomeTaxable(Utils.NVL(item.getTotalIncomeTaxable()) + Utils.NVL(monthsEntity.getIncomeTaxable()));
                monthsEntity.setInsuranceDeduction(Utils.NVL(item.getTotalInsuranceDeduction()) + Utils.NVL(monthsEntity.getInsuranceDeduction()));
                exists = true;
                break;
            }
        }
        if(!exists){
            TaxSettlementMonthsEntity monthsEntity = new TaxSettlementMonthsEntity();
            monthsEntity.setMonth(item.getMonth());
            monthsEntity.setTaxCollected(item.getTotalTaxCollected());
            monthsEntity.setIncomeTaxable(item.getTotalIncomeTaxable());
            monthsEntity.setInsuranceDeduction(item.getTotalInsuranceDeduction());
            monthValues.add(monthsEntity);
        }
        this.setTotalTaxCollected(Utils.NVL(item.getTotalTaxCollected()) + Utils.NVL(this.getTotalTaxCollected()));
        this.setTotalIncomeTaxable(Utils.NVL(item.getTotalIncomeTaxable()) + Utils.NVL(this.getTotalIncomeTaxable()));
        this.setTotalInsuranceDeduction(Utils.NVL(item.getTotalInsuranceDeduction()) + Utils.NVL(this.getTotalInsuranceDeduction()));

        this.setTaxCollected(this.totalTaxCollected);
        this.setIncomeTaxable(this.totalIncomeTaxable);
        this.setInsuranceDeduction(this.totalInsuranceDeduction);
        //lấy đơn vị theo tháng cuối cùng kê khai và chi trả thu nhập
        if(this.month < item.getMonth()){
            this.declareOrgId = item.getDeclareOrgId();
            this.workOrgId = item.getWorkOrgId();
        }
    }
    public interface REPORT_FORM {
        String FORM_05_1 = "05-1";
        String FORM_05_2 = "05-2";
    }
}
