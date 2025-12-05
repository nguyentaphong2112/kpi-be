package vn.hbtplus.controllers;


import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.models.request.CustomersRequest;
import vn.hbtplus.services.ReportService;

@RestController
@RequestMapping(BaseConstants.REQUEST_MAPPING_PREFIX)
@RequiredArgsConstructor
public class ReportController {
    private final ReportService reportService;

    @GetMapping(value = "/v1/export-report/{reportType}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> exportData(CustomersRequest.ReportForm dto, @PathVariable String reportType) throws Exception {
        return reportService.exportData(reportType, dto);
    }
}
