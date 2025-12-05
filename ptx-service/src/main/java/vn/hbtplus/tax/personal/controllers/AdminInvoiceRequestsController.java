/*
 * Copyright (C) 2022 HRPLUS. All rights reserved.
 * EcoIT. Use is subject to license terms.
 */
package vn.hbtplus.tax.personal.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import vn.hbtplus.annotations.HasPermission;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.constants.Scope;
import vn.hbtplus.exceptions.BaseAppException;
import vn.hbtplus.tax.personal.constants.Constant;
import vn.hbtplus.tax.personal.models.request.AdminSearchDTO;
import vn.hbtplus.tax.personal.models.request.InvoiceRequestsDTO;
import vn.hbtplus.models.response.BaseResponseEntity;
import vn.hbtplus.tax.personal.models.response.InvoiceRequestsResponse;
import vn.hbtplus.models.response.TableResponseEntity;
import vn.hbtplus.tax.personal.services.InvoiceRequestsService;
import vn.hbtplus.utils.ResponseUtils;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping(Constant.REQ_ADMIN_MAPPING_PREFIX)
@RequiredArgsConstructor
public class AdminInvoiceRequestsController {
    private final InvoiceRequestsService invoiceRequestsService;

    @GetMapping(value = "/v1/admin/invoice-requests", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public TableResponseEntity<InvoiceRequestsResponse> searchData(AdminSearchDTO dto) {
        return invoiceRequestsService.searchData(dto);
    }

    @GetMapping(value = "/v1/admin/invoice-requests/export", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public ResponseEntity<Object> exportData(AdminSearchDTO dto) {
        return invoiceRequestsService.exportData(dto);
    }

    @PostMapping(value = "/v1/admin/invoice-requests", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.UPDATE)
    public ResponseEntity<Object> saveData(@RequestBody InvoiceRequestsDTO dto) {
        return invoiceRequestsService.saveData(dto, true);
    }

    @PutMapping(value = "/v1/admin/invoice-requests/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.UPDATE)
    public ResponseEntity<Object> saveData(@RequestBody InvoiceRequestsDTO dto, @PathVariable Long id) {
        dto.setInvoiceRequestId(id);
        return invoiceRequestsService.saveData(dto, true);
    }

    @DeleteMapping(value = "/v1/admin/invoice-requests/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.DELETE)
    public ResponseEntity<Object> deleteData(@PathVariable Long id) {
        return invoiceRequestsService.deleteData(id, true);
    }

    @GetMapping(value = "/v1/admin/invoice-requests/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public BaseResponseEntity<InvoiceRequestsResponse> getDataById(@PathVariable Long id) {
        return invoiceRequestsService.getDataById(id, true);
    }

    @GetMapping(value = "/v1/admin/invoice-requests/import-template", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public ResponseEntity<Object> getImportTemplateNewRegisterResult() throws Exception {
        return invoiceRequestsService.getImportTemplate();
    }

    @PostMapping(value = "/v1/admin/invoice-requests/import-process", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.IMPORT)
    public ResponseEntity<Object> importNewRegisterResult(
            HttpServletRequest req,
            @RequestPart(value = "v1file") MultipartFile file) {
        return invoiceRequestsService.importProcess(file, req);
    }

    @PostMapping(value = "/v1/admin/invoice-requests/export-invoice-by-list", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.UPDATE)
    public ResponseEntity<Object> requestExportInvoiceByList(@RequestParam List<Long> listId) {
        return invoiceRequestsService.requestExportInvoiceByList(listId);
    }

    @PostMapping(value = "/v1/admin/invoice-requests/export-invoice-by-form", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.UPDATE)
    public ResponseEntity<Object> requestExportInvoiceByForm(AdminSearchDTO dto) {
        return invoiceRequestsService.requestExportInvoiceByForm(dto);
    }

}

