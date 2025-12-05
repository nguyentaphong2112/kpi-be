/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.services;

import org.springframework.http.ResponseEntity;
import vn.hbtplus.exceptions.BaseAppException;
import vn.hbtplus.exceptions.RecordNotExistsException;
import vn.hbtplus.models.request.TransferringEquipmentsRequest;
import vn.hbtplus.models.response.BaseResponseEntity;
import vn.hbtplus.models.response.TableResponseEntity;
import vn.hbtplus.models.response.TransferringEquipmentsResponse;

/**
 * Lop interface service ung voi bang stk_transferring_equipments
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

public interface TransferringEquipmentsService {

    TableResponseEntity<TransferringEquipmentsResponse> searchData(TransferringEquipmentsRequest.SearchForm dto);

    ResponseEntity saveData(TransferringEquipmentsRequest.SubmitForm dto) throws BaseAppException;

    ResponseEntity deleteData(Long id) throws RecordNotExistsException;

    BaseResponseEntity<TransferringEquipmentsResponse> getDataById(Long id) throws RecordNotExistsException;

    ResponseEntity<Object> exportData(TransferringEquipmentsRequest.SearchForm dto) throws Exception;

}
