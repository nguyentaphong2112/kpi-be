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
import vn.kpi.models.request.ContractTypesRequest;
import vn.kpi.models.response.BaseResponseEntity;
import vn.kpi.models.response.ContractTypesResponse;
import vn.kpi.models.response.ListResponseEntity;
import vn.kpi.models.response.TableResponseEntity;
import vn.kpi.services.ContractTypesService;
import vn.kpi.utils.ResponseUtils;

import javax.validation.Valid;

@RestController
@RequestMapping(BaseConstants.REQUEST_MAPPING_PREFIX)
@RequiredArgsConstructor
@Resource(value = Constant.RESOURCES.CONTRACT_TYPE)
public class ContractTypesController {
    private final ContractTypesService contractTypesService;

    @GetMapping(value = "/v1/contract-types", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public TableResponseEntity<ContractTypesResponse.SearchResult> searchData(ContractTypesRequest.SearchForm dto) {
        return contractTypesService.searchData(dto);
    }

    @PostMapping(value = "/v1/contract-types", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.CREATE)
    public ResponseEntity saveData(@Valid @RequestBody ContractTypesRequest.SubmitForm dto) throws BaseAppException {
        return contractTypesService.saveData(dto, null);
    }

    @PutMapping(value = "/v1/contract-types/{id}", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.UPDATE)
    public ResponseEntity updateData(@Valid @RequestBody ContractTypesRequest.SubmitForm dto, @PathVariable Long id) throws BaseAppException {
        return contractTypesService.saveData(dto, id);
    }

    @DeleteMapping(value = "/v1/contract-types/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.DELETE)
    public ResponseEntity deleteData(@PathVariable Long id) throws RecordNotExistsException {
        return contractTypesService.deleteData(id);
    }

    @GetMapping(value = "/v1/contract-types/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public BaseResponseEntity<ContractTypesResponse.DetailBean> getDataById(@PathVariable Long id) throws RecordNotExistsException {
        return contractTypesService.getDataById(id);
    }

    @GetMapping(value = "/v1/contract-types/export", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public ResponseEntity<Object> exportData(ContractTypesRequest.SearchForm dto) throws Exception {
        return contractTypesService.exportData(dto);
    }

    @GetMapping(value = "/v1/contract-types/list", produces = MediaType.APPLICATION_JSON_VALUE)
    public ListResponseEntity<ContractTypesResponse.DetailBean> getListData(@RequestParam(required = false) String classifyCode,
                                                                            @RequestParam(required = false) Long empTypeId,
                                                                            @RequestParam(required = false) boolean isGetAttribute) {
        return ResponseUtils.ok(contractTypesService.getListData(classifyCode, empTypeId, isGetAttribute));
    }

}
