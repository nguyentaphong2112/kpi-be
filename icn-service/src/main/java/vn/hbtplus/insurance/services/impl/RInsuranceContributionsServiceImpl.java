package vn.hbtplus.insurance.services.impl;

import com.jxcell.CellException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.insurance.constants.Constant;
import vn.hbtplus.insurance.models.ContributionParameterDto;
import vn.hbtplus.insurance.models.OrganizationDto;
import vn.hbtplus.insurance.models.request.InsuranceContributionsRequest;
import vn.hbtplus.insurance.models.response.InsuranceContributionsResponse;
import vn.hbtplus.insurance.repositories.entity.InsuranceContributionsEntity;
import vn.hbtplus.insurance.repositories.entity.SysCategoryEntity;
import vn.hbtplus.insurance.repositories.impl.ConfigParameterRepository;
import vn.hbtplus.insurance.repositories.impl.RInsuranceContributionsRepository;
import vn.hbtplus.insurance.repositories.jpa.SysCategoryRepositoryJPA;
import vn.hbtplus.insurance.services.RInsuranceContributionsService;
import vn.hbtplus.utils.ExportExcel;
import vn.hbtplus.utils.I18n;
import vn.hbtplus.utils.ResponseUtils;
import vn.hbtplus.utils.Utils;

import java.util.*;

@RequiredArgsConstructor
@Service
public class RInsuranceContributionsServiceImpl implements RInsuranceContributionsService {
    private final SysCategoryRepositoryJPA sysCategoryRepositoryJPA;
    private final RInsuranceContributionsRepository rInsuranceContributionsRepository;
    private final ConfigParameterRepository configParameterRepository;

