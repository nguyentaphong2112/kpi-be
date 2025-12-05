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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import vn.hbtplus.annotations.HasPermission;
import vn.hbtplus.annotations.Resource;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.constants.Constant;
import vn.hbtplus.constants.Scope;
import vn.hbtplus.exceptions.BaseAppException;
import vn.hbtplus.exceptions.RecordNotExistsException;
import vn.hbtplus.models.request.PartnersRequest;
import vn.hbtplus.models.response.*;
import vn.hbtplus.services.PartnersService;

import javax.validation.Valid;

@RestController
@RequestMapping(BaseConstants.REQUEST_MAPPING_PREFIX)
@RequiredArgsConstructor
@Resource(value = Constant.RESOURCES.CRM_PARTNERS)
public class PartnersController {
    private final PartnersService partnersService;

    @GetMapping(value = "/v1/partners", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public TableResponseEntity<PartnersResponse.SearchResult> searchData(PartnersRequest.SearchForm dto) {
        return partnersService.searchData(dto);
    }

    @PostMapping(value = "/v1/partners", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.CREATE)
    public ResponseEntity saveData(@Valid @RequestBody PartnersRequest.SubmitForm dto) throws BaseAppException {
        return partnersService.saveData(dto, null);
    }

    @PutMapping(value = "/v1/partners/{id}", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.UPDATE)
    public ResponseEntity updateData(@Valid @RequestBody PartnersRequest.SubmitForm dto, @PathVariable Long id) throws BaseAppException {
        return partnersService.saveData(dto, id);
    }

    @DeleteMapping(value = "/v1/partners/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.DELETE)
    public ResponseEntity deleteData(@PathVariable Long id) throws RecordNotExistsException {
        return partnersService.deleteData(id);
    }

    @GetMapping(value = "/v1/partners/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public BaseResponseEntity<PartnersResponse.DetailBean> getDataById(@PathVariable Long id)  throws RecordNotExistsException {
        return partnersService.getDataById(id);
    }

    @GetMapping(value = "/v1/partners/export", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public ResponseEntity<Object> exportData(PartnersRequest.SearchForm dto) throws Exception {
        return partnersService.exportData(dto);
    }

    @PostMapping(value = "/v1/partners/export-card", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.VIEW)
    public ResponseEntity<Object> exportCard(@RequestBody PartnersRequest.PrintCard dto) throws Exception {
        return partnersService.exportCard(dto);
    }

    @GetMapping(value = "/v1/partners/export-template", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public ResponseEntity<Object> exportTemplate() throws Exception {
        return partnersService.exportTemplate();
    }

    @PostMapping(value = "/v1/partners/import-process", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.VIEW)
    public ResponseEntity<Object> importProcess(@RequestPart(value = "file") MultipartFile file) throws Exception {
        return partnersService.importProcess(file);
    }

    @GetMapping(value = "/v1/partners/list", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public ListResponseEntity<PartnersResponse.DetailBean> getListData() throws Exception {
        return partnersService.getListData();
    }

}
