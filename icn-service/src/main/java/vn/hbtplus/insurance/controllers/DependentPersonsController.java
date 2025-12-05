/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.insurance.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.hbtplus.annotations.HasPermission;
import vn.hbtplus.annotations.Resource;
import vn.hbtplus.insurance.constants.Constant;
import vn.hbtplus.constants.Scope;
import vn.hbtplus.insurance.models.response.DependentPersonsResponse;
import vn.hbtplus.models.response.ListResponseEntity;
import vn.hbtplus.insurance.services.DependentPersonsService;

import java.util.List;

@RestController
@RequestMapping(Constant.REQUEST_MAPPING_PREFIX)
@RequiredArgsConstructor
@Resource(value = Constant.RESOURCE.ICN_CONFIG_PARAMETER)
public class DependentPersonsController {
    private final DependentPersonsService dependentPersonsService;

    @GetMapping(value = "/v1/dependent-persons/emp/{empId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public ListResponseEntity<DependentPersonsResponse> getDataByEmpId(@PathVariable Long empId) {
        return dependentPersonsService.getDataByEmpId(empId);
    }

}
