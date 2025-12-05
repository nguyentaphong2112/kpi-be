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
import vn.hbtplus.models.BaseSearchRequest;
import vn.hbtplus.models.response.*;
import vn.hbtplus.models.request.MembersRequest;
import vn.hbtplus.services.MembersService;
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
@Resource(value = Constant.RESOURCES.EMPLOYEE)
public class MembersController {
    private final MembersService membersService;

    @GetMapping(value = "/v1/members", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public TableResponseEntity<MembersResponse.SearchResult> searchData(MembersRequest.SearchForm dto) {
        return membersService.searchData(dto);
    }

    @PostMapping(value = "/v1/members", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.UPDATE)
    public ResponseEntity saveData(@Valid @RequestBody MembersRequest.SubmitForm dto) throws BaseAppException {
        return membersService.saveData(dto, null);
    }

    @PutMapping(value = "/v1/members/{id}", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.UPDATE)
    public ResponseEntity updateData(@Valid @RequestBody MembersRequest.SubmitForm dto, @PathVariable Long id) throws BaseAppException {
        return membersService.saveData(dto, id);
    }

    @DeleteMapping(value = "/v1/members/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.DELETE)
    public ResponseEntity deleteData(@PathVariable Long id) throws RecordNotExistsException {
        return membersService.deleteData(id);
    }

    @GetMapping(value = "/v1/members/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public BaseResponseEntity<MembersResponse.DetailBean> getDataById(@PathVariable Long id) throws RecordNotExistsException {
        return membersService.getDataById(id);
    }

    @GetMapping(value = "/v1/members/pageable", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public TableResponseEntity<MembersResponse.DetailBean> getPageable(BaseSearchRequest request) {
        return ResponseUtils.ok(membersService.getPageable(request));
    }

    @GetMapping(value = "/v1/members/export", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public ResponseEntity<Object> exportData(MembersRequest.SearchForm dto) throws Exception {
        return membersService.exportData(dto);
    }

}
