/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.services;

import org.springframework.http.ResponseEntity;
import vn.hbtplus.exceptions.BaseAppException;
import vn.hbtplus.exceptions.RecordNotExistsException;
import vn.hbtplus.models.response.*;
import vn.hbtplus.models.request.EmployeeIndicatorsRequest;
import vn.hbtplus.repositories.entity.EmployeeIndicatorsEntity;
import vn.hbtplus.repositories.entity.OrganizationIndicatorsEntity;

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
