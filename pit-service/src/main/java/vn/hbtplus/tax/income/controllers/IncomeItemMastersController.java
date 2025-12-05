/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.tax.income.controllers;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.hbtplus.annotations.HasPermission;
import vn.hbtplus.annotations.Resource;
import vn.hbtplus.constants.Scope;
import vn.hbtplus.exceptions.BaseAppException;
import vn.hbtplus.exceptions.RecordNotExistsException;
import vn.hbtplus.models.response.BaseResponseEntity;
import vn.hbtplus.models.response.TableResponseEntity;
import vn.hbtplus.tax.income.constants.Constant;
import vn.hbtplus.tax.income.models.request.IncomeItemMastersRequest;
import vn.hbtplus.tax.income.models.response.IncomeItemMastersResponse;
import vn.hbtplus.tax.income.services.IncomeItemMastersService;
import vn.hbtplus.utils.ResponseUtils;
import vn.hbtplus.utils.Utils;

import javax.validation.Valid;

@RestController
@RequestMapping(Constant.REQUEST_MAPPING_PREFIX)
@RequiredArgsConstructor
@Resource(value = Constant.RESOURCE.PIT_INCOME_ITEM_MASTERS)
public class IncomeItemMastersController {
    private final IncomeItemMastersService incomeItemMastersService;

    @GetMapping(value = "/v1/income-item-masters", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public TableResponseEntity<IncomeItemMastersResponse> searchData(IncomeItemMastersRequest.SearchForm dto) {
        return ResponseUtils.ok(incomeItemMastersService.searchData(dto));
    }

    @PostMapping(value = "/v1/income-item-masters", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.UPDATE)
    public ResponseEntity saveData(@Valid IncomeItemMastersRequest.SubmitForm dto) throws BaseAppException {
        return incomeItemMastersService.saveData(dto);
    }

    @DeleteMapping(value = "/v1/income-item-masters/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.DELETE)
    public ResponseEntity deleteData(@PathVariable Long id) throws BaseAppException {
        return incomeItemMastersService.deleteData(id);
    }

    @GetMapping(value = "/v1/income-item-masters/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public BaseResponseEntity<IncomeItemMastersResponse> getDataById(@PathVariable Long id) throws RecordNotExistsException {
        return incomeItemMastersService.getDataById(id);
    }

    @GetMapping(value = "/v1/income-item-masters/export", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public ResponseEntity<Object> exportData(IncomeItemMastersRequest.SearchForm dto) throws Exception {
        return incomeItemMastersService.exportData(dto);
    }

    @PostMapping(value = "/v1/income-item-masters/import", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.IMPORT)
    public BaseResponseEntity<Long> importIncome(@ModelAttribute IncomeItemMastersRequest.ImportForm form) throws Exception {
        Long id = incomeItemMastersService.importIncome(form.getFile(), form.getIncomeItemId(), Utils.getLastDay(form.getTaxPeriodDate()),
                StringUtils.isBlank(form.getIsCalculated()) ? "Y" : form.getIsCalculated());
        return ResponseUtils.ok(id);
    }

    @PutMapping(value = "/v1/income-item-masters/tax-calculate/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.IMPORT)
    public ResponseEntity<Object> calculateTax(@PathVariable Long id) throws Exception {
        incomeItemMastersService.calculateTax(id);
        return ResponseUtils.ok();
    }
    @PutMapping(value = "/v1/income-item-masters/undo-calculate/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.IMPORT)
    public ResponseEntity<Object> undoCalculateTax(@PathVariable Long id) throws Exception {
        incomeItemMastersService.undoCalculateTax(id);
        return ResponseUtils.ok();
    }
    @PutMapping(value = "/v1/income-item-masters/lock/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.LOCK)
    public ResponseEntity<Object> lockPeriodById(@PathVariable Long id) throws Exception {
        incomeItemMastersService.lockPeriodById(id);
        return ResponseUtils.ok();
    }

    @PutMapping(value = "/v1/income-item-masters/unlock/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.LOCK)
    public ResponseEntity<Object> unLockPeriodById(@PathVariable Long id) throws Exception {
        incomeItemMastersService.unLockPeriodById(id);
        return ResponseUtils.ok();
    }

    @GetMapping(value = "/v1/income-item-masters/download/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public ResponseEntity<Object> exportDetailIncomeById(@PathVariable Long id, @RequestParam Integer isPreview) throws Exception {
        return incomeItemMastersService.exportDetailIncomeById(id, isPreview);
    }
}
