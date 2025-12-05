/*
 * Copyright (C) 2022 HRPLUS. All rights reserved.
 * EcoIT. Use is subject to license terms.
 */
package vn.hbtplus.tax.personal.controllers;

import feign.Response;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import vn.hbtplus.annotations.HasPermission;
import vn.hbtplus.annotations.Resource;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.constants.Scope;
import vn.hbtplus.models.response.BaseResponseEntity;
import vn.hbtplus.models.response.ListResponseEntity;
import vn.hbtplus.tax.personal.constants.Constant;
import vn.hbtplus.tax.personal.models.dto.EmployeeInfosDTO;
import vn.hbtplus.tax.personal.models.dto.PersonalIdentitiesDTO;
import vn.hbtplus.tax.personal.models.request.InvoiceRequestsDTO;
import vn.hbtplus.tax.personal.models.request.TaxNumberRegistersDTO;
import vn.hbtplus.tax.personal.models.response.TaxNumberRegistersResponse;
import vn.hbtplus.tax.personal.repositories.entity.TaxNumberRegistersEntity;
import vn.hbtplus.tax.personal.services.CommonUtilsService;
import vn.hbtplus.tax.personal.services.TaxNumberRegistersService;
import vn.hbtplus.utils.ResponseUtils;

import javax.validation.Valid;
import java.security.SignatureException;
import java.util.List;

@RestController
@RequestMapping(BaseConstants.REQUEST_MAPPING_PREFIX)
@RequiredArgsConstructor
@Resource(value = Constant.RESOURCE.TAX_NUMBER_REGISTERS)
public class TaxNumberRegistersController {
    private final TaxNumberRegistersService taxNumberRegistersService;
    private final CommonUtilsService commonUtilsService;

    @GetMapping(value = "/v1/tax-number-registers", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public ResponseEntity<Object> searchData(TaxNumberRegistersDTO dto) throws SignatureException {
        return taxNumberRegistersService.searchData(dto);
    }

    @PostMapping(value = "/v1/tax-number-registers", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.UPDATE)
    public ResponseEntity<Object> saveData(
            @RequestPart(value = "files", required = false) List<MultipartFile> files,
            @ModelAttribute TaxNumberRegistersDTO dto
    ) {
        return taxNumberRegistersService.saveData(dto, files, false);
    }

    @PutMapping(value = "/v1/tax-number-registers/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.UPDATE)
    public ResponseEntity<Object> saveDataPut(@RequestPart(value = "fileList", required = false) List<MultipartFile> files,
                                           @ModelAttribute TaxNumberRegistersDTO dto, @PathVariable Long id
    ) {
        dto.setTaxNumberRegisterId(id);
        return taxNumberRegistersService.saveData(dto, files, false);
    }

    @DeleteMapping(value = "/v1/tax-number-registers/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.DELETE)
    public ResponseEntity<Object> deleteData(@PathVariable Long id) {
        return taxNumberRegistersService.deleteData(id, false);
    }

    @GetMapping(value = "/v1/tax-number-registers/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public BaseResponseEntity<TaxNumberRegistersResponse> getDataById(@PathVariable Long id) throws SignatureException {
        return taxNumberRegistersService.getDataById(id, false);
    }

    @PostMapping(value = "/v1/tax-number-registers/update-work-flow/{id}/{status}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public ResponseEntity<Object> updateWorkFLow(@PathVariable Long id, @PathVariable Integer status) {
        return taxNumberRegistersService.updateWorkFLow(id, status, false);
    }

    @GetMapping(value = "/v1/tax-number-registers/recent-register", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public ListResponseEntity<TaxNumberRegistersEntity> getRecentRegister() {
        return taxNumberRegistersService.getRecentRegister(null, false);
    }

    @GetMapping(value = "/v1/employees/{employeeId}/contact-info", produces = MediaType.APPLICATION_JSON_VALUE)
    public EmployeeInfosDTO getContactInfo(@PathVariable Long employeeId) {
        return taxNumberRegistersService.getContactInfo(employeeId);
    }

    @GetMapping(value = "/v1/employees/{employeeId}/identities", produces = MediaType.APPLICATION_JSON_VALUE)
    public ListResponseEntity<PersonalIdentitiesDTO> getPersonalIdentities(@PathVariable Long employeeId) {
        return ResponseUtils.ok(taxNumberRegistersService.getPersonalIdentities(employeeId));
    }

}

