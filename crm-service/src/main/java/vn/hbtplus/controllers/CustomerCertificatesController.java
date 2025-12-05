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
import vn.hbtplus.models.request.CustomerCertificatesRequest;
import vn.hbtplus.services.CustomerCertificatesService;
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
@Resource(value = Constant.RESOURCES.CRM_CUSTOMER_CERTIFICATES)
public class CustomerCertificatesController {
    private final CustomerCertificatesService customerCertificatesService;

    @GetMapping(value = "/v1/customer-certificates", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public TableResponseEntity<CustomerCertificatesResponse.SearchResult> searchData(CustomerCertificatesRequest.SearchForm dto) {
        return customerCertificatesService.searchData(dto);
    }

    @PostMapping(value = "/v1/customer-certificates", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.CREATE)
    public ResponseEntity saveData(@Valid @RequestBody CustomerCertificatesRequest.SubmitForm dto) throws BaseAppException {
        return customerCertificatesService.saveData(dto, null);
    }

    @PutMapping(value = "/v1/customer-certificates/{id}", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.UPDATE)
    public ResponseEntity updateData(@Valid @RequestBody CustomerCertificatesRequest.SubmitForm dto, @PathVariable Long id) throws BaseAppException {
        return customerCertificatesService.saveData(dto, id);
    }

    @DeleteMapping(value = "/v1/customer-certificates/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.DELETE)
    public ResponseEntity deleteData(@PathVariable Long id) throws RecordNotExistsException {
        return customerCertificatesService.deleteData(id);
    }

    @GetMapping(value = "/v1/customer-certificates/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public BaseResponseEntity<CustomerCertificatesResponse.DetailBean> getDataById(@PathVariable Long id)  throws RecordNotExistsException {
        return customerCertificatesService.getDataById(id);
    }

    @PostMapping(value = "/v1/customer-certificates/status/{id}", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.UPDATE)
    public ResponseEntity updateStatusByOrg(@Valid @RequestBody CustomerCertificatesRequest.SubmitForm dto, @PathVariable Long id) throws BaseAppException {
        return customerCertificatesService.updateStatusById(dto, id);
    }

    @GetMapping(value = "/v1/customer-certificates/export", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public ResponseEntity<Object> exportData(CustomerCertificatesRequest.SearchForm dto) throws Exception {
        return customerCertificatesService.exportData(dto);
    }

}
