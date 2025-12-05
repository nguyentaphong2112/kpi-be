package vn.hbtplus.tax.income.services.impl;

import com.jxcell.CellException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import vn.hbtplus.exceptions.BaseAppException;
import vn.hbtplus.tax.income.models.request.RIncomeTaxRequest;
import vn.hbtplus.tax.income.repositories.impl.RIncomeTaxRepository;
import vn.hbtplus.tax.income.services.RIncomeTaxService;
import vn.hbtplus.utils.ExportExcel;
import vn.hbtplus.utils.ResponseUtils;
import vn.hbtplus.utils.Utils;

import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class RIncomeTaxServiceImpl implements RIncomeTaxService {

    private final RIncomeTaxRepository rIncomeTaxRepository;



    @Override
    public ResponseEntity exportPersonalIncome(RIncomeTaxRequest.ReportByIdForm reportForm) throws Exception {
        String pathTemplate = "template/export/tax/tinh_thue/bc-tong-hop-thu-nhap/BC_Tong_hop_theo_nhan_vien.xlsx";
        List<Map<String, Object>> listData = rIncomeTaxRepository.getListPersonalIncome(reportForm);
        ExportExcel dynamicExport = new ExportExcel(pathTemplate, 2, true);
        String orgName = rIncomeTaxRepository.getNameOrganization();
        dynamicExport.replaceText("${ORG_NAME}", orgName.toUpperCase(Locale.ROOT));
        dynamicExport.replaceKeys(listData);

        replaceReportParameter(dynamicExport, reportForm);
        return ResponseUtils.ok(dynamicExport, "BC_Tong_hop_thu_nhap_theo_nhan_vien.xlsx");
    }

    private void replaceReportParameter(ExportExcel dynamicExport, RIncomeTaxRequest.ReportByIdForm reportForm) throws CellException {
        dynamicExport.replaceText("${ngay_xuat_bao_cao}", Utils.formatDate(new Date()));
        if (reportForm.getStartDate().equals(reportForm.getEndDate())) {
            dynamicExport.replaceText("${ky_bao_cao}", "Kỳ kê khai " + Utils.formatDate(reportForm.getStartDate(), "MM/yyyy"));
        } else {
            dynamicExport.replaceText("${ky_bao_cao}", String.format("Kỳ kê khai từ %s đến %s",
                    Utils.formatDate(reportForm.getStartDate(), "MM/yyyy"),
                    Utils.formatDate(reportForm.getEndDate(), "MM/yyyy")));
        }
    }

    @Override
    public ResponseEntity exportOrgIncome(RIncomeTaxRequest.ReportByIdForm reportForm) throws Exception {
        String pathTemplate = "template/export/tax/tinh_thue/bc-tong-hop-thu-nhap/BC_Tong_hop_theo_don_vi.xlsx";
        List<Map<String, Object>> listData = rIncomeTaxRepository.getListIncomeByOrg(reportForm);
        ExportExcel dynamicExport = new ExportExcel(pathTemplate, 2, true);
        String orgName = rIncomeTaxRepository.getNameOrganization();
        dynamicExport.replaceText("${ORG_NAME}", orgName.toUpperCase(Locale.ROOT));
        dynamicExport.replaceKeys(listData);

        replaceReportParameter(dynamicExport, reportForm);
        return ResponseUtils.ok(dynamicExport, "BC_Tong_hop_theo_don_vi.xlsx");
    }

    @Override
    public ResponseEntity exportIncomeItem(RIncomeTaxRequest.ReportByIdForm reportForm) throws Exception {
        String pathTemplate = "template/export/tax/tinh_thue/bc-tong-hop-thu-nhap/BC_Tong_hop_theo_khoan_thu_nhap.xlsx";
        List<Map<String, Object>> listData = rIncomeTaxRepository.getListIncomeByIncomeItem(reportForm);
        ExportExcel dynamicExport = new ExportExcel(pathTemplate, 2, true);
        String orgName = rIncomeTaxRepository.getNameOrganization();
        dynamicExport.replaceText("${ORG_NAME}", orgName.toUpperCase(Locale.ROOT));
        dynamicExport.replaceKeys(listData);

        replaceReportParameter(dynamicExport, reportForm);
        return ResponseUtils.ok(dynamicExport, "BC_Tong_hop_theo_khoan_thu_nhap.xlsx");
    }

    @Override
    public ResponseEntity exportIncomeItemAccounting(RIncomeTaxRequest.ReportByIdForm reportForm) throws Exception {
        String pathTemplate = "template/export/tax/tinh_thue/bc-tong-hop-thu-nhap/BC_Tong_hop_theo_khoan_thu_nhap_hach_toan_tai_chinh.xlsx";
        List<Map<String, Object>> listData = rIncomeTaxRepository.getListIncomeByAccountingItem(reportForm);
        ExportExcel dynamicExport = new ExportExcel(pathTemplate, 2, true);
        String orgName = rIncomeTaxRepository.getNameOrganization();
        dynamicExport.replaceText("${ORG_NAME}", orgName.toUpperCase(Locale.ROOT));
        dynamicExport.replaceKeys(listData);

        replaceReportParameter(dynamicExport, reportForm);
        return ResponseUtils.ok(dynamicExport, "BC_Tong_hop_theo_khoan_thu_nhap_hach_toan_tai_chinh.xlsx");
    }

    @Override
    public ResponseEntity exportIncomeItemAndOrg(RIncomeTaxRequest.ReportByIdForm reportForm) throws Exception {
        String pathTemplate = "template/export/tax/tinh_thue/bc-tong-hop-thu-nhap/BC_Tong_hop_theo_don_vi_va_khoan_tn.xlsx";
        List<Map<String, Object>> listData = rIncomeTaxRepository.getListByOrgAndItem(reportForm);
        ExportExcel dynamicExport = new ExportExcel(pathTemplate, 2, true);
        String orgName = rIncomeTaxRepository.getNameOrganization();
        dynamicExport.replaceText("${ORG_NAME}", orgName.toUpperCase(Locale.ROOT));
        dynamicExport.replaceKeys(listData);

        replaceReportParameter(dynamicExport, reportForm);
        return ResponseUtils.ok(dynamicExport, "BC_Tong_hop_theo_don_vi_va_khoan_tn.xlsx");
    }

    @Override
    public ResponseEntity exportDependentPersons(RIncomeTaxRequest.ReportByIdForm reportForm) throws Exception {
        String pathTemplate = "template/export/tax/tinh_thue/gtgc/BC_Danh_sach_nguoi_phu_thuoc.xlsx";
        List<Map<String, Object>> listData = rIncomeTaxRepository.getDependentPersons(reportForm);
        ExportExcel dynamicExport = new ExportExcel(pathTemplate, 2, true);
        String orgName = rIncomeTaxRepository.getNameOrganization();
        dynamicExport.replaceText("${ORG_NAME}", orgName.toUpperCase(Locale.ROOT));
        dynamicExport.replaceKeys(listData);

        replaceReportParameter(dynamicExport, reportForm);
        return ResponseUtils.ok(dynamicExport, "BC_Danh_sach_nguoi_phu_thuoc.xlsx");
    }

    @Override
    public ResponseEntity exportPersonalIncomeByIncomeItem(RIncomeTaxRequest.ReportByIdForm reportForm) throws Exception {
        String pathTemplate = "template/export/tax/tinh_thue/bc-tong-hop-thu-nhap/BC_Tong_hop_theo_nhan_vien_khoan_thu_nhap.xlsx";
        List<Map<String, Object>> listData = rIncomeTaxRepository.getListPersonalIncomeByIncomeItem(reportForm);
        ExportExcel dynamicExport = new ExportExcel(pathTemplate, 2, true);
        String orgName = rIncomeTaxRepository.getNameOrganization();
        dynamicExport.replaceText("${ORG_NAME}", orgName.toUpperCase(Locale.ROOT));
        dynamicExport.replaceKeys(listData);
        replaceReportParameter(dynamicExport, reportForm);
        return ResponseUtils.ok(dynamicExport, "BC_Tong_hop_theo_nhan_vien_khoan_thu_nhap.xlsx");
    }

    @Override
    public ResponseEntity exportIncomeItemForChecking(RIncomeTaxRequest.ReportByIdForm reportForm) throws Exception {
        String pathTemplate = "template/export/tax/tinh_thue/bc-tong-hop-thu-nhap/BM_BC_Doi_chieu_khoan_thu_nhap_ke_khai_thue.xlsx";
        List<Map<String, Object>> listData = rIncomeTaxRepository.exportIncomeItemForChecking(reportForm);
        //add thêm 5 cot nua
        Map<String, Object> map = listData.get(0);
        map.put("column1","");
        map.put("column2","");
        map.put("column3","");
        map.put("column4","");
        map.put("column5","");
        ExportExcel dynamicExport = new ExportExcel(pathTemplate, 2, true);
        String orgName = rIncomeTaxRepository.getNameOrganization();
        dynamicExport.replaceText("${ORG_NAME}", orgName.toUpperCase(Locale.ROOT));
        dynamicExport.replaceKeys(listData);

        replaceReportParameter(dynamicExport, reportForm);
        return ResponseUtils.ok(dynamicExport, "BM_BC_Doi_chieu_khoan_thu_nhap_ke_khai_thue.xlsx");
    }
}
