/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.insurance.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.hbtplus.annotations.HasPermission;
import vn.hbtplus.annotations.Resource;
import vn.hbtplus.insurance.constants.Constant;
import vn.hbtplus.constants.Scope;
import vn.hbtplus.insurance.models.response.EmployeesResponse;
import vn.hbtplus.models.response.ListResponseEntity;
import vn.hbtplus.insurance.services.EmployeesService;

@RestController
@RequestMapping(Constant.REQUEST_MAPPING_PREFIX)
@RequiredArgsConstructor
@Resource(value = Constant.RESOURCE.ICN_CONFIG_PARAMETER)
public class EmployeesController {
    private final EmployeesService employeesService;

    @GetMapping(value = "/v1/employees/{empId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public ListResponseEntity<EmployeesResponse> getDataByEmpId(@PathVariable Long empId) {
        return employeesService.getDataByEmpId(empId);
    }

}
