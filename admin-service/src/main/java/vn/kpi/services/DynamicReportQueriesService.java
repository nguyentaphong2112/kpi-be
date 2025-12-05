/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.kpi.services;

import org.springframework.http.ResponseEntity;
import vn.kpi.exceptions.BaseAppException;
import vn.kpi.exceptions.RecordNotExistsException;
import vn.kpi.models.request.DynamicReportQueriesRequest;
import vn.kpi.models.response.BaseResponseEntity;
import vn.kpi.models.response.DynamicReportQueriesResponse;
import vn.kpi.models.response.TableResponseEntity;

/**
 * Lop interface service ung voi bang sys_dynamic_report_queries
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

public interface DynamicReportQueriesService {

    TableResponseEntity<DynamicReportQueriesResponse> searchData(DynamicReportQueriesRequest.SearchForm dto);

    ResponseEntity saveData(DynamicReportQueriesRequest.SubmitForm dto) throws BaseAppException;

    ResponseEntity deleteData(Long id) throws RecordNotExistsException;

    BaseResponseEntity<DynamicReportQueriesResponse> getDataById(Long id) throws RecordNotExistsException;

    ResponseEntity<Object> exportData(DynamicReportQueriesRequest.SearchForm dto) throws Exception;

}
