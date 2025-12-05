/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.services;

import org.springframework.http.ResponseEntity;
import vn.hbtplus.exceptions.BaseAppException;
import vn.hbtplus.exceptions.RecordNotExistsException;
import vn.hbtplus.models.response.*;
import vn.hbtplus.models.request.EmployeesRequest;

import java.util.List;

/**
 * Lop interface service ung voi bang crm_employees
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

public interface EmployeesService {

    TableResponseEntity<EmployeesResponse.SearchResult> searchData(EmployeesRequest.SearchForm dto);

    ResponseEntity saveData(EmployeesRequest.SubmitForm dto, Long id) throws BaseAppException;

    ResponseEntity deleteData(Long id) throws RecordNotExistsException;

    BaseResponseEntity<EmployeesResponse.DetailBean> getDataById(Long id) throws RecordNotExistsException;

    ResponseEntity<Object> exportData(EmployeesRequest.SearchForm dto) throws Exception;

    List<EmployeesResponse.SearchResult> getListEmployee(String keySearch);
}
