/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.services;

import org.springframework.http.ResponseEntity;
import vn.hbtplus.exceptions.BaseAppException;
import vn.hbtplus.exceptions.RecordNotExistsException;
import vn.hbtplus.models.response.*;
import vn.hbtplus.models.request.PytagoValuesRequest;

/**
 * Lop interface service ung voi bang crm_pytago_values
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

public interface PytagoValuesService {

    TableResponseEntity<PytagoValuesResponse> searchData(PytagoValuesRequest.SearchForm dto);

    ResponseEntity saveData(PytagoValuesRequest.SubmitForm dto) throws BaseAppException;

    ResponseEntity deleteData(Long id) throws RecordNotExistsException;

    BaseResponseEntity<PytagoValuesResponse> getDataById(Long id) throws RecordNotExistsException;

    ResponseEntity<Object> exportData(PytagoValuesRequest.SearchForm dto) throws Exception;

}
