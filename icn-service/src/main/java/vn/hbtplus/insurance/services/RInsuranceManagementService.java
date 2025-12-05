package vn.hbtplus.insurance.services;

import org.springframework.http.ResponseEntity;
import vn.hbtplus.insurance.models.request.InsuranceContributionsRequest;

public interface RInsuranceManagementService {

    ResponseEntity exportDSChitiet(InsuranceContributionsRequest.ReportForm dto) throws Exception;
    ResponseEntity exportDSHSLuong(InsuranceContributionsRequest.ReportForm dto) throws Exception;
    ResponseEntity exportQTThamGiaBHXH(InsuranceContributionsRequest.ReportForm dto) throws Exception;
    ResponseEntity exportKoThuBHXH(InsuranceContributionsRequest.ReportForm dto) throws Exception;
    ResponseEntity exportTongHop(InsuranceContributionsRequest.ReportForm dto) throws Exception;
    ResponseEntity exportTongHopTheoTru(InsuranceContributionsRequest.ReportForm dto) throws Exception;
    ResponseEntity exportTruyThuTruyLinh(InsuranceContributionsRequest.ReportForm dto) throws Exception;
}
