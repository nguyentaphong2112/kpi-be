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
import vn.hbtplus.models.dto.RejectRequestDTO;
import vn.hbtplus.models.dto.RequestLeavesDTO;
import vn.hbtplus.models.response.*;
import vn.hbtplus.models.request.RequestsRequest;
import vn.hbtplus.services.ApproversService;
import vn.hbtplus.services.RequestsService;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.constants.Scope;
import vn.hbtplus.constants.Constant;
import vn.hbtplus.annotations.Resource;
import vn.hbtplus.exceptions.RecordNotExistsException;
import vn.hbtplus.exceptions.BaseAppException;

import java.util.List;

@RestController
@RequestMapping(BaseConstants.REQUEST_MAPPING_PREFIX)
@RequiredArgsConstructor
@Resource(value = Constant.RESOURCES.ABS_REQUEST_MANAGER)
public class RequestsController {
    private final RequestsService requestsService;
    private final ApproversService approveRequests;

    @GetMapping(value = "/v1/requests", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public TableResponseEntity<RequestsResponse> searchData(RequestsRequest.SearchForm dto) {
        return requestsService.searchData(dto);
    }

    @PostMapping(value = "/v1/requests", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.UPDATE)
    public ResponseEntity saveData(@RequestPart(value = "data") RequestsRequest.SubmitForm dto,
                                   @RequestPart(value = "fileRequest", required = false) MultipartFile fileRequest
    ) throws BaseAppException {
        return requestsService.saveData(dto , fileRequest, null );
    }

    @PutMapping(value = "/v1/requests/{id}", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.UPDATE)
    public ResponseEntity saveData(@RequestPart(value = "data") RequestsRequest.SubmitForm dto,
                                   @RequestPart(value = "fileRequest", required = false) MultipartFile fileRequest
            , @PathVariable Long id) throws BaseAppException {
        return requestsService.saveData(dto, fileRequest, id);
    }

    @DeleteMapping(value = "/v1/requests/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.DELETE)
    public ResponseEntity deleteData(@PathVariable Long id) throws RecordNotExistsException {
        return requestsService.deleteData(id);
    }

    @GetMapping(value = "/v1/requests/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public BaseResponseEntity<RequestsResponse> getDataById(@PathVariable Long id)  throws RecordNotExistsException {
        return requestsService.getDataById(id);
    }

    @GetMapping(value = "/v1/requests/export", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public ResponseEntity<Object> exportData(RequestsRequest.SearchForm dto) throws Exception {
        return requestsService.exportData(dto);
    }

    @PostMapping(value = "/v1/requests/approve-by-list")
    @HasPermission(scope = Scope.APPROVE)
    public ResponseEntity<Object> approveRequests(@RequestParam Long listId) {
        return approveRequests.approveRequests(listId);
    }

    @PostMapping(value = "/v1/reject-by-list")
    @HasPermission(scope = Scope.UPDATE)
    public ResponseEntity<Object> rejectRequests(@RequestBody RejectRequestDTO rejectRequestDTO) {
        return approveRequests.rejectRequests(rejectRequestDTO);
    }

    @PostMapping(value = "/v1/requests/approve-all")
    @HasPermission(scope = Scope.APPROVE)
    public ResponseEntity approveAll() {
        return approveRequests.approveAll();
    }

}
