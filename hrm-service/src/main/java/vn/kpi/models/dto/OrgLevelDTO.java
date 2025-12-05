package vn.kpi.models.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;
import vn.kpi.constants.BaseConstants;

import java.util.Date;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OrgLevelDTO {
    private Long orgId;
    private String orgName;
    private String pathId;
    private String pathName;
    private Long orgLevel;
    private String scope;
    private String functionCode;

    @DateTimeFormat(pattern = BaseConstants.COMMON_DATE_FORMAT)
    @JsonFormat(pattern = BaseConstants.COMMON_DATE_FORMAT, locale = BaseConstants.LOCALE_VN, timezone = BaseConstants.TIMEZONE_VN)
    private Date inputDate;
}
