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
import vn.hbtplus.models.request.WorkCalendarsRequest;
import vn.hbtplus.models.response.*;
import vn.hbtplus.models.request.ReasonTypesRequest;
import vn.hbtplus.services.ReasonTypesService;
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
@Resource(value = Constant.RESOURCES.ABS_REASON_TYPES)
public class ReasonTypesController {
    private final ReasonTypesService reasonTypesService;

    @GetMapping(value = "/v1/reason-types", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public TableResponseEntity<ReasonTypesResponse> searchData(ReasonTypesRequest.SearchForm dto) {
        return reasonTypesService.searchData(dto);
    }

    @PostMapping(value = "/v1/reason-types", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.UPDATE)
    public BaseResponseEntity saveData(@RequestBody @Valid ReasonTypesRequest.SubmitForm dto) throws BaseAppException {
        return reasonTypesService.saveData(dto , null);
    }

    @PutMapping(value = "/v1/reason-types/{id}", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.UPDATE)
    public BaseResponseEntity<Long> saveData(@RequestBody @Valid ReasonTypesRequest.SubmitForm dto,
                                             @PathVariable Long id) throws BaseAppException {
        return reasonTypesService.saveData(dto , id);
    }

    @DeleteMapping(value = "/v1/reason-types/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.DELETE)
    public ResponseEntity deleteData(@PathVariable Long id) throws RecordNotExistsException {
        return reasonTypesService.deleteData(id);
    }

    @GetMapping(value = "/v1/reason-types/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public BaseResponseEntity<ReasonTypesResponse> getDataById(@PathVariable Long id)  throws RecordNotExistsException {
        return reasonTypesService.getDataById(id);
    }

    @GetMapping(value = "/v1/reason-types/export", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public ResponseEntity<Object> exportData(ReasonTypesRequest.SearchForm dto) throws Exception {
        return reasonTypesService.exportData(dto);
    }


    @GetMapping(value = "/v1/reason-types/all", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public BaseResponseEntity<Object> getAllReasonTypes() {
        Object resultObj = reasonTypesService.getAllReasonLeaves();
        return ResponseUtils.ok(resultObj);
    }

    @GetMapping(value = "/v1/reason-types/list", produces = MediaType.APPLICATION_JSON_VALUE)
    public ListResponseEntity<ReasonTypesResponse> getList(@RequestParam(required = false) boolean isGetAttributes) throws Exception {
        return reasonTypesService.getList(isGetAttributes);
    }

}
