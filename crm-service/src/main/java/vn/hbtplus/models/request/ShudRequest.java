package vn.hbtplus.models.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
public class ShudRequest {
    @Data
    @NoArgsConstructor
    @Schema(name = "ExportForm")
    public static class ExportForm {

        @JsonProperty(value = "data_uid")
        private String dataUid;

        @JsonProperty(value = "fullname")
        private String fullName;

        @JsonProperty(value = "birthday")
        private String birthday;

        @JsonProperty(value = "age")
        private Integer age;

        @JsonProperty(value = "age_with_year")
        private String ageWithYear;

        @JsonProperty(value = "email")
        private String email;

        @JsonProperty(value = "mobile")
        private String mobile;

        @JsonProperty(value = "parent_fullname")
        private String parentFullName;

        @JsonProperty(value = "parent_birthday")
        private String parentBirthday;

        @JsonProperty(value = "HK_bieu_do_ho_ten")
        private List<String> chartFullName;

        @JsonProperty(value = "soDuongDoi")
        private String soDuongDoi;

        @JsonProperty(value = "soNgaySinh")
        private String soNgaySinh;

        @JsonProperty(value = "suMenh")
        private String suMenhStr;

        @JsonProperty(value = "tuongTac")
        private String tuongTacStr;

        @JsonProperty(value = "dinh1")
        private Integer dinh1;

        @JsonProperty(value = "dinh2")
        private Integer dinh2;

        @JsonProperty(value = "dinh3")
        private Integer dinh3;

        @JsonProperty(value = "dinh4")
        private Integer dinh4;

        @JsonProperty(value = "dinhcao1")
        private String dinhcao1;

        @JsonProperty(value = "dinhcao2")
        private String dinhcao2;

        @JsonProperty(value = "dinhcao3")
        private String dinhcao3;

        @JsonProperty(value = "dinhcao4")
        private String dinhcao4;

        @JsonProperty(value = "HK_tuoi_dinh_1")
        private String tuoiDinh1;

        @JsonProperty(value = "HK_tuoi_dinh_2")
        private String tuoiDinh2;

        @JsonProperty(value = "HK_tuoi_dinh_3")
        private String tuoiDinh3;

        @JsonProperty(value = "HK_tuoi_dinh_4")
        private String tuoiDinh4;

        @JsonProperty(value = "HK_4_dinh_cao_cuoc_doi")
        private List<Integer> dinhCaoCuocDoi;

        @JsonProperty(value = "HK_nam_ca_nhan")
        private String namCaNhan;

        @JsonProperty(value = "HK_thai_do")
        private Integer thaiDo;

        @JsonProperty(value = "HK_bieu_do_ngay_sinh")
        private List<String> chartBirthday;

        @JsonProperty(value = "HK_noi_tam")
        private Integer noiTam;

        @JsonProperty(value = "HK_so_lap")
        private List<Integer> soLap;

        @JsonProperty(value = "HK_ket_noi_duong_doi_va_su_menh")
        private Integer ketNoiDuongDoiVaSuMenh;

        @JsonProperty(value = "HK_truong_thanh")
        private Integer truongThanh;

        @JsonProperty(value = "HK_can_bang")
        private Integer canBang;

        @JsonProperty(value = "HK_bo_sung")
        private List<Integer> boSung;

        @JsonProperty(value = "HK_ket_noi_noi_tam_va_tuong_tac")
        private Integer ketNoiNoiTamVaTuongTac;

        @JsonProperty(value = "HK_noi_cam")
        private List<Integer> noiCam;

        @JsonProperty(value = "HK_trai_nghiem")
        private Integer traiNghiem;

        @JsonProperty(value = "HK_truc_giac")
        private Integer trucGiac;

        @JsonProperty(value = "HK_cam_xuc")
        private Integer camXuc;

        @JsonProperty(value = "HK_logic")
        private Integer logic;

        @JsonProperty(value = "HK_thach_thuc")
        private List<Integer> thachThuc;

        @JsonProperty(value = "person_type")
        private String personType;

        @JsonProperty(value = "parent_name")
        private String parentName;

        @JsonProperty(value = "address")
        private String address;

        @JsonProperty(value = "HK_su_menh")
        private Integer suMenh;

        @JsonProperty(value = "HK_duong_doi")
        private Integer duongDoi;

        @JsonProperty(value = "HK_ngay_sinh")
        private Integer ngaySinh;

        @JsonProperty(value = "HK_tuong_tac")
        private Integer tuongTac;

        @JsonProperty(value = "HK_mui_ten_tai_nang")
        private List<String> muiTenTaiNang;

        @JsonProperty(value = "HK_mui_ten_khuyet_thieu")
        private List<String> muiTenKhuyetThieu;
    }
}
