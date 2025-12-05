package vn.hbtplus.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.hbtplus.annotations.HasPermission;
import vn.hbtplus.annotations.Resource;
import vn.hbtplus.constant.Constants;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.constants.Scope;
import vn.hbtplus.models.request.StockReportRequest;
import vn.hbtplus.services.StockReportService;

@RestController
@RequestMapping(BaseConstants.REQUEST_MAPPING_PREFIX)
@RequiredArgsConstructor
//@Resource(value = Constants.RESOURCE.STOCK_REPORT)

public class StockReportController {

    private final StockReportService stockReportService;

    @GetMapping(value = "/v1/stock-report/equipment-stock-report", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public ResponseEntity<Object> equipmentExport(StockReportRequest dto) throws Exception {
        return stockReportService.equipmentExport(dto);
    }

    @GetMapping(value = "/v1/stock-report/equipment-type-stock-report", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public ResponseEntity<Object> equipmentTypeExport(StockReportRequest dto) throws Exception {
        return stockReportService.equipmentTypeExport(dto);
    }

    @GetMapping(value = "/v1/stock-report/equipment-department-stock-report", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public ResponseEntity<Object> equipmentDepartmentExport(StockReportRequest dto) throws Exception {
        return stockReportService.equipmentDepartmentExport(dto);
    }

    @GetMapping(value = "/v1/stock-report/equipment-detail-stock-report", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public ResponseEntity<Object> equipmentDetailExport(StockReportRequest dto) throws Exception {
        return stockReportService.equipmentDetailExport(dto);
    }
}