    /**
     * BM01 - DANH SÁCH QUẢN LÝ LAO ĐỘNG VÀ QUỸ LƯƠNG TRÍCH NỘP BẢO HIỂM/KINH PHÍ CÔNG ĐOÀN
     *
     * @param dto tham so tren form
     * @return file
     * @throws Exception ban loi
     */
    @Override
    @Transactional(readOnly = true)
    public ResponseEntity exportDanhSachChiTiet(InsuranceContributionsRequest.ReportForm dto) throws Exception {
        String pathTemplate = "template/export/insurance/mau_tap_doan/BM_BC_chi_tiet_thu_nhap_thang.xlsx";
        // Thông tin trích BHXH nhân viên trong tháng
        List<Map<String, Object>> listData = rInsuranceContributionsRepository.getDanhSachChiTiet(dto, Arrays.asList(InsuranceContributionsEntity.TYPES.DANH_SACH_CHI_TIET));
        ExportExcel dynamicExport = new ExportExcel(pathTemplate, 2, true);
        final ContributionParameterDto parameterDto = configParameterRepository.getConfig(ContributionParameterDto.class, new Date());
        dynamicExport.replaceKeys(listData);
        dynamicExport.replaceText("${luongToiThieuVung1}",
                parameterDto.getLuongToiThieuVung1() != null ? parameterDto.getLuongToiThieuVung1().toString() : "");
        dynamicExport.replaceText("${luongToiThieuVung2}",
                parameterDto.getLuongToiThieuVung2() != null ? parameterDto.getLuongToiThieuVung2().toString() : "");
        dynamicExport.replaceText("${luongToiThieuVung3}",
                parameterDto.getLuongToiThieuVung3() != null ? parameterDto.getLuongToiThieuVung3().toString() : "");

        // thiet lap cong thuc tinh tong
        int startColumn = 22;
        int endColumn = 46;
        int row = 10;
        int rowSumBHXH = row + 1;
        for (int column = startColumn; column < endColumn; column++) {
            int endRow = row + listData.size() + 1;
            String label = dynamicExport.convertColumnIndexToLabel(column);
            dynamicExport.setFormula("SUM(" + label + (row + 2) + ":" + label + endRow + ")", column, row);
        }

        // Thông tin truy thu BHXH
        List<Map<String, Object>> listDataTruyThu = rInsuranceContributionsRepository.getDanhSachChiTiet(dto, Utils.castToList(InsuranceContributionsEntity.TYPES.TRUY_THU));
        dynamicExport.replaceKeys(Utils.castMap(listDataTruyThu, "1"));
        row = row + listData.size() + 1;
        int rowSumTruyThu = row + 1;
        for (int column = startColumn; column < endColumn; column++) {
            int endRow = row + listDataTruyThu.size() + 1;
            String label = dynamicExport.convertColumnIndexToLabel(column);
            dynamicExport.setFormula("SUM(" + label + (row + 2) + ":" + label + endRow + ")", column, row);
        }
        // Thông tin truy linh BHXH
        List<Map<String, Object>> listDataTruyLinh = rInsuranceContributionsRepository.getDanhSachChiTiet(dto, Utils.castToList(InsuranceContributionsEntity.TYPES.TRUY_LINH));
        dynamicExport.replaceKeys(Utils.castMap(listDataTruyLinh, "2"));
        row = row + listDataTruyThu.size() + 1;
        int rowSumTruyLinh = row + 1;
        for (int column = startColumn; column < endColumn; column++) {
            int endRow = row + listDataTruyLinh.size() + 1;
            String label = dynamicExport.convertColumnIndexToLabel(column);
            dynamicExport.setFormula("SUM(" + label + (row + 2) + ":" + label + endRow + ")", column, row);
        }

        // Thông tin truy thu BHYT
//        dto.setListLocationJoin(null);
        List<Map<String, Object>> listDataTruyThuBHYT = rInsuranceContributionsRepository.getDanhSachChiTiet(dto, Utils.castToList(InsuranceContributionsEntity.TYPES.TRUY_THU_BHYT));
        dynamicExport.replaceKeys(Utils.castMap(listDataTruyThuBHYT, "3"));
        row = row + listDataTruyLinh.size() + 1;
        int rowSumTruyThuBHYT = row + 1;
        for (int column = startColumn; column < endColumn; column++) {
            int endRow = row + listDataTruyThuBHYT.size() + 1;
            String label = dynamicExport.convertColumnIndexToLabel(column);
            dynamicExport.setFormula("SUM(" + label + (row + 2) + ":" + label + endRow + ")", column, row);
        }

        // tinh tong
        row = row + listDataTruyThuBHYT.size() + 1;
        for (int column = startColumn; column < endColumn; column++) {
            String label = dynamicExport.convertColumnIndexToLabel(column);
            dynamicExport.setFormula(label + rowSumBHXH + "+" + label + rowSumTruyThu + "+" + label + rowSumTruyLinh + "+" + label + rowSumTruyThuBHYT, column, row);
        }

        this.replaceReportParameter(dynamicExport, dto);
        return ResponseUtils.ok(dynamicExport, "BCTD_chi_tiet_thu_nhap_thang.xlsx");
    }

