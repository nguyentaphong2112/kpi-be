/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.services;

import org.springframework.http.ResponseEntity;
import vn.hbtplus.exceptions.BaseAppException;
import vn.hbtplus.exceptions.RecordNotExistsException;
import vn.hbtplus.models.request.OutgoingEquipmentsRequest;
import vn.hbtplus.models.response.BaseResponseEntity;
import vn.hbtplus.models.response.OutgoingEquipmentsResponse;
import vn.hbtplus.models.response.TableResponseEntity;

/**
 * Lop interface service ung voi bang stk_outgoing_equipments
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

public interface OutgoingEquipmentsService {

    TableResponseEntity<OutgoingEquipmentsResponse> searchData(OutgoingEquipmentsRequest.SearchForm dto);

    ResponseEntity saveData(OutgoingEquipmentsRequest.SubmitForm dto) throws BaseAppException;

    ResponseEntity deleteData(Long id) throws RecordNotExistsException;

    BaseResponseEntity<OutgoingEquipmentsResponse> getDataById(Long id) throws RecordNotExistsException;

    ResponseEntity<Object> exportData(OutgoingEquipmentsRequest.SearchForm dto) throws Exception;

}
