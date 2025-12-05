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
import vn.hbtplus.models.request.TransferringShipmentsRequest;
import vn.hbtplus.models.response.BaseResponseEntity;
import vn.hbtplus.models.response.StockEquipmentsResponse;
import vn.hbtplus.models.response.TableResponseEntity;
import vn.hbtplus.models.response.TransferringShipmentsResponse;

import java.util.List;

/**
 * Lop interface service ung voi bang stk_transfering_shipments
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

public interface TransferringShipmentsService {

    TableResponseEntity<TransferringShipmentsResponse> searchData(TransferringShipmentsRequest.SearchForm dto);

    ResponseEntity saveData(TransferringShipmentsRequest.SubmitForm dto) throws BaseAppException;

    ResponseEntity deleteData(Long id) throws RecordNotExistsException;

    BaseResponseEntity<TransferringShipmentsResponse> getDataById(Long id) throws RecordNotExistsException;

    ResponseEntity<Object> exportData(TransferringShipmentsRequest.SearchForm dto) throws Exception;

    ResponseEntity approve(List<Long> ids) throws BaseAppException;

    ResponseEntity undoApprove(List<Long> ids);

    ResponseEntity sendToApprove(List<Long> ids);

    ResponseEntity reject(List<Long> ids, String note);

    ResponseEntity<Object> downloadImportEquipmentTemplate(Long warehouseId) throws Exception;

    List<StockEquipmentsResponse> importEquipments(MultipartFile fileImport, Long warehouseId) throws ErrorImportException;

    ResponseEntity getSeq();
}