    /**
     * BM02 - BẢNG TỔNG HỢP TRÍCH NỘP BẢO HIỂM
     *
     * @param dto tham so tren form
     * @return file
     * @throws Exception ban loi
     */
    @Override
    @Transactional(readOnly = true)
    public ResponseEntity exportTongHopTheoDoiTuong(InsuranceContributionsRequest.ReportForm dto) throws Exception {
        String pathTemplate = "template/export/insurance/mau_tap_doan/BM_BC_tong_hop_theo_doi_tuong.xlsx";
        int startDataRow = 10;
        ExportExcel dynamicExport = new ExportExcel(pathTemplate, startDataRow, true);
        List<SysCategoryEntity> listEmpType = rInsuranceContributionsRepository.getListEmpType();
        // Thông tin trích BHXH nhân viên trong tháng
        int startColumn = 1;
        int endColumn = 21;
        this.setFormula(dynamicExport, startColumn, endColumn, listEmpType.size(), I18n.getMessage("report.title.bhxh"));
        Map<String, InsuranceContributionsResponse> mapBHXH = rInsuranceContributionsRepository.getTongHopTheoDoiTuong(dto, Arrays.asList(InsuranceContributionsEntity.TYPES.DANH_SACH_CHI_TIET));
        this.setValueByMap(dynamicExport, listEmpType, mapBHXH);

        // Thông tin truy thu BHXH
        this.setFormula(dynamicExport, startColumn, endColumn, listEmpType.size(), I18n.getMessage("report.title.truyThu"));
        Map<String, InsuranceContributionsResponse> mapTruyThu = rInsuranceContributionsRepository.getTongHopTheoDoiTuong(dto, Arrays.asList(InsuranceContributionsEntity.TYPES.TRUY_THU, InsuranceContributionsEntity.TYPES.TRUY_THU_BHYT));
        this.setValueByMap(dynamicExport, listEmpType, mapTruyThu);


        // Thông tin truy linh BHXH
        this.setFormula(dynamicExport, startColumn, endColumn, listEmpType.size(), I18n.getMessage("report.title.truyLinh"));
        Map<String, InsuranceContributionsResponse> mapTruyLinh = rInsuranceContributionsRepository.getTongHopTheoDoiTuong(dto, Utils.castToList(InsuranceContributionsEntity.TYPES.TRUY_LINH));
        this.setValueByMap(dynamicExport, listEmpType, mapTruyLinh);
        // tinh tong
        this.setFormula(dynamicExport, startColumn, endColumn, listEmpType.size(), I18n.getMessage("report.title.tong"));
        List<String> listType = new ArrayList<>();
        Collections.addAll(listType, InsuranceContributionsEntity.TYPES.DANH_SACH_CHI_TIET);
        listType.add(InsuranceContributionsEntity.TYPES.TRUY_THU);
        listType.add(InsuranceContributionsEntity.TYPES.TRUY_THU_BHYT);
        listType.add(InsuranceContributionsEntity.TYPES.TRUY_LINH);

        Map<String, InsuranceContributionsResponse> mapTotal = new HashMap<>();

        for (SysCategoryEntity entity : listEmpType) {
            mapTotal.put(entity.getValue(),
                    new InsuranceContributionsResponse().add(mapBHXH.get(entity.getValue()))
                            .add(mapTruyThu.get(entity.getValue()))
                            .add(mapTruyLinh.get(entity.getValue())));
        }
//        Map<String, InsuranceContributionsResponse> mapTotal = rInsuranceContributionsRepository.getTongHopTheoDoiTuong(dto, listType);
        this.setValueByMap(dynamicExport, listEmpType, mapTotal);
        dynamicExport.setCellFormat(startDataRow - 4, 0, dynamicExport.getLastRow() - 1, endColumn - 1, ExportExcel.BORDER_FORMAT);
        dynamicExport.increaseRow();
        dynamicExport.increaseRow();
        dynamicExport.copyRange(dynamicExport.getLastRow(), 0, dynamicExport.getLastRow() + 1, endColumn - 1, 0, endColumn, 1, endColumn + endColumn - 1);
        dynamicExport.deleteRange(0, endColumn, 1, endColumn + endColumn - 1);

        this.replaceReportParameter(dynamicExport, dto);

        return ResponseUtils.ok(dynamicExport, "BCTD_tong_hop_theo_doi_tuong.xlsx");
    }

