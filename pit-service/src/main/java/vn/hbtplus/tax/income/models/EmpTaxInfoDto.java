package vn.hbtplus.tax.income.models;

import lombok.Data;
import vn.hbtplus.tax.income.constants.Constant;

@Data
public class EmpTaxInfoDto {
    private String empCode;
    private String empTypeCode;
    private String taxNo;
    private String fullName;
    private String personalIdNo;
    private String positionName;
    private int numOfDependents;
    private Long incomeCommitment;
    private Long orgId;
    private String status; //trang thai cua nhan vien

    public String getTaxMethod() {
        return Constant.TAXES_METHOD.LUY_TIEN;
    }
}
