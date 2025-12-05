package vn.hbtplus.tax.personal.models.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.hbtplus.constants.BaseConstants;

import java.util.Date;

@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EmployeeInfoResponse {

    private String empCode;
    private String email;
    private String empName;
    private String orgName;
    private Integer empStatus;
    private String empTypeName;
    private String jobName;
    private String taxNo;
    private String idNo;
    private String fullName;
    private String personalId;
    private String taxPlace;
    private Long employeeId;

    @JsonFormat(pattern = BaseConstants.COMMON_DATE_FORMAT, locale = BaseConstants.LOCALE_VN, timezone = BaseConstants.TIMEZONE_VN)
    private Date taxDate;

    private String orgNameManage;
    private Long orgId;
}
