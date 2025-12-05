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
import vn.hbtplus.models.request.DocumentTypesRequest;
import vn.hbtplus.services.DocumentTypesService;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.constants.Scope;
import vn.hbtplus.constants.Constant;
import vn.hbtplus.annotations.Resource;
import vn.hbtplus.exceptions.RecordNotExistsException;
import vn.hbtplus.exceptions.BaseAppException;

import javax.validation.Valid;

@RestController
@RequestMapping(BaseConstants.REQUEST_MAPPING_PREFIX)
@RequiredArgsConstructor
@Resource(value = Constant.RESOURCES.DOCUMENT_TYPES)
public class DocumentTypesController {
    private final DocumentTypesService documentTypesService;

    @GetMapping(value = "/v1/document-types", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public TableResponseEntity<DocumentTypesResponse.SearchResult> searchData(DocumentTypesRequest.SearchForm dto) {
        return documentTypesService.searchData(dto);
    }

    @PostMapping(value = "/v1/document-types", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.CREATE)
    public ResponseEntity saveData(@Valid @RequestBody DocumentTypesRequest.SubmitForm dto) throws BaseAppException {
        return documentTypesService.saveData(dto, null);
    }

    @PutMapping(value = "/v1/document-types/{id}", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.UPDATE)
    public ResponseEntity updateData(@Valid @RequestBody DocumentTypesRequest.SubmitForm dto, @PathVariable Long id) throws BaseAppException {
        return documentTypesService.saveData(dto, id);
    }


    @DeleteMapping(value = "/v1/document-types/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.DELETE)
    public ResponseEntity deleteData(@PathVariable Long id) throws RecordNotExistsException {
        return documentTypesService.deleteData(id);
    }

    @GetMapping(value = "/v1/document-types/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public BaseResponseEntity<DocumentTypesResponse.DetailBean> getDataById(@PathVariable Long id) throws RecordNotExistsException {
        return documentTypesService.getDataById(id);
    }

    @GetMapping(value = "/v1/document-types/export", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public ResponseEntity<Object> exportData(DocumentTypesRequest.SearchForm dto) throws Exception {
        return documentTypesService.exportData(dto);
    }

    @GetMapping(value = "/v1/document-types/list", produces = MediaType.APPLICATION_JSON_VALUE)
    public ListResponseEntity<DocumentTypesResponse.DetailBean> getList() throws Exception {
        return documentTypesService.getList();
    }

}
