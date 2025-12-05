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
import vn.hbtplus.models.request.IncomingShipmentsRequest;
import vn.hbtplus.models.response.BaseResponseEntity;
import vn.hbtplus.models.response.IncomingShipmentsResponse;
import vn.hbtplus.models.response.StockEquipmentsResponse;
import vn.hbtplus.models.response.TableResponseEntity;

import java.util.List;

/**
 * Lop interface service ung voi bang stk_incoming_shipments
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

public interface IncomingShipmentsService {

    TableResponseEntity<IncomingShipmentsResponse> searchData(IncomingShipmentsRequest.SearchForm dto);

    ResponseEntity saveData(IncomingShipmentsRequest.SubmitForm dto) throws BaseAppException;

    ResponseEntity deleteData(Long id) throws RecordNotExistsException;

    BaseResponseEntity<IncomingShipmentsResponse> getDataById(Long id) throws RecordNotExistsException;

    ResponseEntity<Object> exportData(IncomingShipmentsRequest.SearchForm dto) throws Exception;

    ResponseEntity sendToApprove(List<Long> ids) throws BaseAppException;

    ResponseEntity approve(List<Long> ids);

    ResponseEntity undoApprove(List<Long> ids);

    ResponseEntity<Object> downloadImportEquipmentTemplate() throws Exception;

    List<StockEquipmentsResponse> importEquipments(MultipartFile fileImport) throws ErrorImportException;

    ResponseEntity reject(List<Long> ids, String note);

    ResponseEntity getSeq();
}
