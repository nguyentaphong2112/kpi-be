package vn.kpi.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.kpi.services.ChartService;
import vn.kpi.utils.ResponseUtils;

@RestController
@RequestMapping("")
@RequiredArgsConstructor
public class ChartController {
    private final ChartService chartService;
    @GetMapping(value = "v1/chart/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity getChartData(@PathVariable("id") Long id) {

        return ResponseUtils.ok(chartService.getChartData(id));
    }
}
