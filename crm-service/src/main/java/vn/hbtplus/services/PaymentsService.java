/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.services;

import org.springframework.http.ResponseEntity;
import vn.hbtplus.exceptions.BaseAppException;
import vn.hbtplus.exceptions.RecordNotExistsException;
import vn.hbtplus.models.response.*;
import vn.hbtplus.models.request.PaymentsRequest;

/**
 * Lop interface service ung voi bang crm_payments
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

public interface PaymentsService {

    TableResponseEntity<PaymentsResponse> searchData(PaymentsRequest.SearchForm dto);

    ResponseEntity saveData(PaymentsRequest.SubmitForm dto) throws BaseAppException;

    ResponseEntity deleteData(Long id) throws RecordNotExistsException;

    BaseResponseEntity<PaymentsResponse> getDataById(Long id) throws RecordNotExistsException;

    ResponseEntity<Object> exportData(PaymentsRequest.SearchForm dto) throws Exception;

}
