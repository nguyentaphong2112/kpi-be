/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.services;

import org.springframework.http.ResponseEntity;
import vn.hbtplus.exceptions.BaseAppException;
import vn.hbtplus.exceptions.RecordNotExistsException;
import vn.hbtplus.models.response.*;
import vn.hbtplus.models.request.ShipmentsRequest;

/**
 * Lop interface service ung voi bang crm_shipments
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

public interface ShipmentsService {

    TableResponseEntity<ShipmentsResponse> searchData(ShipmentsRequest.SearchForm dto);

    ResponseEntity saveData(ShipmentsRequest.SubmitForm dto) throws BaseAppException;

    ResponseEntity deleteData(Long id) throws RecordNotExistsException;

    BaseResponseEntity<ShipmentsResponse> getDataById(Long id) throws RecordNotExistsException;

    ResponseEntity<Object> exportData(ShipmentsRequest.SearchForm dto) throws Exception;

}
