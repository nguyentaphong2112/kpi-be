/*
 * Copyright (C) 2022 HRPLUS. All rights reserved.
 * EcoIT. Use is subject to license terms.
 */
package vn.hbtplus.tax.personal.controllers;

import com.jxcell.CellException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import vn.hbtplus.annotations.HasPermission;
import vn.hbtplus.constants.Scope;
import vn.hbtplus.models.response.BaseResponseEntity;
import vn.hbtplus.models.response.TableResponseEntity;
import vn.hbtplus.tax.personal.constants.Constant;
import vn.hbtplus.tax.personal.models.dto.FamilyRelationshipsDTO;
import vn.hbtplus.tax.personal.models.request.AdminSearchDTO;
import vn.hbtplus.tax.personal.models.request.DependentRegistersDTO;
import vn.hbtplus.tax.personal.models.request.RejectDTO;
import vn.hbtplus.tax.personal.models.response.DependentRegistersResponse;
import vn.hbtplus.tax.personal.services.DependentRegistersService;
import vn.hbtplus.tax.personal.services.DependentReportService;
import vn.hbtplus.utils.ResponseUtils;
import vn.hbtplus.utils.Utils;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.security.SignatureException;
import java.util.List;

@RestController
@RequestMapping(Constant.REQ_ADMIN_MAPPING_PREFIX)
@RequiredArgsConstructor
public class AdminDependentRegistersController {
    private final DependentRegistersService dependentRegistersService;
    private final DependentReportService reportService;

    @GetMapping(value = "/v1/admin/dependent-registers", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public TableResponseEntity<DependentRegistersResponse> searchData(AdminSearchDTO dto) throws SignatureException {
        return dependentRegistersService.searchData(dto);
    }

    @GetMapping(value = "/v1/admin/dependent-registers/export", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public ResponseEntity<Object> exportDataDependentRegister(AdminSearchDTO dto) {
        return dependentRegistersService.exportDataDependentRegister(dto);
    }

    @PostMapping(value = "/v1/admin/dependent-registers", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.UPDATE)
    public ResponseEntity<Object> saveData(
            @RequestPart(value = "files", required = false) List<MultipartFile> files,
            @Valid @RequestPart(value = "data") DependentRegistersDTO dto
    ) {
        if (Utils.isNullObject(dto.getEmployeeId())) {
            return ResponseUtils.error(HttpStatus.BAD_REQUEST, "");
        }
        return dependentRegistersService.saveData(dto, files, true);
    }

    @DeleteMapping(value = "/v1/admin/dependent-registers/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.DELETE)
    public ResponseEntity<Object> deleteData(@PathVariable Long id) {
        return dependentRegistersService.deleteData(id, true);
    }

    @GetMapping(value = "/v1/admin/dependent-registers/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public BaseResponseEntity<DependentRegistersResponse> getDataById(@PathVariable Long id) throws SignatureException {
        return dependentRegistersService.getDataById(id, true);
    }

    @PostMapping(value = "/v1/admin/dependent-registers/update-work-flow/{id}/{status}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public ResponseEntity<Object> updateWorkFLow(@PathVariable Long id, @PathVariable Integer status) {
        return dependentRegistersService.updateWorkFLow(id, status, true);
    }

    @PostMapping(value = "/v1/admin/dependent-registers/approve-by-list", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.APPROVE)
    public ResponseEntity<Object> approveByList(@RequestParam List<Long> listId) {
        return dependentRegistersService.approveByList(listId);
    }

    @PostMapping(value = "/v1/admin/dependent-registers/approve-all", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.APPROVE)
    public ResponseEntity<Object> approveAll(@RequestBody AdminSearchDTO dto) {
        return dependentRegistersService.approveAll(dto);
    }

    @PostMapping(value = "/v1/admin/dependent-registers/reject-by-list", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.APPROVE)
    public ResponseEntity<Object> rejectByList(@RequestBody RejectDTO dto) {
        return dependentRegistersService.rejectByList(dto);
    }

    @GetMapping(value = "/v1/admin/dependent-registers/import-template/new-register-result", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public ResponseEntity<Object> getImportTemplateNewRegisterResult() throws Exception {
        return dependentRegistersService.getImportTemplateRegisterResult();
    }

    @PostMapping(value = "/v1/admin/dependent-registers/import-process/new-register-result", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.IMPORT)
    public ResponseEntity<Object> importNewRegisterResult(HttpServletRequest req, @RequestPart(value = "file") MultipartFile file) {
        return dependentRegistersService.importRegisterResult(file, req);
    }

    @GetMapping(value = "/v1/admin/dependent-registers/export-according-tax-authority", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public ResponseEntity<Object> exportDataAccordingTaxAuthority(AdminSearchDTO dto) {
        return dependentRegistersService.exportDataAccordingTaxAuthority(dto);
    }

    @PostMapping(value = "/v1/admin/dependent-registers/auto-register", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.UPDATE)
    public BaseResponseEntity<Object> autoRegister(@RequestParam(value = "employeeId") Long employeeId,
                                               @RequestParam(value = "cancelDate") String cancelDate,
                                               @RequestParam(value = "clientMessageId") String clientMessageId) {
        return dependentRegistersService.autoRegister(employeeId, cancelDate, clientMessageId);
    }

    @GetMapping(value = "/v1/admin/dependent-registers/search-by-month", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public TableResponseEntity<DependentRegistersResponse> searchByMonth(AdminSearchDTO dto) {
        return reportService.searchByMonth(dto);
    }

    @GetMapping(value = "/v1/admin/dependent-registers/export-by-month", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public ResponseEntity<Object> exportByMonth(AdminSearchDTO dto) throws Exception {
        return reportService.exportByMonth(dto);
    }

    @GetMapping(value = "/v1/admin/dependent-registers/export-group-by-month", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public ResponseEntity<Object> exportGroupByMonth(AdminSearchDTO dto) throws CellException {
        return reportService.exportGroupByMonth(dto);
    }

    @GetMapping(value = "/v1/admin/dependent-registers/send-mail-confirm-dependent", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public ResponseEntity<Object> sendMailConfirmDependent(AdminSearchDTO dto) {
        return dependentRegistersService.sendMailConfirmDependent(dto);
    }

    @GetMapping(value = "/v1/admin/dependent-registers/employees/{employeeId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public ResponseEntity<Object> getListDataByEmpId(@PathVariable Long employeeId,
                                                     FamilyRelationshipsDTO familyRelationshipsDTO
    ) {
        familyRelationshipsDTO.setEmployeeId(employeeId);
        return ResponseEntity.ok(dependentRegistersService.getListDataByEmpId(familyRelationshipsDTO));
    }

}

