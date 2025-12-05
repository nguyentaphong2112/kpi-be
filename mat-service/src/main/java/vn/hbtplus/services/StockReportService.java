package vn.hbtplus.services;

import org.springframework.http.ResponseEntity;
import vn.hbtplus.models.request.StockReportRequest;


public interface StockReportService {

    ResponseEntity<Object> equipmentExport(StockReportRequest dto) throws Exception;

    ResponseEntity<Object> equipmentTypeExport(StockReportRequest dto) throws Exception;

    ResponseEntity<Object> equipmentDepartmentExport(StockReportRequest dto) throws Exception;

    ResponseEntity<Object> equipmentDetailExport(StockReportRequest dto) throws Exception;
}
