/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.kpi.services;

import org.springframework.http.ResponseEntity;
import vn.kpi.exceptions.BaseAppException;
import vn.kpi.exceptions.RecordNotExistsException;
import vn.kpi.models.request.EmployeeWorkPlanningsRequest;
import vn.kpi.models.response.*;
import vn.kpi.models.request.EmployeeEvaluationsRequest;

import java.util.List;

/**
 * Lop interface service ung voi bang kpi_employee_evaluations
 *
 * @author tudd
 * @version 1.0
 * @since 1.0
 */

public interface EmployeeEvaluationsService {

    TableResponseEntity<EmployeeEvaluationsResponse.SearchResult> searchData(EmployeeEvaluationsRequest.SearchForm dto);

    ResponseEntity saveData(EmployeeEvaluationsRequest.SubmitForm dto) throws BaseAppException;

    ResponseEntity deleteData(Long id) throws RecordNotExistsException;

    BaseResponseEntity<EmployeeEvaluationsResponse.SearchResult> getDataById(Long id) throws RecordNotExistsException;

    ResponseEntity<Object> exportData(EmployeeEvaluationsRequest.SearchForm dto) throws Exception;

    ResponseEntity<Object> exportDataEvaluation(EmployeeEvaluationsRequest.SearchForm dto) throws Exception;

    ResponseEntity<Object> exportEmpSummary(EmployeeEvaluationsRequest.SearchForm dto) throws Exception;

    ResponseEntity updateEmpSummary(EmployeeEvaluationsRequest.EmpSummarySubmitForm dto, Long id) throws BaseAppException;

    ResponseEntity managerUpdateEmpSummary(EmployeeEvaluationsRequest.EmpSummarySubmitForm dto, Long id) throws BaseAppException;

    ResponseEntity<Object> exportDataById(Long id) throws Exception;

    ResponseEntity<Object> exportEvaluationsById(Long id) throws Exception;

    ResponseEntity updateStatusById(EmployeeWorkPlanningsRequest.Status dto, Long employeeEvaluationId) throws RecordNotExistsException;

    ResponseEntity confirmResult(List<Long> listId) throws RecordNotExistsException;

    ResponseEntity finalResult(List<Long> listId) throws RecordNotExistsException;

    ResponseEntity adjustEvaluate(EmployeeWorkPlanningsRequest.RejectDto dto) throws RecordNotExistsException;

    ResponseEntity getIndicatorById(Long id, boolean isGetAll) throws BaseAppException;

    ResponseEntity getIndicatorByListId(List<Long> listId) throws BaseAppException;

    ResponseEntity saveIndicatorData(EmployeeEvaluationsRequest.IndicatorSubmitForm dto, Long id) throws BaseAppException;

    ResponseEntity saveListEvaluate(EmployeeEvaluationsRequest.Evaluate dto) throws BaseAppException;

    ResponseEntity saveWorkPlanningData(EmployeeWorkPlanningsRequest.SubmitForm dto) throws BaseAppException;

    ResponseEntity getWorkPlanningById(Long id) throws BaseAppException;

    boolean review(String type, EmployeeEvaluationsRequest.Review reviewRequest);

    boolean sendForApproval(Long id, boolean isGetAll);

    boolean approve(String type, EmployeeEvaluationsRequest.Review reviewRequest);

    void validatePermissionEvaluateManagement(Long employeeEvaluationId);

    EmployeeEvaluationsResponse.Validate validatePermissionUpdate(Long employeeEvaluationId);

    String getCurrentJob(Long employeeEvaluationId);

    ResponseEntity getEmpData(Long empId);

    //xu ly tinh lai diem kpi
    List calculateKpiPoints(Long employeeEvaluationId);


    ResponseEntity getErrorWorkPlanning();
}
