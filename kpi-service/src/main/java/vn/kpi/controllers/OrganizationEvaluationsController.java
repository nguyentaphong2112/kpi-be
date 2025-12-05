/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.kpi.controllers;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.kpi.annotations.HasPermission;
import vn.kpi.annotations.Resource;
import vn.kpi.constants.BaseConstants;
import vn.kpi.constants.Constant;
import vn.kpi.constants.Scope;
import vn.kpi.exceptions.BaseAppException;
import vn.kpi.exceptions.RecordNotExistsException;
import vn.kpi.models.request.OrganizationEvaluationsRequest;
import vn.kpi.models.request.OrganizationWorkPlanningsRequest;
import vn.kpi.models.response.BaseResponseEntity;
import vn.kpi.models.response.ListResponseEntity;
import vn.kpi.models.response.OrganizationEvaluationsResponse;
import vn.kpi.models.response.TableResponseEntity;
import vn.kpi.repositories.entity.OrganizationWorkPlanningsEntity;
import vn.kpi.services.OrganizationEvaluationsService;
import vn.kpi.utils.ResponseUtils;

import javax.validation.Valid;
import java.util.Map;

@RestController
@RequestMapping(BaseConstants.REQUEST_MAPPING_PREFIX)
@RequiredArgsConstructor
@Resource(value = Constant.RESOURCES.ORGANIZATION_EVALUATION)
public class OrganizationEvaluationsController {
    private final OrganizationEvaluationsService organizationEvaluationsService;

