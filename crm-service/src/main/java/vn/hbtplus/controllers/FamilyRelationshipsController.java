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
import vn.hbtplus.models.request.FamilyRelationshipsRequest;
import vn.hbtplus.services.FamilyRelationshipsService;
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
@Resource(value = Constant.RESOURCES.EMPLOYEE)
public class FamilyRelationshipsController {
    private final FamilyRelationshipsService familyRelationshipsService;

    @GetMapping(value = "/v1/family-relationships", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public TableResponseEntity<FamilyRelationshipsResponse> searchData(FamilyRelationshipsRequest.SearchForm dto) {
        return familyRelationshipsService.searchData(dto);
    }

    @PostMapping(value = "/v1/family-relationships", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.UPDATE)
    public ResponseEntity saveData(@Valid @RequestBody FamilyRelationshipsRequest.SubmitForm dto) throws BaseAppException {
        return familyRelationshipsService.saveData(dto);
    }

    @DeleteMapping(value = "/v1/family-relationships/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.DELETE)
    public ResponseEntity deleteData(@PathVariable Long id) throws RecordNotExistsException {
        return familyRelationshipsService.deleteData(id);
    }

    @GetMapping(value = "/v1/family-relationships/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public BaseResponseEntity<FamilyRelationshipsResponse> getDataById(@PathVariable Long id)  throws RecordNotExistsException {
        return familyRelationshipsService.getDataById(id);
    }

    @GetMapping(value = "/v1/family-relationships/export", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public ResponseEntity<Object> exportData(FamilyRelationshipsRequest.SearchForm dto) throws Exception {
        return familyRelationshipsService.exportData(dto);
    }

    @PostMapping(value = "/v1/family-relationships/import/{type}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.CREATE)
    public ResponseEntity<Object> importData(@PathVariable String type, @RequestPart MultipartFile file) throws Exception {
        familyRelationshipsService.processImportData(type, file);
        return ResponseUtils.ok();
    }
    @GetMapping(value = "/v1/family-relationships/download-template", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.CREATE)
    public ResponseEntity<Object> downloadTemplate() throws Exception {
        return familyRelationshipsService.downloadTemplate();
    }

}
