/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.services;

import org.springframework.http.ResponseEntity;
import vn.hbtplus.exceptions.BaseAppException;
import vn.hbtplus.exceptions.RecordNotExistsException;
import vn.hbtplus.models.response.*;
import vn.hbtplus.models.request.OrdersRequest;

/**
 * Lop interface service ung voi bang crm_orders
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

public interface OrdersService {

    TableResponseEntity<OrdersResponse> searchData(OrdersRequest.SearchForm dto);

    ResponseEntity saveData(OrdersRequest.SubmitForm dto) throws BaseAppException;

    ResponseEntity deleteData(Long id) throws RecordNotExistsException;

    BaseResponseEntity<OrdersResponse> getDataById(Long id) throws RecordNotExistsException;

    ResponseEntity<Object> exportData(OrdersRequest.SearchForm dto) throws Exception;

}
