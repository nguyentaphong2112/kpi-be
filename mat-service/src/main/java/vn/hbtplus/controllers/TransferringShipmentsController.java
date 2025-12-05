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
import vn.hbtplus.models.request.TransferringShipmentsRequest;
import vn.hbtplus.models.response.BaseResponseEntity;
import vn.hbtplus.models.response.ListResponseEntity;
import vn.hbtplus.models.response.StockEquipmentsResponse;
import vn.hbtplus.models.response.TableResponseEntity;
import vn.hbtplus.models.response.TransferringShipmentsResponse;
import vn.hbtplus.services.TransferringShipmentsService;
import vn.hbtplus.utils.ResponseUtils;
import vn.hbtplus.utils.Utils;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping(BaseConstants.REQUEST_MAPPING_PREFIX)
@RequiredArgsConstructor
//@Resource(value = Constants.RESOURCE.TRANSFERRING)
public class TransferringShipmentsController {
    private final TransferringShipmentsService transferringShipmentsService;

    @GetMapping(value = "/v1/transferring-shipments", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public TableResponseEntity<TransferringShipmentsResponse> searchData(TransferringShipmentsRequest.SearchForm dto) {
        return transferringShipmentsService.searchData(dto);
    }

    @PostMapping(value = "/v1/transferring-shipments", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.CREATE)
    public ResponseEntity saveData(@Valid @ModelAttribute TransferringShipmentsRequest.SubmitForm dto) throws BaseAppException {
        return transferringShipmentsService.saveData(dto);
    }

    @PutMapping(value = "/v1/transferring-shipments", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.CREATE)
    public ResponseEntity saveUpdateData(@Valid @ModelAttribute TransferringShipmentsRequest.SubmitForm dto) throws BaseAppException {
        return transferringShipmentsService.saveData(dto);
    }

    @DeleteMapping(value = "/v1/transferring-shipments/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.DELETE)
    public ResponseEntity deleteData(@PathVariable Long id) throws RecordNotExistsException {
        return transferringShipmentsService.deleteData(id);
    }

    @GetMapping(value = "/v1/transferring-shipments/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public BaseResponseEntity<TransferringShipmentsResponse> getDataById(@PathVariable Long id)  throws RecordNotExistsException {
        return transferringShipmentsService.getDataById(id);
    }

    @GetMapping(value = "/v1/transferring-shipments/export", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public ResponseEntity<Object> exportData(TransferringShipmentsRequest.SearchForm dto) throws Exception {
        return transferringShipmentsService.exportData(dto);
    }

    @PutMapping(value = "/v1/transferring-shipments/approve-by-list", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.CREATE)
    public ResponseEntity approve(@RequestParam List<Long> listId) throws BaseAppException {
        return transferringShipmentsService.approve(listId);
    }

    @PutMapping(value = "/v1/transferring-shipments/send-to-approve", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.CREATE)
    public ResponseEntity sendToApprove(@RequestParam List<Long> listId) throws BaseAppException {
        return transferringShipmentsService.sendToApprove(listId);
    }

    @PutMapping(value = "/v1/transferring-shipments/undo-approve", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.CREATE)
    public ResponseEntity undoApprove(@RequestParam List<Long> listId) throws BaseAppException {
        return transferringShipmentsService.undoApprove(listId);
    }

    @PutMapping(value = "/v1/transferring-shipments/reject-by-list", consumes = {MediaType.APPLICATION_JSON_VALUE})
    @HasPermission(scope = Scope.CREATE)
    public ResponseEntity reject(@RequestBody TransferringShipmentsRequest.RejectDto dto) throws BaseAppException {
        return transferringShipmentsService.reject(dto.getListId(), dto.getRejectReason());
    }
    @GetMapping(value = "/v1/transferring-shipments/get-seq", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public ResponseEntity<Object> getSeq() {
        return transferringShipmentsService.getSeq();
    }

    @GetMapping(value = "/v1/transferring-shipments/import-equipments-template", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public ResponseEntity<Object> downloadImportEquipmentTemplate(@RequestParam("warehouseId") Long warehouseId) throws Exception {
        return transferringShipmentsService.downloadImportEquipmentTemplate(warehouseId);
    }

    @PostMapping(value = "/v1/transferring-shipments/import-equipments", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public ListResponseEntity<StockEquipmentsResponse> importEquipments(@RequestPart("file") MultipartFile fileImport, @RequestPart("warehouseId") String warehouseId) throws Exception {
        Long data = null;
        if (!Utils.isNullOrEmpty(warehouseId)) {
            data = Long.parseLong(warehouseId);
        }
        return ResponseUtils.ok(transferringShipmentsService.importEquipments(fileImport, data));
    }

}
