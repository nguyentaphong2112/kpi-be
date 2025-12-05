package vn.kpi.models.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.kpi.constants.BaseConstants;

import java.util.Date;

@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EmployeeRequestsResponse {
    private String content;
    private Long employeeRequestId;
    @JsonFormat(pattern = BaseConstants.COMMON_DATE_FORMAT, locale = BaseConstants.LOCALE_VN, timezone = BaseConstants.TIMEZONE_VN)
    private Date startDate;
    @JsonFormat(pattern = BaseConstants.COMMON_DATE_FORMAT, locale = BaseConstants.LOCALE_VN, timezone = BaseConstants.TIMEZONE_VN)
    private Date endDate;
    @JsonFormat(pattern = BaseConstants.COMMON_DATE_FORMAT, locale = BaseConstants.LOCALE_VN, timezone = BaseConstants.TIMEZONE_VN)
    private Date expiredDate;
    private String status;

}
