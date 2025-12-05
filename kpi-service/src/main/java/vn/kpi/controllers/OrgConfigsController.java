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
import vn.kpi.models.request.OrgConfigsRequest;
import vn.kpi.services.OrgConfigsService;
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
@Resource(value = Constant.RESOURCES.KPI_ORG_CONFIGS)
public class OrgConfigsController {
    private final OrgConfigsService orgConfigsService;

    @GetMapping(value = "/v1/org-configs", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public TableResponseEntity<OrgConfigsResponse> searchData(OrgConfigsRequest.SearchForm dto) {
        return orgConfigsService.searchData(dto);
    }

    @PostMapping(value = "/v1/org-configs", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.UPDATE)
    public ResponseEntity saveData(@Valid @RequestBody OrgConfigsRequest.SubmitForm dto) throws BaseAppException {
        return orgConfigsService.saveData(dto,null);
    }

    @PutMapping(value = "/v1/org-configs/{id}", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.UPDATE)
    public ResponseEntity update(@Valid @RequestBody OrgConfigsRequest.SubmitForm dto, @PathVariable Long id) throws BaseAppException {
        return orgConfigsService.saveData(dto,id);
    }

    @DeleteMapping(value = "/v1/org-configs/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.DELETE)
    public ResponseEntity deleteData(@PathVariable Long id) throws RecordNotExistsException {
        return orgConfigsService.deleteData(id);
    }

    @GetMapping(value = "/v1/org-configs/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public BaseResponseEntity<OrgConfigsResponse> getDataById(@PathVariable Long id)  throws RecordNotExistsException {
        return orgConfigsService.getDataById(id);
    }

    @GetMapping(value = "/v1/org-configs/export", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public ResponseEntity<Object> exportData(OrgConfigsRequest.SearchForm dto) throws Exception {
        return orgConfigsService.exportData(dto);
    }

}
