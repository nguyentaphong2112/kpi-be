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
import vn.hbtplus.exceptions.BaseAppException;
import vn.hbtplus.exceptions.RecordNotExistsException;
import vn.hbtplus.models.BaseApproveRequest;
import vn.hbtplus.insurance.models.request.EmployeeChangesRequest;
import vn.hbtplus.models.response.BaseResponseEntity;
import vn.hbtplus.insurance.models.response.EmployeeChangesResponse;
import vn.hbtplus.models.response.ListResponseEntity;
import vn.hbtplus.models.response.TableResponseEntity;
import vn.hbtplus.insurance.repositories.entity.EmployeeChangesEntity;
import vn.hbtplus.insurance.services.EmployeeChangesService;
import vn.hbtplus.utils.ResponseUtils;

import javax.validation.Valid;

@RestController
@RequestMapping(Constant.REQUEST_MAPPING_PREFIX)
@RequiredArgsConstructor
@Resource(value = Constant.RESOURCE.ICN_EMPLOYEE_CHANGES)
public class EmployeeChangesController {
    private final EmployeeChangesService employeeChangesService;

    @GetMapping(value = "/v1/employee-changes", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public TableResponseEntity<EmployeeChangesResponse> searchData(EmployeeChangesRequest.SearchForm dto) {
        return employeeChangesService.searchData(dto);
    }

    @PostMapping(value = "/v1/employee-changes", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.UPDATE)
    public ResponseEntity saveData(@Valid @RequestBody EmployeeChangesRequest.SubmitForm dto) throws BaseAppException {
        return employeeChangesService.saveData(dto);
    }

    @PostMapping(value = "/v1/employee-changes/make-list", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.UPDATE)
    public ResponseEntity makeList(@Valid EmployeeChangesRequest.MakeListForm dto) throws BaseAppException {
        return ResponseUtils.ok(employeeChangesService.makeList(dto));
    }

    @DeleteMapping(value = "/v1/employee-changes/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.DELETE)
    public ResponseEntity deleteData(@PathVariable Long id) throws RecordNotExistsException {
        return employeeChangesService.deleteData(id);
    }

    @GetMapping(value = "/v1/employee-changes/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public BaseResponseEntity<EmployeeChangesResponse> getDataById(@PathVariable Long id)  throws RecordNotExistsException {
        return employeeChangesService.getDataById(id);
    }

    @GetMapping(value = "/v1/employee-changes/export", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public ResponseEntity<Object> exportData(EmployeeChangesRequest.SearchForm dto) throws Exception {
        return employeeChangesService.exportData(dto);
    }

    @PutMapping(value = "/v1/employee-changes/approve-by-id", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.APPROVE)
    public ListResponseEntity<Long> approveByList(@RequestBody BaseApproveRequest dto) throws BaseAppException {
        return ResponseUtils.ok(employeeChangesService.updateStatusById(dto, EmployeeChangesEntity.STATUS.PHE_DUYET));
    }
    @PutMapping(value = "/v1/employee-changes/undo-approve-by-id", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.APPROVE)
    public ListResponseEntity<Long> unApproveById(@RequestBody BaseApproveRequest dto) throws BaseAppException {
        return ResponseUtils.ok(employeeChangesService.updateStatusById(dto, EmployeeChangesEntity.STATUS.CHO_PHE_DUYET));
    }

    @PutMapping(value = "/v1/employee-changes/approve-all", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.APPROVE)
    public ListResponseEntity<Long> approveAll(EmployeeChangesRequest.SearchForm dto) throws BaseAppException {
        return ResponseUtils.ok(employeeChangesService.updateStatus(dto, EmployeeChangesEntity.STATUS.PHE_DUYET));
    }

    @PutMapping(value = "/v1/employee-changes/undo-approve-all", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.APPROVE)
    public ListResponseEntity<Long> undoApproveAll(EmployeeChangesRequest.SearchForm dto) throws BaseAppException {
        return ResponseUtils.ok(employeeChangesService.updateStatus(dto, EmployeeChangesEntity.STATUS.CHO_PHE_DUYET));
    }

}
