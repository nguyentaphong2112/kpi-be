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
import vn.hbtplus.annotations.Resource;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.constants.Constant;
import vn.hbtplus.constants.Scope;
import vn.hbtplus.exceptions.BaseAppException;
import vn.hbtplus.models.response.BaseResponseEntity;
import vn.hbtplus.models.response.EmployeeRequestsResponse;
import vn.hbtplus.services.EmployeeRequestsService;
import vn.hbtplus.services.EmployeesService;
import vn.hbtplus.utils.ResponseUtils;
import vn.hbtplus.utils.Utils;

@RestController
@RequestMapping(BaseConstants.REQUEST_MAPPING_PREFIX)
@RequiredArgsConstructor
public class EmployeeRequestsController {
    private final EmployeeRequestsService employeeRequestsService;
    private final EmployeesService employeesService;

    @GetMapping(value = "/v1/employee-requests/{requestType}/active/{employeeId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public BaseResponseEntity<EmployeeRequestsResponse> getActiveRequest(
            @PathVariable String requestType,
            @PathVariable Long employeeId
    ) {
        return ResponseUtils.ok(employeeRequestsService.getActiveRequest(requestType.toUpperCase(), employeeId));
    }

    @GetMapping(value = "/v1/employee-requests/{requestType}/active/personal", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public BaseResponseEntity<EmployeeRequestsResponse> getActiveRequest(
            @PathVariable String requestType
    ) {
        Long employeeId = employeesService.getEmployeeId(Utils.getUserEmpCode());
        return ResponseUtils.ok(employeeRequestsService.getActiveRequest(requestType.toUpperCase(), employeeId));
    }

    @PutMapping(value = "/v1/employee-requests/{requestType}/next-status/{employeeId}/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public ResponseEntity updateStatus(
            @PathVariable String requestType,
            @PathVariable Long employeeId,
            @PathVariable Long id
    ) throws BaseAppException {
        return ResponseUtils.ok(employeeRequestsService.updateStatus(requestType.toUpperCase(), employeeId, id));
    }

    @PutMapping(value = "/v1/employee-requests/{requestType}/next-status/personal/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public ResponseEntity updateStatus(
            @PathVariable String requestType,
            @PathVariable Long id
    ) throws BaseAppException {
        Long employeeId = employeesService.getEmployeeId(Utils.getUserEmpCode());
        return ResponseUtils.ok(employeeRequestsService.updateStatus(requestType.toUpperCase(), employeeId, id));
    }
}
