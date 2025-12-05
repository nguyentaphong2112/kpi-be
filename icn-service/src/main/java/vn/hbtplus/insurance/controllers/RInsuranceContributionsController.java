package vn.hbtplus.insurance.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.hbtplus.annotations.HasPermission;
import vn.hbtplus.annotations.Resource;
import vn.hbtplus.insurance.constants.Constant;
import vn.hbtplus.constants.Scope;
import vn.hbtplus.insurance.models.request.InsuranceContributionsRequest;
import vn.hbtplus.insurance.services.RInsuranceContributionsService;
import vn.hbtplus.insurance.services.RInsuranceManagementService;
import vn.hbtplus.insurance.services.RInsuranceReviewService;
import vn.hbtplus.insurance.services.RInsuranceSignatureService;

@RestController
@RequestMapping(Constant.REQUEST_MAPPING_PREFIX)
@RequiredArgsConstructor
@Resource(value = Constant.RESOURCE.INSURANCE_CONTRIBUTIONS_REPORT)
public class RInsuranceContributionsController {
    private final RInsuranceContributionsService rService;
    private final RInsuranceReviewService reviewService;
    private final RInsuranceManagementService managementService;
    private final RInsuranceSignatureService signatureService;

    @GetMapping(value = "/v1/insurance-contributions-report/{reportType}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public ResponseEntity searchData(InsuranceContributionsRequest.ReportForm dto, @PathVariable String reportType) throws Exception {
        if (dto.getYear() == 0) {
            dto.setYear(dto.getStartYear());
        }

        ResponseEntity result = switch (reportType) {
            //Báo cáo quản trị
            case "TONG_HOP_BHXH_KPCD" -> managementService.exportTongHop(dto);
            case "DS_QT_CHI_TIET" -> managementService.exportDSChitiet(dto);
            case "DS_HE_SO_LUONG" -> managementService.exportDSHSLuong(dto);
            case "QT_BHXH" -> managementService.exportQTThamGiaBHXH(dto);
            case "KO_THU_BHXH" -> managementService.exportKoThuBHXH(dto);
            case "DS_TONG_HOP_THEO_TRU" -> managementService.exportTongHopTheoTru(dto);
            case "TRUY_BHXH" -> managementService.exportTruyThuTruyLinh(dto);

            //báo cáo mẫu TĐ
            case "DS_CHI_TIET" -> rService.exportDanhSachChiTiet(dto);
            case "TONG_HOP_THEO_DOI_TUONG" -> rService.exportTongHopTheoDoiTuong(dto);
            case "TONG_HOP_KPCD_THEO_DOI_TUONG" -> rService.exportTongHopKpcdTheoDoiTuong(dto);
            case "TONG_HOP_TRUY_THU_LINH" -> rService.exportBangTruyThuLinh(dto);


            // báo cáo trình ký
            case "CHI_TIET" -> signatureService.exportDetail(dto);
            case "PHIEU_TRINH" -> signatureService.exportPhieuTrinh(dto);
            case "TONG_HOP_BHXH" -> signatureService.exportSummary(dto);
            case "TONG_HOP_KPCD" -> signatureService.exportSummaryKPCD(dto);
            case "HACH_TOAN_BHXH" -> signatureService.getDataCompareReport(dto);

            //bao cáo rà soát
            case "DS_TANG_GIAM" -> reviewService.exportComparePrePeriod(dto);
            case "DS_THIEU_THONG_TIN" -> reviewService.exportEmpLackInformation(dto);
            case "DS_CD_CHUA_PHAN_LOAI" -> reviewService.exportPositionNotConfig(dto);
            case "DS_NV_CHUA_PHAN_LOAI" -> reviewService.exportEmployeeNotConfig(dto);
            default -> signatureService.exportSummaryKPCD(dto);
        };
        return result;

    }

//    @GetMapping(value = "/v1/insurance-contributions-report/report-by-period/{reportType}", produces = MediaType.APPLICATION_JSON_VALUE)
//    @HasPermission(scope = Scope.VIEW)
//    public ResponseEntity reportByPeriod(@RequestParam String periodDate, @PathVariable String reportType) throws Exception {
//        if ("DS_TANG_GIAM".equalsIgnoreCase(reportType)) {
//            return rService.exportComparePrePeriod(Utils.getLastDay(Utils.stringToDate(periodDate)));
//        } else if ("DS_THIEU_THONG_TIN".equalsIgnoreCase(reportType)) {
//            return rService.exportEmpLackInformation(Utils.getLastDay(Utils.stringToDate(periodDate)));
//        }
//        return null;
//    }
}
