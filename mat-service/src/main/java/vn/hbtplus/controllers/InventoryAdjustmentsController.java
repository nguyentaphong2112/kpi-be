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
import vn.hbtplus.models.request.InventoryAdjustmentsRequest;
import vn.hbtplus.models.request.OutgoingShipmentsRequest;
import vn.hbtplus.models.response.BaseResponseEntity;
import vn.hbtplus.models.response.InventoryAdjustmentsResponse;
import vn.hbtplus.models.response.ListResponseEntity;
import vn.hbtplus.models.response.StockEquipmentsResponse;
import vn.hbtplus.models.response.TableResponseEntity;
import vn.hbtplus.services.InventoryAdjustmentsService;
import vn.hbtplus.utils.ResponseUtils;
import vn.hbtplus.utils.Utils;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping(BaseConstants.REQUEST_MAPPING_PREFIX)
@RequiredArgsConstructor
//@Resource(value = Constants.RESOURCE.INVENTORY)
public class InventoryAdjustmentsController {
    private final InventoryAdjustmentsService inventoryAdjustmentsService;

    @GetMapping(value = "/v1/inventory-adjustments", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public TableResponseEntity<InventoryAdjustmentsResponse> searchData(InventoryAdjustmentsRequest.SearchForm dto) {
        return inventoryAdjustmentsService.searchData(dto);
    }

    @PostMapping(value = "/v1/inventory-adjustments", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.CREATE)
    public ResponseEntity saveData(@Valid @ModelAttribute InventoryAdjustmentsRequest.SubmitForm dto) throws BaseAppException {
        return inventoryAdjustmentsService.saveData(dto);
    }

    @PutMapping(value = "/v1/inventory-adjustments/{id}", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.CREATE)
    public ResponseEntity saveUpdateData(@Valid @ModelAttribute InventoryAdjustmentsRequest.SubmitForm dto) throws BaseAppException {
        return inventoryAdjustmentsService.saveData(dto);
    }


    @DeleteMapping(value = "/v1/inventory-adjustments/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.DELETE)
    public ResponseEntity deleteData(@PathVariable Long id) throws RecordNotExistsException {
        return inventoryAdjustmentsService.deleteData(id);
    }

    @GetMapping(value = "/v1/inventory-adjustments/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public BaseResponseEntity<InventoryAdjustmentsResponse> getDataById(@PathVariable Long id)  throws RecordNotExistsException {
        return inventoryAdjustmentsService.getDataById(id);
    }

    @GetMapping(value = "/v1/inventory-adjustments/export", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public ResponseEntity<Object> exportData(InventoryAdjustmentsRequest.SearchForm dto) throws Exception {
        return inventoryAdjustmentsService.exportData(dto);
    }

    @GetMapping(value = "/v1/inventory-adjustments/get-seq", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public ResponseEntity<Object> getSeq() {
        return inventoryAdjustmentsService.getSeq();
    }

    @PutMapping(value = "/v1/inventory-adjustments/send-to-approve", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.CREATE)
    public ResponseEntity sendToApprove(@RequestParam List<Long> listId) throws BaseAppException {
        return inventoryAdjustmentsService.sendToApprove(listId);
    }

    @PutMapping(value = "/v1/inventory-adjustments/approve-by-list", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.CREATE)
    public ResponseEntity approve(@RequestParam List<Long> listId) throws BaseAppException {
        return inventoryAdjustmentsService.approve(listId);
    }

    @PutMapping(value = "/v1/inventory-adjustments/reject-by-list", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.CREATE)
    public ResponseEntity reject(@RequestBody OutgoingShipmentsRequest.RejectDto dto) throws BaseAppException {
        return inventoryAdjustmentsService.reject(dto.getListId(), dto.getRejectReason());
    }

    @PutMapping(value = "/v1/inventory-adjustments/undo-approve", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.CREATE)
    public ResponseEntity undoApprove(@RequestParam List<Long> listId) throws BaseAppException {
        return inventoryAdjustmentsService.undoApprove(listId);
    }

    @GetMapping(value = "/v1/inventory-adjustments/import-equipments-template", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public ResponseEntity<Object> downloadImportEquipmentTemplate(@RequestParam(value = "warehouseId", required = false) Long warehouseId, @RequestParam(value = "isIncrease", required = false) String isIncrease) throws Exception {
        return inventoryAdjustmentsService.downloadImportEquipmentTemplate(warehouseId, isIncrease);
    }

    @PostMapping(value = "/v1/inventory-adjustments/import-equipments", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public ListResponseEntity<StockEquipmentsResponse> importEquipments(@RequestPart("file") MultipartFile fileImport, @RequestPart(value = "warehouseId", required = false) String warehouseId, @RequestParam(value = "isIncrease", required = false) String isIncrease) throws Exception {
        Long data = null;
        if (!Utils.isNullOrEmpty(warehouseId)) {
            data = Long.parseLong(warehouseId);
        }
        return ResponseUtils.ok(inventoryAdjustmentsService.importEquipments(fileImport, data, isIncrease));
    }
}
