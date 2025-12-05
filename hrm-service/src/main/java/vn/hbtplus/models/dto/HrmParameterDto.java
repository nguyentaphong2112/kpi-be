package vn.hbtplus.models.dto;

import lombok.Data;
import vn.hbtplus.annotations.Parameter;

@Data
public class HrmParameterDto {
    @Parameter(code = "HR_PREFIX_EMP_CODE", name="Ký tự bắt đầu của mã nhân viên", required = false)
    private String prefixEmpCode;
    @Parameter(code = "HR_LENGTH_EMP_CODE", name="Tổng độ dài ký tự của mã nhân viên")
    private Integer empCodeLength;
}
