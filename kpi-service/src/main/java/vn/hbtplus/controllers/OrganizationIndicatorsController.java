/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.hbtplus.annotations.HasPermission;
import vn.hbtplus.models.response.*;
import vn.hbtplus.models.request.OrganizationIndicatorsRequest;
import vn.hbtplus.services.OrganizationIndicatorsService;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.constants.Scope;
import vn.hbtplus.constants.Constant;
import vn.hbtplus.annotations.Resource;
import vn.hbtplus.exceptions.RecordNotExistsException;
import vn.hbtplus.exceptions.BaseAppException;

import javax.validation.Valid;

@RestController
@RequestMapping(BaseConstants.REQUEST_MAPPING_PREFIX)
@RequiredArgsConstructor
@Resource(value = Constant.RESOURCES.ORGANIZATION_INDICATORS)
public class OrganizationIndicatorsController {
    private final OrganizationIndicatorsService organizationIndicatorsService;

    @GetMapping(value = "/v1/organization-indicators", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public TableResponseEntity<OrganizationIndicatorsResponse.SearchResult> searchData(OrganizationIndicatorsRequest.SearchForm dto) {
        return organizationIndicatorsService.searchData(dto);
    }

    @PostMapping(value = "/v1/organization-indicators", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.CREATE)
    public ResponseEntity saveData(@Valid OrganizationIndicatorsRequest.SubmitForm dto) throws BaseAppException {
        return organizationIndicatorsService.saveData(dto, null, null);
    }

    @PutMapping(value = "/v1/organization-indicators/{id}", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.UPDATE)
    public ResponseEntity saveData(@Valid OrganizationIndicatorsRequest.SubmitForm dto, @PathVariable Long id) throws BaseAppException {
        return organizationIndicatorsService.saveData(dto, id, null);
    }

    @DeleteMapping(value = "/v1/organization-indicators/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.DELETE)
    public ResponseEntity deleteData(@PathVariable Long id) throws RecordNotExistsException {
        return organizationIndicatorsService.deleteData(id);
    }

    @GetMapping(value = "/v1/organization-indicators/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public BaseResponseEntity<OrganizationIndicatorsResponse.SearchResult> getDataById(@PathVariable Long id) throws RecordNotExistsException {
        return organizationIndicatorsService.getDataById(id);
    }

    @GetMapping(value = "/v1/organization-indicators/export", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public ResponseEntity<Object> exportData(OrganizationIndicatorsRequest.SearchForm dto) throws Exception {
        return organizationIndicatorsService.exportData(dto);
    }

}
