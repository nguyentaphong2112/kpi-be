package vn.hbtplus.insurance.services;

import org.springframework.http.ResponseEntity;
import vn.hbtplus.insurance.models.request.InsuranceContributionsRequest;

public interface RInsuranceSignatureService {
    ResponseEntity exportDetail(InsuranceContributionsRequest.ReportForm dto) throws Exception;
    ResponseEntity exportSummary(InsuranceContributionsRequest.ReportForm dto) throws Exception;
    ResponseEntity exportSummaryKPCD(InsuranceContributionsRequest.ReportForm dto) throws Exception;
    ResponseEntity getDataCompareReport(InsuranceContributionsRequest.ReportForm dto) throws Exception;
    ResponseEntity exportPhieuTrinh(InsuranceContributionsRequest.ReportForm dto) throws Exception;

}
