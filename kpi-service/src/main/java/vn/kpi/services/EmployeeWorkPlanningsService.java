/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.kpi.services;

import org.springframework.http.ResponseEntity;
import vn.kpi.exceptions.BaseAppException;
import vn.kpi.exceptions.RecordNotExistsException;
import vn.kpi.models.response.*;
import vn.kpi.models.request.EmployeeWorkPlanningsRequest;

/**
 * Lop interface service ung voi bang kpi_employee_work_plannings
 *
 * @author tudd
 * @version 1.0
 * @since 1.0
 */

public interface EmployeeWorkPlanningsService {

    TableResponseEntity<EmployeeWorkPlanningsResponse.SearchForm> searchData(EmployeeWorkPlanningsRequest.SearchForm dto);

    ResponseEntity saveData(EmployeeWorkPlanningsRequest.SubmitForm dto, Long id) throws BaseAppException;

    ResponseEntity saveListData(EmployeeWorkPlanningsRequest.ListData dto) throws BaseAppException;

    ResponseEntity deleteData(Long id) throws RecordNotExistsException;

    BaseResponseEntity<EmployeeWorkPlanningsResponse.SearchForm> getDataById(Long id) throws RecordNotExistsException;

    ResponseEntity<Object> exportData(EmployeeWorkPlanningsRequest.SearchForm dto) throws Exception;

    ResponseEntity getDataByEvaluationId(Long id) throws RecordNotExistsException;

}
