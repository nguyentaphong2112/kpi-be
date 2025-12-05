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
import vn.hbtplus.models.request.ShipmentsRequest;
import vn.hbtplus.services.ShipmentsService;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.constants.Scope;
import vn.hbtplus.constants.Constant;
import vn.hbtplus.annotations.Resource;
import vn.hbtplus.exceptions.RecordNotExistsException;
import vn.hbtplus.exceptions.BaseAppException;
import javax.validation.Valid;

@RestController
@RequestMapping(BaseConstants.REQUEST_MAPPING_PREFIX)
@RequiredArgsConstructor
@Resource(value = Constant.RESOURCES.EMPLOYEE)
public class ShipmentsController {
    private final ShipmentsService shipmentsService;

    @GetMapping(value = "/v1/shipments", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public TableResponseEntity<ShipmentsResponse> searchData(ShipmentsRequest.SearchForm dto) {
        return shipmentsService.searchData(dto);
    }

    @PostMapping(value = "/v1/shipments", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.UPDATE)
    public ResponseEntity saveData(@Valid @RequestBody ShipmentsRequest.SubmitForm dto) throws BaseAppException {
        return shipmentsService.saveData(dto);
    }

    @DeleteMapping(value = "/v1/shipments/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.DELETE)
    public ResponseEntity deleteData(@PathVariable Long id) throws RecordNotExistsException {
        return shipmentsService.deleteData(id);
    }

    @GetMapping(value = "/v1/shipments/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public BaseResponseEntity<ShipmentsResponse> getDataById(@PathVariable Long id)  throws RecordNotExistsException {
        return shipmentsService.getDataById(id);
    }

    @GetMapping(value = "/v1/shipments/export", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public ResponseEntity<Object> exportData(ShipmentsRequest.SearchForm dto) throws Exception {
        return shipmentsService.exportData(dto);
    }

}
