/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.kpi.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.kpi.annotations.HasPermission;
import vn.kpi.annotations.UserLogActivity;
import vn.kpi.models.BaseSearchRequest;
import vn.kpi.models.request.EmployeesRequest;
import vn.kpi.models.response.*;
import vn.kpi.models.request.PoliticalParticipationsRequest;
import vn.kpi.services.EmployeesService;
import vn.kpi.services.PoliticalParticipationsService;
import vn.kpi.constants.BaseConstants;
import vn.kpi.constants.Scope;
import vn.kpi.constants.Constant;
import vn.kpi.annotations.Resource;
import vn.kpi.exceptions.RecordNotExistsException;
import vn.kpi.exceptions.BaseAppException;
import vn.kpi.utils.ResponseUtils;
import vn.kpi.utils.Utils;

import javax.validation.Valid;

@RestController
@RequestMapping(BaseConstants.REQUEST_MAPPING_PREFIX)
@RequiredArgsConstructor
@Resource(value = Constant.RESOURCES.HR_POLITICAL_PARTICIPATIONS)
public class PoliticalParticipationsController {
    private final PoliticalParticipationsService politicalParticipationsService;
    private final EmployeesService employeesService;

    @GetMapping(value = "/v1/political-participations", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    @UserLogActivity()
    public TableResponseEntity<PoliticalParticipationsResponse.SearchResult> searchData(EmployeesRequest.SearchForm dto) {
        return politicalParticipationsService.searchData(dto);
    }

    @PostMapping(value = "/v1/political-participations/{employeeId}", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.CREATE, domainId = "employeeId")
    @UserLogActivity()
    public ResponseEntity saveData(@Valid PoliticalParticipationsRequest.SubmitForm dto, @PathVariable Long employeeId) throws BaseAppException {
        return politicalParticipationsService.saveData(dto, employeeId, null);
    }

    @PutMapping(value = "/v1/political-participations/{employeeId}/{id}", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.UPDATE, domainId = "employeeId")
    @UserLogActivity()
    public ResponseEntity saveData(@Valid PoliticalParticipationsRequest.SubmitForm dto, @PathVariable Long employeeId, @PathVariable Long id) throws BaseAppException {
        return politicalParticipationsService.saveData(dto, employeeId, id);
    }

    @DeleteMapping(value = "/v1/political-participations/{employeeId}/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.DELETE, domainId = "employeeId")
    @UserLogActivity()
    public ResponseEntity deleteData(@PathVariable Long employeeId, @PathVariable Long id) throws RecordNotExistsException {
        return politicalParticipationsService.deleteData(employeeId, id);
    }

    @GetMapping(value = "/v1/political-participations/{employeeId}/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW, domainId = "employeeId")
    public BaseResponseEntity<PoliticalParticipationsResponse.DetailBean> getDataById(@PathVariable Long employeeId, @PathVariable Long id) throws RecordNotExistsException {
        return politicalParticipationsService.getDataById(employeeId, id);
    }

    @GetMapping(value = "/v1/political-participations/pageable/{employeeId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW, domainId = "employeeId")
    public TableResponseEntity<PositionSalaryProcessResponse.SearchResult> getTableList(@PathVariable Long employeeId, BaseSearchRequest request) throws RecordNotExistsException {
        return ResponseUtils.ok(politicalParticipationsService.getTableList(employeeId, request));
    }

    @GetMapping(value = "/v1/political-participations/pageable/personal", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW, resource = Constant.RESOURCES.PERSONAL_POLITICAL_PARTICIPATION)
    public TableResponseEntity<PositionSalaryProcessResponse.SearchResult> getTableList(BaseSearchRequest request) throws RecordNotExistsException {
        Long employeeId = employeesService.getEmployeeId(Utils.getUserEmpCode());
        return ResponseUtils.ok(politicalParticipationsService.getTableList(employeeId, request));
    }

    @GetMapping(value = "/v1/political-participations/personal/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW, resource = Constant.RESOURCES.PERSONAL_POLITICAL_PARTICIPATION)
    public BaseResponseEntity<PoliticalParticipationsResponse.DetailBean> getDataById(@PathVariable Long id) throws BaseAppException {
        Long employeeId = employeesService.getEmployeeId(Utils.getUserEmpCode());
        return politicalParticipationsService.getDataById(employeeId, id);
    }

    @PostMapping(value = "/v1/political-participations/personal", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.CREATE, resource = Constant.RESOURCES.PERSONAL_POLITICAL_PARTICIPATION)
    @UserLogActivity()
    public ResponseEntity saveData(@Valid PoliticalParticipationsRequest.SubmitForm dto) throws BaseAppException {
        Long employeeId = employeesService.getEmployeeId(Utils.getUserEmpCode());
        return politicalParticipationsService.saveData(dto, employeeId, null);
    }

    @PutMapping(value = "/v1/political-participations/personal/{id}", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.UPDATE, resource = Constant.RESOURCES.PERSONAL_POLITICAL_PARTICIPATION)
    @UserLogActivity()
    public ResponseEntity saveDataV2(@Valid PoliticalParticipationsRequest.SubmitForm dto, @PathVariable Long id) throws BaseAppException {
        Long employeeId = employeesService.getEmployeeId(Utils.getUserEmpCode());
        return politicalParticipationsService.saveData(dto, employeeId, id);
    }

    @DeleteMapping(value = "/v1/political-participations/personal/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.DELETE, resource = Constant.RESOURCES.PERSONAL_POLITICAL_PARTICIPATION)
    public ResponseEntity deleteData(@PathVariable Long id) throws BaseAppException {
        Long employeeId = employeesService.getEmployeeId(Utils.getUserEmpCode());
        return politicalParticipationsService.deleteData(employeeId, id);
    }

    @GetMapping(value = "/v1/political-participations/export", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public ResponseEntity<Object> exportData(EmployeesRequest.SearchForm dto) throws Exception {
        return politicalParticipationsService.exportData(dto);
    }

}
