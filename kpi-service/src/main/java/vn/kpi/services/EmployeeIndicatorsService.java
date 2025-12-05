/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.kpi.services;

import org.springframework.http.ResponseEntity;
import vn.kpi.exceptions.BaseAppException;
import vn.kpi.exceptions.RecordNotExistsException;
import vn.kpi.models.response.*;
import vn.kpi.models.request.EmployeeIndicatorsRequest;

import java.util.List;

/**
 * Lop interface service ung voi bang kpi_employee_indicators
 *
 * @author tudd
 * @version 1.0
 * @since 1.0
 */

public interface EmployeeIndicatorsService {

    TableResponseEntity<EmployeeIndicatorsResponse.SearchResult> searchData(EmployeeIndicatorsRequest.SearchForm dto);

    ResponseEntity saveData(EmployeeIndicatorsRequest.SubmitForm dto, Long id, String adjustReason) throws BaseAppException;

    ResponseEntity deleteData(Long id) throws RecordNotExistsException;

    BaseResponseEntity<EmployeeIndicatorsResponse.SearchResult> getDataById(Long id) throws RecordNotExistsException;

    ResponseEntity<Object> exportData(EmployeeIndicatorsRequest.SearchForm dto) throws Exception;

    ResponseEntity deleteListData(List<Long> employeeIndicatorId, Long employeeEvaluationId, String adjustReason) throws RecordNotExistsException;

    List<EmployeeIndicatorsResponse.EmployeeEvaluation> getDataByEvaluationId(Long id, boolean isGetAll) throws RecordNotExistsException;

}
