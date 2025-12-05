/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.services;

import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
import vn.hbtplus.exceptions.BaseAppException;
import vn.hbtplus.exceptions.ErrorImportException;
import vn.hbtplus.exceptions.RecordNotExistsException;
import vn.hbtplus.models.request.InventoryAdjustmentsRequest;
import vn.hbtplus.models.response.BaseResponseEntity;
import vn.hbtplus.models.response.InventoryAdjustmentsResponse;
import vn.hbtplus.models.response.StockEquipmentsResponse;
import vn.hbtplus.models.response.TableResponseEntity;

import java.util.List;

/**
 * Lop interface service ung voi bang stk_inventory_adjustments
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

public interface InventoryAdjustmentsService {

    TableResponseEntity<InventoryAdjustmentsResponse> searchData(InventoryAdjustmentsRequest.SearchForm dto);

    ResponseEntity saveData(InventoryAdjustmentsRequest.SubmitForm dto) throws BaseAppException;

    ResponseEntity deleteData(Long id) throws RecordNotExistsException;

    BaseResponseEntity<InventoryAdjustmentsResponse> getDataById(Long id) throws RecordNotExistsException;

    ResponseEntity<Object> exportData(InventoryAdjustmentsRequest.SearchForm dto) throws Exception;

    ResponseEntity getSeq();

    ResponseEntity sendToApprove(List<Long> ids);

    ResponseEntity approve(List<Long> ids);

    ResponseEntity undoApprove(List<Long> ids);

    ResponseEntity reject(List<Long> ids, String note);

    ResponseEntity<Object> downloadImportEquipmentTemplate(Long warehouseId, String isIncrease) throws Exception;

    List<StockEquipmentsResponse> importEquipments(MultipartFile fileImport, Long warehouseId, String isIncrease) throws ErrorImportException;

}
