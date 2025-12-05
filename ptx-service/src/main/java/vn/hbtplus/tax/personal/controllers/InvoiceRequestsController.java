/*
 * Copyright (C) 2022 HRPLUS. All rights reserved.
 * EcoIT. Use is subject to license terms.
 */
package vn.hbtplus.tax.personal.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import vn.hbtplus.annotations.HasPermission;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.constants.Scope;
import vn.hbtplus.models.response.TableResponseEntity;
import vn.hbtplus.tax.personal.constants.Constant;
import vn.hbtplus.tax.personal.models.request.AdminSearchDTO;
import vn.hbtplus.tax.personal.models.request.InvoiceRequestsDTO;
import vn.hbtplus.models.response.BaseResponseEntity;
import vn.hbtplus.tax.personal.models.response.DeclarationRegistersResponse;
import vn.hbtplus.tax.personal.models.response.InvoiceRequestsResponse;
import vn.hbtplus.tax.personal.services.InvoiceRequestsService;

import javax.validation.Valid;
import java.security.SignatureException;

@RestController
@RequestMapping(Constant.REQ_EMP_MAPPING_PREFIX)
@RequiredArgsConstructor
public class InvoiceRequestsController {
    private final InvoiceRequestsService invoiceRequestsService;

    @GetMapping(value = "/v1/invoice-requests", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public TableResponseEntity<InvoiceRequestsResponse> searchData(AdminSearchDTO dto) throws SignatureException {
        return invoiceRequestsService.searchData(dto);
    }

    @PostMapping(value = "/v1/invoice-requests", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.UPDATE)
    public ResponseEntity<Object> saveData(@Valid @RequestBody InvoiceRequestsDTO dto) {
        return invoiceRequestsService.saveData(dto, false);
    }

    @GetMapping(value = "/v1/invoice-requests/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public BaseResponseEntity<InvoiceRequestsResponse> getDataById(@PathVariable Long id) {
        return invoiceRequestsService.getDataById(id, false);
    }

    @DeleteMapping(value = "/v1/invoice-requests/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.DELETE)
    public ResponseEntity<Object> deleteData(@PathVariable Long id) {
        return invoiceRequestsService.deleteData(id, false);
    }

}

