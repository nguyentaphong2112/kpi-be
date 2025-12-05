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
import vn.hbtplus.models.request.WarningConfigsRequest;
import vn.hbtplus.services.WarningConfigsService;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.constants.Scope;
import vn.hbtplus.constants.Constant;
import vn.hbtplus.annotations.Resource;
import vn.hbtplus.exceptions.RecordNotExistsException;
import vn.hbtplus.exceptions.BaseAppException;
import vn.hbtplus.utils.ResponseUtils;

import javax.validation.Valid;

@RestController
@RequestMapping(BaseConstants.REQUEST_MAPPING_PREFIX)
@RequiredArgsConstructor
@Resource(value = Constant.RESOURCES.WARNING_CONFIG)
public class WarningConfigsController {
    private final WarningConfigsService warningConfigsService;

    @GetMapping(value = "/v1/warning-configs", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public TableResponseEntity<WarningConfigsResponse> searchData(WarningConfigsRequest.SearchForm dto) {
        return warningConfigsService.searchData(dto);
    }

    @PostMapping(value = "/v1/warning-configs", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.UPDATE)
    public ResponseEntity saveData(@Valid @RequestBody WarningConfigsRequest.SubmitForm dto) throws BaseAppException {
        return warningConfigsService.saveData(dto, null);
    }

    @PutMapping(value = "/v1/warning-configs/{id}", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.UPDATE)
    public ResponseEntity saveData(@Valid @RequestBody WarningConfigsRequest.SubmitForm dto, @PathVariable Long id) throws BaseAppException {
        return warningConfigsService.saveData(dto, id);
    }

    @DeleteMapping(value = "/v1/warning-configs/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.DELETE)
    public ResponseEntity deleteData(@PathVariable Long id) throws RecordNotExistsException {
        return warningConfigsService.deleteData(id);
    }

    @GetMapping(value = "/v1/warning-configs/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public BaseResponseEntity<WarningConfigsResponse> getDataById(@PathVariable Long id) throws RecordNotExistsException {
        return warningConfigsService.getDataById(id);
    }

    @GetMapping(value = "/v1/warning-configs/list", produces = MediaType.APPLICATION_JSON_VALUE)
    public ListResponseEntity<WarningConfigsResponse> getListWarning() {
        return ResponseUtils.ok(warningConfigsService.getListWarning());
    }

    @GetMapping(value = "/v1/warning-configs/export", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public ResponseEntity<Object> exportData(WarningConfigsRequest.SearchForm dto) throws Exception {
        return warningConfigsService.exportData(dto);
    }

}
