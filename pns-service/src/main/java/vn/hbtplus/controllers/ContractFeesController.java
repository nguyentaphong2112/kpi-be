/*
 * Copyright (C) 2022 HRPLUS. All rights reserved.
 * EcoIT. Use is subject to license terms.
 */
package vn.hbtplus.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import vn.hbtplus.annotations.HasPermission;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.constants.Scope;
import vn.hbtplus.models.dto.ApproveDTO;
import vn.hbtplus.models.dto.ContractFeesDTO;
import vn.hbtplus.models.dto.RejectDTO;
import vn.hbtplus.models.response.ContractFeesResponse;
import vn.hbtplus.models.response.TableResponseEntity;
import vn.hbtplus.repositories.entity.ContractFeesEntity;
import vn.hbtplus.services.ContractFeesService;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.security.SignatureException;
import java.util.List;

@RestController
@RequestMapping(BaseConstants.REQUEST_MAPPING_PREFIX)
@RequiredArgsConstructor
public class ContractFeesController {
    private final ContractFeesService contractFeesService;

    @GetMapping(value = "/v1/contract-fees", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public TableResponseEntity<ContractFeesResponse> searchData(ContractFeesDTO dto) throws SignatureException {
        return contractFeesService.searchData(dto);
    }

    @PostMapping(value = "/v1/contract-fees", consumes = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.CREATE)
    public ResponseEntity<Object> saveData(@Valid @RequestBody ContractFeesDTO dto
    ) {
        return contractFeesService.saveData(dto);
    }

    @PostMapping(value = "/v1/contract-fees/save-list", consumes = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public ResponseEntity<Object> saveListData(@Valid @RequestBody List<ContractFeesEntity> listEntity
    ) {
        return contractFeesService.saveListData(listEntity);
    }

    @DeleteMapping(value = "/v1/contract-fees/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.DELETE)
    public ResponseEntity<Object> deleteData(@PathVariable Long id) {
        return contractFeesService.deleteData(id);
    }

    @GetMapping(value = "/v1/contract-fees/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public ResponseEntity<Object> getDataById(@PathVariable Long id) throws SignatureException {
        return contractFeesService.getDataById(id);
    }

    @GetMapping(value = "/v1/contract-fees/export", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public ResponseEntity<Object> exportData(ContractFeesDTO dto) throws Exception {
        return contractFeesService.exportData(dto);
    }

    @PostMapping(value = "/v1/contract-fees/approve-by-id", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.APPROVE)
    public ResponseEntity<Object> approveById(@RequestBody ApproveDTO dto) {
        return contractFeesService.approveById(dto);
    }

    @PostMapping(value = "/v1/contract-fees/approve-by-list", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.APPROVE)
    public ResponseEntity<Object> approveByList(@RequestBody ApproveDTO dto) {
        return contractFeesService.approveByList(dto);
    }

    @PostMapping(value = "/v1/contract-fees/approve-all", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.APPROVE)
    public ResponseEntity<Object> approveAll(ContractFeesDTO dto) {
        return contractFeesService.approveAll(dto);
    }

    @PostMapping(value = "/v1/contract-fees/reject-by-id", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.APPROVE)
    public ResponseEntity<Object> rejectById(@RequestBody RejectDTO dto) {
        return contractFeesService.rejectById(dto);
    }

    @PostMapping(value = "/v1/contract-fees/reject-by-list", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.APPROVE)
    public ResponseEntity<Object> rejectByList(@RequestBody RejectDTO dto) {
        return contractFeesService.rejectByList(dto);
    }

    @PostMapping(value = "/v1/contract-fees/import", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.CREATE)
    public ResponseEntity<Object> importContractFee(HttpServletRequest req,
                                                    @RequestPart(value = "fileImport") MultipartFile fileImport) throws Exception {
        return contractFeesService.importContractFee(fileImport, req);
    }

    @GetMapping(value = "/v1/contract-fees/template-import", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.CREATE)
    public ResponseEntity<Object> getTemplateImport() throws Exception {
        return contractFeesService.getTemplateImport();
    }
}
