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
import org.springframework.web.multipart.MultipartFile;
import vn.hbtplus.annotations.HasPermission;
import vn.hbtplus.annotations.Resource;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.constants.Scope;
import vn.hbtplus.models.response.BaseResponseEntity;
import vn.hbtplus.models.response.TableResponseEntity;
import vn.hbtplus.exceptions.BaseAppException;
import vn.hbtplus.exceptions.RecordNotExistsException;
import vn.hbtplus.tax.income.constants.Constant;
import vn.hbtplus.tax.income.models.request.TaxSettlementMastersRequest;
import vn.hbtplus.tax.income.models.response.TaxSettlementMastersResponse;
import vn.hbtplus.tax.income.repositories.entity.TaxDeclareMastersEntity;
import vn.hbtplus.tax.income.repositories.entity.TaxSettlementMastersEntity;
import vn.hbtplus.tax.income.services.TaxSettlementMastersService;
import vn.hbtplus.utils.ResponseUtils;
import vn.hbtplus.utils.Utils;

import javax.validation.Valid;
import java.util.Date;

@RestController
@RequestMapping(Constant.REQUEST_MAPPING_PREFIX)
@RequiredArgsConstructor
@Resource(value = Constant.RESOURCE.PIT_TAX_SETTLEMENT_MASTERS)
public class TaxSettlementMastersController {
    private final TaxSettlementMastersService taxSettlementMastersService;

    @GetMapping(value = "/v1/tax-settlement-masters", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public TableResponseEntity<TaxSettlementMastersResponse> searchData(TaxSettlementMastersRequest.SearchForm dto) {
        return taxSettlementMastersService.searchData(dto);
    }

    @PostMapping(value = "/v1/tax-settlement-masters", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.UPDATE)
    public ResponseEntity saveData(@Valid  TaxSettlementMastersRequest.SubmitForm dto) throws BaseAppException {
        return taxSettlementMastersService.saveData(dto);
    }

    @DeleteMapping(value = "/v1/tax-settlement-masters/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.DELETE)
    public ResponseEntity deleteData(@PathVariable Long id) throws BaseAppException {
        return taxSettlementMastersService.deleteData(id);
    }

    @GetMapping(value = "/v1/tax-settlement-masters/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public BaseResponseEntity<TaxSettlementMastersResponse> getDataById(@PathVariable Long id)  throws RecordNotExistsException {
        return taxSettlementMastersService.getDataById(id);
    }

    @GetMapping(value = "/v1/tax-settlement-masters/export", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public ResponseEntity<Object> exportData(TaxSettlementMastersRequest.SearchForm dto) throws Exception {
        return taxSettlementMastersService.exportData(dto);
    }

    @GetMapping(value = "/v1/tax-settlement-masters/download-template", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public ResponseEntity<Object> downloadTemplate() throws Exception {

        return taxSettlementMastersService.downloadTemplate();
    }

    @PostMapping(value = "/v1/tax-settlement-masters/import", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.IMPORT)
    public BaseResponseEntity<Long> importSettlement(@RequestPart MultipartFile file,
                                                     @RequestParam Date taxPeriodDate) throws Exception {
        Long id = taxSettlementMastersService.importSettlement(file, Utils.getYearByDate(taxPeriodDate));
        return ResponseUtils.ok(id);
    }

    @GetMapping(value = "/v1/tax-settlement-masters/export/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public ResponseEntity<Object> exportDataById(@PathVariable Long id) throws Exception {
        return taxSettlementMastersService.exportDataById(id);
    }

    @PostMapping(value = "/v1/tax-settlement-masters/calculate/{year}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public BaseResponseEntity<TaxSettlementMastersEntity> calculate(@PathVariable Integer year, @RequestBody TaxSettlementMastersRequest.CalculateForm calculateForm) throws Exception {
        return ResponseUtils.ok(taxSettlementMastersService.calculate(year, calculateForm));
    }

    @PutMapping(value = "/v1/tax-settlement-masters/lock/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.LOCK)
    public BaseResponseEntity<Long> lockPeriodById(@PathVariable Long id) throws Exception {
        taxSettlementMastersService.updateStatus(id, TaxDeclareMastersEntity.STATUS.DA_CHOT);
        return ResponseUtils.ok(id);
    }

    @PutMapping(value = "/v1/tax-settlement-masters/unlock/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.LOCK)
    public BaseResponseEntity<Object> unLockPeriodById(@PathVariable Long id) throws Exception {
        taxSettlementMastersService.updateStatus(id, TaxDeclareMastersEntity.STATUS.DU_THAO);
        return ResponseUtils.ok(id);
    }

    @GetMapping(value = "/v1/tax-settlement-masters/export-group/{masterId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public ResponseEntity<Object> exportDetailByMasterId(@PathVariable Long masterId) throws Exception {
        return taxSettlementMastersService.exportOrgGroupDetailByMasterId(masterId);
    }

    @GetMapping(value = "/v1/tax-settlement-masters/export-month/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public ResponseEntity<Object> exportMonth(@PathVariable Long id) throws Exception {
        return taxSettlementMastersService.exportMonth(id);
    }
}
