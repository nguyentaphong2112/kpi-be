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
import vn.kpi.models.request.ConfigMappingsRequest;
import vn.kpi.services.ConfigMappingsService;
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
@Resource(value = Constant.RESOURCES.CONFIG_PARAMETER)
public class ConfigMappingsController {
    private final ConfigMappingsService configMappingsService;

    @GetMapping(value = "/v1/config-mappings", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public TableResponseEntity<ConfigMappingsResponse> searchData(ConfigMappingsRequest.SearchForm dto) {
        return configMappingsService.searchData(dto);
    }

    @PostMapping(value = "/v1/config-mappings", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.UPDATE)
    public ResponseEntity saveData(@Valid @RequestBody ConfigMappingsRequest.SubmitForm dto) throws BaseAppException {
        return configMappingsService.saveData(dto,null);
    }

    @PutMapping(value = "/v1/config-mappings/{id}", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.UPDATE)
    public ResponseEntity updateData(@Valid @RequestBody ConfigMappingsRequest.SubmitForm dto, @PathVariable Long id) throws BaseAppException {
        return configMappingsService.saveData(dto,id);
    }

    @DeleteMapping(value = "/v1/config-mappings/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.DELETE)
    public ResponseEntity deleteData(@PathVariable Long id) throws RecordNotExistsException {
        return configMappingsService.deleteData(id);
    }

    @GetMapping(value = "/v1/config-mappings/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public BaseResponseEntity<ConfigMappingsResponse> getDataById(@PathVariable Long id)  throws RecordNotExistsException {
        return configMappingsService.getDataById(id);
    }

    @GetMapping(value = "/v1/config-mappings/export", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public ResponseEntity<Object> exportData(ConfigMappingsRequest.SearchForm dto) throws Exception {
        return configMappingsService.exportData(dto);
    }

}
