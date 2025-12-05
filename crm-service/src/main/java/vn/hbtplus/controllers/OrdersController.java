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
import vn.hbtplus.models.request.OrdersRequest;
import vn.hbtplus.services.OrdersService;
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
public class OrdersController {
    private final OrdersService ordersService;

    @GetMapping(value = "/v1/orders", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public TableResponseEntity<OrdersResponse> searchData(OrdersRequest.SearchForm dto) {
        return ordersService.searchData(dto);
    }

    @PostMapping(value = "/v1/orders", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.UPDATE)
    public ResponseEntity saveData(@Valid @RequestBody OrdersRequest.SubmitForm dto) throws BaseAppException {
        return ordersService.saveData(dto);
    }

    @PutMapping(value = "/v1/orders/{id}", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.UPDATE)
    public ResponseEntity updateData(@Valid @ModelAttribute OrdersRequest.SubmitForm dto) throws BaseAppException {
        return ordersService.saveData(dto);
    }

    @DeleteMapping(value = "/v1/orders/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.DELETE)
    public ResponseEntity deleteData(@PathVariable Long id) throws RecordNotExistsException {
        return ordersService.deleteData(id);
    }

    @GetMapping(value = "/v1/orders/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public BaseResponseEntity<OrdersResponse> getDataById(@PathVariable Long id)  throws RecordNotExistsException {
        return ordersService.getDataById(id);
    }

    @GetMapping(value = "/v1/orders/export", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public ResponseEntity<Object> exportData(OrdersRequest.SearchForm dto) throws Exception {
        return ordersService.exportData(dto);
    }

}
