package vn.hbtplus.tax.personal.models.request;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
public class TaxNumberChangeRegistersDTO {

    private Long taxNumberRegisterId;
    private Long employeeId;
    private String idTypeCode;
    private String idNo;
    private Date idDate;
    private String idPlace;
    private String idPlaceCode;
    private String mobileNumber;
    private String email;
    private String oldIdTypeCode;
    private String oldIdNo;
    private Date oldIdDate;
    private String oldIdPlace;
    private String oldIdPlaceCode;
//    private Integer status;
    private String regType;
    private String permanentNationCode;
    private String permanentProvinceCode;
    private String permanentDistrictCode;
    private String permanentWardCode;
    private String permanentDetail;
    private String currentNationCode;
    private String currentProvinceCode;
    private String currentDistrictCode;
    private String currentWardCode;
    private String currentDetail;
    private String note;
    private String taxNo;
    private String taxPlace;
}
