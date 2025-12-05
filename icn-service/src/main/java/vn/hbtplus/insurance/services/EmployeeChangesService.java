/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.insurance.services;

import org.springframework.http.ResponseEntity;
import vn.hbtplus.exceptions.BaseAppException;
import vn.hbtplus.exceptions.RecordNotExistsException;
import vn.hbtplus.models.BaseApproveRequest;
import vn.hbtplus.insurance.models.request.EmployeeChangesRequest;
import vn.hbtplus.models.response.BaseResponseEntity;
import vn.hbtplus.insurance.models.response.EmployeeChangesResponse;
import vn.hbtplus.models.response.TableResponseEntity;

import java.util.List;

/**
 * Lop interface service ung voi bang icn_employee_changes
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

public interface EmployeeChangesService {

    TableResponseEntity<EmployeeChangesResponse> searchData(EmployeeChangesRequest.SearchForm dto);

    ResponseEntity saveData(EmployeeChangesRequest.SubmitForm dto) throws BaseAppException;

    ResponseEntity deleteData(Long id) throws RecordNotExistsException;

    BaseResponseEntity<EmployeeChangesResponse> getDataById(Long id) throws RecordNotExistsException;

    ResponseEntity<Object> exportData(EmployeeChangesRequest.SearchForm dto) throws Exception;

    boolean makeList(EmployeeChangesRequest.MakeListForm dto);

    List<Long> updateStatusById(BaseApproveRequest dto, String status) throws BaseAppException;

    List<Long> updateStatus(EmployeeChangesRequest.SearchForm dto, String status) throws BaseAppException;
}
