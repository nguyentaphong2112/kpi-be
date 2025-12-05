package vn.hbtplus.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import vn.hbtplus.services.ReportService;

import java.util.Map;

@RestController
@RequestMapping("")
@RequiredArgsConstructor
@Slf4j
public class ReportController {
    private final ReportService reportService;

    @GetMapping(value = "/v1/dynamic-reports/export/{reportCode}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity export(@RequestParam Map<String, String> params, @PathVariable String reportCode) throws Exception {
        return reportService.export(reportCode, params);
    }
}
