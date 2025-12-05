package vn.hbtplus.insurance.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.exceptions.BaseAppException;
import vn.hbtplus.insurance.constants.Constant;
import vn.hbtplus.insurance.models.InsuranceContributionsDto;
import vn.hbtplus.insurance.models.request.InsuranceContributionsRequest;
import vn.hbtplus.insurance.models.response.InsuranceContributionsResponse;
import vn.hbtplus.insurance.repositories.entity.InsuranceContributionsEntity;
import vn.hbtplus.insurance.repositories.entity.SysCategoryEntity;
import vn.hbtplus.insurance.repositories.impl.RInsuranceManagementRepository;
import vn.hbtplus.insurance.repositories.jpa.SysCategoryRepositoryJPA;
import vn.hbtplus.insurance.services.RInsuranceContributionsService;
import vn.hbtplus.insurance.services.RInsuranceManagementService;
import vn.hbtplus.utils.ExportExcel;
import vn.hbtplus.utils.I18n;
import vn.hbtplus.utils.ResponseUtils;
import vn.hbtplus.utils.Utils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Service
public class RInsuranceManagementServiceImpl implements RInsuranceManagementService {
    private final SysCategoryRepositoryJPA sysCategoryRepositoryJPA;
    private final RInsuranceManagementRepository rManagementRepository;
    private final RInsuranceContributionsService rInsuranceContributionsService;

