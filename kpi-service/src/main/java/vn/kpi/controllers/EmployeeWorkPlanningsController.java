/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.kpi.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.kpi.annotations.HasPermission;
import vn.kpi.constants.BaseConstants;
import vn.kpi.constants.Scope;
import vn.kpi.exceptions.BaseAppException;
import vn.kpi.exceptions.RecordNotExistsException;
import vn.kpi.models.request.EmployeeWorkPlanningsRequest;
import vn.kpi.models.response.BaseResponseEntity;
import vn.kpi.models.response.EmployeeWorkPlanningsResponse;
import vn.kpi.models.response.TableResponseEntity;
import vn.kpi.services.EmployeeWorkPlanningsService;

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
