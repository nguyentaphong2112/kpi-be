/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.services;

import org.springframework.http.ResponseEntity;
import vn.hbtplus.exceptions.BaseAppException;
import vn.hbtplus.exceptions.RecordNotExistsException;
import vn.hbtplus.models.request.EmpTypesRequest;
import vn.hbtplus.models.response.BaseResponseEntity;
import vn.hbtplus.models.response.EmpTypesResponse;
import vn.hbtplus.models.response.ListResponseEntity;
import vn.hbtplus.models.response.TableResponseEntity;

/**
 * Lop interface service ung voi bang hr_emp_types
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

public interface EmpTypesService {

    TableResponseEntity<EmpTypesResponse.SearchResult> searchData(EmpTypesRequest.SearchForm dto);

    ResponseEntity saveData(EmpTypesRequest.SubmitForm dto, Long empTypeId) throws BaseAppException;

    ResponseEntity deleteData(Long id) throws RecordNotExistsException;

    BaseResponseEntity<EmpTypesResponse.DetailBean> getDataById(Long id) throws RecordNotExistsException;

    ListResponseEntity<EmpTypesResponse.DetailBean> getList(boolean isGetAttributes) throws RecordNotExistsException;

    ResponseEntity<Object> exportData(EmpTypesRequest.SearchForm dto) throws Exception;

}
