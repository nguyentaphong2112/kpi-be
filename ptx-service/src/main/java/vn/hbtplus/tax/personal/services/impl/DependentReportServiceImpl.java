package vn.hbtplus.tax.personal.services.impl;

import com.jxcell.CellException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.tax.personal.models.request.AdminSearchDTO;
import vn.hbtplus.tax.personal.models.response.DependentRegistersResponse;
import vn.hbtplus.models.response.TableResponseEntity;
import vn.hbtplus.tax.personal.repositories.impl.DependentReportRepositoryImpl;
import vn.hbtplus.tax.personal.services.CommonUtilsService;
import vn.hbtplus.tax.personal.services.DependentReportService;
import vn.hbtplus.utils.ExportExcel;
import vn.hbtplus.utils.ResponseUtils;
import vn.hbtplus.utils.Utils;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class DependentReportServiceImpl implements DependentReportService {
    private final DependentReportRepositoryImpl dependentReportRepository;
    private final CommonUtilsService commonUtilsService;

    @Override
    @Transactional(readOnly = true)
    public TableResponseEntity<DependentRegistersResponse> searchByMonth(AdminSearchDTO dto) {
        return ResponseUtils.ok(dependentReportRepository.searchData(dto));
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<Object> exportByMonth(AdminSearchDTO dto) throws Exception {
        List<DependentRegistersResponse> listData = dependentReportRepository.exportData(dto);
        int startRow = 8;
        ExportExcel dynamicExport = new ExportExcel("template/export/dependent/BM_DanhSachGiamTru.xlsx", startRow, true);
        int row = 1;
        int col = 0;
//        String tempEmpCode = null;
//        int rowTemp = startRow;
        for (DependentRegistersResponse obj : listData) {
            col = 0;
            dynamicExport.setText(String.valueOf(row++), col++);
            dynamicExport.setText(obj.getEmployeeCode(), col++);
            dynamicExport.setText(obj.getEmpName(), col++);
            dynamicExport.setText(obj.getEmpTaxNo(), col++);
            dynamicExport.setText(obj.getEmpStatusName(), col++);
            dynamicExport.setText(obj.getOrgName(), col++);
            dynamicExport.setText(obj.getJobName(), col++);
            dynamicExport.setText(obj.getEmpTypeName(), col++);
            dynamicExport.setText(obj.getRelationType(), col++);
            dynamicExport.setText(obj.getDependentName(), col++);
            dynamicExport.setText(Utils.formatDate(obj.getFDateOfBith()), col++);
            dynamicExport.setText(obj.getDependentPersonCode(), col++);
            dynamicExport.setText(obj.getFTaxNo(), col++);
            dynamicExport.setText(obj.getFPersonalId(), col++);
            dynamicExport.setText(obj.getCodeNo(), col++);
            dynamicExport.setText(obj.getBookNo(), col++);
            dynamicExport.setText(Utils.formatDate(obj.getFromDate(), BaseConstants.SHORT_DATE_FORMAT), col++);
            dynamicExport.setText(Utils.formatDate(obj.getToDate(), BaseConstants.SHORT_DATE_FORMAT), col++);
            dynamicExport.setText(Utils.formatDate(obj.getCreatedTime()), col++);
            dynamicExport.setText(obj.getNote(), col);

//            if (obj.getEmployeeCode().equals(tempEmpCode)) {
//                for (int i = 1; i <= 7; i++) {
//                    dynamicExport.mergeCell(rowTemp, i, dynamicExport.getLastRow(), i);
//                }
//            } else {
//                rowTemp = dynamicExport.getLastRow();
//            }
//            tempEmpCode = obj.getEmployeeCode();
            dynamicExport.increaseRow();
        }
        dynamicExport.setCellFormat(startRow - 1, 0, dynamicExport.getLastRow() - 1, col, ExportExcel.BORDER_FORMAT);
        if (dto.getDateReport() == null) {
            dynamicExport.setText(null, 0, 3);
        } else {
            dynamicExport.setText("Tháng: " + Utils.formatDate(dto.getDateReport(), BaseConstants.SHORT_DATE_FORMAT), 0, 3);
        }

        String fileNameResponse = Utils.getFilePathExport("DanhSachGiamTru.xlsx");
        dynamicExport.exportFile(fileNameResponse);
        return ResponseUtils.getResponseFileEntity(fileNameResponse,false);
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<Object> exportGroupByMonth(AdminSearchDTO dto) throws CellException {
        ExportExcel dynamicExport = new ExportExcel("template/export/dependent/BM_ThongKeGiamTru.xlsx", 2, true);
        if (dto.getDateReport() == null) {
            dynamicExport.setText("", 0, 3);
        } else {
            dynamicExport.setText("Tháng: " + Utils.formatDate(dto.getDateReport(), BaseConstants.SHORT_DATE_FORMAT), 0, 3);
        }
        List<Map<String, Object>> listData = dependentReportRepository.getReportGroupByMonth(dto);
        return commonUtilsService.processExport(dynamicExport, "ThongKeGiamTru.xlsx", listData);
    }
}
