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
import vn.hbtplus.models.response.TableResponseEntity;
import vn.hbtplus.tax.personal.constants.Constant;
import vn.hbtplus.tax.personal.models.dto.FamilyRelationshipsDTO;
import vn.hbtplus.tax.personal.models.request.AdminSearchDTO;
import vn.hbtplus.tax.personal.models.request.DependentRegistersDTO;
import vn.hbtplus.models.response.BaseResponseEntity;
import vn.hbtplus.tax.personal.models.response.DependentRegistersResponse;
import vn.hbtplus.tax.personal.services.DependentRegistersService;

import javax.validation.Valid;
import java.security.SignatureException;
import java.util.List;

@RestController
@RequestMapping(Constant.REQ_EMP_MAPPING_PREFIX)
@RequiredArgsConstructor
public class DependentRegistersController {
    private final DependentRegistersService dependentRegistersService;

    @GetMapping(value = "/v1/dependent-registers", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public TableResponseEntity<DependentRegistersResponse> searchData(AdminSearchDTO dto) throws SignatureException {
        return dependentRegistersService.searchData(dto);
    }

    @PostMapping(value = "/v1/dependent-registers", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.UPDATE)
    public ResponseEntity<Object> saveData(
            @RequestPart(value = "files", required = false) List<MultipartFile> files,
            @Valid @RequestPart(value = "data") DependentRegistersDTO dto
    ) {
        return dependentRegistersService.saveData(dto, files, false);
    }

    @DeleteMapping(value = "/v1/dependent-registers/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.DELETE)
    public ResponseEntity<Object> deleteData(@PathVariable Long id) {
        return dependentRegistersService.deleteData(id, false);
    }

    @GetMapping(value = "/v1/dependent-registers/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public BaseResponseEntity<DependentRegistersResponse> getDataById(@PathVariable Long id) throws SignatureException {
        return dependentRegistersService.getDataById(id, false);
    }

    @PostMapping(value = "/v1/dependent-registers/update-work-flow/{id}/{status}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public ResponseEntity<Object> updateWorkFLow(@PathVariable Long id, @PathVariable Integer status) {
        return dependentRegistersService.updateWorkFLow(id, status, false);
    }


    @GetMapping(value = "/v1/dependent-registers/employees/{employeeId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public ResponseEntity<Object> getListDataByEmpId(@PathVariable Long employeeId,
                                                     FamilyRelationshipsDTO familyRelationshipsDTO
    ) {
        familyRelationshipsDTO.setEmployeeId(employeeId);
        return ResponseEntity.ok(dependentRegistersService.getListDataByEmpId(familyRelationshipsDTO));
    }

}

