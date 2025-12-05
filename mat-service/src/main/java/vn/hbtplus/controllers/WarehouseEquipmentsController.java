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
import vn.hbtplus.models.request.WarehouseEquipmentsRequest;
import vn.hbtplus.models.response.BaseResponseEntity;
import vn.hbtplus.models.response.TableResponseEntity;
import vn.hbtplus.models.response.WarehouseEquipmentsResponse;
import vn.hbtplus.services.WarehouseEquipmentsService;

import javax.validation.Valid;

@RestController
@RequestMapping(BaseConstants.REQUEST_MAPPING_PREFIX)
@RequiredArgsConstructor
//@Resource(value = Constants.RESOURCE.WAREHOUSES)
public class WarehouseEquipmentsController {
    private final WarehouseEquipmentsService warehouseEquipmentsService;

    @GetMapping(value = "/v1/warehouse-equipments", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public TableResponseEntity<WarehouseEquipmentsResponse> searchData(WarehouseEquipmentsRequest.SearchForm dto) {
        return warehouseEquipmentsService.searchData(dto);
    }

    @PostMapping(value = "/v1/warehouse-equipments", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.CREATE)
    public ResponseEntity saveData(@Valid @RequestBody WarehouseEquipmentsRequest.SubmitForm dto) throws BaseAppException {
        return warehouseEquipmentsService.saveData(dto);
    }

    @PutMapping(value = "/v1/warehouse-equipments/{id}", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.CREATE)
    public ResponseEntity saveUpdateData(@Valid @RequestBody WarehouseEquipmentsRequest.SubmitForm dto) throws BaseAppException {
        return warehouseEquipmentsService.saveData(dto);
    }

    @DeleteMapping(value = "/v1/warehouse-equipments/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.DELETE)
    public ResponseEntity deleteData(@PathVariable Long id) throws RecordNotExistsException {
        return warehouseEquipmentsService.deleteData(id);
    }

    @GetMapping(value = "/v1/warehouse-equipments/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public BaseResponseEntity<WarehouseEquipmentsResponse> getDataById(@PathVariable Long id)  throws RecordNotExistsException {
        return warehouseEquipmentsService.getDataById(id);
    }

    @GetMapping(value = "/v1/warehouse-equipments/export", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public ResponseEntity<Object> exportData(WarehouseEquipmentsRequest.SearchForm dto) throws Exception {
        return warehouseEquipmentsService.exportData(dto);
    }

}
