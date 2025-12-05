package vn.hbtplus.tax.income.models;

import lombok.Data;
import vn.hbtplus.annotations.Parameter;

import java.util.List;

@Data
public class TaxParameterDto {
    @Parameter(code = "THUE_THU_NHAP_TOI_THIEU", name="Mức thu nhập tối thiểu không thu thuế/tháng")
    private Long minIncome;
    @Parameter(code = "THUE_GIAM_TRU_BAN_THAN", name="Mức giảm trừ bản thân")
    private Long selfDeduct;
    @Parameter(code = "THUE_GIAM_TRU_PHU_THUOC", name="Mức giảm trừ người phụ thuộc")
    private Long dependentDeduct;
    @Parameter(code = "THUE_DOI_TUONG_LUY_TIEN", name="Đối tượng tính thuế theo biểu lũy tiến")
    private List<String> doiTuongLuyTiens;
}
