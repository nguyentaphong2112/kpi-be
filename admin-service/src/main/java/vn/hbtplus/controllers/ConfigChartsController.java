/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.hbtplus.annotations.HasPermission;
import vn.hbtplus.models.response.*;
import vn.hbtplus.models.request.ConfigChartsRequest;
import vn.hbtplus.services.ConfigChartsService;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.constants.Scope;
import vn.hbtplus.constants.Constant;
import vn.hbtplus.annotations.Resource;
import vn.hbtplus.exceptions.RecordNotExistsException;
import vn.hbtplus.exceptions.BaseAppException;
import vn.hbtplus.utils.ResponseUtils;

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
