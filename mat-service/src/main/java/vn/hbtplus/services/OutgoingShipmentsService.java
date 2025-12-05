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
import vn.hbtplus.models.request.OutgoingShipmentsRequest;
import vn.hbtplus.models.response.BaseResponseEntity;
import vn.hbtplus.models.response.OutgoingShipmentsResponse;
import vn.hbtplus.models.response.StockEquipmentsResponse;
import vn.hbtplus.models.response.TableResponseEntity;

import java.util.List;

/**
 * Lop interface service ung voi bang stk_outgoing_shipments
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

public interface OutgoingShipmentsService {

    TableResponseEntity<OutgoingShipmentsResponse> searchData(OutgoingShipmentsRequest.SearchForm dto);

    ResponseEntity saveData(OutgoingShipmentsRequest.SubmitForm dto) throws BaseAppException;

    ResponseEntity deleteData(Long id) throws RecordNotExistsException;

    BaseResponseEntity<OutgoingShipmentsResponse> getDataById(Long id) throws RecordNotExistsException;

    ResponseEntity<Object> exportData(OutgoingShipmentsRequest.SearchForm dto) throws Exception;

    ResponseEntity sendToApprove(List<Long> ids);

    ResponseEntity approve(List<Long> ids) throws BaseAppException;

    ResponseEntity reject(List<Long> ids, String note);

    ResponseEntity getSeq();

    ResponseEntity<Object> downloadImportEquipmentTemplate(Long warehouseId) throws Exception;

    List<StockEquipmentsResponse> importEquipments(MultipartFile fileImport, Long warehouseId) throws ErrorImportException;


}
