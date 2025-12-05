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
import vn.hbtplus.models.request.IncomingShipmentsRequest;
import vn.hbtplus.models.response.BaseResponseEntity;
import vn.hbtplus.models.response.IncomingShipmentsResponse;
import vn.hbtplus.models.response.ListResponseEntity;
import vn.hbtplus.models.response.StockEquipmentsResponse;
import vn.hbtplus.models.response.TableResponseEntity;
import vn.hbtplus.services.IncomingShipmentsService;
import vn.hbtplus.utils.ResponseUtils;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping(BaseConstants.REQUEST_MAPPING_PREFIX)
@RequiredArgsConstructor
//@Resource(value = Constants.RESOURCE.INCOMING)
public class IncomingShipmentsController {
    private final IncomingShipmentsService incomingShipmentsService;

    @GetMapping(value = "/v1/incoming-shipments", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public TableResponseEntity<IncomingShipmentsResponse> searchData(IncomingShipmentsRequest.SearchForm dto) {
        return incomingShipmentsService.searchData(dto);
    }

    @PostMapping(value = "/v1/incoming-shipments", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.CREATE)
    public ResponseEntity saveData(@Valid @ModelAttribute IncomingShipmentsRequest.SubmitForm dto) throws BaseAppException {
        return incomingShipmentsService.saveData(dto);
    }

    @PutMapping(value = "/v1/incoming-shipments/{id}", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.CREATE)
    public ResponseEntity saveUpdateData(@Valid @ModelAttribute IncomingShipmentsRequest.SubmitForm dto) throws BaseAppException {
        return incomingShipmentsService.saveData(dto);
    }

    @PutMapping(value = "/v1/incoming-shipments/send-to-approve", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.CREATE)
    public ResponseEntity sendToApprove(@RequestParam List<Long> listId) throws BaseAppException {
        return incomingShipmentsService.sendToApprove(listId);
    }

    @PutMapping(value = "/v1/incoming-shipments/approve-by-list", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.CREATE)
    public ResponseEntity approve(@RequestParam List<Long> listId) throws BaseAppException {
        return incomingShipmentsService.approve(listId);
    }

    @PutMapping(value = "/v1/incoming-shipments/reject-by-list", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.CREATE)
    public ResponseEntity reject(@RequestBody IncomingShipmentsRequest.RejectDto dto) throws BaseAppException {
        return incomingShipmentsService.reject(dto.getListId(), dto.getRejectReason());
    }

    @PutMapping(value = "/v1/incoming-shipments/undo-approve", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.CREATE)
    public ResponseEntity undoApprove(@RequestParam List<Long> listId) throws BaseAppException {
        return incomingShipmentsService.undoApprove(listId);
    }


    @DeleteMapping(value = "/v1/incoming-shipments/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.DELETE)
    public ResponseEntity deleteData(@PathVariable Long id) throws RecordNotExistsException {
        return incomingShipmentsService.deleteData(id);
    }

    @GetMapping(value = "/v1/incoming-shipments/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public BaseResponseEntity<IncomingShipmentsResponse> getDataById(@PathVariable Long id) throws RecordNotExistsException {
        return incomingShipmentsService.getDataById(id);
    }
    @GetMapping(value = "/v1/incoming-shipments/import-equipments-template", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public ResponseEntity<Object> downloadImportEquipmentTemplate() throws Exception {
        return incomingShipmentsService.downloadImportEquipmentTemplate();
    }

    @PostMapping(value = "/v1/incoming-shipments/import-equipments", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public ListResponseEntity<StockEquipmentsResponse> importEquipments(@RequestPart("file") MultipartFile fileImport ) throws Exception {
        return ResponseUtils.ok(incomingShipmentsService.importEquipments(fileImport));
    }

    @GetMapping(value = "/v1/incoming-shipments/export", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public ResponseEntity<Object> exportData(IncomingShipmentsRequest.SearchForm dto) throws Exception {
        return incomingShipmentsService.exportData(dto);
    }

    @GetMapping(value = "/v1/incoming-shipments/get-seq", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public ResponseEntity<Object> getSeq() {
        return incomingShipmentsService.getSeq();
    }


}
