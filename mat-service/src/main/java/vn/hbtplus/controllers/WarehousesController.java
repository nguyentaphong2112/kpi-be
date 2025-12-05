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
import vn.hbtplus.annotations.Resource;
import vn.hbtplus.constant.Constants;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.constants.Scope;
import vn.hbtplus.exceptions.BaseAppException;
import vn.hbtplus.exceptions.RecordNotExistsException;
import vn.hbtplus.models.request.WarehousesRequest;
import vn.hbtplus.models.response.BaseResponseEntity;
import vn.hbtplus.models.response.EmployeesResponse;
import vn.hbtplus.models.response.TableResponseEntity;
import vn.hbtplus.models.response.WarehousesResponse;
import vn.hbtplus.services.WarehousesService;

import javax.validation.Valid;

@RestController
@RequestMapping(BaseConstants.REQUEST_MAPPING_PREFIX)
@RequiredArgsConstructor
//@Resource(value = Constants.RESOURCE.WAREHOUSES)
public class WarehousesController {
    private final WarehousesService warehousesService;

    @GetMapping(value = "/v1/warehouses", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public ResponseEntity searchData(WarehousesRequest.SearchForm dto) {
        return warehousesService.searchData(dto);
    }

    @GetMapping(value = "/v1/warehouses/search-list", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public ResponseEntity searchList(WarehousesRequest.SearchForm dto) {
        return warehousesService.searchList(dto);
    }

    @PostMapping(value = "/v1/warehouses", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.CREATE)
    public ResponseEntity saveData(@Valid @RequestBody WarehousesRequest.SubmitForm dto) throws BaseAppException {
        return warehousesService.saveData(dto);
    }

    @PutMapping(value = "/v1/warehouses/{id}", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.CREATE)
    public ResponseEntity saveUpdateData(@Valid @RequestBody WarehousesRequest.SubmitForm dto) throws BaseAppException {
        return warehousesService.saveData(dto);
    }

    @DeleteMapping(value = "/v1/warehouses/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.DELETE)
    public ResponseEntity deleteData(@PathVariable Long id) throws RecordNotExistsException {
        return warehousesService.deleteData(id);
    }

    @GetMapping(value = "/v1/warehouses/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public BaseResponseEntity<WarehousesResponse> getDataById(@PathVariable Long id)  throws RecordNotExistsException {
        return warehousesService.getDataById(id);
    }

    @PutMapping(value = "/v1/warehouses/lock-and-unlock/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.DELETE)
    public ResponseEntity processLockOrUnlock(@PathVariable Long id) throws RecordNotExistsException {
        return warehousesService.lockOrUnlockWarehouse(id);
    }

    @GetMapping(value = "/v1/warehouses/export", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public ResponseEntity<Object> exportData(WarehousesRequest.SearchForm dto) throws Exception {
        return warehousesService.exportData(dto);
    }

    @GetMapping(value = "/v1/warehouses/get-emp-by-code/{code}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public BaseResponseEntity<EmployeesResponse> getEmpByCode(@PathVariable String code)  throws RecordNotExistsException {
        return warehousesService.getEmpByCode(code);
    }

}