    @Override
    @Transactional(readOnly = true)
    public ResponseEntity exportDSChitiet(InsuranceContributionsRequest.ReportForm dto) throws Exception {
        String pathTemplate = "template/export/insurance/bao_cao_quan_tri/BC_Danh-sach-chi-tiet.xlsx";
        List<Map<String, Object>> listEmps = rManagementRepository.getListExportDSChiTiet(dto);
        if (Utils.isNullOrEmpty(listEmps)) {
            throw new BaseAppException(I18n.getMessage("global.notFound"));
        }
        ExportExcel dynamicExport = new ExportExcel(pathTemplate, 2, true);

        dynamicExport.replaceKeys(listEmps);
        rInsuranceContributionsService.replaceReportParameter(dynamicExport, dto);
        return ResponseUtils.ok(dynamicExport, "BCQT_DS_chi_tiet_trich_nop_bao_hiem.xlsx");
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseEntity exportDSHSLuong(InsuranceContributionsRequest.ReportForm dto) throws Exception {
        String pathTemplate = "template/export/insurance/bao_cao_quan_tri/DS_Qua_trinh_he_so_luong.xlsx";
        ExportExcel dynamicExport = new ExportExcel(pathTemplate, 2, true);
        List<Map<String, Object>> listEmps = rManagementRepository.getCurrentInsuranceSalaryProcess(dto);
        if (Utils.isNullOrEmpty(listEmps)) {
            throw new BaseAppException(I18n.getMessage("global.notFound"));
        }
        dynamicExport.setActiveSheet(0);
        dynamicExport.replaceKeys(listEmps);

        dynamicExport.setActiveSheet(1);
        listEmps = rManagementRepository.getAllInsuranceSalaryProcess(dto);
        dynamicExport.replaceKeys(listEmps);

        dynamicExport.setActiveSheet(0);
        rInsuranceContributionsService.replaceReportParameter(dynamicExport, dto);
        return ResponseUtils.ok(dynamicExport, "BCQT_DS_Qua_trinh_he_so_luong.xlsx");
    }


    @Override
    @Transactional(readOnly = true)
    public ResponseEntity exportQTThamGiaBHXH(InsuranceContributionsRequest.ReportForm dto) throws Exception {
        String pathTemplate = "template/export/insurance/bao_cao_quan_tri/BM_BC_Qua_trinh_tham_gia_BHXH.xlsx";
        List<Map<String, Object>> listData = rManagementRepository.getListQTThamGiaBHXH(dto);
        if (Utils.isNullOrEmpty(listData)) {
            throw new BaseAppException(I18n.getMessage("global.notFound"));
        }
        ExportExcel dynamicExport = new ExportExcel(pathTemplate, 2, true);
        rInsuranceContributionsService.replaceReportParameter(dynamicExport, dto);
        dynamicExport.replaceKeys(listData);
        return ResponseUtils.ok(dynamicExport, "BCQT_Qua_trinh_tham_gia_BHXH.xlsx");
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseEntity exportKoThuBHXH(InsuranceContributionsRequest.ReportForm dto) throws Exception {
        String pathTemplate = "template/export/insurance/bao_cao_quan_tri/BM_BC_Danh_sach_khong_trich_nop_BHXH.xlsx";
        List<Map<String, Object>> listData = rManagementRepository.getListKoThuBHXH(dto);
        if (Utils.isNullOrEmpty(listData)) {
            throw new BaseAppException(I18n.getMessage("global.notFound"));
        }
        ExportExcel dynamicExport = new ExportExcel(pathTemplate, 2, true);
        rInsuranceContributionsService.replaceReportParameter(dynamicExport, dto);
        dynamicExport.replaceKeys(listData);
        return ResponseUtils.ok(dynamicExport, "BCQT_DS_khong_trich_nop_BHXH.xlsx");
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseEntity exportTongHop(InsuranceContributionsRequest.ReportForm dto) throws Exception {
        String pathTemplate = "template/export/insurance/bao_cao_quan_tri/BM_BC_Tong_hop_BHXH-KPCD_luy_ke.xlsx";
        List<InsuranceContributionsResponse> listData = rManagementRepository.getDataTongHop(dto);
        if (Utils.isNullOrEmpty(listData)) {
            throw new BaseAppException(I18n.getMessage("global.notFound"));
        }
        Map<String, Object> mapData = new HashMap<>();
        for (InsuranceContributionsResponse item : listData) {
            String month = Utils.formatDate(item.getPeriodDate(), BaseConstants.MONTH_FORMAT);
            mapData.put("bhxh_" + item.getInsuranceAgency() + "_" + month, item.getSumUnitTotal());
            mapData.put("kpcd_" + item.getInsuranceAgency() + "_" + month, item.getSumUnitUnionAmount());
            mapData.put("total_bhxh_" + item.getInsuranceAgency() + "_" + month, item.getSumUnitTotal() + item.getSumPerTotal());
        }

        List<SysCategoryEntity> listAgency = rManagementRepository.findByProperties(
                SysCategoryEntity.class, "categoryType", Constant.CATEGORY_TYPE.NOI_TGIA_BHXH);

        for (int i = 1; i <= 12; i++) {
            String month = String.format("%02d", i);
            for (SysCategoryEntity entity : listAgency) {
                String keyBHXH = "bhxh_" + entity.getValue() + "_" + month;
                String keyKpCd = "kpcd_" + entity.getValue() + "_" + month;
                String keyTotalBHXH = "total_bhxh_" + entity.getValue() + "_" + month;
                if (!mapData.containsKey(keyBHXH)) {
                    mapData.put(keyBHXH, 0);
                }

                if (!mapData.containsKey(keyKpCd)) {
                    mapData.put(keyKpCd, 0);
                }

                if (!mapData.containsKey(keyTotalBHXH)) {
                    mapData.put(keyTotalBHXH, 0);
                }
            }
        }
        mapData.put("year", dto.getYear() + "");
        ExportExcel dynamicExport = new ExportExcel(pathTemplate, 2, true);
        dynamicExport.replaceKeys(mapData);

        dynamicExport.setActiveSheet(1);
        List<Map<String, Object>> listDataDetail = rManagementRepository.getDataTongHopChiTiet(dto, Arrays.asList(InsuranceContributionsEntity.TYPES.DANH_SACH_CHI_TIET));
        dynamicExport.replaceKeys(listDataDetail);

        listDataDetail = rManagementRepository.getDataTongHopChiTiet(dto, Arrays.asList(InsuranceContributionsEntity.TYPES.TRUY_THU_LINH));
        dynamicExport.replaceKeys(listDataDetail);

        listDataDetail = rManagementRepository.getDataTongHopChiTiet(dto, Arrays.asList(InsuranceContributionsEntity.TYPES.TRUY_THU_BHYT));
        dynamicExport.replaceKeys(listDataDetail);

        dynamicExport.setActiveSheet(0);
        dynamicExport.setLastRow(listDataDetail.size());
        return ResponseUtils.ok(dynamicExport, "BCQT_Tong_hop_BHXH-KPCD_luy_ke.xlsx");
    }

    @Override
    public ResponseEntity exportTongHopTheoTru(InsuranceContributionsRequest.ReportForm dto) throws Exception {
        List<InsuranceContributionsDto> listExps = rManagementRepository.getListSummaryForPositionGroup(dto, "PHAN_LOAI_TRU");
        List<SysCategoryEntity> listCategoryEntities = sysCategoryRepositoryJPA.getListCategories("PHAN_LOAI_TRU");
        if (Utils.isNullOrEmpty(listCategoryEntities)) {
            throw new BaseAppException(I18n.getMessage("global.notFound"));
        }
        Map<String, InsuranceContributionsDto> mapExp = new HashMap<>();
        listExps.forEach(item -> {
            item.getPositionGroups().forEach(positionGroup -> {
                if (mapExp.get(positionGroup) == null) {
                    mapExp.put(positionGroup, new InsuranceContributionsDto());
                }
                mapExp.get(positionGroup).addAmount(item, item.getPositionGroups().size());
            });
        });
        String pathTemplate = "template/export/insurance/bao_cao_quan_tri/BC_Tong_hop_theo_tru.xlsx";
        ExportExcel dynamicExport = new ExportExcel(pathTemplate, 9, true);
        int stt = 1;
        for (SysCategoryEntity categoryEntity : listCategoryEntities) {
            int col = 0;
            dynamicExport.setEntry(String.valueOf(stt++), col++);
            dynamicExport.setText(categoryEntity.getName(), col++);
            InsuranceContributionsDto expDto = mapExp.get(categoryEntity.getValue());
            if (expDto != null) {
                dynamicExport.setNumber(expDto.getPerSocialAmount(), col++);
                dynamicExport.setNumber(expDto.getUnitSocialAmount(), col++);
                dynamicExport.setNumber(expDto.getPerMedicalAmount(), col++);
                dynamicExport.setNumber(expDto.getUnitMedicalAmount(), col++);
                dynamicExport.setNumber(expDto.getPerUnempAmount(), col++);
                dynamicExport.setNumber(expDto.getUnitUnempAmount(), col++);
                dynamicExport.setNumber(expDto.getTotalAmount(), col++);
                dynamicExport.setNumber(expDto.getUnitUnionAmount(), col);
            }
            dynamicExport.increaseRow();
        }
        if (mapExp.get("N/A") != null) {
            int col = 0;
            dynamicExport.setEntry(String.valueOf(stt), col++);
            dynamicExport.setText("Khác", col++);
            InsuranceContributionsDto expDto = mapExp.get("N/A");
            if (expDto != null) {
                dynamicExport.setNumber(expDto.getPerSocialAmount(), col++);
                dynamicExport.setNumber(expDto.getUnitSocialAmount(), col++);
                dynamicExport.setNumber(expDto.getPerMedicalAmount(), col++);
                dynamicExport.setNumber(expDto.getUnitMedicalAmount(), col++);
                dynamicExport.setNumber(expDto.getPerUnempAmount(), col++);
                dynamicExport.setNumber(expDto.getUnitUnempAmount(), col++);
                dynamicExport.setNumber(expDto.getTotalAmount(), col++);
                dynamicExport.setNumber(expDto.getUnitUnionAmount(), col);
            }
            dynamicExport.increaseRow();
        }
        rInsuranceContributionsService.replaceReportParameter(dynamicExport, dto);
        dynamicExport.setCellFormat(8, 0, dynamicExport.getLastRow(), 9, ExportExcel.BORDER_FORMAT);
        return ResponseUtils.ok(dynamicExport, "BCQT_Tong_hop_theo_tru.xlsx");
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseEntity exportTruyThuTruyLinh(InsuranceContributionsRequest.ReportForm dto) throws Exception {
        String pathTemplate = "template/export/insurance/bao_cao_quan_tri/BM_BC_Danh_sach_truy_thu_linh.xlsx";
        List<Map<String, Object>> listData = rManagementRepository.getListTruyThuTruyLinh(dto);
        if (Utils.isNullOrEmpty(listData)) {
            throw new BaseAppException(I18n.getMessage("global.notFound"));
        }
        ExportExcel dynamicExport = new ExportExcel(pathTemplate, 2, true);
        dynamicExport.replaceKeys(listData);
        rInsuranceContributionsService.replaceReportParameter(dynamicExport, dto);

        return ResponseUtils.ok(dynamicExport, "BCQT_Danh_sach_truy_thu_truy_linh.xlsx");
    }

}
