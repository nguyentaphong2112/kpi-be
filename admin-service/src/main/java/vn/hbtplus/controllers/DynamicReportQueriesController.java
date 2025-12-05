/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.hbtplus.annotations.HasPermission;
import vn.hbtplus.annotations.Resource;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.constants.Constant;
import vn.hbtplus.constants.Scope;
import vn.hbtplus.exceptions.BaseAppException;
import vn.hbtplus.exceptions.RecordNotExistsException;
import vn.hbtplus.models.request.DynamicReportQueriesRequest;
import vn.hbtplus.models.response.BaseResponseEntity;
import vn.hbtplus.models.response.DynamicReportQueriesResponse;
import vn.hbtplus.models.response.TableResponseEntity;
import vn.hbtplus.services.DynamicReportQueriesService;

import javax.validation.Valid;

@RestController
@RequestMapping(BaseConstants.REQUEST_MAPPING_PREFIX)
@RequiredArgsConstructor
@Resource(value = Constant.RESOURCES.CONFIG_PARAMETER)
public class DynamicReportQueriesController {
    private final DynamicReportQueriesService dynamicReportQueriesService;

    @GetMapping(value = "/v1/dynamic-report-queries", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public TableResponseEntity<DynamicReportQueriesResponse> searchData(DynamicReportQueriesRequest.SearchForm dto) {
        return dynamicReportQueriesService.searchData(dto);
    }

    @PostMapping(value = "/v1/dynamic-report-queries", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.UPDATE)
    public ResponseEntity saveData(@Valid  DynamicReportQueriesRequest.SubmitForm dto) throws BaseAppException {
        return dynamicReportQueriesService.saveData(dto);
    }

    @DeleteMapping(value = "/v1/dynamic-report-queries/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.DELETE)
    public ResponseEntity deleteData(@PathVariable Long id) throws RecordNotExistsException {
        return dynamicReportQueriesService.deleteData(id);
    }

    @GetMapping(value = "/v1/dynamic-report-queries/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public BaseResponseEntity<DynamicReportQueriesResponse> getDataById(@PathVariable Long id)  throws RecordNotExistsException {
        return dynamicReportQueriesService.getDataById(id);
    }

    @GetMapping(value = "/v1/dynamic-report-queries/export", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public ResponseEntity<Object> exportData(DynamicReportQueriesRequest.SearchForm dto) throws Exception {
        return dynamicReportQueriesService.exportData(dto);
    }

}
