package vn.kpi.services;

import org.springframework.http.ResponseEntity;
import vn.kpi.models.request.EmployeesRequest;

public interface REmployeeService {
    ResponseEntity<Object> exportData(String reportType, EmployeesRequest.ReportForm dto) throws Exception;

    ResponseEntity<Object> exportStatisticReport(EmployeesRequest.ReportForm dto) throws Exception;
}
