/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.services;

import org.springframework.http.ResponseEntity;
import vn.hbtplus.exceptions.BaseAppException;
import vn.hbtplus.exceptions.RecordNotExistsException;
import vn.hbtplus.models.response.*;
import vn.hbtplus.models.request.WorkdayTypesRequest;

import java.util.List;

/**
 * Lop interface service ung voi bang abs_workday_types
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

public interface WorkdayTypesService {

    TableResponseEntity<WorkdayTypesResponse> searchData(WorkdayTypesRequest.SearchForm dto);

    BaseResponseEntity saveData(WorkdayTypesRequest.SubmitForm dto , Long id) throws BaseAppException;

    ResponseEntity deleteData(Long id) throws RecordNotExistsException;

    BaseResponseEntity<WorkdayTypesResponse> getDataById(Long id) throws RecordNotExistsException;

    ResponseEntity<Object> exportData(WorkdayTypesRequest.SearchForm dto) throws Exception;

    List<WorkdayTypesResponse> getList();
}
