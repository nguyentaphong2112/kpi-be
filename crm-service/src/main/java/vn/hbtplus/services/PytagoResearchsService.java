/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.services;

import org.springframework.http.ResponseEntity;
import vn.hbtplus.exceptions.BaseAppException;
import vn.hbtplus.exceptions.RecordNotExistsException;
import vn.hbtplus.models.response.*;
import vn.hbtplus.models.request.PytagoResearchsRequest;

/**
 * Lop interface service ung voi bang crm_pytago_researchs
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

public interface PytagoResearchsService {

    TableResponseEntity<PytagoResearchsResponse> searchData(PytagoResearchsRequest.SearchForm dto);

    ResponseEntity saveData(PytagoResearchsRequest.SubmitForm dto) throws BaseAppException;

    ResponseEntity deleteData(Long id) throws RecordNotExistsException;

    BaseResponseEntity<PytagoResearchsResponse> getDataById(Long id) throws RecordNotExistsException;

    ResponseEntity<Object> exportData(PytagoResearchsRequest.SearchForm dto) throws Exception;

    PytagoResearchsResponse.SearchCount getSearchCount();

    ResponseEntity createCustomer(Long id);
}
