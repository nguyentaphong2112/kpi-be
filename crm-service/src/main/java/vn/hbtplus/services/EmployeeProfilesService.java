/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.services;

import org.springframework.http.ResponseEntity;
import vn.hbtplus.exceptions.BaseAppException;
import vn.hbtplus.exceptions.RecordNotExistsException;
import vn.hbtplus.models.response.*;
import vn.hbtplus.models.request.EmployeeProfilesRequest;

/**
 * Lop interface service ung voi bang crm_employee_profiles
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

public interface EmployeeProfilesService {

    TableResponseEntity<EmployeeProfilesResponse> searchData(EmployeeProfilesRequest.SearchForm dto);

    ResponseEntity saveData(EmployeeProfilesRequest.SubmitForm dto) throws BaseAppException;

    ResponseEntity deleteData(Long id) throws RecordNotExistsException;

    BaseResponseEntity<EmployeeProfilesResponse> getDataById(Long id) throws RecordNotExistsException;

    ResponseEntity<Object> exportData(EmployeeProfilesRequest.SearchForm dto) throws Exception;

}
