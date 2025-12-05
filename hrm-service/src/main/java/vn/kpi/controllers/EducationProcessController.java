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
import vn.kpi.models.request.EducationProcessRequest;
import vn.kpi.services.EducationProcessService;
import vn.kpi.constants.BaseConstants;
import vn.kpi.constants.Scope;
import vn.kpi.constants.Constant;
import vn.kpi.annotations.Resource;
import vn.kpi.exceptions.RecordNotExistsException;
import vn.kpi.exceptions.BaseAppException;
import vn.kpi.services.EmployeesService;
import vn.kpi.utils.ResponseUtils;
import vn.kpi.utils.Utils;

import javax.validation.Valid;

@RestController
@RequestMapping(BaseConstants.REQUEST_MAPPING_PREFIX)
@RequiredArgsConstructor
@Resource(value = Constant.RESOURCES.EDUCATION_PROCESS)
public class EducationProcessController {
    private final EducationProcessService educationProcessService;
    private final EmployeesService employeesService;

    @GetMapping(value = "/v1/education-process", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public TableResponseEntity<EducationProcessResponse.SearchResult> searchData(EmployeesRequest.SearchForm dto) {
        return educationProcessService.searchData(dto);
    }

    @GetMapping(value = "/v1/education-process/export", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public ResponseEntity<Object> exportData(EmployeesRequest.SearchForm dto) throws Exception {
        return educationProcessService.exportData(dto);
    }

    @PostMapping(value = "/v1/education-process/{employeeId}", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.CREATE, domainId = "employeeId")
    public BaseResponseEntity<Long> saveData(@RequestBody @Valid EducationProcessRequest.SubmitForm dto, @PathVariable Long employeeId) throws BaseAppException {
        return educationProcessService.saveData(dto, employeeId, null);
    }

    @PutMapping(value = "/v1/education-process/{employeeId}/{id}", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.UPDATE, domainId = "employeeId")
    public BaseResponseEntity<Long> updateData(@RequestBody @Valid EducationProcessRequest.SubmitForm dto, @PathVariable Long employeeId, @PathVariable Long id) throws BaseAppException {
        return educationProcessService.saveData(dto, employeeId, id);
    }

    @DeleteMapping(value = "/v1/education-process/{employeeId}/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.DELETE, domainId = "employeeId")
    public BaseResponseEntity<Long> deleteData(@PathVariable Long employeeId, @PathVariable Long id) throws BaseAppException {
        return educationProcessService.deleteData(employeeId, id);
    }

    @GetMapping(value = "/v1/education-process/{employeeId}/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW, domainId = "employeeId")
    public BaseResponseEntity<EducationProcessResponse.DetailBean> getDataById(@PathVariable Long employeeId, @PathVariable Long id) throws BaseAppException {
        return educationProcessService.getDataById(employeeId, id);
    }

    @GetMapping(value = "/v1/education-process/pageable/{employeeId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW, domainId = "employeeId")
    public TableResponseEntity<EducationProcessResponse.DetailBean> getTableList(@PathVariable Long employeeId, BaseSearchRequest request) throws RecordNotExistsException {
        return ResponseUtils.ok(educationProcessService.getTableList(employeeId, request));
    }

    //thong tin ca nhan
    @PostMapping(value = "/v1/education-process/personal", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.CREATE, resource = Constant.RESOURCES.PERSONAL_EDUCATION_PROCESS)
    public BaseResponseEntity<Long> saveData(@RequestBody @Valid EducationProcessRequest.SubmitForm dto) throws BaseAppException {
        Long employeeId = employeesService.getEmployeeId(Utils.getUserEmpCode());
        return educationProcessService.saveData(dto, employeeId, null);
    }

    @PutMapping(value = "/v1/education-process/personal/{id}", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.UPDATE, resource = Constant.RESOURCES.PERSONAL_EDUCATION_PROCESS)
    public BaseResponseEntity<Long> updateData(@RequestBody @Valid EducationProcessRequest.SubmitForm dto, @PathVariable Long id) throws BaseAppException {
        Long employeeId = employeesService.getEmployeeId(Utils.getUserEmpCode());
        return educationProcessService.saveData(dto, employeeId, id);
    }

    @DeleteMapping(value = "/v1/education-process/personal/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.DELETE, resource = Constant.RESOURCES.PERSONAL_EDUCATION_PROCESS)
    public BaseResponseEntity<Long> deleteData(@PathVariable Long id) throws BaseAppException {
        Long employeeId = employeesService.getEmployeeId(Utils.getUserEmpCode());
        return educationProcessService.deleteData(employeeId, id);
    }

    @GetMapping(value = "/v1/education-process/personal/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW, resource = Constant.RESOURCES.PERSONAL_EDUCATION_PROCESS)
    public BaseResponseEntity<EducationProcessResponse.DetailBean> getDataById(@PathVariable Long id) throws BaseAppException {
        Long employeeId = employeesService.getEmployeeId(Utils.getUserEmpCode());
        return educationProcessService.getDataById(employeeId, id);
    }

    @GetMapping(value = "/v1/education-process/pageable/personal", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW, resource = Constant.RESOURCES.PERSONAL_EDUCATION_PROCESS)
    public TableResponseEntity<EducationProcessResponse.DetailBean> getTableList(BaseSearchRequest request) throws RecordNotExistsException {
        Long employeeId = employeesService.getEmployeeId(Utils.getUserEmpCode());
        return ResponseUtils.ok(educationProcessService.getTableList(employeeId, request));
    }
}
