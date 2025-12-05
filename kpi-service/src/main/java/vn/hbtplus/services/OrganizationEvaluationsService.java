/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.services;

import org.springframework.http.ResponseEntity;
import vn.hbtplus.exceptions.BaseAppException;
import vn.hbtplus.exceptions.RecordNotExistsException;
import vn.hbtplus.models.request.EmployeeEvaluationsRequest;
import vn.hbtplus.models.request.OrganizationEvaluationsRequest;
import vn.hbtplus.models.request.OrganizationWorkPlanningsRequest;
import vn.hbtplus.models.response.BaseResponseEntity;
import vn.hbtplus.models.response.ListResponseEntity;
import vn.hbtplus.models.response.OrganizationEvaluationsResponse;
import vn.hbtplus.models.response.TableResponseEntity;
import vn.hbtplus.repositories.entity.OrganizationWorkPlanningsEntity;

import java.util.List;
import java.util.Map;

/**
 * Lop interface service ung voi bang kpi_organization_evaluations
 *
 * @author tudd
 * @version 1.0
 * @since 1.0
 */

public interface OrganizationEvaluationsService {

    TableResponseEntity<OrganizationEvaluationsResponse.SearchResult> searchData(OrganizationEvaluationsRequest.SearchForm dto);

    ResponseEntity saveData(OrganizationEvaluationsRequest.SubmitForm dto) throws BaseAppException;

    ResponseEntity saveEmpManager(OrganizationEvaluationsRequest.SubmitForm dto, Long id) throws BaseAppException;

    ResponseEntity deleteData(Long id) throws RecordNotExistsException;

    BaseResponseEntity<Object> getDataById(Long id) throws RecordNotExistsException;

    ResponseEntity<Object> exportData(OrganizationEvaluationsRequest.SearchForm dto) throws Exception;

    ResponseEntity<Object> exportDataEvaluation(OrganizationEvaluationsRequest.SearchForm dto) throws Exception;

    ResponseEntity<Object> exportOrgSummary(OrganizationEvaluationsRequest.SearchForm dto) throws Exception;

    ResponseEntity updateOrgSummary(OrganizationEvaluationsRequest.OrgSummarySubmitForm dto, Long id) throws BaseAppException;

    ResponseEntity managerUpdateOrgSummary(OrganizationEvaluationsRequest.OrgSummarySubmitForm dto, Long id) throws BaseAppException;

    ResponseEntity<Object> exportDataById(Long id) throws Exception;

    ResponseEntity<Object> exportEvaluateById(Long id) throws Exception;

    ResponseEntity<Object> exportAllEmp(Long id) throws Exception;

    ResponseEntity<Object> exportAggregateData(OrganizationEvaluationsRequest.SearchForm dto) throws Exception;

    ResponseEntity<Object> exportAggregateKHCTSchool() throws Exception;

    ResponseEntity<Object> exportAggregateKHCTSchoolLevel1() throws Exception;

    ResponseEntity<Object> exportAggregateKHCTSchoolInvalid(OrganizationEvaluationsRequest.SearchForm dto) throws Exception;

    TableResponseEntity<Map<String, Object>> searchAggregateData(OrganizationEvaluationsRequest.SearchForm dto);

    ResponseEntity getIndicatorById(Long id) throws BaseAppException;

    ResponseEntity getIndicatorByIdLevel1(OrganizationEvaluationsRequest.SearchForm dto) throws BaseAppException;

    ListResponseEntity<OrganizationWorkPlanningsEntity> getWorkPlanningById(Long id) throws BaseAppException;

    ResponseEntity saveIndicatorData(OrganizationEvaluationsRequest.IndicatorSubmitForm dto, Long id) throws BaseAppException;

    ResponseEntity saveWorkPlanningData(OrganizationWorkPlanningsRequest.SubmitForm dto) throws BaseAppException;

    ResponseEntity updateStatusById(OrganizationWorkPlanningsRequest.Status dto, Long id) throws RecordNotExistsException;

    boolean review(String type, OrganizationEvaluationsRequest.Review reviewRequest);

    boolean sendForApproval(Long id);

    boolean approve(String type, OrganizationEvaluationsRequest.Review reviewRequest);

    List<OrganizationEvaluationsResponse.OrganizationDto> getOrgParent(Long periodId, Long orgId, Long employeeId);

    TableResponseEntity<OrganizationEvaluationsResponse.OrgParent> getTableDataOrgParent(OrganizationEvaluationsRequest.OrgParent data);

    void validatePermissionEvaluateManage(Long organizationEvaluationId);

    OrganizationEvaluationsResponse.Validate validatePermissionUpdate(Long organizationEvaluationId);

    ResponseEntity getErrorWorkPlanning();


    ResponseEntity confirmResult(List<Long> listId) throws RecordNotExistsException;

    ResponseEntity finalResult(List<Long> listId) throws RecordNotExistsException;

    ResponseEntity adjustEvaluate(OrganizationEvaluationsRequest.RejectDto dto) throws RecordNotExistsException;

    ResponseEntity sendForApprovalLevel1(OrganizationEvaluationsRequest.RejectDto dto) throws RecordNotExistsException;

    ResponseEntity confirmLevel1(OrganizationEvaluationsRequest.RejectDto dto) throws RecordNotExistsException;
}
