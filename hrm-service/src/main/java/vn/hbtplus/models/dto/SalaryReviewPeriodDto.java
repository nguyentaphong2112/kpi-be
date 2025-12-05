package vn.hbtplus.models.dto;

import lombok.Data;
import vn.hbtplus.annotations.Attribute;

import java.util.Date;

@Data
public class SalaryReviewPeriodDto {
    private Long categoryId;
    private String code;
    private String name;
    private String value;

    @Attribute(code = "moc_nang_den_ngay")
    private Date endDate;

    @Attribute(code = "ngay_chot_du_lieu")
    private Date periodDate;
}
