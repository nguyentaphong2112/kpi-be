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
import vn.hbtplus.annotations.Resource;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.constants.Constant;
import vn.hbtplus.constants.Scope;
import vn.hbtplus.exceptions.BaseAppException;
import vn.hbtplus.exceptions.RecordNotExistsException;
import vn.hbtplus.models.request.EmpTypesRequest;
import vn.hbtplus.models.response.BaseResponseEntity;
import vn.hbtplus.models.response.EmpTypesResponse;
import vn.hbtplus.models.response.ListResponseEntity;
import vn.hbtplus.models.response.TableResponseEntity;
import vn.hbtplus.services.EmpTypesService;

import javax.validation.Valid;

@RestController
@RequestMapping(BaseConstants.REQUEST_MAPPING_PREFIX)
@RequiredArgsConstructor
@Resource(value = Constant.RESOURCES.EMP_TYPES)
public class EmpTypesController {
    private final EmpTypesService empTypesService;

    @GetMapping(value = "/v1/emp-types", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public TableResponseEntity<EmpTypesResponse.SearchResult> searchData(EmpTypesRequest.SearchForm dto) {
        return empTypesService.searchData(dto);
    }

    @PostMapping(value = "/v1/emp-types", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.CREATE)
    public ResponseEntity saveData(@Valid @RequestBody EmpTypesRequest.SubmitForm dto) throws BaseAppException {
        return empTypesService.saveData(dto, null);
    }

    @PutMapping(value = "/v1/emp-types/{id}", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.UPDATE)
    public ResponseEntity updateData(@Valid @RequestBody EmpTypesRequest.SubmitForm dto, @PathVariable Long id) throws BaseAppException {
        return empTypesService.saveData(dto, id);
    }

    @DeleteMapping(value = "/v1/emp-types/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.DELETE)
    public ResponseEntity deleteData(@PathVariable Long id) throws RecordNotExistsException {
        return empTypesService.deleteData(id);
    }

    @GetMapping(value = "/v1/emp-types/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public BaseResponseEntity<EmpTypesResponse.DetailBean> getDataById(@PathVariable Long id) throws RecordNotExistsException {
        return empTypesService.getDataById(id);
    }

    @GetMapping(value = "/v1/emp-types/export", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public ResponseEntity<Object> exportData(EmpTypesRequest.SearchForm dto) throws Exception {
        return empTypesService.exportData(dto);
    }

    @GetMapping(value = "/v1/emp-types/list", produces = MediaType.APPLICATION_JSON_VALUE)
    public ListResponseEntity<EmpTypesResponse.DetailBean> getList(@RequestParam(required = false) boolean isGetAttributes) throws Exception {
        return empTypesService.getList(isGetAttributes);
    }

}
