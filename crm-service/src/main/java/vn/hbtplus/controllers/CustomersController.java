/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import vn.hbtplus.annotations.HasPermission;
import vn.hbtplus.annotations.Resource;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.constants.Constant;
import vn.hbtplus.constants.Scope;
import vn.hbtplus.exceptions.BaseAppException;
import vn.hbtplus.exceptions.RecordNotExistsException;
import vn.hbtplus.models.request.CustomerCareRecordsRequest;
import vn.hbtplus.models.request.CustomersRequest;
import vn.hbtplus.models.request.PartnersRequest;
import vn.hbtplus.models.response.*;
import vn.hbtplus.services.CustomersService;

import javax.validation.Valid;

@RestController
@RequestMapping(BaseConstants.REQUEST_MAPPING_PREFIX)
@RequiredArgsConstructor
@Resource(value = Constant.RESOURCES.CRM_CUSTOMERS)
public class CustomersController {
    private final CustomersService customersService;

    @GetMapping(value = "/v1/customers", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public TableResponseEntity<CustomersResponse.SearchResult> searchData(CustomersRequest.SearchForm dto) {
        return customersService.searchData(dto);
    }
    @GetMapping(value = "/v1/customers/pageable", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public TableResponseEntity<CustomersResponse.SearchResult> getListPageable(CustomersRequest.SearchForm dto) {
        return customersService.getListPageable(dto);
    }

    @PostMapping(value = "/v1/customers", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.CREATE)
    public ResponseEntity saveData(@Valid @RequestBody CustomersRequest.SubmitForm dto) throws BaseAppException {
        return customersService.saveData(dto, null);
    }

    @PutMapping(value = "/v1/customers/{id}", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.UPDATE)
    public ResponseEntity upDateData(@Valid @RequestBody CustomersRequest.SubmitForm dto, @PathVariable Long id) throws BaseAppException {
        return customersService.saveData(dto, id);
    }

    @DeleteMapping(value = "/v1/customers/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.DELETE)
    public ResponseEntity deleteData(@PathVariable Long id) throws RecordNotExistsException {
        return customersService.deleteData(id);
    }

    @GetMapping(value = "/v1/customers/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public BaseResponseEntity<CustomersResponse.DetailBean> getDataById(@PathVariable Long id)  throws RecordNotExistsException {
        return customersService.getDataById(id);
    }

    @GetMapping(value = "/v1/customers/export", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public ResponseEntity<Object> exportData(CustomersRequest.SearchForm dto) throws Exception {
        return customersService.exportData(dto);
    }

    @PostMapping(value = "/v1/customers/export-card", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.VIEW)
    public ResponseEntity<Object> exportCard(@RequestBody PartnersRequest.PrintCard dto) throws Exception {
        return customersService.exportCard(dto);
    }

    @PostMapping(value = "/v1/customers/care", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.VIEW)
    public ResponseEntity<Object> customerCare(@RequestBody CustomerCareRecordsRequest.SubmitForm dto) {
        return customersService.customerCare(dto);
    }

    @PostMapping(value = "/v1/customers/course", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.VIEW)
    public ResponseEntity<Object> addCourse(@RequestBody CustomersRequest.CourseForm dto) {
        return customersService.addCourse(dto);
    }

    @GetMapping(value = "/v1/customers/export-template", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public ResponseEntity<Object> exportTemplate() throws Exception {
        return customersService.exportTemplate();
    }

    @PostMapping(value = "/v1/customers/import-process", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.VIEW)
    public ResponseEntity<Object> importProcess(@RequestPart(value = "file") MultipartFile file) throws Exception {
        return customersService.importProcess(file);
    }

    @GetMapping(value = "/v1/customers/list", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public ListResponseEntity<CustomersResponse.DataSelected> getListData() throws Exception {
        return customersService.getListData();
    }

}
