package vn.hbtplus.insurance.models;

import lombok.Data;
import org.springframework.data.annotation.Transient;
import vn.hbtplus.annotations.Parameter;
import vn.hbtplus.utils.Utils;

import java.util.List;


@Data
public class ContributionParameterDto {

    @Parameter(code = "KPCD_TYLE_CO_SO")
    private Double baseUnionPercent;
    @Parameter(code = "BHXH_TYLE_HUU_TRI")
    private Double retirementSocialPercent;
    @Parameter(code = "BHXH_TYLE_OM_DAU")
    private Double sicknessSocialPercent;
    @Parameter(code = "BHXH_TYLE_TNLD")
    private Double accidentSocialPercent;
    @Parameter(code = "KPCD_TYLE_CAP_TREN")
    private Double superiorUnionPercent;
    @Parameter(code = "KPCD_TYLE_BQP")
    private Double modUnionPercent;
    @Parameter(code = "ICN_LUONG_CO_SO")
    private Double luongCoSo;
    @Parameter(code = "ICN_CONG_TIEU_CHUAN")
    private Double congTieuChuan;
    @Parameter(code = "ICN_NGAY_NGHI_TOI_DA")
    private Double soNgayNghiToiDa;
    @Parameter(code = "ICN_LUONG_VUNG_1")
    private Double luongToiThieuVung1;
    @Parameter(code = "ICN_LUONG_VUNG_2")
    private Double luongToiThieuVung2;
    @Parameter(code = "ICN_LUONG_VUNG_3")
    private Double luongToiThieuVung3;
    @Parameter(code = "ICN_LUONG_VUNG_4")
    private Double luongToiThieuVung4;

    @Transient
    private List<Long> idCongThaiSans;

    @Transient
    private List<Long> idCongTrichNops;
    @Parameter(code = "ICN_LOAI_PC_CHUC_VU")
    private String allowanceTypeCode;
    @Parameter(code = "ICN_CONG_THAI_SAN")
    private List<String> congThaiSans;
    @Parameter(code = "ICN_CONG_TRICH_NOP")
    private List<String> congTrichNops;


    public Double getLuongToiThieuVung(String region) {
        switch (region) {
            case "1":
                return Utils.NVL(this.luongToiThieuVung1);
            case "2":
                return Utils.NVL(this.luongToiThieuVung2);
            case "3":
                return Utils.NVL(this.luongToiThieuVung3);
            case "4":
                return Utils.NVL(this.luongToiThieuVung4);
            default:
                return 0d;
        }
    }
}
