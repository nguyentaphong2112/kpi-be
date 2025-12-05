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
import vn.hbtplus.annotations.Resource;
import vn.hbtplus.constant.Constants;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.constants.Scope;
import vn.hbtplus.exceptions.BaseAppException;
import vn.hbtplus.exceptions.RecordNotExistsException;
import vn.hbtplus.models.request.EquipmentsRequest;
import vn.hbtplus.models.response.BaseResponseEntity;
import vn.hbtplus.models.response.EquipmentsResponse;
import vn.hbtplus.models.response.ListResponseEntity;
import vn.hbtplus.models.response.TableResponseEntity;
import vn.hbtplus.services.EquipmentsService;
import vn.hbtplus.utils.ResponseUtils;

import javax.validation.Valid;

@RestController
@RequestMapping(BaseConstants.REQUEST_MAPPING_PREFIX)
@RequiredArgsConstructor
//@Resource(value = Constants.RESOURCE.FPN_EQUIPMENTS)
public class EquipmentsController {
    private final EquipmentsService equipmentsService;

    @GetMapping(value = "/v1/equipments", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public TableResponseEntity<EquipmentsResponse> searchData(EquipmentsRequest.SearchForm dto) {
        return equipmentsService.searchData(dto);
    }

    @GetMapping(value = "/v1/equipments/search/list", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public TableResponseEntity<EquipmentsResponse> searchListData(EquipmentsRequest.SearchForm dto) {
        return equipmentsService.searchListData(dto);
    }

    @GetMapping(value = "/v1/equipments/list-by-type", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public ListResponseEntity<EquipmentsResponse> getListByType(@RequestParam(required = false) Long equipmentTypeId) {
        return ResponseUtils.ok(equipmentsService.getListByType(equipmentTypeId));
    }

    @PostMapping(value = "/v1/equipments", consumes = {MediaType.APPLICATION_JSON_VALUE})
    @HasPermission(scope = Scope.CREATE)
    public ResponseEntity saveData(@RequestBody @Valid EquipmentsRequest.SubmitForm dto) throws BaseAppException {
        return equipmentsService.saveData(dto);
    }

    @PutMapping(value = "/v1/equipments/{id}", consumes = {MediaType.APPLICATION_JSON_VALUE})
    @HasPermission(scope = Scope.CREATE)
    public ResponseEntity saveUpdateData(@RequestBody @Valid EquipmentsRequest.SubmitForm dto) throws BaseAppException {
        return equipmentsService.saveData(dto);
    }

    @DeleteMapping(value = "/v1/equipments/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.DELETE)
    public ResponseEntity deleteData(@PathVariable Long id) throws RecordNotExistsException {
        return equipmentsService.deleteData(id);
    }

    @GetMapping(value = "/v1/equipments/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public BaseResponseEntity<EquipmentsResponse> getDataById(@PathVariable Long id)  throws RecordNotExistsException {
        return equipmentsService.getDataById(id);
    }

    @GetMapping(value = "/v1/equipments/export", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public ResponseEntity<Object> exportData(EquipmentsRequest.SearchForm dto) throws Exception {
        return equipmentsService.exportData(dto);
    }

    @GetMapping(value = "/v1/equipments/all", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public ListResponseEntity<EquipmentsResponse> getAllEquipment() throws Exception {
        return ResponseUtils.ok(equipmentsService.getAllEquipment());
    }

    @GetMapping(value = "/v1/equipments/list", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public ListResponseEntity<EquipmentsResponse> getListEquipment(EquipmentsRequest.SearchForm dto) throws Exception {
        return ResponseUtils.ok(equipmentsService.getListEquipment(dto));
    }


    @GetMapping(value = "/v1/equipments/download-template", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public ResponseEntity<Object> downloadTemplate() throws Exception {
        return equipmentsService.downloadTemplate();
    }

    @PostMapping(value = "/v1/equipments/import", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public BaseResponseEntity<Object> processImport(@RequestPart MultipartFile file) throws Exception {
        return ResponseUtils.ok(equipmentsService.processImport(file));
    }

}
