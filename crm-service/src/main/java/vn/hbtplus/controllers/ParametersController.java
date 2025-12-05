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
import vn.hbtplus.models.request.ParametersRequest;
import vn.hbtplus.services.CrmParametersService;
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
public class ParametersController {
    private final CrmParametersService parametersService;

    @GetMapping(value = "/v1/parameters", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public TableResponseEntity<ParametersResponse> searchData(ParametersRequest.SearchForm dto) {
        return parametersService.searchData(dto);
    }

    @PostMapping(value = "/v1/parameters", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.UPDATE)
    public ResponseEntity saveData(@Valid  ParametersRequest.SubmitForm dto) throws BaseAppException {
        return parametersService.saveData(dto);
    }

    @DeleteMapping(value = "/v1/parameters/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.DELETE)
    public ResponseEntity deleteData(@PathVariable Long id) throws RecordNotExistsException {
        return parametersService.deleteData(id);
    }

    @GetMapping(value = "/v1/parameters/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public BaseResponseEntity<ParametersResponse> getDataById(@PathVariable Long id)  throws RecordNotExistsException {
        return parametersService.getDataById(id);
    }

    @GetMapping(value = "/v1/parameters/export", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public ResponseEntity<Object> exportData(ParametersRequest.SearchForm dto) throws Exception {
        return parametersService.exportData(dto);
    }

}
