/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.services;

import org.springframework.http.ResponseEntity;
import vn.hbtplus.exceptions.BaseAppException;
import vn.hbtplus.exceptions.RecordNotExistsException;
import vn.hbtplus.models.request.CustomerCertificatesRequest;
import vn.hbtplus.models.response.*;
import vn.hbtplus.models.request.OrderPayablesRequest;

/**
 * Lop interface service ung voi bang crm_order_payables
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

public interface OrderPayablesService {

    TableResponseEntity<OrderPayablesResponse> searchData(OrderPayablesRequest.SearchForm dto);

    ResponseEntity saveData(OrderPayablesRequest.SubmitForm dto, Long id) throws BaseAppException;

    ResponseEntity deleteData(Long id) throws RecordNotExistsException;

    BaseResponseEntity<OrderPayablesResponse> getDataById(Long id) throws RecordNotExistsException;

    ResponseEntity<Object> exportData(OrderPayablesRequest.SearchForm dto) throws Exception;

    boolean makeList(OrderPayablesRequest.MakeListForm dto);

    ResponseEntity updateStatusById(OrderPayablesRequest.SubmitForm dto, Long id) throws RecordNotExistsException;

    ResponseEntity approveAll(OrderPayablesRequest.SubmitForm dto) throws RecordNotExistsException;
}
