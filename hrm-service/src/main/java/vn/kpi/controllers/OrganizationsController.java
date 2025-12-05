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
import vn.kpi.exceptions.RecordNotExistsException;
import vn.kpi.models.request.OrganizationsRequest;
import vn.kpi.models.response.*;
import vn.kpi.services.OrganizationsService;
import vn.kpi.utils.ResponseUtils;

import javax.validation.Valid;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping(BaseConstants.REQUEST_MAPPING_PREFIX)
@RequiredArgsConstructor
@Resource(value = Constant.RESOURCES.ORGANIZATION)
public class OrganizationsController {
    private final OrganizationsService organizationsService;

    @GetMapping(value = "/v1/organizations", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public TableResponseEntity<OrganizationsResponse.SearchResult> searchData(OrganizationsRequest.SearchForm dto) {
        return ResponseUtils.ok(organizationsService.searchData(dto));
    }

    @PostMapping(value = "/v1/organizations", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.CREATE)
    public BaseResponseEntity<Long> saveData(@RequestBody @Valid OrganizationsRequest.SubmitForm dto) throws BaseAppException {
        return organizationsService.saveData(dto, null);
    }

    @PutMapping(value = "/v1/organizations/{id}", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.UPDATE)
    public BaseResponseEntity<Long> updateData(@RequestBody @Valid  OrganizationsRequest.SubmitForm dto, @PathVariable Long id) throws BaseAppException {
        return organizationsService.saveData(dto, id);
    }

    @DeleteMapping(value = "/v1/organizations/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.DELETE)
    public BaseResponseEntity<Long> deleteData(@PathVariable Long id) throws BaseAppException {
        return organizationsService.deleteData(id);
    }

    @GetMapping(value = "/v1/organizations/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public BaseResponseEntity<OrganizationsResponse.DetailBean> getDataById(@PathVariable Long id)  throws RecordNotExistsException {
        return organizationsService.getDataById(id);
    }

    @GetMapping(value = "/v1/organizations/export", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public ResponseEntity<Object> exportData(OrganizationsRequest.SearchForm dto) throws Exception {
        return organizationsService.exportData(dto);
    }

    @PostMapping(value = "/v1/organizations/import", produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @HasPermission(scope = Scope.IMPORT)
    public ResponseEntity<Object> processImport(@RequestPart MultipartFile file, @RequestParam(required = false) boolean isForceUpdate) throws Exception {
        return organizationsService.processImport(file, isForceUpdate);
    }

    @GetMapping(value = "/v1/organizations/search-list-payroll", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public TableResponseEntity<EmployeesResponse.SearchResult> searchListPayroll(OrganizationsRequest.SearchForm dto) {
        return ResponseUtils.ok(organizationsService.searchListPayroll(dto));
    }


    @GetMapping(value = "/v1/organizations/hierarchy/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ListResponseEntity<OrgHierarchyResponse> getHierarchy(@PathVariable Long id) throws Exception {
        return ResponseUtils.ok(organizationsService.getHierarchy(id));
    }

    @GetMapping(value = "/v1/organizations/chart/{chartType}", produces = MediaType.APPLICATION_JSON_VALUE)
    public BaseResponseEntity<Object> getChart(@PathVariable String chartType,  @RequestParam Long organizationId) throws ExecutionException, InterruptedException {
        return organizationsService.getChart(chartType, organizationId);
    }

    @GetMapping(value = "/v1/organizations/chart-labor-structure", produces = MediaType.APPLICATION_JSON_VALUE)
    public BaseResponseEntity<Object> getChartLaborStructure(@RequestParam Long organizationId)  {
        return organizationsService.getChartLaborStructure(organizationId);
    }

}
