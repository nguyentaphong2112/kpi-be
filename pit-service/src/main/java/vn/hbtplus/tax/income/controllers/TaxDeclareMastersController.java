/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.tax.income.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import vn.hbtplus.annotations.HasPermission;
import vn.hbtplus.annotations.Resource;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.tax.income.constants.Constant;
import vn.hbtplus.constants.Scope;
import vn.hbtplus.exceptions.BaseAppException;
import vn.hbtplus.exceptions.RecordNotExistsException;
import vn.hbtplus.models.ValidateResponseDto;
import vn.hbtplus.tax.income.models.request.TaxDeclareMastersRequest;
import vn.hbtplus.models.response.BaseResponseEntity;
import vn.hbtplus.models.response.TableResponseEntity;
import vn.hbtplus.tax.income.models.response.TaxDeclareMastersResponse;
import vn.hbtplus.tax.income.services.TaxDeclareMastersService;
import vn.hbtplus.utils.ResponseUtils;
import vn.hbtplus.utils.Utils;

import javax.validation.Valid;
import java.util.Date;

@RestController
@RequestMapping(Constant.REQUEST_MAPPING_PREFIX)
@RequiredArgsConstructor
@Resource(value = Constant.RESOURCE.PIT_TAX_DECLARE_MASTERS)
public class TaxDeclareMastersController {
    private final TaxDeclareMastersService taxDeclareMastersService;

    @GetMapping(value = "/v1/tax-declare-masters", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public TableResponseEntity<TaxDeclareMastersResponse> searchData(TaxDeclareMastersRequest.SearchForm dto) {
        return taxDeclareMastersService.searchData(dto);
    }

    @PostMapping(value = "/v1/tax-declare-masters", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.UPDATE)
    public ResponseEntity saveData(@Valid TaxDeclareMastersRequest.SubmitForm dto) throws BaseAppException {
        return taxDeclareMastersService.saveData(dto);
    }

    @DeleteMapping(value = "/v1/tax-declare-masters/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.DELETE)
    public ResponseEntity deleteData(@PathVariable Long id) throws BaseAppException {
        return taxDeclareMastersService.deleteData(id);
    }

    @GetMapping(value = "/v1/tax-declare-masters/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public BaseResponseEntity<TaxDeclareMastersResponse> getDataById(@PathVariable Long id) throws RecordNotExistsException {
        return taxDeclareMastersService.getDataById(id);
    }

    @PostMapping(value = "/v1/tax-declare-masters/calculate", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.CREATE)
    public ResponseEntity calculate(@RequestParam String taxPeriodDate) throws Exception {
        return ResponseUtils.ok(taxDeclareMastersService.calculate(Utils.getLastDay(Utils.stringToDate(taxPeriodDate))));
    }

    @GetMapping(value = "/v1/tax-declare-masters/validate/{inputType}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public BaseResponseEntity<ValidateResponseDto> validateCalculate(@RequestParam String taxPeriodDate, @PathVariable String inputType) throws Exception {
        return ResponseUtils.ok(taxDeclareMastersService.validateCalculate(Utils.getLastDay(Utils.stringToDate(taxPeriodDate)), inputType.toUpperCase()));
    }

    @GetMapping(value = "/v1/tax-declare-masters/download-template", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public ResponseEntity downloadTemplate(@RequestParam String taxPeriodDate) throws Exception {
        return taxDeclareMastersService.downloadTemplate(Utils.getLastDay(Utils.stringToDate(taxPeriodDate)));
    }

    @PostMapping(value = "/v1/tax-declare-masters/import", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.IMPORT)
    public BaseResponseEntity<Long> importTaxDeclare(@RequestPart MultipartFile file,
                                                     @RequestParam Date taxPeriodDate
                                                     ) throws Exception {
        Long id = taxDeclareMastersService.importTaxDeclare(file, Utils.getLastDay(taxPeriodDate));
        return ResponseUtils.ok(id);
    }

    @GetMapping(value = "/v1/tax-declare-masters/export", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public ResponseEntity<Object> exportData(TaxDeclareMastersRequest.SearchForm dto) throws Exception {
        return taxDeclareMastersService.exportData(dto);
    }

    @GetMapping(value = "/v1/tax-declare-masters/export/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public ResponseEntity<Object> exportDetailKK02(@PathVariable Long id) throws Exception {
        return taxDeclareMastersService.exportDetailKK02(id, null);
    }

    @GetMapping(value = "/v1/tax-declare-masters/export-xml/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public ResponseEntity<Object> exportKK02Xml(@PathVariable Long id) throws Exception {
        return taxDeclareMastersService.exportXml(id, null);
    }

    @GetMapping(value = "/v1/tax-declare-masters/export-tax-allocation/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public ResponseEntity<Object> exportTaxAllocation(@PathVariable Long id) throws Exception {
        return taxDeclareMastersService.exportTaxAllocation(id, null);
    }

    @PutMapping(value = "/v1/tax-declare-masters/lock/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.LOCK)
    public BaseResponseEntity<Long> lockPeriodById(@PathVariable Long id) throws Exception {
        taxDeclareMastersService.lockPeriodById(id);
        return ResponseUtils.ok(id);
    }

    @PutMapping(value = "/v1/tax-declare-masters/unlock/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.LOCK)
    public BaseResponseEntity<Object> unLockPeriodById(@PathVariable Long id) throws Exception {
        taxDeclareMastersService.unLockPeriodById(id);
        return ResponseUtils.ok(id);
    }

}
