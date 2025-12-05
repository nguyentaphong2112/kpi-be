/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.kpi.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.kpi.annotations.HasPermission;
import vn.kpi.annotations.Resource;
import vn.kpi.constants.BaseConstants;
import vn.kpi.constants.Constant;
import vn.kpi.constants.Scope;
import vn.kpi.exceptions.BaseAppException;
import vn.kpi.exceptions.RecordNotExistsException;
import vn.kpi.models.request.DynamicReportParametersRequest;
import vn.kpi.models.response.BaseResponseEntity;
import vn.kpi.models.response.DynamicReportParametersResponse;
import vn.kpi.models.response.TableResponseEntity;
import vn.kpi.services.DynamicReportParametersService;

import javax.validation.Valid;

@RestController
@RequestMapping(BaseConstants.REQUEST_MAPPING_PREFIX)
@RequiredArgsConstructor
@Resource(value = Constant.RESOURCES.DYNAMIC_REPORTS)
public class DynamicReportParametersController {
    private final DynamicReportParametersService dynamicReportParametersService;

    @GetMapping(value = "/v1/dynamic-report-parameters", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public TableResponseEntity<DynamicReportParametersResponse> searchData(DynamicReportParametersRequest.SearchForm dto) {
        return dynamicReportParametersService.searchData(dto);
    }

    @PostMapping(value = "/v1/dynamic-report-parameters", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.UPDATE)
    public ResponseEntity saveData(@Valid  DynamicReportParametersRequest.SubmitForm dto) throws BaseAppException {
        return dynamicReportParametersService.saveData(dto);
    }

    @DeleteMapping(value = "/v1/dynamic-report-parameters/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.DELETE)
    public ResponseEntity deleteData(@PathVariable Long id) throws RecordNotExistsException {
        return dynamicReportParametersService.deleteData(id);
    }

    @GetMapping(value = "/v1/dynamic-report-parameters/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public BaseResponseEntity<DynamicReportParametersResponse> getDataById(@PathVariable Long id)  throws RecordNotExistsException {
        return dynamicReportParametersService.getDataById(id);
    }

    @GetMapping(value = "/v1/dynamic-report-parameters/export", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public ResponseEntity<Object> exportData(DynamicReportParametersRequest.SearchForm dto) throws Exception {
        return dynamicReportParametersService.exportData(dto);
    }

}
