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
import vn.hbtplus.models.request.ProductsRequest;
import vn.hbtplus.services.ProductsService;
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
@Resource(value = Constant.RESOURCES.CRM_PRODUCTS)
public class ProductsController {
    private final ProductsService productsService;

    @GetMapping(value = "/v1/products", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public TableResponseEntity<ProductsResponse> searchData(ProductsRequest.SearchForm dto) {
        return productsService.searchData(dto);
    }

    @PostMapping(value = "/v1/products", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.UPDATE)
    public ResponseEntity saveData(@Valid @RequestBody ProductsRequest.SubmitForm dto) throws BaseAppException {
        return productsService.saveData(dto, null);
    }

    @PutMapping(value = "/v1/products/{id}", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.UPDATE)
    public ResponseEntity saveData(@Valid @RequestBody ProductsRequest.SubmitForm dto, @PathVariable Long id) throws BaseAppException {
        return productsService.saveData(dto, id);
    }

    @DeleteMapping(value = "/v1/products/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.DELETE)
    public ResponseEntity deleteData(@PathVariable Long id) throws RecordNotExistsException {
        return productsService.deleteData(id);
    }

    @GetMapping(value = "/v1/products/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public BaseResponseEntity<ProductsResponse> getDataById(@PathVariable Long id)  throws RecordNotExistsException {
        return productsService.getDataById(id);
    }

    @GetMapping(value = "/v1/products/export", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public ResponseEntity<Object> exportData(ProductsRequest.SearchForm dto) throws Exception {
        return productsService.exportData(dto);
    }

    @GetMapping(value = "/v1/products/download-template", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public ResponseEntity<Object> exportTemplate() throws Exception {
        return productsService.downloadTemplate();
    }

    @PostMapping(value = "/v1/products/import-process", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.VIEW)
    public ResponseEntity<Object> importProcess(@RequestPart(value = "file") MultipartFile file) throws Exception {
        return productsService.importProcess(file);
    }

}
