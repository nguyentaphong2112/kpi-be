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
import vn.hbtplus.tax.personal.models.request.DeclarationRegistersDTO;
import vn.hbtplus.models.response.BaseResponseEntity;
import vn.hbtplus.tax.personal.models.response.DeclarationRegistersResponse;
import vn.hbtplus.models.response.ListResponseEntity;
import vn.hbtplus.tax.personal.models.response.DependentRegistersResponse;
import vn.hbtplus.tax.personal.repositories.entity.DeclarationRegistersEntity;
import vn.hbtplus.tax.personal.services.DeclarationRegistersService;

import javax.validation.Valid;
import java.security.SignatureException;

@RestController
@RequestMapping(Constant.REQ_EMP_MAPPING_PREFIX)
@RequiredArgsConstructor
public class DeclarationRegistersController {
    private final DeclarationRegistersService declarationRegistersService;

    @GetMapping(value = "/v1/declaration-registers", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public TableResponseEntity<DeclarationRegistersResponse> searchData(AdminSearchDTO dto) throws SignatureException {
        return declarationRegistersService.searchData(dto);
    }

    @PostMapping(value = "/v1/declaration-registers", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.UPDATE)
    public ResponseEntity<Object> saveData(@Valid @RequestBody DeclarationRegistersDTO dto) {
        return declarationRegistersService.saveData(dto, false);
    }

    @GetMapping(value = "/v1/declaration-registers/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public BaseResponseEntity<DeclarationRegistersResponse> getDataById(@PathVariable Long id) {
        return declarationRegistersService.getDataById(id, false);
    }

    @DeleteMapping(value = "/v1/declaration-registers/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.DELETE)
    public ResponseEntity<Object> deleteData(@PathVariable Long id) {
        return declarationRegistersService.deleteData(id, false);
    }

    @GetMapping(value = "/v1/declaration-registers/check-rev-invoice", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public ListResponseEntity<DeclarationRegistersEntity> getDataByProperties(DeclarationRegistersDTO dto) {
        return declarationRegistersService.getDataByProperties(dto, false);
    }

}

