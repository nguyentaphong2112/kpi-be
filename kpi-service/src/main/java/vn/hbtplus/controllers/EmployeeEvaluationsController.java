/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.controllers;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.hbtplus.annotations.HasPermission;
import vn.hbtplus.annotations.Resource;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.constants.Constant;
import vn.hbtplus.constants.Scope;
import vn.hbtplus.exceptions.BaseAppException;
import vn.hbtplus.exceptions.RecordNotExistsException;
import vn.hbtplus.models.request.EmployeeEvaluationsRequest;
import vn.hbtplus.models.request.EmployeeWorkPlanningsRequest;
import vn.hbtplus.models.response.BaseResponseEntity;
import vn.hbtplus.models.response.EmployeeEvaluationsResponse;
import vn.hbtplus.models.response.TableResponseEntity;
import vn.hbtplus.repositories.jpa.EmployeesRepositoryJPA;
import vn.hbtplus.services.EmployeeEvaluationsService;
import vn.hbtplus.utils.ResponseUtils;
import vn.hbtplus.utils.Utils;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping(BaseConstants.REQUEST_MAPPING_PREFIX)
@RequiredArgsConstructor
@Resource(value = Constant.RESOURCES.EMPLOYEE_EVALUATIONS)
public class EmployeeEvaluationsController {
    private final EmployeeEvaluationsService employeeEvaluationsService;
    private final EmployeesRepositoryJPA employeesRepositoryJPA;

