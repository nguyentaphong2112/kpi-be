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
import vn.hbtplus.models.request.CustomerCertificatesRequest;
import vn.hbtplus.models.response.*;
import vn.hbtplus.models.request.OrderPayablesRequest;
import vn.hbtplus.repositories.entity.OrderPayablesEntity;
import vn.hbtplus.services.OrderPayablesService;
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
@Resource(value = Constant.RESOURCES.CRM_ORDER_PAYABLE)
public class OrderPayablesController {
    private final OrderPayablesService orderPayablesService;

    @GetMapping(value = "/v1/order-payables", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public TableResponseEntity<OrderPayablesResponse> searchData(OrderPayablesRequest.SearchForm dto) {
        return orderPayablesService.searchData(dto);
    }
    @PostMapping(value = "/v1/order-payables/make-list", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.CREATE)
    public ResponseEntity makeList(@Valid @RequestBody OrderPayablesRequest.MakeListForm dto) throws BaseAppException {
        return ResponseUtils.ok(orderPayablesService.makeList(dto));
    }

    @PostMapping(value = "/v1/order-payables", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.CREATE)
    public ResponseEntity saveData(@Valid @RequestBody OrderPayablesRequest.SubmitForm dto) throws BaseAppException {
        return orderPayablesService.saveData(dto, null);
    }

    @PutMapping(value = "/v1/order-payables/{id}", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.UPDATE)
    public ResponseEntity saveData(@Valid @RequestBody OrderPayablesRequest.SubmitForm dto, @PathVariable Long id) throws BaseAppException {
        return orderPayablesService.saveData(dto, id);
    }

    @DeleteMapping(value = "/v1/order-payables/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.DELETE)
    public ResponseEntity deleteData(@PathVariable Long id) throws RecordNotExistsException {
        return orderPayablesService.deleteData(id);
    }

    @GetMapping(value = "/v1/order-payables/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public BaseResponseEntity<OrderPayablesResponse> getDataById(@PathVariable Long id)  throws RecordNotExistsException {
        return orderPayablesService.getDataById(id);
    }

    @GetMapping(value = "/v1/order-payables/export", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public ResponseEntity<Object> exportData(OrderPayablesRequest.SearchForm dto) throws Exception {
        return orderPayablesService.exportData(dto);
    }

    @PostMapping(value = "/v1/order-payables/status/{id}", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.APPROVE)
    public ResponseEntity updateStatusByOrg(@Valid @RequestBody OrderPayablesRequest.SubmitForm dto, @PathVariable Long id) throws BaseAppException {
        return orderPayablesService.updateStatusById(dto, id);
    }

    @PostMapping(value = "/v1/order-payables/approve-all", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.APPROVE)
    public ResponseEntity approveAll(@Valid @RequestBody OrderPayablesRequest.SubmitForm dto) throws BaseAppException {
        return orderPayablesService.approveAll(dto);
    }

    @PutMapping(value = "/v1/order-payables/undoApprove/{id}", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.APPROVE)
    public ResponseEntity undoApprove( @PathVariable Long id) throws BaseAppException {
        OrderPayablesRequest.SubmitForm dto = new OrderPayablesRequest.SubmitForm();
        dto.setStatusId(OrderPayablesEntity.STATUS.CHO_PHE_DUYET);
        return orderPayablesService.updateStatusById(dto, id);
    }
}
