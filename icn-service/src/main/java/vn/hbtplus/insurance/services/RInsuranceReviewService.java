package vn.hbtplus.insurance.services;

import org.springframework.http.ResponseEntity;
import vn.hbtplus.insurance.models.request.InsuranceContributionsRequest;

public interface RInsuranceReviewService {

    ResponseEntity exportComparePrePeriod(InsuranceContributionsRequest.ReportForm dto) throws Exception;
    ResponseEntity exportEmpLackInformation(InsuranceContributionsRequest.ReportForm dto) throws Exception;
    ResponseEntity exportPositionNotConfig(InsuranceContributionsRequest.ReportForm dto) throws Exception;
    ResponseEntity exportEmployeeNotConfig(InsuranceContributionsRequest.ReportForm dto) throws Exception;

}
