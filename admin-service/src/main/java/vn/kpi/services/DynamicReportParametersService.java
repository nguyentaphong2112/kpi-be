/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.kpi.services;

import org.springframework.http.ResponseEntity;
import vn.kpi.exceptions.BaseAppException;
import vn.kpi.exceptions.RecordNotExistsException;
import vn.kpi.models.request.DynamicReportParametersRequest;
import vn.kpi.models.response.BaseResponseEntity;
import vn.kpi.models.response.DynamicReportParametersResponse;
import vn.kpi.models.response.TableResponseEntity;

/**
 * Lop interface service ung voi bang sys_dynamic_report_parameters
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

public interface DynamicReportParametersService {

    TableResponseEntity<DynamicReportParametersResponse> searchData(DynamicReportParametersRequest.SearchForm dto);

    ResponseEntity saveData(DynamicReportParametersRequest.SubmitForm dto) throws BaseAppException;

    ResponseEntity deleteData(Long id) throws RecordNotExistsException;

    BaseResponseEntity<DynamicReportParametersResponse> getDataById(Long id) throws RecordNotExistsException;

    ResponseEntity<Object> exportData(DynamicReportParametersRequest.SearchForm dto) throws Exception;

}
