package vn.hbtplus.models.dto;

import lombok.Data;
import vn.hbtplus.annotations.Parameter;

@Data
public class LibraryParameterDto {
    @Parameter(code = "LIB_PREFIX_MA_SACH", name="Ký tự bắt đầu của mã sách")
    private String prefixBookNo;

    @Parameter(code = "LIB_DO_DAI_MA_SACH", name="Tổng số ký tự của mã sách")
    private Integer bookNoLength;
}
