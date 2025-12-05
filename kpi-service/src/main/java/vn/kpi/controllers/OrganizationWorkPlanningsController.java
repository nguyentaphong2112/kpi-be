/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.kpi.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.kpi.annotations.HasPermission;
import vn.kpi.models.response.*;
import vn.kpi.models.request.OrganizationWorkPlanningsRequest;
import vn.kpi.services.OrganizationWorkPlanningsService;
import vn.kpi.constants.BaseConstants;
import vn.kpi.constants.Scope;
import vn.kpi.constants.Constant;
import vn.kpi.annotations.Resource;
import vn.kpi.exceptions.RecordNotExistsException;
import vn.kpi.exceptions.BaseAppException;

import javax.validation.Valid;

@RestController
@RequestMapping(BaseConstants.REQUEST_MAPPING_PREFIX)
@RequiredArgsConstructor
@Resource(value = Constant.RESOURCES.ORGANIZATION_WORK_PLANNINGS)
public class OrganizationWorkPlanningsController {
    private final OrganizationWorkPlanningsService organizationWorkPlanningsService;

    @GetMapping(value = "/v1/organization-work-plannings", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public TableResponseEntity<OrganizationWorkPlanningsResponse> searchData(OrganizationWorkPlanningsRequest.SearchForm dto) {
        return organizationWorkPlanningsService.searchData(dto);
    }

    @PostMapping(value = "/v1/organization-work-plannings", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.CREATE)
    public ResponseEntity saveData(@Valid OrganizationWorkPlanningsRequest.SubmitForm dto) throws BaseAppException {
        return organizationWorkPlanningsService.saveData(dto, null);
    }

    @PutMapping(value = "/v1/organization-work-plannings/{id}", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.UPDATE)
    public ResponseEntity saveData(@Valid OrganizationWorkPlanningsRequest.SubmitForm dto, @PathVariable Long id) throws BaseAppException {
        return organizationWorkPlanningsService.saveData(dto, id);
    }

    @DeleteMapping(value = "/v1/organization-work-plannings/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.DELETE)
    public ResponseEntity deleteData(@PathVariable Long id) throws RecordNotExistsException {
        return organizationWorkPlanningsService.deleteData(id);
    }

    @GetMapping(value = "/v1/organization-work-plannings/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public BaseResponseEntity<OrganizationWorkPlanningsResponse.DetailBean> getDataById(@PathVariable Long id) throws RecordNotExistsException {
        return organizationWorkPlanningsService.getDataById(id);
    }

    @GetMapping(value = "/v1/organization-work-plannings/export", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public ResponseEntity<Object> exportData(OrganizationWorkPlanningsRequest.SearchForm dto) throws Exception {
        return organizationWorkPlanningsService.exportData(dto);
    }

}
