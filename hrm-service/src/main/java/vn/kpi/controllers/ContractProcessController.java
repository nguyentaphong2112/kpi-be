/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.kpi.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import vn.kpi.annotations.HasPermission;
import vn.kpi.annotations.Resource;
import vn.kpi.constants.BaseConstants;
import vn.kpi.constants.Constant;
import vn.kpi.constants.Scope;
import vn.kpi.exceptions.BaseAppException;
import vn.kpi.exceptions.RecordNotExistsException;
import vn.kpi.models.BaseSearchRequest;
import vn.kpi.models.request.ContractProcessRequest;
import vn.kpi.models.request.EmployeesRequest;
import vn.kpi.models.response.BaseResponseEntity;
import vn.kpi.models.response.ContractProcessResponse;
import vn.kpi.models.response.TableResponseEntity;
import vn.kpi.services.ContractProcessService;
import vn.kpi.services.EmployeesService;
import vn.kpi.utils.ResponseUtils;
import vn.kpi.utils.Utils;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping(BaseConstants.REQUEST_MAPPING_PREFIX)
@RequiredArgsConstructor
@Resource(value = Constant.RESOURCES.CONTRACT_PROCESS)
public class ContractProcessController {
    private final ContractProcessService contractProcessService;
    private final EmployeesService employeesService;

    @GetMapping(value = "/v1/contract-process", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public TableResponseEntity<ContractProcessResponse.SearchResult> searchData(EmployeesRequest.SearchForm dto) {
        return contractProcessService.searchData(dto);
    }

    @GetMapping(value = "/v1/contract-process/export", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public ResponseEntity<Object> exportData(EmployeesRequest.SearchForm dto) throws Exception {
        return contractProcessService.exportData(dto);
    }

    @PostMapping(value = "/v1/contract-process/{employeeId}", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.CREATE, domainId = "employeeId")
    public BaseResponseEntity<Long> saveData(@Valid @RequestPart(value = "data") ContractProcessRequest.SubmitForm dto,
                                             @PathVariable Long employeeId,
                                             @RequestPart(value = "files", required = false) List<MultipartFile> files) throws BaseAppException {
        return contractProcessService.saveData(dto, files, employeeId, null);
    }

    @PutMapping(value = "/v1/contract-process/{employeeId}/{id}", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.UPDATE, domainId = "employeeId")
    public BaseResponseEntity<Long> saveData(@Valid @RequestPart(value = "data") ContractProcessRequest.SubmitForm dto,
                                             @RequestPart(value = "files", required = false) List<MultipartFile> files,
                                             @PathVariable Long employeeId,
                                             @PathVariable Long id) throws BaseAppException {
        return contractProcessService.saveData(dto, files, employeeId, id);
    }

    @DeleteMapping(value = "/v1/contract-process/{employeeId}/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.DELETE, domainId = "employeeId")
    public BaseResponseEntity<Long> deleteData(@PathVariable Long id, @PathVariable Long employeeId) throws RecordNotExistsException {
        return contractProcessService.deleteData(employeeId, id);
    }

    @GetMapping(value = "/v1/contract-process/{employeeId}/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW, domainId = "employeeId")
    public BaseResponseEntity<ContractProcessResponse.DetailBean> getDataById(@PathVariable Long id, @PathVariable Long employeeId) throws RecordNotExistsException {
        return contractProcessService.getDataById(employeeId, id);
    }

    @GetMapping(value = "/v1/contract-process/pageable/{employeeId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW, domainId = "employeeId")
    public TableResponseEntity<ContractProcessResponse.SearchResult> getTableList(@PathVariable Long employeeId, BaseSearchRequest request) throws RecordNotExistsException {
        return ResponseUtils.ok(contractProcessService.getTableList(employeeId, request));
    }

    @PostMapping(value = "/v1/contract-process/personal", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.CREATE, resource = Constant.RESOURCES.PERSONAL_CONTRACT_PROCESS)
    public BaseResponseEntity<Long> saveData(@Valid @RequestPart(value = "data") ContractProcessRequest.SubmitForm dto,
                                             @RequestPart(value = "files", required = false) List<MultipartFile> files) throws BaseAppException {
        Long employeeId = employeesService.getEmployeeId(Utils.getUserEmpCode());
        return contractProcessService.saveData(dto, files, employeeId, null);
    }

    @PutMapping(value = "/v1/contract-process/personal/{id}", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.UPDATE, resource = Constant.RESOURCES.PERSONAL_CONTRACT_PROCESS)
    public BaseResponseEntity<Long> saveData(@Valid @RequestPart(value = "data") ContractProcessRequest.SubmitForm dto,
                                             @RequestPart(value = "files", required = false) List<MultipartFile> files,
                                             @PathVariable Long id) throws BaseAppException {
        Long employeeId = employeesService.getEmployeeId(Utils.getUserEmpCode());
        return contractProcessService.saveData(dto, files, employeeId, id);
    }

    @DeleteMapping(value = "/v1/contract-process/personal/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.DELETE, resource = Constant.RESOURCES.PERSONAL_CONTRACT_PROCESS)
    public BaseResponseEntity<Long> deleteData(@PathVariable Long id) throws RecordNotExistsException {
        Long employeeId = employeesService.getEmployeeId(Utils.getUserEmpCode());
        return contractProcessService.deleteData(employeeId, id);
    }

    @GetMapping(value = "/v1/contract-process/personal/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW, resource = Constant.RESOURCES.PERSONAL_CONTRACT_PROCESS)
    public BaseResponseEntity<ContractProcessResponse.DetailBean> getDataById(@PathVariable Long id) throws RecordNotExistsException {
        Long employeeId = employeesService.getEmployeeId(Utils.getUserEmpCode());
        return contractProcessService.getDataById(employeeId, id);
    }

    @GetMapping(value = "/v1/contract-process/pageable/personal", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW, resource = Constant.RESOURCES.PERSONAL_CONTRACT_PROCESS)
    public TableResponseEntity<ContractProcessResponse.SearchResult> getTableList(BaseSearchRequest request) throws RecordNotExistsException {
        Long employeeId = employeesService.getEmployeeId(Utils.getUserEmpCode());
        return ResponseUtils.ok(contractProcessService.getTableList(employeeId, request));
    }
}
