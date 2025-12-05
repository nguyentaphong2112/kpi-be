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
import vn.hbtplus.tax.personal.models.request.DeclarationRegistersDTO;
import vn.hbtplus.models.response.BaseResponseEntity;
import vn.hbtplus.tax.personal.models.response.DeclarationRegistersResponse;
import vn.hbtplus.models.response.ListResponseEntity;
import vn.hbtplus.models.response.TableResponseEntity;
import vn.hbtplus.tax.personal.repositories.entity.DeclarationRegistersEntity;
import vn.hbtplus.tax.personal.services.DeclarationRegistersService;
import vn.hbtplus.utils.ResponseUtils;
import vn.hbtplus.utils.Utils;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

@RestController
@RequestMapping(Constant.REQ_ADMIN_MAPPING_PREFIX)
@RequiredArgsConstructor
public class AdminDeclarationRegistersController {
    private final DeclarationRegistersService declarationRegistersService;

    @GetMapping(value = "/v1/admin/declaration-registers", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public TableResponseEntity<DeclarationRegistersResponse> searchData(AdminSearchDTO dto) {
        return declarationRegistersService.searchData(dto);
    }

    @GetMapping(value = "/v1/admin/declaration-registers/export", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public ResponseEntity<Object> exportData(AdminSearchDTO dto) {
        return declarationRegistersService.exportData(dto);
    }

    @PostMapping(value = "/v1/admin/declaration-registers", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.UPDATE)
    public ResponseEntity<Object> saveData(@Valid @RequestBody DeclarationRegistersDTO dto) {
        if (Utils.isNullObject(dto.getEmployeeId())) {
            return ResponseUtils.error(HttpStatus.BAD_REQUEST, "");
        }
        return declarationRegistersService.saveData(dto, true);
    }

    @DeleteMapping(value = "/v1/admin/declaration-registers/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.DELETE)
    public ResponseEntity<Object> deleteData(@PathVariable Long id) {
        return declarationRegistersService.deleteData(id, true);
    }

    @GetMapping(value = "/v1/admin/declaration-registers/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public BaseResponseEntity<DeclarationRegistersResponse> getDataById(@PathVariable Long id) {
        return declarationRegistersService.getDataById(id, true);
    }

    @GetMapping(value = "/v1/admin/declaration-registers/check-rev-invoice", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public ListResponseEntity<DeclarationRegistersEntity> getDataByProperties(DeclarationRegistersDTO dto) {
        return declarationRegistersService.getDataByProperties(dto, true);
    }

    @GetMapping(value = "/v1/admin/declaration-registers/import-template", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public ResponseEntity<Object> getImportTemplate() throws Exception {
        return declarationRegistersService.getImportTemplate();
    }

    @PostMapping(value = "/v1/admin/declaration-registers/import-process", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.IMPORT)
    public ResponseEntity<Object> importProcess(HttpServletRequest req, @RequestPart(value = "file") MultipartFile file) {
        return declarationRegistersService.importProcess(file, req);
    }
}

