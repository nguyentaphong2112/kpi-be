/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.services;

import org.springframework.http.ResponseEntity;
import vn.hbtplus.exceptions.BaseAppException;
import vn.hbtplus.exceptions.RecordNotExistsException;
import vn.hbtplus.models.BaseDataTableDto;
import vn.hbtplus.models.BaseSearchRequest;
import vn.hbtplus.models.request.EmployeesRequest;
import vn.hbtplus.models.response.*;
import vn.hbtplus.models.request.WorkedHistoriesRequest;

/**
 * Lop interface service ung voi bang hr_worked_histories
 *
 * @author tudd
 * @version 1.0
 * @since 1.0
 */

public interface WorkedHistoriesService {

    TableResponseEntity<WorkedHistoriesResponse.SearchResult> searchData(EmployeesRequest.SearchForm dto);

    BaseResponseEntity<Long> saveData(WorkedHistoriesRequest.SubmitForm dto, Long employeeId, Long id) throws BaseAppException;

    ResponseEntity deleteData(Long employeeId, Long id) throws BaseAppException;

    BaseResponseEntity<WorkedHistoriesResponse.DetailBean> getDataById(Long employeeId, Long id) throws BaseAppException;

    ResponseEntity<Object> exportData(EmployeesRequest.SearchForm dto) throws Exception;

    BaseDataTableDto<WorkedHistoriesResponse.DetailBean> getTableList(Long employeeId, BaseSearchRequest request);
}
