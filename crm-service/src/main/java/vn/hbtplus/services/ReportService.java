package vn.hbtplus.services;

import org.springframework.http.ResponseEntity;
import vn.hbtplus.models.request.CustomersRequest;

public interface ReportService {
    ResponseEntity<Object> exportData(String reportType, CustomersRequest.ReportForm dto) throws Exception;

}