    @GetMapping(value = "/v1/employee-evaluations", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public TableResponseEntity<EmployeeEvaluationsResponse.SearchResult> searchData(EmployeeEvaluationsRequest.SearchForm dto) {
        return employeeEvaluationsService.searchData(dto);
    }

    @PostMapping(value = "/v1/employee-evaluations", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.UPDATE)
    public ResponseEntity saveData(@Valid EmployeeEvaluationsRequest.SubmitForm dto) throws BaseAppException {
        //validate không được cập nhật khi hết thời han
//        employeeEvaluationsService.validatePermissionUpdate(dto.getEmployeeEvaluationId());

        return employeeEvaluationsService.saveData(dto);
    }

    @GetMapping(value = "/v1/employee-evaluations/get-validate/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public ResponseEntity getValidate(@PathVariable Long id) throws BaseAppException {
        //validate không được cập nhật khi hết thời han

        return ResponseUtils.ok(employeeEvaluationsService.validatePermissionUpdate(id));
    }

    @GetMapping(value = "/v1/employee-evaluations/get-current-job/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public ResponseEntity getCurrentJob(@PathVariable Long id) throws BaseAppException {
        //validate không được cập nhật khi hết thời han

        return ResponseUtils.ok(employeeEvaluationsService.getCurrentJob(id));
    }

    @PostMapping(value = "/v1/employee-evaluations/evaluate", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.UPDATE, resource = Constant.RESOURCES.EMPLOYEE_EVALUATE)
    public ResponseEntity evaluate(@Valid EmployeeEvaluationsRequest.SubmitForm dto) throws BaseAppException {
        return employeeEvaluationsService.saveData(dto);
    }

    @PostMapping(value = "/v1/employee-evaluations/evaluate-manage", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.APPROVE, resource = Constant.RESOURCES.EMPLOYEE_EVALUATE)
    public ResponseEntity evaluateManage(@Valid EmployeeEvaluationsRequest.SubmitForm dto) throws BaseAppException {
        //validate user không nhập thông tin tự đánh giá cho chính mình
        employeeEvaluationsService.validatePermissionEvaluateManagement(dto.getEmployeeEvaluationId());
        return employeeEvaluationsService.saveData(dto);
    }

    @DeleteMapping(value = "/v1/employee-evaluations/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.DELETE)
    public ResponseEntity deleteData(@PathVariable Long id) throws RecordNotExistsException {
        return employeeEvaluationsService.deleteData(id);
    }

    @GetMapping(value = "/v1/employee-evaluations/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public BaseResponseEntity<EmployeeEvaluationsResponse.SearchResult> getDataById(@PathVariable Long id) throws RecordNotExistsException {
        return employeeEvaluationsService.getDataById(id);
    }

    @GetMapping(value = "/v1/employee-evaluations/get-data-emp/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public ResponseEntity getDataEmp(@PathVariable Long id) throws RecordNotExistsException {
        return employeeEvaluationsService.getEmpData(id);
    }

    @GetMapping(value = "/v1/employee-evaluations/get-data-user", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity getDataUser() throws RecordNotExistsException {
        Long employeeId = employeesRepositoryJPA.getIdByEmployeeCode(Utils.getUserEmpCode());
        if (employeeId == null) {
            return ResponseUtils.ok();
        }
        return employeeEvaluationsService.getEmpData(employeeId);
    }


    @GetMapping(value = "/v1/employee-evaluations/export", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public ResponseEntity<Object> exportData(EmployeeEvaluationsRequest.SearchForm dto) throws Exception {
        return employeeEvaluationsService.exportData(dto);
    }

    @GetMapping(value = "/v1/employee-evaluations/export/evaluation", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW, resource = Constant.RESOURCES.EMPLOYEE_EVALUATE)
    public ResponseEntity<Object> exportDataEvaluation(EmployeeEvaluationsRequest.SearchForm dto) throws Exception {
        return employeeEvaluationsService.exportDataEvaluation(dto);
    }

    @GetMapping(value = "/v1/employee-evaluations/export-emp-summary", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW, resource = Constant.RESOURCES.EMPLOYEE_KPI_SUMARY)
    public ResponseEntity<Object> exportEmpSummary(EmployeeEvaluationsRequest.SearchForm dto) throws Exception {
        return employeeEvaluationsService.exportEmpSummary(dto);
    }

    @PutMapping(value = "/v1/employee-evaluations/update-emp-summary/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.APPROVE, resource = Constant.RESOURCES.EMPLOYEE_KPI_SUMARY)
    public ResponseEntity updateEmpSummary(@Valid @RequestBody EmployeeEvaluationsRequest.EmpSummarySubmitForm dto,@PathVariable Long id) {
        return employeeEvaluationsService.updateEmpSummary(dto, id);
    }

    @PutMapping(value = "/v1/employee-evaluations/manager-update-emp-summary/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.UPDATE, resource = Constant.RESOURCES.EMPLOYEE_KPI_SUMARY)
    public ResponseEntity managerUpdateEmpSummary(@Valid @RequestBody EmployeeEvaluationsRequest.EmpSummarySubmitForm dto,@PathVariable Long id) {
        return employeeEvaluationsService.managerUpdateEmpSummary(dto, id);
    }

    @GetMapping(value = "/v1/employee-evaluations/export/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public ResponseEntity<Object> exportDataById(@PathVariable Long id) throws Exception {
        return employeeEvaluationsService.exportDataById(id);
    }

    @GetMapping(value = "/v1/employee-evaluations/export-evaluate/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public ResponseEntity<Object> exportEvaluationsById(@PathVariable Long id) throws Exception {
        return employeeEvaluationsService.exportEvaluationsById(id);
    }

    @GetMapping(value = "/v1/employee-evaluations/indicator/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public ResponseEntity getIndicatorById(@PathVariable Long id) throws BaseAppException {
        return employeeEvaluationsService.getIndicatorById(id, false);
    }

    @GetMapping(value = "/v1/employee-evaluations/indicator-list", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public ResponseEntity getIndicatorByListId(@RequestParam List<Long> listId) throws BaseAppException {
        return employeeEvaluationsService.getIndicatorByListId(listId);
    }

    @PostMapping(value = "/v1/employee-evaluations/save-list-evaluate", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.UPDATE, resource = Constant.RESOURCES.EMPLOYEE_EVALUATE)
    public ResponseEntity saveListEvaluate(@RequestBody EmployeeEvaluationsRequest.Evaluate dto) throws BaseAppException {
        return employeeEvaluationsService.saveListEvaluate(dto);
    }

    @PutMapping(value = "/v1/employee-evaluations/indicator/{id}", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.UPDATE)
    public ResponseEntity saveIndicatorData(@Valid EmployeeEvaluationsRequest.IndicatorSubmitForm dto, @PathVariable Long id) throws BaseAppException {
        return employeeEvaluationsService.saveIndicatorData(dto, id);
    }

    @GetMapping(value = "/v1/employee-evaluations/work-planning/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public ResponseEntity getWorkPlanningById(@PathVariable Long id) throws BaseAppException {
        return employeeEvaluationsService.getWorkPlanningById(id);
    }

    @PostMapping(value = "/v1/employee-evaluations/work-planning", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.UPDATE)
    public ResponseEntity saveWorkPlanningData(@Valid EmployeeWorkPlanningsRequest.SubmitForm dto) throws BaseAppException {
        return employeeEvaluationsService.saveWorkPlanningData(dto);
    }

    @PutMapping(value = "/v1/employee-evaluations/send-for-approval/{id}", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.UPDATE)
    public ResponseEntity sendForApproval(@PathVariable Long id) throws BaseAppException {
        return ResponseUtils.ok(employeeEvaluationsService.sendForApproval(id, false));
    }

    @PostMapping(value = "/v1/employee-evaluations/review/{type}", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = {Scope.REVIEW, Scope.APPROVE})
    public ResponseEntity actionReview(@PathVariable String type, @RequestBody EmployeeEvaluationsRequest.Review reviewRequest) throws BaseAppException {
        if (!StringUtils.equalsAnyIgnoreCase(type, "ok", "not-ok")) {
            throw new BaseAppException("type is invalid");
        }
        return ResponseUtils.ok(employeeEvaluationsService.review(type, reviewRequest));
    }

    @PostMapping(value = "/v1/employee-evaluations/approve/{type}", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.APPROVE)
    public ResponseEntity actionApprove(@PathVariable String type, @RequestBody EmployeeEvaluationsRequest.Review reviewRequest) throws BaseAppException {
        if (!StringUtils.equalsAnyIgnoreCase(type, "ok", "not-ok")) {
            throw new BaseAppException("type is invalid");
        }
        return ResponseUtils.ok(employeeEvaluationsService.approve(type, reviewRequest));
    }

    @PutMapping(value = "/v1/employee-evaluations/status/{employeeEvaluationId}", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.UPDATE, resource = Constant.RESOURCES.EMPLOYEE_EVALUATE)
    public ResponseEntity updateStatus(@RequestBody @Valid EmployeeWorkPlanningsRequest.Status dto, @PathVariable Long employeeEvaluationId) throws BaseAppException {
        return employeeEvaluationsService.updateStatusById(dto, employeeEvaluationId);
    }

    @PutMapping(value = "/v1/employee-evaluations/status-approved/{employeeEvaluationId}", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.APPROVE)
    public ResponseEntity updateAdjustEvaluate(@RequestBody @Valid EmployeeWorkPlanningsRequest.Status dto, @PathVariable Long employeeEvaluationId) throws BaseAppException {
        return employeeEvaluationsService.updateStatusById(dto, employeeEvaluationId);
    }

    @PutMapping(value = "/v1/employee-evaluations/confirm-result", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.REVIEW)
    public ResponseEntity confirmResult(@RequestBody EmployeeEvaluationsRequest.SearchForm dto) throws BaseAppException {
        return employeeEvaluationsService.confirmResult(dto.getListId());
    }

    @PutMapping(value = "/v1/employee-evaluations/adjust-manage-evaluate", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.REVIEW)
    public ResponseEntity updateAdjustEvaluate(@RequestBody @Valid EmployeeWorkPlanningsRequest.RejectDto dto) throws BaseAppException {
        return employeeEvaluationsService.adjustEvaluate(dto);
    }

    @PutMapping(value = "/v1/employee-evaluations/final-result", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.REVIEW, resource = Constant.RESOURCES.EMPLOYEE_KPI_SUMARY)
    public ResponseEntity finalResult(@RequestBody EmployeeEvaluationsRequest.SearchForm dto) throws BaseAppException {
        return employeeEvaluationsService.finalResult(dto.getListId());
    }


    @GetMapping(value = "/v1/public/employee-evaluations", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity updateAdjustEvaluate() throws BaseAppException {
        return ResponseUtils.ok(employeeEvaluationsService.calculateKpiPoints(null));
    }


    @GetMapping(value = "/v1/employee-evaluations/work-planning-error", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public ResponseEntity getErrorWorkPlanning() throws BaseAppException {
        return employeeEvaluationsService.getErrorWorkPlanning();
    }


}
