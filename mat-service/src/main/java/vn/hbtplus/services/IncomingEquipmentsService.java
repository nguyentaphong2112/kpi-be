/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.services;

import org.springframework.http.ResponseEntity;
import vn.hbtplus.exceptions.BaseAppException;
import vn.hbtplus.exceptions.RecordNotExistsException;
import vn.hbtplus.models.request.IncomingEquipmentsRequest;
import vn.hbtplus.models.response.BaseResponseEntity;
import vn.hbtplus.models.response.IncomingEquipmentsResponse;
import vn.hbtplus.models.response.TableResponseEntity;

/**
 * Lop interface service ung voi bang stk_incoming_equipments
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

public interface IncomingEquipmentsService {

    TableResponseEntity<IncomingEquipmentsResponse> searchData(IncomingEquipmentsRequest.SearchForm dto);

    ResponseEntity saveData(IncomingEquipmentsRequest.SubmitForm dto) throws BaseAppException;

    ResponseEntity deleteData(Long id) throws RecordNotExistsException;

    BaseResponseEntity<IncomingEquipmentsResponse> getDataById(Long id) throws RecordNotExistsException;

    ResponseEntity<Object> exportData(IncomingEquipmentsRequest.SearchForm dto) throws Exception;

}
