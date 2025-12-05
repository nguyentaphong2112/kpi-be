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
import vn.hbtplus.models.request.EmployeesRequest;
import vn.hbtplus.models.request.InsuranceSalaryProcessRequest;
import vn.hbtplus.models.response.BaseResponseEntity;
import vn.hbtplus.models.response.InsuranceSalaryProcessResponse;
import vn.hbtplus.models.response.TableResponseEntity;
import vn.hbtplus.services.EmployeesService;
import vn.hbtplus.services.InsuranceSalaryProcessService;
import vn.hbtplus.utils.ResponseUtils;
import vn.hbtplus.utils.Utils;

import java.util.List;

@RestController
@RequestMapping(BaseConstants.REQUEST_MAPPING_PREFIX)
@RequiredArgsConstructor
@Resource(value = Constant.RESOURCES.INSURANCE_SALARY_PROCESS)
public class InsuranceSalaryProcessController {
    private final InsuranceSalaryProcessService insuranceSalaryProcessService;
    private final EmployeesService employeesService;

    @GetMapping(value = "/v1/insurance-salary-process", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public TableResponseEntity<InsuranceSalaryProcessResponse.SearchResult> searchData(EmployeesRequest.SearchForm dto) {
        return insuranceSalaryProcessService.searchData(dto);
    }

    @GetMapping(value = "/v1/insurance-salary-process/export", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public ResponseEntity<Object> exportData(EmployeesRequest.SearchForm dto) throws Exception {
        return insuranceSalaryProcessService.exportData(dto);
    }

    @PostMapping(value = "/v1/insurance-salary-process/{employeeId}", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.CREATE, domainId = "employeeId")
    public BaseResponseEntity<Long> saveData(@PathVariable Long employeeId, @RequestPart(value = "data") InsuranceSalaryProcessRequest.SubmitForm dto,
                                             @RequestPart(value = "files", required = false) List<MultipartFile> files) throws BaseAppException {
        return insuranceSalaryProcessService.saveData(dto, files, employeeId, null);
    }

    @PutMapping(value = "/v1/insurance-salary-process/{employeeId}/{id}", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.UPDATE, domainId = "employeeId")
    public BaseResponseEntity<Long> updateData(@PathVariable Long employeeId, @RequestPart(value = "data") InsuranceSalaryProcessRequest.SubmitForm dto,
                                               @RequestPart(value = "files", required = false) List<MultipartFile> files,
                                               @PathVariable Long id) throws BaseAppException {
        return insuranceSalaryProcessService.saveData(dto, files, employeeId, id);
    }

    @DeleteMapping(value = "/v1/insurance-salary-process/{employeeId}/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.DELETE, domainId = "employeeId")
    public BaseResponseEntity<Long> deleteData(@PathVariable Long employeeId, @PathVariable Long id) throws BaseAppException {
        return insuranceSalaryProcessService.deleteData(employeeId, id);
    }

    @GetMapping(value = "/v1/insurance-salary-process/{employeeId}/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW, domainId = "employeeId")
    public BaseResponseEntity<InsuranceSalaryProcessResponse.DetailBean> getDataById(@PathVariable Long employeeId, @PathVariable Long id) throws BaseAppException {
        return insuranceSalaryProcessService.getDataById(employeeId, id);
    }

    @GetMapping(value = "/v1/insurance-salary-process/pageable/{employeeId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW, domainId = "employeeId")
    public TableResponseEntity<InsuranceSalaryProcessResponse.SearchResult> getTableList(@PathVariable Long employeeId, BaseSearchRequest request) throws RecordNotExistsException {
        return ResponseUtils.ok(insuranceSalaryProcessService.getTableList(employeeId, request));
    }


    @PostMapping(value = "/v1/insurance-salary-process/personal", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.CREATE, resource = Constant.RESOURCES.PERSONAL_INSURANCE_SALARY_PROCESS)
    public BaseResponseEntity<Long> saveData(@RequestPart(value = "data") InsuranceSalaryProcessRequest.SubmitForm dto,
                                             @RequestPart(value = "files", required = false) List<MultipartFile> files) throws BaseAppException {
        Long employeeId = employeesService.getEmployeeId(Utils.getUserEmpCode());
        return insuranceSalaryProcessService.saveData(dto, files, employeeId, null);
    }

    @PutMapping(value = "/v1/insurance-salary-process/personal/{id}", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.UPDATE, resource = Constant.RESOURCES.PERSONAL_INSURANCE_SALARY_PROCESS)
    public BaseResponseEntity<Long> updateData(@RequestPart(value = "data") InsuranceSalaryProcessRequest.SubmitForm dto,
                                               @RequestPart(value = "files", required = false) List<MultipartFile> files,
                                               @PathVariable Long id) throws BaseAppException {
        Long employeeId = employeesService.getEmployeeId(Utils.getUserEmpCode());
        return insuranceSalaryProcessService.saveData(dto, files, employeeId, id);
    }

    @DeleteMapping(value = "/v1/insurance-salary-process/personal/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.DELETE, resource = Constant.RESOURCES.PERSONAL_INSURANCE_SALARY_PROCESS)
    public BaseResponseEntity<Long> deleteData(@PathVariable Long id) throws BaseAppException {
        Long employeeId = employeesService.getEmployeeId(Utils.getUserEmpCode());
        return insuranceSalaryProcessService.deleteData(employeeId, id);
    }

    @GetMapping(value = "/v1/insurance-salary-process/personal/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW, resource = Constant.RESOURCES.PERSONAL_INSURANCE_SALARY_PROCESS)
    public BaseResponseEntity<InsuranceSalaryProcessResponse.DetailBean> getDataById(@PathVariable Long id) throws BaseAppException {
        Long employeeId = employeesService.getEmployeeId(Utils.getUserEmpCode());
        return insuranceSalaryProcessService.getDataById(employeeId, id);
    }

    @GetMapping(value = "/v1/insurance-salary-process/pageable/personal", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW, resource = Constant.RESOURCES.PERSONAL_INSURANCE_SALARY_PROCESS)
    public TableResponseEntity<InsuranceSalaryProcessResponse.SearchResult> getTableList(BaseSearchRequest request) throws RecordNotExistsException {
        Long employeeId = employeesService.getEmployeeId(Utils.getUserEmpCode());
        return ResponseUtils.ok(insuranceSalaryProcessService.getTableList(employeeId, request));
    }

    @PostMapping(value = "/v1/insurance-salary-process/import", produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @HasPermission(scope = Scope.IMPORT, resource = Constant.RESOURCES.INSURANCE_SALARY_PROCESS)
    public ResponseEntity<Object> processImport(@RequestPart MultipartFile file, @RequestParam(required = false) boolean isForceUpdate) throws Exception {
        return insuranceSalaryProcessService.processImport(file, isForceUpdate);
    }

    @GetMapping(value = "/v1/insurance-salary-process/import-template", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW, resource = Constant.RESOURCES.INSURANCE_SALARY_PROCESS)
    public ResponseEntity<Object> downloadImportTemplate() throws Exception {
        return insuranceSalaryProcessService.downloadImportTemplate();
    }
}
