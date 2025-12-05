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
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.constants.Scope;
import vn.hbtplus.exceptions.BaseAppException;
import vn.hbtplus.exceptions.RecordNotExistsException;
import vn.hbtplus.models.request.EmployeeWorkPlanningsRequest;
import vn.hbtplus.models.response.BaseResponseEntity;
import vn.hbtplus.models.response.EmployeeWorkPlanningsResponse;
import vn.hbtplus.models.response.TableResponseEntity;
import vn.hbtplus.services.EmployeeWorkPlanningsService;

import javax.validation.Valid;

@RestController
@RequestMapping(BaseConstants.REQUEST_MAPPING_PREFIX)
@RequiredArgsConstructor
//@Resource(value = Constant.RESOURCES.EMPLOYEE_WORK_PLANNINGS)
public class EmployeeWorkPlanningsController {
    private final EmployeeWorkPlanningsService employeeWorkPlanningsService;

    @GetMapping(value = "/v1/employee-work-plannings", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public TableResponseEntity<EmployeeWorkPlanningsResponse.SearchForm> searchData(EmployeeWorkPlanningsRequest.SearchForm dto) {
        return employeeWorkPlanningsService.searchData(dto);
    }

    @PostMapping(value = "/v1/employee-work-plannings", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
//    @HasPermission(scope = Scope.CREATE)
    public ResponseEntity saveData(@Valid EmployeeWorkPlanningsRequest.SubmitForm dto) throws BaseAppException {
        return employeeWorkPlanningsService.saveData(dto, null);
    }

    @PutMapping(value = "/v1/employee-work-plannings/{id}", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
//    @HasPermission(scope = Scope.UPDATE)
    public ResponseEntity saveData(@Valid EmployeeWorkPlanningsRequest.SubmitForm dto, @PathVariable Long id) throws BaseAppException {
        return employeeWorkPlanningsService.saveData(dto, id);
    }

    @PostMapping(value = "/v1/employee-work-plannings/list", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.CREATE)
    public ResponseEntity saveListData(@Valid EmployeeWorkPlanningsRequest.ListData dto) throws BaseAppException {
        return employeeWorkPlanningsService.saveListData(dto);
    }

    @DeleteMapping(value = "/v1/employee-work-plannings/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.DELETE)
    public ResponseEntity deleteData(@PathVariable Long id) throws RecordNotExistsException {
        return employeeWorkPlanningsService.deleteData(id);
    }

    @GetMapping(value = "/v1/employee-work-plannings/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public BaseResponseEntity<EmployeeWorkPlanningsResponse.SearchForm> getDataById(@PathVariable Long id)  throws RecordNotExistsException {
        return employeeWorkPlanningsService.getDataById(id);
    }

    @GetMapping(value = "/v1/employee-work-plannings/export", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public ResponseEntity<Object> exportData(EmployeeWorkPlanningsRequest.SearchForm dto) throws Exception {
        return employeeWorkPlanningsService.exportData(dto);
    }

}
