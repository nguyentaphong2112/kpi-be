package vn.hbtplus.insurance.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.hbtplus.configs.MdcForkJoinPool;
import vn.hbtplus.exceptions.BaseAppException;
import vn.hbtplus.insurance.models.request.InsuranceContributionsRequest;
import vn.hbtplus.insurance.repositories.impl.RInsuranceReviewRepository;
import vn.hbtplus.insurance.services.RInsuranceContributionsService;
import vn.hbtplus.insurance.services.RInsuranceReviewService;
import vn.hbtplus.utils.ExportExcel;
import vn.hbtplus.utils.I18n;
import vn.hbtplus.utils.ResponseUtils;
import vn.hbtplus.utils.Utils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class RInsuranceReviewServiceImpl implements RInsuranceReviewService {

    private final MdcForkJoinPool mdcForkJoinPool;
    private final RInsuranceReviewRepository reviewRepository;
    private final RInsuranceContributionsService rInsuranceContributionsService;



    @Override
    @Transactional(readOnly = true)
    public ResponseEntity exportComparePrePeriod(InsuranceContributionsRequest.ReportForm dto) throws Exception {
        if (!dto.getPeriodType().equals("MONTH")) {
            throw new BaseAppException(I18n.getMessage("report.validate.periodType.month"));
        }

        String pathTemplate = "template/export/insurance/bao_cao_ra_soat/BC_Ra_soat_du_lieu_trich_nop.xlsx";
        ExportExcel dynamicExport = new ExportExcel(pathTemplate, 2, true);

        Date periodDate = Utils.getLastDay(dto.getEndDate());

        List<CompletableFuture<List<Map<String, Object>>>> completableFutures = new ArrayList<>();
        //Lay danh sach tang moi
        Supplier<List<Map<String, Object>>> getDatas = () -> reviewRepository.getListIncrease(periodDate);
        completableFutures.add(CompletableFuture.supplyAsync(getDatas, mdcForkJoinPool));
        //Lay danh sach ket thuc nghi thai san
        getDatas = () -> reviewRepository.getListEndMaternity(periodDate);
        completableFutures.add(CompletableFuture.supplyAsync(getDatas, mdcForkJoinPool));
        //lay danh sach giam
        getDatas = () -> reviewRepository.getListDecrease(periodDate);
        completableFutures.add(CompletableFuture.supplyAsync(getDatas, mdcForkJoinPool));
        //lay danh sach thai san moi
        getDatas = () -> reviewRepository.getListNewMaternity(periodDate);
        completableFutures.add(CompletableFuture.supplyAsync(getDatas, mdcForkJoinPool));
        //lay danh sach thay doi muc dong
        getDatas = () -> reviewRepository.getListChangeSalary(periodDate);
        completableFutures.add(CompletableFuture.supplyAsync(getDatas, mdcForkJoinPool));

        CompletableFuture<Void> allReturn = CompletableFuture.allOf(completableFutures.toArray(new CompletableFuture[completableFutures.size()]));
        CompletableFuture<List<Object>> allFutures = allReturn.thenApply(v -> completableFutures.stream().map(CompletableFuture::join).collect(Collectors.toList()));

        List<Object> objs = allFutures.get();
        for (Object items : objs) {
            dynamicExport.replaceKeys((List<Map<String, Object>>) items);
        }

        rInsuranceContributionsService.replaceReportParameter(dynamicExport, dto);
        return ResponseUtils.ok(dynamicExport, "BCRS_Ra_soat_du_lieu_trich_nop.xlsx");
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseEntity exportEmpLackInformation(InsuranceContributionsRequest.ReportForm dto) throws Exception {
        Date periodDate = Utils.getLastDay(dto.getEndDate());
        //query lay thong tin
        List<CompletableFuture<List<Map<String, Object>>>> completableFutures = new ArrayList<>();
        //Lay danh sach thieu thong tin qua trinh dien doi tuong
        Supplier<List<Map<String, Object>>> getDatas = () -> reviewRepository.getListLackEmpTypeProcess(periodDate);
        completableFutures.add(CompletableFuture.supplyAsync(getDatas, mdcForkJoinPool));
        //Danh sach bi long qua trinh dien doi tuong
        getDatas = () -> reviewRepository.getListDuplicateEmpTypeProcess(periodDate);
        completableFutures.add(CompletableFuture.supplyAsync(getDatas, mdcForkJoinPool));
        //Lay danh sach thieu thong tin qua trinh luong
        getDatas = () -> reviewRepository.getListLackInsuranceSalaryProcess(periodDate);
        completableFutures.add(CompletableFuture.supplyAsync(getDatas, mdcForkJoinPool));
        //Lay danh sach lồng thong tin qua trinh luong
        getDatas = () -> reviewRepository.getListDuplicateInsuranceSalaryProcess(periodDate);
        completableFutures.add(CompletableFuture.supplyAsync(getDatas, mdcForkJoinPool));
        //Lay danh sach long qua trinh cong tac
        getDatas = () -> reviewRepository.getListDuplicateWorkProcess(periodDate);
        completableFutures.add(CompletableFuture.supplyAsync(getDatas, mdcForkJoinPool));


        CompletableFuture<Void> allReturn = CompletableFuture.allOf(completableFutures.toArray(new CompletableFuture[completableFutures.size()]));
        CompletableFuture<List<Object>> allFutures = allReturn.thenApply(v -> completableFutures.stream().map(CompletableFuture::join).collect(Collectors.toList()));

        List<Object> objs = allFutures.get();


        if (!dto.getPeriodType().equals("MONTH")) {
            throw new BaseAppException(I18n.getMessage("report.validate.periodType.month"));
        }

        String pathTemplate = "template/export/insurance/bao_cao_ra_soat/BC_Ra_soat_danh_sach_thieu_thong_tin.xlsx";
        ExportExcel dynamicExport = new ExportExcel(pathTemplate, 2, true);

        for (Object items : objs) {
            dynamicExport.replaceKeys((List<Map<String, Object>>) items);
        }
        rInsuranceContributionsService.replaceReportParameter(dynamicExport, dto);
        return ResponseUtils.ok(dynamicExport, "BCRS_Ra_soat_danh_sach_thieu_thong_tin.xlsx");
    }

    @Override
    public ResponseEntity exportPositionNotConfig(InsuranceContributionsRequest.ReportForm dto) throws Exception {
        String pathTemplate = "template/export/insurance/bao_cao_ra_soat/BC_DS_CD_chua_cau_hinh.xlsx";
        // Thông tin trích BHXH nhân viên trong tháng
        List<Map<String, Object>> listData = reviewRepository.getPositionNotConfig(dto);
        ExportExcel dynamicExport = new ExportExcel(pathTemplate, 2, true);
        dynamicExport.replaceKeys(listData);
        rInsuranceContributionsService.replaceReportParameter(dynamicExport, dto);

        return ResponseUtils.ok(dynamicExport, "BCRS_DANH_SACH_CHUC_DANH_CHUA_CAU_HINH.xlsx");
    }

    @Override
    public ResponseEntity exportEmployeeNotConfig(InsuranceContributionsRequest.ReportForm dto) throws Exception {
        String pathTemplate = "template/export/insurance/bao_cao_ra_soat/BC_DS_NV_chua_cau_hinh_chuc_danh.xlsx";
        // Thông tin trích BHXH nhân viên trong tháng
        List<Map<String, Object>> listData = reviewRepository.getEmployeeNotConfig(dto);
        ExportExcel dynamicExport = new ExportExcel(pathTemplate, 2, true);
        dynamicExport.replaceKeys(listData);
        rInsuranceContributionsService.replaceReportParameter(dynamicExport, dto);

        return ResponseUtils.ok(dynamicExport, "BCRS_DS_NV_chua_cau_hinh_chuc_danh.xlsx");
    }

}
