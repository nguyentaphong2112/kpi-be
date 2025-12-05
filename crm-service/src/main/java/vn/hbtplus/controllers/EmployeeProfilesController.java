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
import vn.hbtplus.models.request.EmployeeProfilesRequest;
import vn.hbtplus.services.EmployeeProfilesService;
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
public class EmployeeProfilesController {
    private final EmployeeProfilesService employeeProfilesService;

    @GetMapping(value = "/v1/employee-profiles", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public TableResponseEntity<EmployeeProfilesResponse> searchData(EmployeeProfilesRequest.SearchForm dto) {
        return employeeProfilesService.searchData(dto);
    }

    @PostMapping(value = "/v1/employee-profiles", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.UPDATE)
    public ResponseEntity saveData(@Valid @RequestBody EmployeeProfilesRequest.SubmitForm dto) throws BaseAppException {
        return employeeProfilesService.saveData(dto);
    }

    @DeleteMapping(value = "/v1/employee-profiles/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.DELETE)
    public ResponseEntity deleteData(@PathVariable Long id) throws RecordNotExistsException {
        return employeeProfilesService.deleteData(id);
    }

    @GetMapping(value = "/v1/employee-profiles/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public BaseResponseEntity<EmployeeProfilesResponse> getDataById(@PathVariable Long id)  throws RecordNotExistsException {
        return employeeProfilesService.getDataById(id);
    }

    @GetMapping(value = "/v1/employee-profiles/export", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public ResponseEntity<Object> exportData(EmployeeProfilesRequest.SearchForm dto) throws Exception {
        return employeeProfilesService.exportData(dto);
    }

}
