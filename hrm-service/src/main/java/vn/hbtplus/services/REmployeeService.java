package vn.hbtplus.services;

import com.jxcell.CellException;
import org.springframework.http.ResponseEntity;
import vn.hbtplus.models.request.EmployeesRequest;

public interface REmployeeService {
    ResponseEntity<Object> exportData(String reportType, EmployeesRequest.ReportForm dto) throws Exception;

    ResponseEntity<Object> exportStatisticReport(EmployeesRequest.ReportForm dto) throws Exception;
}
