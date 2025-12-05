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
import vn.hbtplus.models.response.*;
import vn.hbtplus.models.request.CustomerCareRecordsRequest;
import vn.hbtplus.services.CustomerCareRecordsService;
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
@Resource(value = Constant.RESOURCES.CRM_CUSTOMER_CARE_RECORDS)
public class CustomerCareRecordsController {
    private final CustomerCareRecordsService customerCareRecordsService;

    @GetMapping(value = "/v1/customer-care-records", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public TableResponseEntity<CustomerCareRecordsResponse> searchData(CustomerCareRecordsRequest.SearchForm dto) {
        return customerCareRecordsService.searchData(dto);
    }

    @PostMapping(value = "/v1/customer-care-records", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.UPDATE)
    public ResponseEntity saveData(@Valid @RequestBody CustomerCareRecordsRequest.SubmitForm dto) throws BaseAppException {
        return customerCareRecordsService.saveData(dto, null);
    }

    @PutMapping(value = "/v1/customer-care-records/{id}", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.UPDATE)
    public ResponseEntity saveData(@Valid @RequestBody CustomerCareRecordsRequest.SubmitForm dto, @PathVariable Long id) throws BaseAppException {
        return customerCareRecordsService.saveData(dto,id);
    }

    @DeleteMapping(value = "/v1/customer-care-records/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.DELETE)
    public ResponseEntity deleteData(@PathVariable Long id) throws RecordNotExistsException {
        return customerCareRecordsService.deleteData(id);
    }

    @GetMapping(value = "/v1/customer-care-records/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public BaseResponseEntity<CustomerCareRecordsResponse> getDataById(@PathVariable Long id)  throws RecordNotExistsException {
        return customerCareRecordsService.getDataById(id);
    }


    @GetMapping(value = "/v1/customer-care-records/download-template", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public ResponseEntity<Object> exportTemplate() throws Exception {
        return customerCareRecordsService.downloadTemplate();
    }

    @PostMapping(value = "/v1/customer-care-records/import-process", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.VIEW)
    public ResponseEntity<Object> importProcess(@RequestPart(value = "file") MultipartFile file) throws Exception {
        return customerCareRecordsService.importProcess(file);
    }

    @GetMapping(value = "/v1/customer-care-records/export", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public ResponseEntity<Object> exportData(CustomerCareRecordsRequest.SearchForm dto) throws Exception {
        return customerCareRecordsService.exportData(dto);
    }

}
