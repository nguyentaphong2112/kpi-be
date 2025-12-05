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
import vn.hbtplus.models.request.EmployeeIndicatorsRequest;
import vn.hbtplus.services.EmployeeIndicatorsService;
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
@Resource(value = Constant.RESOURCES.EMPLOYEE_INDICATORS)
public class EmployeeIndicatorsController {
    private final EmployeeIndicatorsService employeeIndicatorsService;

    @GetMapping(value = "/v1/employee-indicators", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public TableResponseEntity<EmployeeIndicatorsResponse.SearchResult> searchData(EmployeeIndicatorsRequest.SearchForm dto) {
        return employeeIndicatorsService.searchData(dto);
    }

    @PostMapping(value = "/v1/employee-indicators", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.CREATE)
    public ResponseEntity saveData(@Valid @RequestBody EmployeeIndicatorsRequest.SubmitForm dto) throws BaseAppException {
        return employeeIndicatorsService.saveData(dto, null, null);
    }

    @PutMapping(value = "/v1/employee-indicators/{id}", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.UPDATE)
    public ResponseEntity saveData(@Valid @RequestBody EmployeeIndicatorsRequest.SubmitForm dto, @PathVariable Long id) throws BaseAppException {
        return employeeIndicatorsService.saveData(dto, id, null);
    }

    @DeleteMapping(value = "/v1/employee-indicators/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.DELETE)
    public ResponseEntity deleteData(@PathVariable Long id) throws RecordNotExistsException {
        return employeeIndicatorsService.deleteData(id);
    }

    @GetMapping(value = "/v1/employee-indicators/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public BaseResponseEntity<EmployeeIndicatorsResponse.SearchResult> getDataById(@PathVariable Long id)  throws RecordNotExistsException {
        return employeeIndicatorsService.getDataById(id);
    }

    @GetMapping(value = "/v1/employee-indicators/export", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public ResponseEntity<Object> exportData(EmployeeIndicatorsRequest.SearchForm dto) throws Exception {
        return employeeIndicatorsService.exportData(dto);
    }

}
