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
import vn.hbtplus.models.request.PaymentsRequest;
import vn.hbtplus.services.PaymentsService;
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
public class PaymentsController {
    private final PaymentsService paymentsService;

    @GetMapping(value = "/v1/payments", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public TableResponseEntity<PaymentsResponse> searchData(PaymentsRequest.SearchForm dto) {
        return paymentsService.searchData(dto);
    }

    @PostMapping(value = "/v1/payments", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.UPDATE)
    public ResponseEntity saveData(@Valid @RequestBody PaymentsRequest.SubmitForm dto) throws BaseAppException {
        return paymentsService.saveData(dto);
    }

    @DeleteMapping(value = "/v1/payments/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.DELETE)
    public ResponseEntity deleteData(@PathVariable Long id) throws RecordNotExistsException {
        return paymentsService.deleteData(id);
    }

    @GetMapping(value = "/v1/payments/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public BaseResponseEntity<PaymentsResponse> getDataById(@PathVariable Long id)  throws RecordNotExistsException {
        return paymentsService.getDataById(id);
    }

    @GetMapping(value = "/v1/payments/export", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public ResponseEntity<Object> exportData(PaymentsRequest.SearchForm dto) throws Exception {
        return paymentsService.exportData(dto);
    }

}
