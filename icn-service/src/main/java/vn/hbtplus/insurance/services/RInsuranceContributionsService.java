package vn.hbtplus.insurance.services;

import com.jxcell.CellException;
import org.springframework.http.ResponseEntity;
import vn.hbtplus.insurance.models.request.InsuranceContributionsRequest;
import vn.hbtplus.utils.ExportExcel;

import java.util.Map;

public interface RInsuranceContributionsService {
    ResponseEntity exportDanhSachChiTiet(InsuranceContributionsRequest.ReportForm dto) throws Exception;
    ResponseEntity exportTongHopTheoDoiTuong(InsuranceContributionsRequest.ReportForm dto) throws Exception;
    ResponseEntity exportTongHopKpcdTheoDoiTuong(InsuranceContributionsRequest.ReportForm dto) throws Exception;
    ResponseEntity exportBangTruyThuLinh(InsuranceContributionsRequest.ReportForm dto) throws Exception;

    void replaceReportParameter(ExportExcel dynamicExport, InsuranceContributionsRequest.ReportForm dto) throws CellException;
    Map getReportParameter(InsuranceContributionsRequest.ReportForm dto);

}
