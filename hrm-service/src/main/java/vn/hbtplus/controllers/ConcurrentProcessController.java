/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import vn.hbtplus.annotations.HasPermission;
import vn.hbtplus.annotations.Resource;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.constants.Constant;
import vn.hbtplus.constants.Scope;
import vn.hbtplus.exceptions.BaseAppException;
import vn.hbtplus.exceptions.RecordNotExistsException;
import vn.hbtplus.models.BaseSearchRequest;
import vn.hbtplus.models.request.ConcurrentProcessRequest;
import vn.hbtplus.models.request.EmployeesRequest;
import vn.hbtplus.models.response.BaseResponseEntity;
import vn.hbtplus.models.response.ConcurrentProcessResponse;
import vn.hbtplus.models.response.EducationProcessResponse;
import vn.hbtplus.models.response.TableResponseEntity;
import vn.hbtplus.services.ConcurrentProcessService;
import vn.hbtplus.services.EmployeesService;
import vn.hbtplus.utils.ResponseUtils;
import vn.hbtplus.utils.Utils;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping(BaseConstants.REQUEST_MAPPING_PREFIX)
@RequiredArgsConstructor
@Resource(value = Constant.RESOURCES.CONCURRENT_PROCESS)
public class ConcurrentProcessController {
    private final ConcurrentProcessService concurrentProcessService;
    private final EmployeesService employeesService;

    @GetMapping(value = "/v1/concurrent-process", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public TableResponseEntity<ConcurrentProcessResponse.SearchResult> searchData(EmployeesRequest.SearchForm dto) {
        return concurrentProcessService.searchData(dto);
    }

    @GetMapping(value = "/v1/concurrent-process/export", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public ResponseEntity<Object> exportData(EmployeesRequest.SearchForm dto) throws Exception {
        return concurrentProcessService.exportData(dto);
    }

    @PostMapping(value = "/v1/concurrent-process/{employeeId}", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.CREATE, domainId = "employeeId")
    public BaseResponseEntity<Long> saveData(@Valid @RequestPart(value = "data") ConcurrentProcessRequest.SubmitForm dto,
                                             @PathVariable Long employeeId,
                                             @RequestPart(value = "files", required = false) List<MultipartFile> files) throws BaseAppException {
        return concurrentProcessService.saveData(dto, files, employeeId, null);
    }

    @PutMapping(value = "/v1/concurrent-process/{employeeId}/{id}", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.UPDATE, domainId = "employeeId")
    public BaseResponseEntity<Long> updateData(@Valid @RequestPart(value = "data") ConcurrentProcessRequest.SubmitForm dto,
                                               @PathVariable Long employeeId,
                                               @RequestPart(value = "files", required = false) List<MultipartFile> files,
                                               @PathVariable Long id) throws BaseAppException {
        return concurrentProcessService.saveData(dto, files, employeeId, id);
    }

    @DeleteMapping(value = "/v1/concurrent-process/{employeeId}/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.DELETE, domainId = "employeeId")
    public BaseResponseEntity<Long> deleteData(@PathVariable Long id, @PathVariable Long employeeId) throws RecordNotExistsException {
        return concurrentProcessService.deleteData(employeeId, id);
    }

    @GetMapping(value = "/v1/concurrent-process/{employeeId}/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW, domainId = "employeeId")
    public BaseResponseEntity<ConcurrentProcessResponse.DetailBean> getDataById(@PathVariable Long id, @PathVariable Long employeeId) throws RecordNotExistsException {
        return concurrentProcessService.getDataById(employeeId, id);
    }

    @GetMapping(value = "/v1/concurrent-process/pageable/{employeeId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW, domainId = "employeeId")
    public TableResponseEntity<EducationProcessResponse.DetailBean> getTableList(@PathVariable Long employeeId, BaseSearchRequest request) throws RecordNotExistsException {
        return ResponseUtils.ok(concurrentProcessService.getTableList(employeeId, request));
    }

    //thong tin ca nhan
    @PostMapping(value = "/v1/concurrent-process/personal", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.CREATE, resource = Constant.RESOURCES.PERSONAL_CONCURRENT_PROCESS)
    public BaseResponseEntity<Long> saveData(@Valid @RequestPart(value = "data") ConcurrentProcessRequest.SubmitForm dto,
                                             @RequestPart(value = "files", required = false) List<MultipartFile> files) throws BaseAppException {
        Long employeeId = employeesService.getEmployeeId(Utils.getUserEmpCode());
        return concurrentProcessService.saveData(dto, files, employeeId, null);
    }

    @PutMapping(value = "/v1/concurrent-process/personal/{id}", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.UPDATE, resource = Constant.RESOURCES.PERSONAL_CONCURRENT_PROCESS)
    public BaseResponseEntity<Long> updateData(@Valid @RequestPart(value = "data") ConcurrentProcessRequest.SubmitForm dto,
                                               @RequestPart(value = "files", required = false) List<MultipartFile> files,
                                               @PathVariable Long id) throws BaseAppException {
        Long employeeId = employeesService.getEmployeeId(Utils.getUserEmpCode());
        return concurrentProcessService.saveData(dto, files, employeeId, id);
    }

    @DeleteMapping(value = "/v1/concurrent-process/personal/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.DELETE, resource = Constant.RESOURCES.PERSONAL_CONCURRENT_PROCESS)
    public BaseResponseEntity<Long> deleteData(@PathVariable Long id) throws RecordNotExistsException {
        Long employeeId = employeesService.getEmployeeId(Utils.getUserEmpCode());
        return concurrentProcessService.deleteData(employeeId, id);
    }

    @GetMapping(value = "/v1/concurrent-process/personal/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW, resource = Constant.RESOURCES.PERSONAL_CONCURRENT_PROCESS)
    public BaseResponseEntity<ConcurrentProcessResponse.DetailBean> getDataById(@PathVariable Long id) throws RecordNotExistsException {
        Long employeeId = employeesService.getEmployeeId(Utils.getUserEmpCode());
        return concurrentProcessService.getDataById(employeeId, id);
    }

    @GetMapping(value = "/v1/concurrent-process/pageable/personal", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW, resource = Constant.RESOURCES.PERSONAL_CONCURRENT_PROCESS)
    public TableResponseEntity<EducationProcessResponse.DetailBean> getTableList(BaseSearchRequest request) throws RecordNotExistsException {
        Long employeeId = employeesService.getEmployeeId(Utils.getUserEmpCode());
        return ResponseUtils.ok(concurrentProcessService.getTableList(employeeId, request));
    }

}
