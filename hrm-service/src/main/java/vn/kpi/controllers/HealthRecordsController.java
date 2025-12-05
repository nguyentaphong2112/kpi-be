/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.kpi.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import vn.kpi.annotations.HasPermission;
import vn.kpi.models.response.*;
import vn.kpi.models.request.HealthRecordsRequest;
import vn.kpi.services.HealthRecordsService;
import vn.kpi.constants.BaseConstants;
import vn.kpi.constants.Scope;
import vn.kpi.constants.Constant;
import vn.kpi.annotations.Resource;
import vn.kpi.exceptions.RecordNotExistsException;
import vn.kpi.exceptions.BaseAppException;
import javax.validation.Valid;

@RestController
@RequestMapping(BaseConstants.REQUEST_MAPPING_PREFIX)
@RequiredArgsConstructor
@Resource(value = Constant.RESOURCES.EMPLOYEE)
public class HealthRecordsController {
    private final HealthRecordsService healthRecordsService;

    @GetMapping(value = "/v1/health-records", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public TableResponseEntity<HealthRecordsResponse> searchData(HealthRecordsRequest.SearchForm dto) {
        return healthRecordsService.searchData(dto);
    }

    @PostMapping(value = "/v1/health-records", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.UPDATE)
    public ResponseEntity saveData(@Valid @RequestBody HealthRecordsRequest.SubmitForm dto) throws BaseAppException {
        return healthRecordsService.saveData(dto,null);
    }

    @PutMapping(value = "/v1/health-records/{id}", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.UPDATE)
    public ResponseEntity update(@Valid @RequestBody HealthRecordsRequest.SubmitForm dto,@PathVariable Long id) throws BaseAppException {
        return healthRecordsService.saveData(dto,id);
    }

    @DeleteMapping(value = "/v1/health-records/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.DELETE)
    public ResponseEntity deleteData(@PathVariable Long id) throws RecordNotExistsException {
        return healthRecordsService.deleteData(id);
    }

    @GetMapping(value = "/v1/health-records/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public BaseResponseEntity<HealthRecordsResponse> getDataById(@PathVariable Long id)  throws RecordNotExistsException {
        return healthRecordsService.getDataById(id);
    }

    @GetMapping(value = "/v1/health-records/export", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public ResponseEntity<Object> exportData(HealthRecordsRequest.SearchForm dto) throws Exception {
        return healthRecordsService.exportData(dto);
    }

    @GetMapping(value = "/v1/health-records/download-template/{periodId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public ResponseEntity<Object> exportTemplate(@PathVariable String periodId) throws Exception {
        return healthRecordsService.downloadTemplate(periodId);
    }

    @PostMapping(value = "/v1/health-records/import-process/{periodId}", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.VIEW)
    public ResponseEntity<Object> importProcess(@PathVariable String periodId,@RequestPart(value = "file") MultipartFile file) throws Exception {
        return healthRecordsService.importProcess(periodId,file);
    }

}
