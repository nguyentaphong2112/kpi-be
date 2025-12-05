/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.tax.income.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.hbtplus.annotations.HasPermission;
import vn.hbtplus.annotations.Resource;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.tax.income.constants.Constant;
import vn.hbtplus.constants.Scope;
import vn.hbtplus.exceptions.BaseAppException;
import vn.hbtplus.exceptions.RecordNotExistsException;
import vn.hbtplus.tax.income.models.request.IncomeItemsRequest;
import vn.hbtplus.models.response.BaseResponseEntity;
import vn.hbtplus.tax.income.models.response.IncomeItemsResponse;
import vn.hbtplus.models.response.ListResponseEntity;
import vn.hbtplus.models.response.TableResponseEntity;
import vn.hbtplus.tax.income.services.IncomeItemsService;
import vn.hbtplus.utils.ResponseUtils;

import javax.validation.Valid;

@RestController
@RequestMapping(Constant.REQUEST_MAPPING_PREFIX)
@RequiredArgsConstructor
@Resource(value = Constant.RESOURCE.PIT_INCOME_ITEMS)
public class IncomeItemsController {
    private final IncomeItemsService incomeItemsService;

    @GetMapping(value = "/v1/income-items", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public TableResponseEntity<IncomeItemsResponse> searchData(IncomeItemsRequest.SearchForm dto) {
        return ResponseUtils.ok(incomeItemsService.searchData(dto));
    }

    @PostMapping(value = "/v1/income-items", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.UPDATE)
    public BaseResponseEntity<Long> saveData(@RequestBody @Valid IncomeItemsRequest.SubmitForm dto) throws RecordNotExistsException {
        return ResponseUtils.ok(incomeItemsService.saveData(dto, null));
    }

    @PutMapping(value = "/v1/income-items/{id}", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.UPDATE)
    public BaseResponseEntity<Long> updateData(@RequestBody @Valid IncomeItemsRequest.SubmitForm dto, @PathVariable Long id) throws RecordNotExistsException {
        return ResponseUtils.ok(incomeItemsService.saveData(dto, id));
    }

    @DeleteMapping(value = "/v1/income-items/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.DELETE)
    public ResponseEntity deleteData(@PathVariable Long id) throws BaseAppException {
        return incomeItemsService.deleteData(id);
    }

    @GetMapping(value = "/v1/income-items/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public BaseResponseEntity<IncomeItemsResponse> getDataById(@PathVariable Long id)  throws RecordNotExistsException {
        return incomeItemsService.getDataById(id);
    }

    @GetMapping(value = "/v1/income-items/export", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public ResponseEntity<Object> exportData(IncomeItemsRequest.SearchForm dto) throws Exception {
        return incomeItemsService.exportData(dto);
    }

    @GetMapping(value = "/v1/income-items/download-template/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public ResponseEntity<Object> downloadTemplate(@PathVariable Long id) throws Exception {
        return incomeItemsService.downloadTemplate(id);
    }

    @GetMapping(value = "/v1/income-items/get-data-by-period", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public ListResponseEntity<IncomeItemsResponse> getDataBySalaryPeriod(@RequestParam String salaryPeriodDate,
                                                                         @RequestParam(required = false) String isImport) {
        return ResponseUtils.ok(incomeItemsService.getDataBySalaryPeriod(salaryPeriodDate, isImport));
    }
}
