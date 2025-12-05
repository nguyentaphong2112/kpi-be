/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.kpi.services;

import org.springframework.http.ResponseEntity;
import vn.kpi.exceptions.BaseAppException;
import vn.kpi.exceptions.RecordNotExistsException;
import vn.kpi.models.request.EmpTypesRequest;
import vn.kpi.models.response.BaseResponseEntity;
import vn.kpi.models.response.EmpTypesResponse;
import vn.kpi.models.response.ListResponseEntity;
import vn.kpi.models.response.TableResponseEntity;

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
