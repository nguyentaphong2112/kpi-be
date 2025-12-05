/*
 * Copyright (C) 2022 HRPLUS. All rights reserved.
 * EcoIT. Use is subject to license terms.
 */
package vn.hbtplus.tax.personal.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import vn.hbtplus.annotations.HasPermission;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.constants.Scope;
import vn.hbtplus.tax.personal.constants.Constant;
import vn.hbtplus.tax.personal.models.request.AdminSearchDTO;
import vn.hbtplus.tax.personal.models.request.InvoiceRequestsDTO;
import vn.hbtplus.tax.personal.models.request.RejectDTO;
import vn.hbtplus.tax.personal.models.request.TaxNumberRegistersDTO;
import vn.hbtplus.models.response.BaseResponseEntity;
import vn.hbtplus.models.response.ListResponseEntity;
import vn.hbtplus.tax.personal.models.response.TaxNumberRegistersResponse;
import vn.hbtplus.tax.personal.repositories.entity.TaxNumberRegistersEntity;
import vn.hbtplus.tax.personal.services.TaxNumberRegistersService;
import vn.hbtplus.utils.ResponseUtils;
import vn.hbtplus.utils.Utils;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.security.SignatureException;
import java.util.List;

@RestController
@RequestMapping(Constant.REQ_ADMIN_MAPPING_PREFIX)
@RequiredArgsConstructor
public class AdminTaxNumberRegistersController {
    private final TaxNumberRegistersService taxNumberRegistersService;

    @GetMapping(value = "/v1/admin/tax-number-registers", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public ResponseEntity<Object> searchDataAdmin(@Valid TaxNumberRegistersDTO dto) throws SignatureException {
        return taxNumberRegistersService.searchData(dto);
    }

    @PostMapping(value = "/v1/admin/tax-number-registers", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.UPDATE)
    public ResponseEntity<Object> saveData(
            @RequestPart(value = "files", required = false) List<MultipartFile> fileList,
            @ModelAttribute TaxNumberRegistersDTO dto
    ) {
        if (Utils.isNullObject(dto.getEmployeeId())) {
            return ResponseUtils.error(HttpStatus.BAD_REQUEST, "");
        }
        return taxNumberRegistersService.saveData(dto, fileList, true);
    }

    @PutMapping(value = "/v1/admin/tax-number-registers/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.UPDATE)
    public ResponseEntity<Object> saveData(
            @RequestPart(value = "fileList", required = false) List<MultipartFile> fileList,
            @ModelAttribute TaxNumberRegistersDTO dto, @PathVariable Long id
    ) {
        dto.setTaxNumberRegisterId(id);
        return taxNumberRegistersService.saveData(dto, fileList, true);
    }

    @DeleteMapping(value = "/v1/admin/tax-number-registers/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.DELETE)
    public ResponseEntity<Object> deleteData(@PathVariable Long id) {
        return taxNumberRegistersService.deleteData(id, true);
    }

    @GetMapping(value = "/v1/admin/tax-number-registers/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public BaseResponseEntity<TaxNumberRegistersResponse> getDataById(@PathVariable Long id) throws SignatureException {
        return taxNumberRegistersService.getDataById(id, true);
    }

    /**
     * api phe duyet/tu choi theo id ban ghi
     *
     * @param id
     * @param status
     * @return
     */
    @PostMapping(value = "/v1/admin/tax-number-registers/update-work-flow/{id}/{status}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public ResponseEntity<Object> updateWorkFLow(@PathVariable Long id, @PathVariable Integer status) {
        return taxNumberRegistersService.updateWorkFLow(id, status, true);
    }

    @PostMapping(value = "/v1/admin/tax-number-registers/approve-by-list", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.APPROVE)
    public ResponseEntity<Object> approveByList(@RequestParam List<Long> listId) {
        return taxNumberRegistersService.approveByList(listId);
    }

    @PostMapping(value = "/v1/admin/tax-number-registers/approve-all", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.APPROVE)
    public ResponseEntity<Object> approveAll(@Valid @RequestBody AdminSearchDTO dto) {
        return taxNumberRegistersService.approveAll(dto);
    }

    @PostMapping(value = "/v1/admin/tax-number-registers/reject-by-list", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.APPROVE)
    public ResponseEntity<Object> rejectByList(@RequestBody RejectDTO dto) {
        return taxNumberRegistersService.rejectByList(dto);
    }

    @GetMapping(value = "/v1/admin/tax-number-registers/export-new-register", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public ResponseEntity<Object> exportNewRegister(@Valid AdminSearchDTO dto) throws Exception {
        return taxNumberRegistersService.exportNewRegister(dto);
    }

    @GetMapping(value = "/v1/admin/tax-number-registers/export-change-register", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public ResponseEntity<Object> exportChangeRegister(@Valid AdminSearchDTO dto) throws Exception {
        return taxNumberRegistersService.exportChangeRegister(dto);
    }

    @GetMapping(value = "/v1/admin/tax-number-registers/import-template/new-register-result", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public ResponseEntity<Object> getImportTemplateNewRegisterResult() throws Exception {
        return taxNumberRegistersService.getImportTemplateNewRegisterResult();
    }

    @PostMapping(value = "/v1/admin/tax-number-registers/import-process/new-register-result", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.IMPORT)
    public ResponseEntity<Object> importNewRegisterResult(HttpServletRequest req, @RequestPart(value = "v1file") MultipartFile file) {
        return taxNumberRegistersService.importNewRegisterResult(file, req);
    }

    @GetMapping(value = "/v1/admin/tax-number-registers/import-template/change-register-result", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public ResponseEntity<Object> getImportTemplateChangeRegisterResult() throws Exception {
        return taxNumberRegistersService.getImportTemplateChangeRegisterResult();
    }

    @PostMapping(value = "/v1/admin/tax-number-registers/import-process/change-register-result", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.IMPORT)
    public ResponseEntity<Object> importChangeRegisterResult(HttpServletRequest req, @RequestPart(value = "v1file") MultipartFile file) {
        return taxNumberRegistersService.importChangeRegisterResult(file, req);
    }

    @GetMapping(value = "/v1/admin/tax-number-registers/recent-register/{employeeId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public ListResponseEntity<TaxNumberRegistersEntity> getRecentRegister(@PathVariable Long employeeId) {
        return taxNumberRegistersService.getRecentRegister(employeeId, true);
    }

    @GetMapping(value = "/v1/admin/tax-number-registers/export", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public ResponseEntity<Object> exportTaxNumberRegister(TaxNumberRegistersDTO dto) throws Exception {
        return taxNumberRegistersService.exportRegisterByTaxOfficeTemplate(dto);
    }
}

