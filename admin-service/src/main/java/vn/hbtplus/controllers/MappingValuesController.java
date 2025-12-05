/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import vn.hbtplus.annotations.HasPermission;
import vn.hbtplus.models.response.*;
import vn.hbtplus.models.request.MappingValuesRequest;
import vn.hbtplus.services.ConfigMappingsService;
import vn.hbtplus.services.MappingValuesService;
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
@Resource(value = Constant.RESOURCES.SYS_MAPPING_VALUES)
public class MappingValuesController {
    private final MappingValuesService mappingValuesService;

    @GetMapping(value = "/v1/mapping-values/{configMappingCode}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public TableResponseEntity<MappingValuesResponse> searchData(MappingValuesRequest.SearchForm dto, @PathVariable String configMappingCode) {
        return mappingValuesService.searchData(dto,configMappingCode);
    }

    @PostMapping(value = "/v1/mapping-values/{configMappingCode}", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.UPDATE)
    public ResponseEntity saveData(@Valid @RequestBody MappingValuesRequest.SubmitForm dto, @PathVariable String configMappingCode) throws BaseAppException {
        return mappingValuesService.saveData(dto,configMappingCode, null);
    }

    @PutMapping(value = "/v1/mapping-values/{configMappingCode}/{id}", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.UPDATE)
    public ResponseEntity updateData(@Valid @RequestBody MappingValuesRequest.SubmitForm dto, @PathVariable String configMappingCode, @PathVariable Long id) throws BaseAppException {
        return mappingValuesService.saveData(dto,configMappingCode, id);
    }

    @DeleteMapping(value = "/v1/mapping-values/{configMappingCode}/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.DELETE)
    public ResponseEntity deleteData(@PathVariable Long id, @PathVariable String configMappingCode) throws RecordNotExistsException {
        return mappingValuesService.deleteData(id);
    }

    @GetMapping(value = "/v1/mapping-values/{configMappingCode}/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public BaseResponseEntity<MappingValuesResponse> getDataById(@PathVariable Long id,@PathVariable String configMappingCode)  throws RecordNotExistsException {
        return mappingValuesService.getDataById(id);
    }

    @GetMapping(value = "/v1/mapping-values/{configMappingCode}/export", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public ResponseEntity<Object> exportData(MappingValuesRequest.SearchForm dto) throws Exception {
        return mappingValuesService.exportData(dto);
    }

    @GetMapping(value = "/v1/mapping-values/{configMappingCode}/download-template", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.CREATE)
    public ResponseEntity<Object> downloadTemplate(@PathVariable String configMappingCode) throws Exception {
        return mappingValuesService.downloadTemplate(configMappingCode);
    }

    @PostMapping(value = "/v1/mapping-values/{configMappingCode}/import", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.IMPORT)
    public ResponseEntity importData(@RequestPart(value = "file") MultipartFile file, MappingValuesRequest.ImportForm dto,@PathVariable String configMappingCode) throws Exception {
        return ResponseUtils.ok(mappingValuesService.importData(file,dto,configMappingCode));
    }

}
