package vn.hbtplus.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.models.request.EmployeesRequest;
import vn.hbtplus.services.REmployeeService;

@RestController
@RequestMapping(BaseConstants.REQUEST_MAPPING_PREFIX)
@RequiredArgsConstructor
public class REmployeeController {

    private final REmployeeService rEmployeeService;

    @GetMapping(value = "/v1/employee-report/{reportType}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> exportData(EmployeesRequest.ReportForm dto, @PathVariable String reportType) throws Exception {
        if("CO_CAU_LAO_DONG".equalsIgnoreCase(reportType)) {
            return rEmployeeService.exportStatisticReport(dto);
        }
        return rEmployeeService.exportData(reportType, dto);
    }

}
