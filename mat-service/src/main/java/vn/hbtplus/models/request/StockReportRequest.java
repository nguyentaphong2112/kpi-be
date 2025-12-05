package vn.hbtplus.models.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import vn.hbtplus.constants.BaseConstants;

import java.util.Date;

@Data
@NoArgsConstructor
public class StockReportRequest {
    private Long warehouseId;
    private Long departmentId;
    private Long equipmentTypeId;
    private Long equipmentId;

    @DateTimeFormat(pattern = BaseConstants.COMMON_DATE_FORMAT)
    @JsonFormat(pattern = BaseConstants.COMMON_DATE_FORMAT, locale = BaseConstants.LOCALE_VN, timezone = BaseConstants.TIMEZONE_VN)
    private Date fromDate;

    @DateTimeFormat(pattern = BaseConstants.COMMON_DATE_FORMAT)
    @JsonFormat(pattern = BaseConstants.COMMON_DATE_FORMAT, locale = BaseConstants.LOCALE_VN, timezone = BaseConstants.TIMEZONE_VN)
    private Date toDate;

}
