/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.kpi.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import vn.kpi.annotations.HasPermission;
import vn.kpi.models.BaseSearchRequest;
import vn.kpi.models.request.EmployeesRequest;
import vn.kpi.models.response.*;
import vn.kpi.models.request.FamilyRelationshipsRequest;
import vn.kpi.services.EmployeesService;
import vn.kpi.services.FamilyRelationshipsService;
import vn.kpi.constants.BaseConstants;
import vn.kpi.constants.Scope;
import vn.kpi.constants.Constant;
import vn.kpi.annotations.Resource;
import vn.kpi.exceptions.RecordNotExistsException;
import vn.kpi.exceptions.BaseAppException;
import vn.kpi.utils.ResponseUtils;
import vn.kpi.utils.Utils;

import javax.validation.Valid;

@RestController
@RequestMapping(BaseConstants.REQUEST_MAPPING_PREFIX)
@RequiredArgsConstructor
@Resource(value = Constant.RESOURCES.FAMILY_RELATIONSHIPS)
public class FamilyRelationshipsController {
    private final FamilyRelationshipsService familyRelationshipsService;
    private final EmployeesService employeesService;

    @GetMapping(value = "/v1/family-relationships", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public TableResponseEntity<FamilyRelationshipsResponse.SearchResult> searchData(EmployeesRequest.SearchForm dto) {
        return familyRelationshipsService.searchData(dto);
    }

    @GetMapping(value = "/v1/family-relationships/export", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public ResponseEntity<Object> exportData(EmployeesRequest.SearchForm dto) throws Exception {
        return familyRelationshipsService.exportData(dto);
    }

    @GetMapping(value = "/v1/family-relationships/download-template", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public ResponseEntity<Object> exportTemplate() throws Exception {
        return familyRelationshipsService.downloadTemplate();
    }

    @PostMapping(value = "/v1/family-relationships/import", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.VIEW)
    public ResponseEntity<Object> importProcess(@RequestPart(value = "file") MultipartFile file) throws Exception {
        return familyRelationshipsService.importProcess(file);
    }

    @PostMapping(value = "/v1/family-relationships/{employeeId}", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.CREATE, domainId = "employeeId")
    public ResponseEntity saveData(@RequestBody @Valid FamilyRelationshipsRequest.SubmitForm dto, @PathVariable Long employeeId) throws BaseAppException {
        return familyRelationshipsService.saveData(dto, employeeId, null);
    }

    @PutMapping(value = "/v1/family-relationships/{employeeId}/{id}", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.UPDATE, domainId = "employeeId")
    public ResponseEntity updateData(@RequestBody @Valid FamilyRelationshipsRequest.SubmitForm dto, @PathVariable Long employeeId, @PathVariable Long id) throws BaseAppException {
        return familyRelationshipsService.saveData(dto, employeeId, id);
    }

    @DeleteMapping(value = "/v1/family-relationships/{employeeId}/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.DELETE, domainId = "employeeId")
    public ResponseEntity deleteData(@PathVariable Long employeeId, @PathVariable Long id) throws BaseAppException {
        return familyRelationshipsService.deleteData(employeeId, id);
    }

    @GetMapping(value = "/v1/family-relationships/{employeeId}/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW, domainId = "employeeId")
    public BaseResponseEntity<FamilyRelationshipsResponse.DetailBean> getDataById(@PathVariable Long employeeId, @PathVariable Long id) throws BaseAppException {
        return familyRelationshipsService.getDataById(employeeId, id);
    }

    @GetMapping(value = "/v1/family-relationships/pageable/{employeeId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW, domainId = "employeeId")
    public TableResponseEntity<FamilyRelationshipsResponse.DetailBean> getTableList(@PathVariable Long employeeId, BaseSearchRequest request) throws RecordNotExistsException {
        return ResponseUtils.ok(familyRelationshipsService.getTableList(employeeId, request));
    }

    @PostMapping(value = "/v1/family-relationships/personal", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.CREATE, resource = Constant.RESOURCES.PERSONAL_FAMILY_RELATIONSHIPS)
    public ResponseEntity saveData(@RequestBody @Valid FamilyRelationshipsRequest.SubmitForm dto) throws BaseAppException {
        Long employeeId = employeesService.getEmployeeId(Utils.getUserEmpCode());
        return familyRelationshipsService.saveData(dto, employeeId, null);
    }

    @PutMapping(value = "/v1/family-relationships/personal/{id}", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.UPDATE, resource = Constant.RESOURCES.PERSONAL_FAMILY_RELATIONSHIPS)
    public ResponseEntity updateData(@RequestBody @Valid FamilyRelationshipsRequest.SubmitForm dto, @PathVariable Long id) throws BaseAppException {
        Long employeeId = employeesService.getEmployeeId(Utils.getUserEmpCode());
        return familyRelationshipsService.saveData(dto, employeeId, id);
    }

    @DeleteMapping(value = "/v1/family-relationships/personal/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.DELETE, resource = Constant.RESOURCES.PERSONAL_FAMILY_RELATIONSHIPS)
    public ResponseEntity deleteData(@PathVariable Long id) throws BaseAppException {
        Long employeeId = employeesService.getEmployeeId(Utils.getUserEmpCode());
        return familyRelationshipsService.deleteData(employeeId, id);
    }

    @GetMapping(value = "/v1/family-relationships/personal/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW, resource = Constant.RESOURCES.PERSONAL_FAMILY_RELATIONSHIPS)
    public BaseResponseEntity<FamilyRelationshipsResponse.DetailBean> getDataById(@PathVariable Long id) throws BaseAppException {
        Long employeeId = employeesService.getEmployeeId(Utils.getUserEmpCode());
        return familyRelationshipsService.getDataById(employeeId, id);
    }

    @GetMapping(value = "/v1/family-relationships/pageable/personal", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW, resource = Constant.RESOURCES.PERSONAL_FAMILY_RELATIONSHIPS)
    public TableResponseEntity<FamilyRelationshipsResponse.DetailBean> getTableList(BaseSearchRequest request) throws RecordNotExistsException {
        Long employeeId = employeesService.getEmployeeId(Utils.getUserEmpCode());
        return ResponseUtils.ok(familyRelationshipsService.getTableList(employeeId, request));
    }

}