    /**
     * BM02 - BẢNG TỔNG HỢP TRÍCH NỘP BẢO HIỂM KPCD
     *
     * @param dto tham so tren form
     * @return file
     * @throws Exception ban loi
     */
    @Override
    @Transactional(readOnly = true)
    public ResponseEntity exportTongHopKpcdTheoDoiTuong(InsuranceContributionsRequest.ReportForm dto) throws Exception {
        String pathTemplate = "template/export/insurance/mau_tap_doan/BM_BC_tong_hop_kpcd_theo_doi_tuong.xlsx";
        int startDataRow = 9;
        ExportExcel dynamicExport = new ExportExcel(pathTemplate, startDataRow, true);
        List<SysCategoryEntity> listEmpType = rInsuranceContributionsRepository.getListEmpType();
        // Thông tin trích BHXH nhân viên trong tháng
        int startColumn = 1;
        int endColumn = 7;
        this.setFormula(dynamicExport, startColumn, endColumn, listEmpType.size(), I18n.getMessage("report.title.bhxh"));
        Map<String, InsuranceContributionsResponse> mapBHXH = rInsuranceContributionsRepository.getTongHopKPCDTheoDoiTuong(dto, Arrays.asList(InsuranceContributionsEntity.TYPES.DANH_SACH_CHI_TIET));
        this.setValueKpcdByMap(dynamicExport, listEmpType, mapBHXH);

        // Thông tin truy thu BHXH
        this.setFormula(dynamicExport, startColumn, endColumn, listEmpType.size(), I18n.getMessage("report.title.truyThu"));
        Map<String, InsuranceContributionsResponse> mapTruyThu = rInsuranceContributionsRepository.getTongHopKPCDTheoDoiTuong(dto, Utils.castToList(InsuranceContributionsEntity.TYPES.TRUY_THU));
        this.setValueKpcdByMap(dynamicExport, listEmpType, mapTruyThu);


        // Thông tin truy linh BHXH
        this.setFormula(dynamicExport, startColumn, endColumn, listEmpType.size(), I18n.getMessage("report.title.truyLinh"));
        Map<String, InsuranceContributionsResponse> mapTruyLinh = rInsuranceContributionsRepository.getTongHopKPCDTheoDoiTuong(dto, Utils.castToList(InsuranceContributionsEntity.TYPES.TRUY_LINH));
        this.setValueKpcdByMap(dynamicExport, listEmpType, mapTruyLinh);
        // tinh tong
        this.setFormula(dynamicExport, startColumn, endColumn, listEmpType.size(), I18n.getMessage("report.title.tong"));
        List<String> listType = new ArrayList<>();
        Collections.addAll(listType, InsuranceContributionsEntity.TYPES.DANH_SACH_CHI_TIET);
        listType.add(InsuranceContributionsEntity.TYPES.TRUY_THU);
        listType.add(InsuranceContributionsEntity.TYPES.TRUY_LINH);
//        Map<String, InsuranceContributionsResponse> mapTotal = rInsuranceContributionsRepository.getTongHopKPCDTheoDoiTuong(dto, listType);
        Map<String, InsuranceContributionsResponse> mapTotal = new HashMap<>();

        for (SysCategoryEntity entity : listEmpType) {
            mapTotal.put(entity.getValue(),
                    new InsuranceContributionsResponse().add(mapBHXH.get(entity.getValue()))
                            .add(mapTruyThu.get(entity.getValue()))
                            .add(mapTruyLinh.get(entity.getValue())));
        }
        this.setValueKpcdByMap(dynamicExport, listEmpType, mapTotal);
        dynamicExport.setCellFormat(startDataRow - 3, 0, dynamicExport.getLastRow() - 1, 6, ExportExcel.BORDER_FORMAT);
        dynamicExport.increaseRow();
        dynamicExport.increaseRow();
        dynamicExport.copyRange(dynamicExport.getLastRow(), 0, dynamicExport.getLastRow() + 1, 6, 0, 7, 1, 13);
        dynamicExport.deleteRange(0, 7, 1, 13);

        this.replaceReportParameter(dynamicExport, dto);

        return ResponseUtils.ok(dynamicExport, "BCTD_tong_hop_kpcd_theo_doi_tuong.xlsx");
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseEntity exportBangTruyThuLinh(InsuranceContributionsRequest.ReportForm dto) throws Exception {
        String pathTemplate = "template/export/insurance/mau_tap_doan/BM_BC_tong_hop_truy_thu_linh.xlsx";
        // Thông tin trích BHXH nhân viên trong tháng
        List<Map<String, Object>> listData = rInsuranceContributionsRepository.getListBangTruyThuLinh(dto);
        ExportExcel dynamicExport = new ExportExcel(pathTemplate, 2, true);
        dynamicExport.replaceKeys(listData);
        replaceReportParameter(dynamicExport, dto);
        dynamicExport.setCellFormat(4, 0, listData.size() + 6, 82, ExportExcel.BORDER_FORMAT);

        return ResponseUtils.ok(dynamicExport, "BCTD_tong_hop_truy_thu_linh.xlsx");
    }

    private void setFormula(ExportExcel dynamicExport, int startColumn, int endColumn, int size, String title) throws CellException {
        dynamicExport.setText(title, 0);
        dynamicExport.setCellFormat(0, endColumn - 1, ExportExcel.BOLD_FORMAT);
        for (int column = startColumn; column < endColumn; column++) {
            int endRow = dynamicExport.getLastRow() + size + 1;
            String label = dynamicExport.convertColumnIndexToLabel(column);
            dynamicExport.setFormula("SUM(" + label + (dynamicExport.getLastRow() + 2) + ":" + label + endRow + ")", column);
        }
        dynamicExport.increaseRow();
    }

    private void setValueByMap(ExportExcel dynamicExport, List<SysCategoryEntity> listEmpType, Map<String, InsuranceContributionsResponse> mapData) throws CellException {
        for (SysCategoryEntity sysCategoryEntity : listEmpType) {
            int column = 0;
            dynamicExport.setText(sysCategoryEntity.getValue(), column++);
            InsuranceContributionsResponse item = mapData.get(sysCategoryEntity.getValue());
            if (item != null) {
                dynamicExport.setNumber(item.getCountEmp(), column++);
                dynamicExport.setNumber(item.getContractSalary(), column++);
                dynamicExport.setNumber(item.getReserveSalary(), column++);
                dynamicExport.setNumber(item.getPosAllowanceSalary(), column++);
                dynamicExport.setNumber(item.getSenioritySalary(), column++);
                dynamicExport.setNumber(item.getPosSenioritySalary(), column++);
                dynamicExport.setNumber(item.getTotalSalary(), column++);

                //BHXH ca nhan
                dynamicExport.setNumber(item.getPerSocialAmount(), column++);// Huu tri-tu tuat
                column += 2;//om dau, tnld
                dynamicExport.setNumber(item.getPerSocialAmount(), column++);
                // BHXH don vi
                dynamicExport.setNumber(item.getRetirementSocialAmount(), column++);
                dynamicExport.setNumber(item.getSicknessSocialAmount(), column++);
                dynamicExport.setNumber(item.getAccidentSocialAmount(), column++);
                dynamicExport.setNumber(item.getUnitSocialAmount(), column++);
                // BHYT
                dynamicExport.setNumber(item.getPerMedicalAmount(), column++);
                dynamicExport.setNumber(item.getUnitMedicalAmount(), column++);
                dynamicExport.setNumber(item.getPerUnempAmount(), column++);
                dynamicExport.setNumber(item.getUnitUnempAmount(), column++);
                dynamicExport.setNumber(item.getTotalAmount(), column);
            }
            dynamicExport.increaseRow();
        }
    }

    private void setValueKpcdByMap(ExportExcel dynamicExport, List<SysCategoryEntity> listEmpType, Map<String, InsuranceContributionsResponse> mapData) throws CellException {
        for (SysCategoryEntity sysCategoryEntity : listEmpType) {
            int column = 0;
            dynamicExport.setText(sysCategoryEntity.getValue(), column++);
            InsuranceContributionsResponse item = mapData.get(sysCategoryEntity.getValue());
            if (item != null) {
                dynamicExport.setNumber(item.getCountEmp(), column++);
                dynamicExport.setNumber(item.getContractSalary(), column++);
                dynamicExport.setNumber(item.getUnitUnionAmount(), column++);
                dynamicExport.setNumber(item.getBaseUnionAmount(), column++);
                dynamicExport.setNumber(item.getSuperiorUnionAmount(), column++);
                dynamicExport.setNumber(item.getModUnionAmount(), column);
            }
            dynamicExport.increaseRow();
        }
    }

    public void replaceReportParameter(ExportExcel dynamicExport, InsuranceContributionsRequest.ReportForm dto) throws CellException {
        dynamicExport.replaceKeys(getReportParameter(dto));
    }

    public Map<String, Object> getReportParameter(InsuranceContributionsRequest.ReportForm dto) {
        Map<String, Object> params = new HashMap<>();
        params.put("noi_tham_gia", "");
        if (!Utils.isNullOrEmpty(dto.getListLocationJoin())) {
            List<SysCategoryEntity> listData = sysCategoryRepositoryJPA.getListDataByIds(Constant.CATEGORY_TYPE.NOI_TGIA_BHXH, dto.getListLocationJoin());
            if (!Utils.isNullOrEmpty(listData)) {
                StringBuilder str = new StringBuilder(I18n.getMessage("global.report.at")).append(" ");
                int index = 1;
                for (SysCategoryEntity sysCategoryEntity : listData) {
                    str.append(sysCategoryEntity.getName());
                    if (index < listData.size()) {
                        str.append(", ");
                    }
                    index++;
                }
                params.put("noi_tham_gia", str.toString());
            }
        }
        if (Constant.PERIOD_TYPE.MONTH.equals(dto.getPeriodType())) {
            if (dto.getStartDate().equals(dto.getEndDate())) {
                params.put("ky_bao_cao", I18n.getMessage("global.report.period.onlyMonth", Utils.formatDate(dto.getStartDate(), BaseConstants.MONTH_FORMAT), Utils.formatDate(dto.getStartDate(), BaseConstants.YEAR_FORMAT)));
            } else {
                params.put("ky_bao_cao", I18n.getMessage("global.report.period.fromMonth", Utils.formatDate(dto.getStartDate(), BaseConstants.SHORT_DATE_FORMAT), Utils.formatDate(dto.getEndDate(), BaseConstants.SHORT_DATE_FORMAT)));
            }
            params.put("nam_bao_cao", Utils.formatDate(dto.getEndDate(), "yyyy"));
        } else if (Constant.PERIOD_TYPE.YEAR.equals(dto.getPeriodType())) {
            if (dto.getEndYear() == dto.getStartYear()) {
                params.put("ky_bao_cao", I18n.getMessage("global.report.period.onlyYear", dto.getStartYear()));
            } else {
                params.put("ky_bao_cao", I18n.getMessage("global.report.period.fromYear", dto.getStartYear(), dto.getEndYear()));
            }
            params.put("nam_bao_cao", dto.getEndYear());
        } else {
            params.put("ky_bao_cao", I18n.getMessage("global.report.period.quarter", dto.getQuarter(), dto.getYear()));
            params.put("nam_bao_cao", dto.getYear());
        }
        params.put("ngay", Utils.formatDate(new Date(), "dd"));
        params.put("thang", Utils.formatDate(new Date(), "MM"));
        params.put("nam", Utils.formatDate(new Date(), "yyyy"));
        params.put("ngayXuatBC", Utils.formatDate(new Date(), BaseConstants.COMMON_DATE_FORMAT));
        OrganizationDto org = rInsuranceContributionsRepository.getOrg();
        params.put("ten_don_vi", org.getOrgName());
        return params;
    }


}
