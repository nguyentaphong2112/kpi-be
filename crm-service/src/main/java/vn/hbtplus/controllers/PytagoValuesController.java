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
import vn.hbtplus.models.request.PytagoValuesRequest;
import vn.hbtplus.services.PytagoValuesService;
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
@Resource(value = Constant.RESOURCES.EMPLOYEE)
public class PytagoValuesController {
    private final PytagoValuesService pytagoValuesService;

    @GetMapping(value = "/v1/pytago-values", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public TableResponseEntity<PytagoValuesResponse> searchData(PytagoValuesRequest.SearchForm dto) {
        return pytagoValuesService.searchData(dto);
    }

    @PostMapping(value = "/v1/pytago-values", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.UPDATE)
    public ResponseEntity saveData(@Valid @RequestBody PytagoValuesRequest.SubmitForm dto) throws BaseAppException {
        return pytagoValuesService.saveData(dto);
    }

    @DeleteMapping(value = "/v1/pytago-values/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.DELETE)
    public ResponseEntity deleteData(@PathVariable Long id) throws RecordNotExistsException {
        return pytagoValuesService.deleteData(id);
    }

    @GetMapping(value = "/v1/pytago-values/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public BaseResponseEntity<PytagoValuesResponse> getDataById(@PathVariable Long id)  throws RecordNotExistsException {
        return pytagoValuesService.getDataById(id);
    }

    @GetMapping(value = "/v1/pytago-values/export", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public ResponseEntity<Object> exportData(PytagoValuesRequest.SearchForm dto) throws Exception {
        return pytagoValuesService.exportData(dto);
    }

}
