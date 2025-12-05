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
import vn.hbtplus.models.request.PytagoResearchsRequest;
import vn.hbtplus.services.PytagoResearchsService;
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
@Resource(value = Constant.RESOURCES.CRM_PYTAGO_RESEARCHS)
public class PytagoResearchsController {
    private final PytagoResearchsService pytagoResearchsService;

    @GetMapping(value = "/v1/public/pytago-researchs", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public TableResponseEntity<PytagoResearchsResponse> searchData(PytagoResearchsRequest.SearchForm dto) {
        return pytagoResearchsService.searchData(dto);
    }

    @PostMapping(value = "/v1/public/pytago-researchs", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity saveData(@Valid @RequestBody PytagoResearchsRequest.SubmitForm dto) throws BaseAppException {
        return pytagoResearchsService.saveData(dto);
    }

    @GetMapping(value = "/v1/public/pytago-researchs/count", produces = {MediaType.APPLICATION_JSON_VALUE})
    public BaseResponseEntity<PytagoResearchsResponse.SearchCount> getSearchCount() throws BaseAppException {
        return ResponseUtils.ok(pytagoResearchsService.getSearchCount());
    }

    @PostMapping(value = "/v1/public/pytago-researchs/create-customer/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity createCustomer(@PathVariable Long id) throws RecordNotExistsException {
        return pytagoResearchsService.createCustomer(id);
    }

    @DeleteMapping(value = "/v1/public/pytago-researchs/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.DELETE)
    public ResponseEntity deleteData(@PathVariable Long id) throws RecordNotExistsException {
        return pytagoResearchsService.deleteData(id);
    }

    @GetMapping(value = "/v1/public/pytago-researchs/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public BaseResponseEntity<PytagoResearchsResponse> getDataById(@PathVariable Long id)  throws RecordNotExistsException {
        return pytagoResearchsService.getDataById(id);
    }

    @GetMapping(value = "/v1/public/pytago-researchs/export", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public ResponseEntity<Object> exportData(PytagoResearchsRequest.SearchForm dto) throws Exception {
        return pytagoResearchsService.exportData(dto);
    }

}
