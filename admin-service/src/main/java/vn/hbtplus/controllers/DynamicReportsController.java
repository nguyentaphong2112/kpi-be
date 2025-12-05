/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import vn.hbtplus.annotations.HasPermission;
import vn.hbtplus.annotations.Resource;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.constants.Constant;
import vn.hbtplus.constants.Scope;
import vn.hbtplus.exceptions.BaseAppException;
import vn.hbtplus.exceptions.RecordNotExistsException;
import vn.hbtplus.models.request.DynamicReportsRequest;
import vn.hbtplus.models.response.BaseResponseEntity;
import vn.hbtplus.models.response.DynamicReportsResponse;
import vn.hbtplus.models.response.TableResponseEntity;
import vn.hbtplus.services.DynamicReportsService;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping(BaseConstants.REQUEST_MAPPING_PREFIX)
@RequiredArgsConstructor
@Resource(value = Constant.RESOURCES.DYNAMIC_REPORTS)
public class DynamicReportsController {
    private final DynamicReportsService dynamicReportsService;

    @GetMapping(value = "/v1/dynamic-reports", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public TableResponseEntity<DynamicReportsResponse> searchData(DynamicReportsRequest.SearchForm dto) {
        return dynamicReportsService.searchData(dto);
    }

    @PostMapping(value = "/v1/dynamic-reports", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.UPDATE)
    public ResponseEntity saveData(@ModelAttribute @Valid DynamicReportsRequest.SubmitForm dto) throws BaseAppException {
        return dynamicReportsService.saveData(null, dto);
    }

    @PutMapping(value = "/v1/dynamic-reports/{id}", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.UPDATE)
    public ResponseEntity updateData(@ModelAttribute @Valid DynamicReportsRequest.SubmitForm dto, @PathVariable Long id) throws BaseAppException {
        return dynamicReportsService.saveData(id, dto);
    }

    @DeleteMapping(value = "/v1/dynamic-reports/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.DELETE)
    public ResponseEntity deleteData(@PathVariable Long id) throws RecordNotExistsException {
        return dynamicReportsService.deleteData(id);
    }

    @GetMapping(value = "/v1/dynamic-reports/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public BaseResponseEntity<DynamicReportsResponse> getDataById(@PathVariable String id) throws RecordNotExistsException {
        Long reportId;
        if (id.matches("\\d+")) {
            reportId = Long.valueOf(id);
        } else {
            reportId = dynamicReportsService.getReportId(id);
        }
        return reportId == null ? null : dynamicReportsService.getDataById(reportId);
    }

    @GetMapping(value = "/v1/dynamic-reports/export", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public ResponseEntity<Object> exportData(DynamicReportsRequest.SearchForm dto) throws Exception {
        return dynamicReportsService.exportData(dto);
    }

    @PutMapping(value = "/v1/dynamic-reports/file/{id}", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.UPDATE)
    public ResponseEntity updateFile(@RequestPart(value = "files", required = false) List<MultipartFile> files,
                                     @RequestPart(value = "data", required = false) DynamicReportsRequest.FileData data,
                                     @PathVariable Long id) throws BaseAppException {
        return dynamicReportsService.saveFile(id, files, data);
    }


}
