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
import vn.hbtplus.models.request.OrderDetailsRequest;
import vn.hbtplus.services.OrderDetailsService;
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
public class OrderDetailsController {
    private final OrderDetailsService orderDetailsService;

    @GetMapping(value = "/v1/order-details", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public TableResponseEntity<OrderDetailsResponse> searchData(OrderDetailsRequest.SearchForm dto) {
        return orderDetailsService.searchData(dto);
    }

    @PostMapping(value = "/v1/order-details", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.UPDATE)
    public ResponseEntity saveData(@Valid @RequestBody OrderDetailsRequest.SubmitForm dto) throws BaseAppException {
        return orderDetailsService.saveData(dto);
    }

    @DeleteMapping(value = "/v1/order-details/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.DELETE)
    public ResponseEntity deleteData(@PathVariable Long id) throws RecordNotExistsException {
        return orderDetailsService.deleteData(id);
    }

    @GetMapping(value = "/v1/order-details/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public BaseResponseEntity<OrderDetailsResponse> getDataById(@PathVariable Long id)  throws RecordNotExistsException {
        return orderDetailsService.getDataById(id);
    }

    @GetMapping(value = "/v1/order-details/export", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public ResponseEntity<Object> exportData(OrderDetailsRequest.SearchForm dto) throws Exception {
        return orderDetailsService.exportData(dto);
    }

}