    @GetMapping(value = "/v1/organization-evaluations", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public TableResponseEntity<OrganizationEvaluationsResponse.SearchResult> searchData(OrganizationEvaluationsRequest.SearchForm dto) {
        return organizationEvaluationsService.searchData(dto);
    }

    @PostMapping(value = "/v1/organization-evaluations", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.UPDATE)
    public ResponseEntity saveData(@Valid OrganizationEvaluationsRequest.SubmitForm dto) throws BaseAppException {
//        organizationEvaluationsService.validatePermissionUpdate(dto.getOrganizationEvaluationId());
        return organizationEvaluationsService.saveData(dto);
    }

    @GetMapping(value = "/v1/organization-evaluations/get-validate/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public ResponseEntity getValidate(@PathVariable Long id) throws BaseAppException {
        //validate không được cập nhật khi hết thời han

        return ResponseUtils.ok(organizationEvaluationsService.validatePermissionUpdate(id));
    }

    @PostMapping(value = "/v1/organization-evaluations/evaluate", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.UPDATE, resource = Constant.RESOURCES.ORGANIZATION_EVALUATE)
    public ResponseEntity evaluate(@Valid OrganizationEvaluationsRequest.SubmitForm dto) throws BaseAppException {
        return organizationEvaluationsService.saveData(dto);
    }

    @PostMapping(value = "/v1/organization-evaluations/evaluate-manage", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.APPROVE, resource = Constant.RESOURCES.ORGANIZATION_EVALUATE)
    public ResponseEntity evaluateManage(@Valid OrganizationEvaluationsRequest.SubmitForm dto) throws BaseAppException {
        organizationEvaluationsService.validatePermissionEvaluateManage(dto.getOrganizationEvaluationId());
        return organizationEvaluationsService.saveData(dto);
    }

    @DeleteMapping(value = "/v1/organization-evaluations/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.DELETE)
    public ResponseEntity deleteData(@PathVariable Long id) throws RecordNotExistsException {
        return organizationEvaluationsService.deleteData(id);
    }

    @GetMapping(value = "/v1/organization-evaluations/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public BaseResponseEntity<Object> getDataById(@PathVariable Long id) throws RecordNotExistsException {
        return organizationEvaluationsService.getDataById(id);
    }

    @GetMapping(value = "/v1/organization-evaluations/export", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public ResponseEntity<Object> exportData(OrganizationEvaluationsRequest.SearchForm dto) throws Exception {
        return organizationEvaluationsService.exportData(dto);
    }

    @GetMapping(value = "/v1/organization-evaluations/export/evaluation", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW, resource = Constant.RESOURCES.ORGANIZATION_EVALUATE)
    public ResponseEntity<Object> exportDataEvaluation(OrganizationEvaluationsRequest.SearchForm dto) throws Exception {
        return organizationEvaluationsService.exportDataEvaluation(dto);
    }

    @GetMapping(value = "/v1/organization-evaluations/export-org-summary", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW, resource = Constant.RESOURCES.ORGANIZATION_SUMMARY)
    public ResponseEntity<Object> exportOrgSummary(OrganizationEvaluationsRequest.SearchForm dto) throws Exception {
        return organizationEvaluationsService.exportOrgSummary(dto);
    }


    @PutMapping(value = "/v1/organization-evaluations/update-emp-summary/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.APPROVE, resource = Constant.RESOURCES.ORGANIZATION_SUMMARY)
    public ResponseEntity updateOrgSummary(@Valid @RequestBody OrganizationEvaluationsRequest.OrgSummarySubmitForm dto, @PathVariable Long id) {
        return organizationEvaluationsService.updateOrgSummary(dto, id);
    }

    @PutMapping(value = "/v1/organization-evaluations/manager-update-emp-summary/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.UPDATE, resource = Constant.RESOURCES.ORGANIZATION_SUMMARY)
    public ResponseEntity managerUpdateOrgSummary(@Valid @RequestBody OrganizationEvaluationsRequest.OrgSummarySubmitForm dto, @PathVariable Long id) {
        return organizationEvaluationsService.managerUpdateOrgSummary(dto, id);
    }

    @GetMapping(value = "/v1/organization-evaluations/export/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public ResponseEntity<Object> exportDataById(@PathVariable Long id) throws Exception {
        return organizationEvaluationsService.exportDataById(id);
    }

    @GetMapping(value = "/v1/organization-evaluations/export-evaluate/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public ResponseEntity<Object> exportEvaluateById(@PathVariable Long id) throws Exception {
        return organizationEvaluationsService.exportEvaluateById(id);
    }

    @GetMapping(value = "/v1/organization-evaluations/export-all-emp/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public ResponseEntity<Object> exportAllEmp(@PathVariable Long id) throws Exception {
        return organizationEvaluationsService.exportAllEmp(id);
    }


    @GetMapping(value = "/v1/organization-evaluations/aggregate-data/export", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW, resource = Constant.RESOURCES.ORGANIZATION_AGGREGATE_DATA)
    public ResponseEntity<Object> exportAggregateData(OrganizationEvaluationsRequest.SearchForm dto) throws Exception {
        return organizationEvaluationsService.exportAggregateData(dto);
    }


    @GetMapping(value = "/v1/organization-evaluations/aggregate-data", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW, resource = Constant.RESOURCES.ORGANIZATION_AGGREGATE_DATA)
    public TableResponseEntity<Map<String, Object>> searchAggregateData(OrganizationEvaluationsRequest.SearchForm dto) throws Exception {
        return organizationEvaluationsService.searchAggregateData(dto);
    }


    @GetMapping(value = "/v1/organization-evaluations/aggregate-export/{reportType}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW, resource = Constant.RESOURCES.KPI_EXPORT)
    public ResponseEntity<Object> exportAggregateKHCTSchool(OrganizationEvaluationsRequest.SearchForm dto, @PathVariable String reportType) throws Exception {
        return switch (reportType) {
            case "SCHOOL" -> organizationEvaluationsService.exportAggregateKHCTSchool();
            case "SCHOOL_LEVEL1" -> organizationEvaluationsService.exportAggregateKHCTSchoolLevel1();
            case "SCHOOL_INVALID" -> organizationEvaluationsService.exportAggregateKHCTSchoolInvalid(dto);
            default -> ResponseUtils.ok();
        };
    }


    @GetMapping(value = "/v1/organization-evaluations/indicator/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public ResponseEntity getIndicatorById(@PathVariable Long id) throws BaseAppException {
        return organizationEvaluationsService.getIndicatorById(id);
    }

    @PutMapping(value = "/v1/organization-evaluations/indicator/{id}", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.UPDATE)
    public ResponseEntity saveIndicatorData(@Valid OrganizationEvaluationsRequest.IndicatorSubmitForm dto, @PathVariable Long id) throws BaseAppException {
        return organizationEvaluationsService.saveIndicatorData(dto, id);
    }

    @GetMapping(value = "/v1/organization-evaluations/work-planning/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public ListResponseEntity<OrganizationWorkPlanningsEntity> getWorkPlanningById(@PathVariable Long id) throws BaseAppException {
        return organizationEvaluationsService.getWorkPlanningById(id);
    }

    @PostMapping(value = "/v1/organization-evaluations/work-planning", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.UPDATE)
    public ResponseEntity saveWorkPlanningData(@Valid OrganizationWorkPlanningsRequest.SubmitForm dto) throws BaseAppException {
        return organizationEvaluationsService.saveWorkPlanningData(dto);
    }

    @PutMapping(value = "/v1/organization-evaluations/status/{id}", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.UPDATE, resource = Constant.RESOURCES.ORGANIZATION_EVALUATE)
    public ResponseEntity updateStatus(@RequestBody @Valid OrganizationWorkPlanningsRequest.Status dto, @PathVariable Long id) throws BaseAppException {
        return organizationEvaluationsService.updateStatusById(dto, id);
    }

    @PutMapping(value = "/v1/organization-evaluations/status-approved/{id}", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.APPROVE)
    public ResponseEntity updateAdjustEvaluate(@RequestBody @Valid OrganizationWorkPlanningsRequest.Status dto, @PathVariable Long id) throws BaseAppException {
        return organizationEvaluationsService.updateStatusById(dto, id);
    }

    @PutMapping(value = "/v1/organization-evaluations/empManager/{id}", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.UPDATE)
    public ResponseEntity saveEmpManager(@Valid @RequestBody OrganizationEvaluationsRequest.SubmitForm dto, @PathVariable Long id) throws BaseAppException {
        return organizationEvaluationsService.saveEmpManager(dto, id);
    }

    @PutMapping(value = "/v1/organization-evaluations/send-for-approval/{id}", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.UPDATE)
    public ResponseEntity sendForApproval(@PathVariable Long id) throws BaseAppException {
        return ResponseUtils.ok(organizationEvaluationsService.sendForApproval(id));
    }

    @PostMapping(value = "/v1/organization-evaluations/review/{type}", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.REVIEW)
    public ResponseEntity actionReview(@PathVariable String type, @RequestBody OrganizationEvaluationsRequest.Review reviewRequest) throws BaseAppException {
        if (!StringUtils.equalsAnyIgnoreCase(type, "ok", "not-ok")) {
            throw new BaseAppException("type is invalid");
        }
        return ResponseUtils.ok(organizationEvaluationsService.review(type, reviewRequest));
    }

    @PostMapping(value = "/v1/organization-evaluations/approve/{type}", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.APPROVE)
    public ResponseEntity actionApprove(@PathVariable String type, @RequestBody OrganizationEvaluationsRequest.Review reviewRequest) throws BaseAppException {
        if (!StringUtils.equalsAnyIgnoreCase(type, "ok", "not-ok")) {
            throw new BaseAppException("type is invalid");
        }
        return ResponseUtils.ok(organizationEvaluationsService.approve(type, reviewRequest));
    }

    @GetMapping(value = "/v1/organization-evaluations/org-parent/{periodId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ListResponseEntity<OrganizationEvaluationsResponse.OrganizationDto> getOrgParent(
            @PathVariable Long periodId,
            @RequestParam(required = false) Long orgId, @RequestParam(required = false) Long employeeId) throws BaseAppException {
        return ResponseUtils.ok(organizationEvaluationsService.getOrgParent(periodId, orgId, employeeId));
    }

    @GetMapping(value = "/v1/organization-evaluations/org-parent", produces = MediaType.APPLICATION_JSON_VALUE)
    public TableResponseEntity<OrganizationEvaluationsResponse.OrgParent> getTableDataOrgParent(OrganizationEvaluationsRequest.OrgParent data) {
        return organizationEvaluationsService.getTableDataOrgParent(data);
    }

    @GetMapping(value = "/v1/organization-evaluations/work-planning-error", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public ResponseEntity getErrorWorkPlanning() throws BaseAppException {
        return organizationEvaluationsService.getErrorWorkPlanning();
    }


    @PutMapping(value = "/v1/organization-evaluations/confirm-result", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.REVIEW)
    public ResponseEntity confirmResult(@RequestBody OrganizationEvaluationsRequest.SearchForm dto) throws BaseAppException {
        return organizationEvaluationsService.confirmResult(dto.getListId());
    }

    @PutMapping(value = "/v1/organization-evaluations/final-result", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.REVIEW, resource = Constant.RESOURCES.ORGANIZATION_SUMMARY)
    public ResponseEntity finalResult(@RequestBody OrganizationEvaluationsRequest.SearchForm dto) throws BaseAppException {
        return organizationEvaluationsService.finalResult(dto.getListId());
    }

    @PutMapping(value = "/v1/organization-evaluations/adjust-manage-evaluate", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.REVIEW)
    public ResponseEntity updateAdjustEvaluate(@RequestBody @Valid OrganizationEvaluationsRequest.RejectDto dto) throws BaseAppException {
        return organizationEvaluationsService.adjustEvaluate(dto);
    }

    @PostMapping(value = "/v1/organization-evaluations/level1", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.UPDATE, resource = Constant.RESOURCES.ORGANIZATION_PROVIDE_LEVEL1)
    public ResponseEntity saveDataLevel1(@Valid OrganizationEvaluationsRequest.SubmitForm dto) throws BaseAppException {
        dto.setIsLevel1(true);
        return organizationEvaluationsService.saveData(dto);
    }

    @GetMapping(value = "/v1/organization-evaluations/level1/indicator", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW, resource = Constant.RESOURCES.ORGANIZATION_PROVIDE_LEVEL1)
    public ResponseEntity getIndicatorLevel1ById(OrganizationEvaluationsRequest.SearchForm dto) throws BaseAppException {
        return organizationEvaluationsService.getIndicatorByIdLevel1(dto);
    }

    @PostMapping(value = "/v1/organization-evaluations/send-for-approval/level1", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.UPDATE, resource = Constant.RESOURCES.ORGANIZATION_PROVIDE_LEVEL1)
    public ResponseEntity sendForApprovalLevel1(@Valid OrganizationEvaluationsRequest.RejectDto dto) throws BaseAppException {
        return organizationEvaluationsService.sendForApprovalLevel1(dto);
    }

    @PostMapping(value = "/v1/organization-evaluations/confirm/level1", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.APPROVE, resource = Constant.RESOURCES.ORGANIZATION_PROVIDE_LEVEL1)
    public ResponseEntity confirmLevel1(@Valid OrganizationEvaluationsRequest.RejectDto dto) throws BaseAppException {
        return organizationEvaluationsService.confirmLevel1(dto);
    }
}
