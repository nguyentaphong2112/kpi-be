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
import vn.kpi.annotations.Resource;
import vn.kpi.constants.BaseConstants;
import vn.kpi.constants.Constant;
import vn.kpi.constants.Scope;
import vn.kpi.exceptions.BaseAppException;
import vn.kpi.models.BaseSearchRequest;
import vn.kpi.models.request.EmployeesRequest;
import vn.kpi.models.request.PersonalIdentitiesRequest;
import vn.kpi.models.response.BaseResponseEntity;
import vn.kpi.models.response.PersonalIdentitiesResponse;
import vn.kpi.models.response.TableResponseEntity;
import vn.kpi.services.EmployeesService;
import vn.kpi.services.PersonalIdentitiesService;
import vn.kpi.utils.ResponseUtils;
import vn.kpi.utils.Utils;

import javax.validation.Valid;

@RestController
@RequestMapping(BaseConstants.REQUEST_MAPPING_PREFIX)
@RequiredArgsConstructor
@Resource(value = Constant.RESOURCES.PERSONAL_IDENTITIES)
public class PersonalIdentitiesController {
    private final PersonalIdentitiesService personalIdentitiesService;
    private final EmployeesService employeesService;

    @GetMapping(value = "/v1/personal-identities", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public TableResponseEntity<PersonalIdentitiesResponse.SearchResult> searchData(EmployeesRequest.SearchForm dto) {
        return personalIdentitiesService.searchData(dto);
    }

    @GetMapping(value = "/v1/personal-identities/export", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public ResponseEntity<Object> exportData(EmployeesRequest.SearchForm dto) throws Exception {
        return personalIdentitiesService.exportData(dto);
    }

    @PostMapping(value = "/v1/personal-identities/{employeeId}", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.CREATE, domainId = "employeeId")
    public BaseResponseEntity<Long> saveData(@PathVariable Long employeeId, @RequestBody @Valid PersonalIdentitiesRequest.SubmitForm dto) throws BaseAppException {
        return personalIdentitiesService.saveData(dto, employeeId, null);
    }

    @PutMapping(value = "/v1/personal-identities/{employeeId}/{id}", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.UPDATE, domainId = "employeeId")
    public BaseResponseEntity<Long> updateData(@PathVariable Long employeeId, @PathVariable Long id, @RequestBody @Valid PersonalIdentitiesRequest.SubmitForm dto) throws BaseAppException {
        return personalIdentitiesService.saveData(dto, employeeId, id);
    }

    @DeleteMapping(value = "/v1/personal-identities/{employeeId}/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.DELETE, domainId = "employeeId")
    public BaseResponseEntity<Long> deleteData(@PathVariable Long employeeId, @PathVariable Long id) throws BaseAppException {
        return personalIdentitiesService.deleteData(employeeId, id);
    }

    @GetMapping(value = "/v1/personal-identities/{employeeId}/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW, domainId = "employeeId")
    public BaseResponseEntity<PersonalIdentitiesResponse.DetailBean> getDataById(@PathVariable Long employeeId, @PathVariable Long id) throws BaseAppException {
        return personalIdentitiesService.getDataById(employeeId, id);
    }


    @GetMapping(value = "/v1/personal-identities/pageable/{employeeId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW, domainId = "employeeId")
    public TableResponseEntity<PersonalIdentitiesResponse.SearchResult> getIdentities(@PathVariable Long employeeId, BaseSearchRequest request) {
        return ResponseUtils.ok(personalIdentitiesService.getPersonalIdentities(employeeId, request));
    }

    @PostMapping(value = "/v1/personal-identities/personal", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.CREATE, resource = Constant.RESOURCES.PERSONAL_PERSONAL_IDENTITIES)
    public BaseResponseEntity<Long> saveData(@RequestBody @Valid PersonalIdentitiesRequest.SubmitForm dto) throws BaseAppException {
        Long employeeId = employeesService.getEmployeeId(Utils.getUserEmpCode());
        return personalIdentitiesService.saveData(dto, employeeId, null);
    }

    @PutMapping(value = "/v1/personal-identities/personal/{id}", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.UPDATE, resource = Constant.RESOURCES.PERSONAL_PERSONAL_IDENTITIES)
    public BaseResponseEntity<Long> updateData(@PathVariable Long id, @RequestBody @Valid PersonalIdentitiesRequest.SubmitForm dto) throws BaseAppException {
        Long employeeId = employeesService.getEmployeeId(Utils.getUserEmpCode());
        return personalIdentitiesService.saveData(dto, employeeId, id);
    }

    @DeleteMapping(value = "/v1/personal-identities/personal/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.DELETE, resource = Constant.RESOURCES.PERSONAL_PERSONAL_IDENTITIES)
    public BaseResponseEntity<Long> deleteData(@PathVariable Long id) throws BaseAppException {
        Long employeeId = employeesService.getEmployeeId(Utils.getUserEmpCode());
        return personalIdentitiesService.deleteData(employeeId, id);
    }

    @GetMapping(value = "/v1/personal-identities/personal/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW, resource = Constant.RESOURCES.PERSONAL_PERSONAL_IDENTITIES)
    public BaseResponseEntity<PersonalIdentitiesResponse.DetailBean> getDataById(@PathVariable Long id) throws BaseAppException {
        Long employeeId = employeesService.getEmployeeId(Utils.getUserEmpCode());
        return personalIdentitiesService.getDataById(employeeId, id);
    }

    @GetMapping(value = "/v1/personal-identities/pageable/personal", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW, resource = Constant.RESOURCES.PERSONAL_PERSONAL_IDENTITIES)
    public TableResponseEntity<PersonalIdentitiesResponse.SearchResult> getIdentities(BaseSearchRequest request) {
        Long employeeId = employeesService.getEmployeeId(Utils.getUserEmpCode());
        return ResponseUtils.ok(personalIdentitiesService.getPersonalIdentities(employeeId, request));
    }

    @PostMapping(value = "/v1/personal-identities/import", produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @HasPermission(scope = Scope.IMPORT)
    public ResponseEntity<Object> processImport(@RequestPart MultipartFile file, @RequestParam(required = false) boolean isForceUpdate, @RequestParam String identityTypeId) throws Exception {
        return personalIdentitiesService.processImport(file, isForceUpdate, identityTypeId);
    }

    @GetMapping(value = "/v1/personal-identities/import-template", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public ResponseEntity<Object> downloadImportTemplate(@RequestParam String identityTypeId) throws Exception {
        return personalIdentitiesService.downloadImportTemplate(identityTypeId);
    }
}
