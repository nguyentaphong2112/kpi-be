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
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import vn.hbtplus.annotations.HasPermission;
import vn.hbtplus.annotations.Resource;
import vn.hbtplus.constant.Constants;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.constants.Scope;
import vn.hbtplus.exceptions.BaseAppException;
import vn.hbtplus.exceptions.RecordNotExistsException;
import vn.hbtplus.models.request.OutgoingShipmentsRequest;
import vn.hbtplus.models.response.BaseResponseEntity;
import vn.hbtplus.models.response.ListResponseEntity;
import vn.hbtplus.models.response.OutgoingShipmentsResponse;
import vn.hbtplus.models.response.StockEquipmentsResponse;
import vn.hbtplus.models.response.TableResponseEntity;
import vn.hbtplus.services.OutgoingShipmentsService;
import vn.hbtplus.utils.ResponseUtils;
import vn.hbtplus.utils.Utils;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping(BaseConstants.REQUEST_MAPPING_PREFIX)
@RequiredArgsConstructor
//@Resource(value = Constants.RESOURCE.OUTGOING)
public class OutgoingShipmentsController {
    private final OutgoingShipmentsService outgoingShipmentsService;

    @GetMapping(value = "/v1/outgoing-shipments", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public TableResponseEntity<OutgoingShipmentsResponse> searchData(OutgoingShipmentsRequest.SearchForm dto) {
        return outgoingShipmentsService.searchData(dto);
    }

    @PostMapping(value = "/v1/outgoing-shipments", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.CREATE)
    public ResponseEntity saveData(@Valid @ModelAttribute OutgoingShipmentsRequest.SubmitForm dto) throws BaseAppException {
        return outgoingShipmentsService.saveData(dto);
    }

    @PutMapping(value = "/v1/outgoing-shipments/{id}", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.CREATE)
    public ResponseEntity saveUpdateData(@Valid @ModelAttribute OutgoingShipmentsRequest.SubmitForm dto) throws BaseAppException {
        return outgoingShipmentsService.saveData(dto);
    }

    @DeleteMapping(value = "/v1/outgoing-shipments/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.DELETE)
    public ResponseEntity deleteData(@PathVariable Long id) throws RecordNotExistsException {
        return outgoingShipmentsService.deleteData(id);
    }

    @GetMapping(value = "/v1/outgoing-shipments/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public BaseResponseEntity<OutgoingShipmentsResponse> getDataById(@PathVariable Long id)  throws RecordNotExistsException {
        return outgoingShipmentsService.getDataById(id);
    }

    @GetMapping(value = "/v1/outgoing-shipments/export", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public ResponseEntity<Object> exportData(OutgoingShipmentsRequest.SearchForm dto) throws Exception {
        return outgoingShipmentsService.exportData(dto);
    }

    @PutMapping(value = "/v1/outgoing-shipments/send-to-approve", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.CREATE)
    public ResponseEntity sendToApprove(@RequestParam List<Long> listId) throws BaseAppException {
        return outgoingShipmentsService.sendToApprove(listId);
    }

    @PutMapping(value = "/v1/outgoing-shipments/approve-by-list", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.CREATE)
    public ResponseEntity approve(@RequestParam List<Long> listId) throws BaseAppException {
        return outgoingShipmentsService.approve(listId);
    }

    @PutMapping(value = "/v1/outgoing-shipments/reject-by-list", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.CREATE)
    public ResponseEntity reject(@RequestBody OutgoingShipmentsRequest.RejectDto dto) throws BaseAppException {
        return outgoingShipmentsService.reject(dto.getListId(), dto.getRejectReason());
    }

    @GetMapping(value = "/v1/outgoing-shipments/get-seq", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public ResponseEntity<Object> getSeq() {
        return outgoingShipmentsService.getSeq();
    }


    @GetMapping(value = "/v1/outgoing-shipments/import-equipments-template", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public ResponseEntity<Object> downloadImportEquipmentTemplate(@RequestParam("warehouseId") Long warehouseId) throws Exception {
        return outgoingShipmentsService.downloadImportEquipmentTemplate(warehouseId);
    }

    @PostMapping(value = "/v1/outgoing-shipments/import-equipments", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public ListResponseEntity<StockEquipmentsResponse> importEquipments(@RequestPart("file") MultipartFile fileImport, @RequestPart("warehouseId") String warehouseId) throws Exception {
        Long data = null;
        if (!Utils.isNullOrEmpty(warehouseId)) {
            data = Long.parseLong(warehouseId);
        }
        return ResponseUtils.ok(outgoingShipmentsService.importEquipments(fileImport, data));
    }
}
