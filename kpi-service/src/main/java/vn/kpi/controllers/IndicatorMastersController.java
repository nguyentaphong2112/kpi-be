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
import vn.kpi.models.request.IndicatorMastersRequest;
import vn.kpi.services.IndicatorMastersService;
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
@Resource(value = Constant.RESOURCES.INDICATOR_CONVERSION)
public class IndicatorMastersController {
    private final IndicatorMastersService indicatorMastersService;

    @GetMapping(value = "/v1/indicator-masters", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public TableResponseEntity<IndicatorMastersResponse> searchData(IndicatorMastersRequest.SearchForm dto) throws Exception {
        return indicatorMastersService.searchData(dto);
    }

    @PostMapping(value = "/v1/indicator-masters", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.UPDATE)
    public ResponseEntity saveData(@Valid @RequestBody IndicatorMastersRequest.SubmitForm dto) throws BaseAppException {
        return indicatorMastersService.saveData(dto);
    }

    @DeleteMapping(value = "/v1/indicator-masters/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.DELETE)
    public ResponseEntity deleteData(@PathVariable Long id) throws RecordNotExistsException {
        return indicatorMastersService.deleteData(id);
    }

    @GetMapping(value = "/v1/indicator-masters/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public BaseResponseEntity<IndicatorMastersResponse> getDataById(@PathVariable Long id) throws RecordNotExistsException {
        return indicatorMastersService.getDataById(id);
    }

    @GetMapping(value = "/v1/indicator-masters/export", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public ResponseEntity<Object> exportData(IndicatorMastersRequest.SearchForm dto) throws Exception {
        return indicatorMastersService.exportData(dto);
    }

    @PostMapping(value = "/v1/indicator-masters/status", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.UPDATE)
    public ResponseEntity updateStatusByOrg(@Valid @RequestBody IndicatorMastersRequest.SubmitForm dto) throws BaseAppException {
        return indicatorMastersService.updateStatusByOrg(dto);
    }

    @GetMapping(value = "/v1/indicator-masters/export/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public ResponseEntity<Object> exportDataById(@PathVariable Long id) throws Exception {
        return indicatorMastersService.exportDataId(id);
    }

    @PostMapping(value = "/v1/indicator-masters/approval-all",  produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.APPROVE)
    public ResponseEntity updateStatusByOrg() throws BaseAppException {
        return indicatorMastersService.approvalAll();
    }

    @PostMapping(value = "/v1/indicator-masters/import", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.VIEW)
    public ResponseEntity<Object> importData(@ModelAttribute @Valid IndicatorMastersRequest.ImportRequest dto) throws Exception {
        return indicatorMastersService.importData(dto);
    }


    @GetMapping(value = "/v1/indicator-masters/download-template", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public ResponseEntity<Object> getTemplateImport(@ModelAttribute @Valid IndicatorMastersRequest.ImportRequest dto) throws Exception {
        return ResponseUtils.getResponseFileEntity(indicatorMastersService.getTemplateImport(dto), true);
    }
}
