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
import vn.hbtplus.models.BaseSearchRequest;
import vn.hbtplus.models.request.BankAccountsRequest;
import vn.hbtplus.models.request.EmployeesRequest;
import vn.hbtplus.models.response.BankAccountsResponse;
import vn.hbtplus.models.response.BaseResponseEntity;
import vn.hbtplus.models.response.TableResponseEntity;
import vn.hbtplus.services.BankAccountsService;
import vn.hbtplus.services.EmployeesService;
import vn.hbtplus.utils.ResponseUtils;
import vn.hbtplus.utils.Utils;

import javax.validation.Valid;

@RestController
@RequestMapping(BaseConstants.REQUEST_MAPPING_PREFIX)
@RequiredArgsConstructor
@Resource(value = Constant.RESOURCES.BANK_ACCOUNTS)
public class BankAccountsController {
    private final BankAccountsService bankAccountsService;
    private final EmployeesService employeesService;

    @GetMapping(value = "/v1/bank-accounts", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public TableResponseEntity<BankAccountsResponse.SearchResult> searchData(EmployeesRequest.SearchForm dto) {
        return bankAccountsService.searchData(dto);
    }

    @GetMapping(value = "/v1/bank-accounts/export", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public ResponseEntity<Object> exportData(EmployeesRequest.SearchForm dto) throws Exception {
        return bankAccountsService.exportData(dto);
    }

    @PostMapping(value = "/v1/bank-accounts/{employeeId}", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.UPDATE, domainId = "employeeId")
    public BaseResponseEntity<Long> saveData(@PathVariable Long employeeId, @RequestBody @Valid BankAccountsRequest.SubmitForm dto) throws BaseAppException {
        return bankAccountsService.saveData(dto, employeeId, null);
    }

    @PutMapping(value = "/v1/bank-accounts/{employeeId}/{id}", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.UPDATE, domainId = "employeeId")
    public BaseResponseEntity<Long> updateData(@PathVariable Long employeeId, @PathVariable Long id, @RequestBody @Valid BankAccountsRequest.SubmitForm dto) throws BaseAppException {
        return bankAccountsService.saveData(dto, employeeId, id);
    }

    @DeleteMapping(value = "/v1/bank-accounts/{employeeId}/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.DELETE, domainId = "employeeId")
    public BaseResponseEntity<Long> deleteData(@PathVariable Long employeeId, @PathVariable Long id) throws BaseAppException {
        return bankAccountsService.deleteData(employeeId, id);
    }

    @GetMapping(value = "/v1/bank-accounts/{employeeId}/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW, domainId = "employeeId")
    public BaseResponseEntity<BankAccountsResponse.DetailBean> getDataById(@PathVariable Long employeeId, @PathVariable Long id) throws BaseAppException {
        return bankAccountsService.getDataById(employeeId, id);
    }


    @GetMapping(value = "/v1/bank-accounts/pageable/{employeeId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW, domainId = "employeeId")
    public TableResponseEntity<BankAccountsResponse.SearchResult> getBankAccounts(@PathVariable Long employeeId, BaseSearchRequest request) {
        return ResponseUtils.ok(bankAccountsService.getBankAccounts(employeeId, request));
    }

    //thong tin ca nhan
    @PostMapping(value = "/v1/bank-accounts/personal", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.UPDATE, resource = Constant.RESOURCES.PERSONAL_BANK_ACCOUNTS)
    public BaseResponseEntity<Long> saveData(@RequestBody @Valid BankAccountsRequest.SubmitForm dto) throws BaseAppException {
        Long employeeId = employeesService.getEmployeeId(Utils.getUserEmpCode());
        return bankAccountsService.saveData(dto, employeeId, null);
    }

    @PutMapping(value = "/v1/bank-accounts/personal/{id}", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.UPDATE, resource = Constant.RESOURCES.PERSONAL_BANK_ACCOUNTS)
    public BaseResponseEntity<Long> updateData(@PathVariable Long id, @RequestBody @Valid BankAccountsRequest.SubmitForm dto) throws BaseAppException {
        Long employeeId = employeesService.getEmployeeId(Utils.getUserEmpCode());
        return bankAccountsService.saveData(dto, employeeId, id);
    }

    @DeleteMapping(value = "/v1/bank-accounts/personal/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.DELETE, resource = Constant.RESOURCES.PERSONAL_BANK_ACCOUNTS)
    public BaseResponseEntity<Long> deleteData(@PathVariable Long id) throws BaseAppException {
        Long employeeId = employeesService.getEmployeeId(Utils.getUserEmpCode());
        return bankAccountsService.deleteData(employeeId, id);
    }

    @GetMapping(value = "/v1/bank-accounts/personal/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW, resource = Constant.RESOURCES.PERSONAL_BANK_ACCOUNTS)
    public BaseResponseEntity<BankAccountsResponse.DetailBean> getDataById(@PathVariable Long id) throws BaseAppException {
        Long employeeId = employeesService.getEmployeeId(Utils.getUserEmpCode());
        return bankAccountsService.getDataById(employeeId, id);
    }


    @GetMapping(value = "/v1/bank-accounts/pageable/personal", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW, resource = Constant.RESOURCES.PERSONAL_BANK_ACCOUNTS)
    public TableResponseEntity<BankAccountsResponse.SearchResult> getBankAccounts(BaseSearchRequest request) {
        Long employeeId = employeesService.getEmployeeId(Utils.getUserEmpCode());
        return ResponseUtils.ok(bankAccountsService.getBankAccounts(employeeId, request));
    }

    @PostMapping(value = "/v1/bank-accounts/import", produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @HasPermission(scope = Scope.IMPORT)
    public ResponseEntity<Object> processImport(@RequestPart MultipartFile file, @RequestParam(required = false) boolean isForceUpdate) throws Exception {
        return bankAccountsService.processImport(file, isForceUpdate);
    }

    @GetMapping(value = "/v1/bank-accounts/import-template", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public ResponseEntity<Object> downloadImportTemplate() throws Exception {
        return bankAccountsService.downloadImportTemplate();
    }

}
