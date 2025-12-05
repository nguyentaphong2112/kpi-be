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
import vn.kpi.annotations.Resource;
import vn.kpi.constants.BaseConstants;
import vn.kpi.constants.Constant;
import vn.kpi.constants.Scope;
import vn.kpi.exceptions.BaseAppException;
import vn.kpi.exceptions.RecordNotExistsException;
import vn.kpi.models.request.EmpTypesRequest;
import vn.kpi.models.response.BaseResponseEntity;
import vn.kpi.models.response.EmpTypesResponse;
import vn.kpi.models.response.ListResponseEntity;
import vn.kpi.models.response.TableResponseEntity;
import vn.kpi.services.EmpTypesService;

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
