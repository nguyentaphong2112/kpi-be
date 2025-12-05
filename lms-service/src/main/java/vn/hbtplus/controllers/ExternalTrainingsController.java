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
import vn.hbtplus.models.request.ExternalTrainingsRequest;
import vn.hbtplus.services.ExternalTrainingsService;
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
@Resource(value = Constant.RESOURCES.EXTERNAL_TRAINING)
public class ExternalTrainingsController {
    private final ExternalTrainingsService externalTrainingsService;

    @GetMapping(value = "/v1/external-trainings", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public TableResponseEntity<ExternalTrainingsResponse.SearchResult> searchData(ExternalTrainingsRequest.SearchForm dto) {
        return externalTrainingsService.searchData(dto);
    }

    @PostMapping(value = "/v1/external-trainings", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.UPDATE)
    public ResponseEntity saveData(@Valid @RequestBody ExternalTrainingsRequest.SubmitForm dto) throws BaseAppException {
        return externalTrainingsService.saveData(dto, null);
    }

    @PutMapping(value = "/v1/external-trainings/{id}", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.UPDATE)
    public ResponseEntity updateData(@Valid @RequestBody ExternalTrainingsRequest.SubmitForm dto,
                                     @PathVariable Long id) throws BaseAppException {
        return externalTrainingsService.saveData(dto, id);
    }


    @DeleteMapping(value = "/v1/external-trainings/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.DELETE)
    public ResponseEntity deleteData(@PathVariable Long id) throws RecordNotExistsException {
        return externalTrainingsService.deleteData(id);
    }

    @GetMapping(value = "/v1/external-trainings/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public BaseResponseEntity<ExternalTrainingsResponse.Detail> getDataById(@PathVariable Long id)  throws RecordNotExistsException {
        return externalTrainingsService.getDataById(id);
    }

    @GetMapping(value = "/v1/external-trainings/export", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public ResponseEntity<Object> exportData(ExternalTrainingsRequest.SearchForm dto) throws Exception {
        return externalTrainingsService.exportData(dto);
    }


    @GetMapping(value = "/v1/external-trainings/download-template", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public ResponseEntity<Object> getTemplateImport() throws Exception {
        return ResponseUtils.getResponseFileEntity(externalTrainingsService.getTemplateIndicator() , true);
    }

    @PostMapping(value = "/v1/external-trainings/import", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.IMPORT)
    public ResponseEntity importData(@RequestPart(value = "file") MultipartFile file) throws Exception {
        return ResponseUtils.ok(externalTrainingsService.importData(file));
    }

}
