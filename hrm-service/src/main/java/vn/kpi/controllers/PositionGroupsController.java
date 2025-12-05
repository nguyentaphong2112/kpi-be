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
import vn.kpi.models.request.PositionGroupsRequest;
import vn.kpi.models.response.BaseResponseEntity;
import vn.kpi.models.response.ListResponseEntity;
import vn.kpi.models.response.PositionGroupsResponse;
import vn.kpi.models.response.TableResponseEntity;
import vn.kpi.services.PositionGroupsService;

import javax.validation.Valid;

@RestController
@RequestMapping(BaseConstants.REQUEST_MAPPING_PREFIX)
@RequiredArgsConstructor
@Resource(value = Constant.RESOURCES.POSITION_GROUP)
public class PositionGroupsController {
    private final PositionGroupsService positionGroupsService;

    @GetMapping(value = "/v1/position-groups", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public TableResponseEntity<PositionGroupsResponse> searchData(PositionGroupsRequest.SearchForm dto) {
        return positionGroupsService.searchData(dto);
    }

    @PostMapping(value = "/v1/position-groups", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.UPDATE)
    public ResponseEntity saveData(@Valid @RequestBody PositionGroupsRequest.SubmitForm dto) throws BaseAppException {
        return positionGroupsService.saveData(dto, null);
    }

    @PutMapping(value = "/v1/position-groups/{id}", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.UPDATE)
    public ResponseEntity updateData(@Valid @RequestBody PositionGroupsRequest.SubmitForm dto, @PathVariable Long id) throws BaseAppException {
        return positionGroupsService.saveData(dto, id);
    }

    @DeleteMapping(value = "/v1/position-groups/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.DELETE)
    public ResponseEntity deleteData(@PathVariable Long id) throws RecordNotExistsException {
        return positionGroupsService.deleteData(id);
    }

    @GetMapping(value = "/v1/position-groups/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public BaseResponseEntity<PositionGroupsResponse.DetailBean> getDataById(@PathVariable Long id) throws RecordNotExistsException {
        return positionGroupsService.getDataById(id);
    }

    @GetMapping(value = "/v1/position-groups/export", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public ResponseEntity<Object> exportData(PositionGroupsRequest.SearchForm dto) throws Exception {
        return positionGroupsService.exportData(dto);
    }

    @GetMapping(value = "/v1/position-groups/list", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public ListResponseEntity<PositionGroupsResponse.DetailBean> getListData() {
        return positionGroupsService.getListData();
    }
}
