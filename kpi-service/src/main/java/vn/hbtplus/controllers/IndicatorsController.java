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
import vn.hbtplus.models.response.*;
import vn.hbtplus.models.request.IndicatorsRequest;
import vn.hbtplus.services.IndicatorsService;
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
@Resource(value = Constant.RESOURCES.INDICATOR)
public class IndicatorsController {
    private final IndicatorsService indicatorsService;

    @GetMapping(value = "/v1/indicators", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public TableResponseEntity<IndicatorsResponse.SearchResult> searchData(IndicatorsRequest.SearchForm dto) {
        return indicatorsService.searchData(dto);
    }

    @PostMapping(value = "/v1/indicators", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.CREATE)
    public ResponseEntity saveData(@Valid IndicatorsRequest.SubmitForm dto) throws BaseAppException {
        return indicatorsService.saveData(dto, null);
    }

    @PostMapping(value = "/v1/indicators/import", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.IMPORT)
    public ResponseEntity importData(@RequestPart(value = "file") MultipartFile file) throws Exception {
        return ResponseUtils.ok(indicatorsService.importData(file));
    }

    @PutMapping(value = "/v1/indicators/{id}", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.UPDATE)
    public ResponseEntity saveData(@Valid IndicatorsRequest.SubmitForm dto, @PathVariable Long id) throws BaseAppException {
        return indicatorsService.saveData(dto, id);
    }

    @DeleteMapping(value = "/v1/indicators/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.DELETE)
    public ResponseEntity deleteData(@PathVariable Long id) throws RecordNotExistsException {
        return indicatorsService.deleteData(id);
    }

    @GetMapping(value = "/v1/indicators/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public BaseResponseEntity<IndicatorsResponse.SearchResult> getDataById(@PathVariable Long id) throws RecordNotExistsException {
        return indicatorsService.getDataById(id);
    }

    @GetMapping(value = "/v1/indicators/export", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public ResponseEntity<Object> exportData(IndicatorsRequest.SearchForm dto) throws Exception {
        return indicatorsService.exportData(dto);
    }

    @GetMapping(value = "/v1/indicators/data-picker/{organizationId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public TableResponseEntity<IndicatorsResponse.SearchResult> getIndicatorPicker(@PathVariable Long organizationId, IndicatorsRequest.SearchForm dto) throws Exception {
        return ResponseUtils.ok(indicatorsService.getIndicatorPicker(organizationId, dto));
    }

    @GetMapping(value = "/v1/indicators/list/{organizationId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ListResponseEntity<IndicatorsResponse.DetailList> getIndicatorPicker(@PathVariable Long organizationId) throws Exception {
        return ResponseUtils.ok(indicatorsService.getList(organizationId));
    }

    @GetMapping(value = "/v1/indicators/list/employee/{employeeId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ListResponseEntity<IndicatorsResponse.DetailList> getEmployeeIndicatorPicker(@PathVariable Long employeeId) throws Exception {
        return ResponseUtils.ok(indicatorsService.getListEmployee(employeeId));
    }

    @GetMapping(value = "/v1/indicators/download-template", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public ResponseEntity<Object> getTemplateImportWorkScheduleDetail() throws Exception {
        return ResponseUtils.getResponseFileEntity(indicatorsService.getTemplateIndicator(), true);
    }

}
