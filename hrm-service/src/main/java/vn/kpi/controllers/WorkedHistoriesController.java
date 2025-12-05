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
import vn.kpi.models.BaseSearchRequest;
import vn.kpi.models.request.EmployeesRequest;
import vn.kpi.models.response.*;
import vn.kpi.models.request.WorkedHistoriesRequest;
import vn.kpi.services.EmployeesService;
import vn.kpi.services.WorkedHistoriesService;
import vn.kpi.constants.BaseConstants;
import vn.kpi.constants.Scope;
import vn.kpi.constants.Constant;
import vn.kpi.annotations.Resource;
import vn.kpi.exceptions.RecordNotExistsException;
import vn.kpi.exceptions.BaseAppException;
import vn.kpi.utils.ResponseUtils;
import vn.kpi.utils.Utils;

import javax.validation.Valid;

@RestController
@RequestMapping(BaseConstants.REQUEST_MAPPING_PREFIX)
@RequiredArgsConstructor
@Resource(value = Constant.RESOURCES.WORKED_HISTORIES)
public class WorkedHistoriesController {
    private final WorkedHistoriesService workedHistoriesService;
    private final EmployeesService employeesService;

    @GetMapping(value = "/v1/worked-histories", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public TableResponseEntity<WorkedHistoriesResponse.SearchResult> searchData(EmployeesRequest.SearchForm dto) {
        return workedHistoriesService.searchData(dto);
    }

    @GetMapping(value = "/v1/worked-histories/export", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public ResponseEntity<Object> exportData(EmployeesRequest.SearchForm dto) throws Exception {
        return workedHistoriesService.exportData(dto);
    }

    @PostMapping(value = "/v1/worked-histories/{employeeId}", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.CREATE, domainId = "employeeId")
    public BaseResponseEntity<Long> saveData(@PathVariable Long employeeId, @RequestBody @Valid WorkedHistoriesRequest.SubmitForm dto) throws BaseAppException {
        return workedHistoriesService.saveData(dto, employeeId, null);
    }

    @PutMapping(value = "/v1/worked-histories/{employeeId}/{id}", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.UPDATE, domainId = "employeeId")
    public BaseResponseEntity<Long> updateData(@PathVariable Long employeeId, @RequestBody @Valid WorkedHistoriesRequest.SubmitForm dto, @PathVariable Long id) throws BaseAppException {
        return workedHistoriesService.saveData(dto, employeeId, id);
    }

    @DeleteMapping(value = "/v1/worked-histories/{employeeId}/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.DELETE, domainId = "employeeId")
    public ResponseEntity deleteData(@PathVariable Long employeeId, @PathVariable Long id) throws BaseAppException {
        return workedHistoriesService.deleteData(employeeId, id);
    }

    @GetMapping(value = "/v1/worked-histories/{employeeId}/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW, domainId = "employeeId")
    public BaseResponseEntity<WorkedHistoriesResponse.DetailBean> getDataById(@PathVariable Long employeeId, @PathVariable Long id) throws BaseAppException {
        return workedHistoriesService.getDataById(employeeId, id);
    }

    @GetMapping(value = "/v1/worked-histories/pageable/{employeeId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW, domainId = "employeeId")
    public TableResponseEntity<WorkedHistoriesResponse.DetailBean> getTableList(@PathVariable Long employeeId, BaseSearchRequest request) throws RecordNotExistsException {
        return ResponseUtils.ok(workedHistoriesService.getTableList(employeeId, request));
    }

    @PostMapping(value = "/v1/worked-histories/personal", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.CREATE, resource = Constant.RESOURCES.PERSONAL_WORKED_HISTORIES)
    public BaseResponseEntity<Long> saveData(@RequestBody @Valid WorkedHistoriesRequest.SubmitForm dto) throws BaseAppException {
        Long employeeId = employeesService.getEmployeeId(Utils.getUserEmpCode());
        return workedHistoriesService.saveData(dto, employeeId, null);
    }

    @PutMapping(value = "/v1/worked-histories/personal/{id}", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.UPDATE, resource = Constant.RESOURCES.PERSONAL_WORKED_HISTORIES)
    public BaseResponseEntity<Long> updateData(@RequestBody @Valid WorkedHistoriesRequest.SubmitForm dto, @PathVariable Long id) throws BaseAppException {
        Long employeeId = employeesService.getEmployeeId(Utils.getUserEmpCode());
        return workedHistoriesService.saveData(dto, employeeId, id);
    }

    @DeleteMapping(value = "/v1/worked-histories/personal/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.DELETE, resource = Constant.RESOURCES.PERSONAL_WORKED_HISTORIES)
    public ResponseEntity deleteData(@PathVariable Long id) throws BaseAppException {
        Long employeeId = employeesService.getEmployeeId(Utils.getUserEmpCode());
        return workedHistoriesService.deleteData(employeeId, id);
    }

    @GetMapping(value = "/v1/worked-histories/personal/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW, resource = Constant.RESOURCES.PERSONAL_WORKED_HISTORIES)
    public BaseResponseEntity<WorkedHistoriesResponse.DetailBean> getDataById(@PathVariable Long id) throws BaseAppException {
        Long employeeId = employeesService.getEmployeeId(Utils.getUserEmpCode());
        return workedHistoriesService.getDataById(employeeId, id);
    }

    @GetMapping(value = "/v1/worked-histories/pageable/personal", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW, resource = Constant.RESOURCES.PERSONAL_WORKED_HISTORIES)
    public TableResponseEntity<WorkedHistoriesResponse.DetailBean> getTableList(BaseSearchRequest request) throws RecordNotExistsException {
        Long employeeId = employeesService.getEmployeeId(Utils.getUserEmpCode());
        return ResponseUtils.ok(workedHistoriesService.getTableList(employeeId, request));
    }


}
