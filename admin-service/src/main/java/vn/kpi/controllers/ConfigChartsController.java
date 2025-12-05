/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.kpi.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.kpi.annotations.HasPermission;
import vn.kpi.models.response.*;
import vn.kpi.models.request.ConfigChartsRequest;
import vn.kpi.services.ConfigChartsService;
import vn.kpi.constants.BaseConstants;
import vn.kpi.constants.Scope;
import vn.kpi.constants.Constant;
import vn.kpi.annotations.Resource;
import vn.kpi.exceptions.RecordNotExistsException;
import vn.kpi.exceptions.BaseAppException;
import vn.kpi.utils.ResponseUtils;

import javax.validation.Valid;

@RestController
@RequestMapping(BaseConstants.REQUEST_MAPPING_PREFIX)
@RequiredArgsConstructor
@Resource(value = Constant.RESOURCES.SYS_CONFIG_CHART)
public class ConfigChartsController {
    private final ConfigChartsService configChartsService;

    @GetMapping(value = "/v1/config-charts", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public TableResponseEntity<ConfigChartsResponse> searchData(ConfigChartsRequest.SearchForm dto) {
        return configChartsService.searchData(dto);
    }

    @PostMapping(value = "/v1/config-charts", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.UPDATE)
    public ResponseEntity saveData(@Valid @RequestBody ConfigChartsRequest.SubmitForm dto) throws BaseAppException {
        return configChartsService.saveData(dto,null);
    }

    @PutMapping(value = "/v1/config-charts/{id}", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.UPDATE)
    public ResponseEntity updateData(@Valid @RequestBody ConfigChartsRequest.SubmitForm dto, @PathVariable Long id) throws BaseAppException {
        return configChartsService.saveData(dto,id);
    }

    @DeleteMapping(value = "/v1/config-charts/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.DELETE)
    public ResponseEntity deleteData(@PathVariable Long id) throws RecordNotExistsException {
        return configChartsService.deleteData(id);
    }

    @GetMapping(value = "/v1/config-charts/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public BaseResponseEntity<ConfigChartsResponse> getDataById(@PathVariable Long id)  throws RecordNotExistsException {
        return configChartsService.getDataById(id);
    }

    @GetMapping(value = "/v1/config-charts/export", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public ResponseEntity<Object> exportData(ConfigChartsRequest.SearchForm dto) throws Exception {
        return configChartsService.exportData(dto);
    }

    @GetMapping(value = "/v1/config-charts/list", produces = MediaType.APPLICATION_JSON_VALUE)
    public ListResponseEntity<ConfigChartsResponse> getListCharts() {
        return ResponseUtils.ok(configChartsService.getListCharts());
    }



}
