package vn.hbtplus.insurance.services.impl;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.hbtplus.configs.MdcForkJoinPool;
import vn.hbtplus.insurance.constants.Constant;
import vn.hbtplus.exceptions.BaseAppException;
import vn.hbtplus.insurance.models.request.InsuranceContributionsRequest;
import vn.hbtplus.insurance.repositories.entity.ContributionRateEntity;
import vn.hbtplus.insurance.repositories.entity.InsuranceContributionsEntity;
import vn.hbtplus.insurance.repositories.impl.InsuranceRetractionsRepository;
import vn.hbtplus.insurance.repositories.impl.RInsuranceSignatureRepository;
import vn.hbtplus.insurance.repositories.jpa.ContributionRateRepositoryJPA;
import vn.hbtplus.insurance.services.RInsuranceContributionsService;
import vn.hbtplus.insurance.services.RInsuranceSignatureService;
import vn.hbtplus.utils.ExportExcel;
import vn.hbtplus.utils.ExportWorld;
import vn.hbtplus.utils.ResponseUtils;
import vn.hbtplus.utils.Utils;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class RInsuranceSignatureServiceImpl implements RInsuranceSignatureService {
    private final RInsuranceSignatureRepository signatureRepository;
    private final InsuranceRetractionsRepository insuranceRetractionsRepository;
    private final RInsuranceContributionsService rInsuranceContributionsService;
    private final MdcForkJoinPool mdcForkJoinPool;
    private final ContributionRateRepositoryJPA contributionRateRepositoryJPA;

    @Override
    @Transactional(readOnly = true)
    public ResponseEntity exportDetail(InsuranceContributionsRequest.ReportForm dto) throws Exception {
        String pathTemplate = "template/export/insurance/bao_cao_trinh_ky/Danh-sach-chi-tiet.xlsx";
        List<CompletableFuture<List<Map<String, Object>>>> completableFutures = new ArrayList<>();
        //Lay danh sach thieu thong tin qua trinh dien doi tuong
        Supplier<List<Map<String, Object>>> getDatas = () -> signatureRepository.getListDetails(dto, InsuranceContributionsEntity.TYPES.DANH_SACH_CHI_TIET);
        completableFutures.add(CompletableFuture.supplyAsync(getDatas, mdcForkJoinPool));

        getDatas = () -> signatureRepository.getListDetails(dto, InsuranceContributionsEntity.TYPES.TRUY_THU_LINH);
        completableFutures.add(CompletableFuture.supplyAsync(getDatas, mdcForkJoinPool));

        getDatas = () -> signatureRepository.getListDetails(dto, new String[]{InsuranceContributionsEntity.TYPES.TRUY_THU_BHYT});
        completableFutures.add(CompletableFuture.supplyAsync(getDatas, mdcForkJoinPool));

        CompletableFuture<Void> allReturn = CompletableFuture.allOf(completableFutures.toArray(new CompletableFuture[completableFutures.size()]));
        CompletableFuture<List<Object>> allFutures = allReturn.thenApply(v -> completableFutures.stream().map(CompletableFuture::join).collect(Collectors.toList()));

        List<Object> objs = allFutures.get();
        ExportExcel dynamicExport = new ExportExcel(pathTemplate, 2, true);
        for (Object items : objs) {
            dynamicExport.replaceKeys((List<Map<String, Object>>) items);
        }
        rInsuranceContributionsService.replaceReportParameter(dynamicExport, dto);
        return ResponseUtils.ok(dynamicExport, "BCTK_DS_trich_nop_bao_hiem.xlsx");
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseEntity exportSummary(InsuranceContributionsRequest.ReportForm dto) throws Exception {
        String pathTemplate = "template/export/insurance/bao_cao_trinh_ky/Tong_hop_trich_nop_bhxh.xlsx";
        List<Map<String, Object>> expDoiTuongs = signatureRepository.getListSummary(dto, "doi_tuong", false);
        ExportExcel dynamicExport = new ExportExcel(pathTemplate, 2, true);

        dynamicExport.replaceKeys(expDoiTuongs);
        List<Map<String, Object>> listByLabourType = signatureRepository.getListSummary(dto, "loai_lao_dong", false);
        dynamicExport.replaceKeys(listByLabourType);
        rInsuranceContributionsService.replaceReportParameter(dynamicExport, dto);
        return ResponseUtils.ok(dynamicExport, "BCTK_Tong_hop_trich_nop_bhxh.xlsx");
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseEntity exportSummaryKPCD(InsuranceContributionsRequest.ReportForm dto) throws Exception {
        String pathTemplate = "template/export/insurance/bao_cao_trinh_ky/Tong_hop_kpcd.xlsx";
        List<Map<String, Object>> expDoiTuongs = signatureRepository.getListSummary(dto, "doi_tuong", true);
        ExportExcel dynamicExport = new ExportExcel(pathTemplate, 2, true);

        dynamicExport.replaceKeys(expDoiTuongs);
        List<Map<String, Object>> listByLabourType = signatureRepository.getListSummary(dto, "loai_lao_dong", true);
        dynamicExport.replaceKeys(listByLabourType);
        rInsuranceContributionsService.replaceReportParameter(dynamicExport, dto);
        return ResponseUtils.ok(dynamicExport, "BCTK_Tong_hop_kpcd.xlsx");
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseEntity getDataCompareReport(InsuranceContributionsRequest.ReportForm dto) throws Exception {
        String pathTemplate = "template/export/insurance/bao_cao_trinh_ky/BM_BC_hach_toan_so_lieu_trich_nop_bh.xlsx";
        List<Map<String, Object>> listData = signatureRepository.getDataCompareReport(dto);
        ExportExcel dynamicExport = new ExportExcel(pathTemplate, 2, true);
        rInsuranceContributionsService.replaceReportParameter(dynamicExport, dto);
        dynamicExport.replaceKeys(listData);
        return ResponseUtils.ok(dynamicExport, "BCTK_hach_toan_so_lieu_trich_nop_bh.xlsx");
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseEntity exportPhieuTrinh(InsuranceContributionsRequest.ReportForm dto) throws Exception {
        Date pDate = dto.getStartDate();
        while (!pDate.after(Utils.getLastDay(dto.getEndDate()))) {
            if (insuranceRetractionsRepository.existsNotApproved(Utils.getLastDay(pDate))) {
                throw new BaseAppException(String.format("Tồn tại dữ liệu của kỳ tháng %s chưa được phê duyệt!", Utils.formatDate(pDate, "MM/yyyy")));
            }
            pDate = DateUtils.addMonths(pDate, 1);
        }

        ExportWorld exportWorld = new ExportWorld("template/export/insurance/bao_cao_trinh_ky/BM_Phieu_trinh.docx");
        Map<String, Object> mapValues = new HashMap<>();
        mapValues.put("so_hdld", signatureRepository.getNumOfEmps(dto, Constant.INSURANCE_AGENCY.BHXH_BA_DINH));
        mapValues.put("so_quan_nhan", signatureRepository.getNumOfEmps(dto, Constant.INSURANCE_AGENCY.BHXH_BQP));
        mapValues.put("tong_so_nv", Utils.NVL((Integer) mapValues.get("so_quan_nhan"))
                + Utils.NVL((Integer) mapValues.get("so_hdld")));

        mapValues.putAll(signatureRepository.getSummaryForPhieuTrinh(dto));

        mapValues.put("tong_so_dong_bang_chu",
                Utils.readNumber(Long.valueOf(Utils.NVL(mapValues.get("tong_so_dong"), 0).toString())));

        exportWorld.replaceKeys(mapValues);
        exportWorld.replaceKeys(rInsuranceContributionsService.getReportParameter(dto));
        return ResponseUtils.ok(exportWorld, "BCTK_Phieu_trinh.docx");
    }

}
