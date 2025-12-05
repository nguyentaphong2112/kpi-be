package vn.hbtplus.tax.personal.models.request;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.models.BaseSearchRequest;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
public class AdminSearchDTO extends BaseSearchRequest {

    private Long orgId;
    private List<String> listEmpStatus;
    private List<String> listStatus;
    private String taxNo;
    private String idNo;
    private String empCode;
    private String empName;
    private String empInfo;
    private List<String> listEmpTypeCode;
    private List<String> listPositionId;
    private List<String> listRegType;
    private Integer hasTaxNo;
    private Integer status;
    private String empStatusName;

    // MST của người phụ thuộc: trường này phục vụ cho chức năng xuất báo cáo theo mẫu của cơ quan thuế
    private Integer isTaxNoOfDependentPerson;

    @NotBlank
    private String regType;

    @DateTimeFormat(pattern = BaseConstants.COMMON_DATE_FORMAT)
    private Date fromDate;

    @DateTimeFormat(pattern = BaseConstants.COMMON_DATE_FORMAT)
    private Date toDate;


    private Integer year;
    private String dependentTaxNo;
    private String dependentPersonCode;

    private List<Integer> listAccepted;

    private List<Integer> listInvoiceStatus;

    @DateTimeFormat(pattern = BaseConstants.COMMON_DATE_FORMAT)
    private Date dateReport;

}
