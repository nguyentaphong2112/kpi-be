package vn.hbtplus.models.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import vn.hbtplus.constants.Constant;

import java.util.Date;

@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AbsTimekeepingDTO {
    private Long employeeId;
    private Long calendarDetailId;
    private String workdayTypeCode;
    private String workdayTypeName;
    private Long workdayTypeId;
    private Integer isShowTime;
    private Double totalHours;
    @DateTimeFormat(pattern = Constant.COMMON_DATE_FORMAT)
    @JsonFormat(pattern = Constant.COMMON_DATE_FORMAT, locale = Constant.LOCALE_VN, timezone = Constant.TIMEZONE_VN)
    private Date dateTimekeeping;

    public AbsTimekeepingDTO(Long employeeId, Long workdayTypeId, Double totalHours) {
        this.employeeId = employeeId;
        this.workdayTypeId = workdayTypeId;
        this.totalHours = totalHours;
    }


}
