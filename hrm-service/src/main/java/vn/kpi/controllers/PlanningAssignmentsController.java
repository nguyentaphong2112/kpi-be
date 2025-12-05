/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.kpi.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import vn.kpi.annotations.HasPermission;
import vn.kpi.annotations.Resource;
import vn.kpi.constants.BaseConstants;
import vn.kpi.constants.Constant;
import vn.kpi.constants.Scope;
import vn.kpi.exceptions.BaseAppException;
import vn.kpi.models.BaseSearchRequest;
import vn.kpi.models.request.EmployeesRequest;
import vn.kpi.models.request.PlanningAssignmentsRequest;
import vn.kpi.models.response.BaseResponseEntity;
import vn.kpi.models.response.EducationProcessResponse;
import vn.kpi.models.response.PlanningAssignmentsResponse;
import vn.kpi.models.response.TableResponseEntity;
import vn.kpi.services.EmployeesService;
import vn.kpi.services.PlanningAssignmentsService;
import vn.kpi.utils.ResponseUtils;
import vn.kpi.utils.Utils;

import java.util.List;

@RestController
@RequestMapping(BaseConstants.REQUEST_MAPPING_PREFIX)
@RequiredArgsConstructor
@Resource(value = Constant.RESOURCES.PLANNING_ASSIGNMENTS)
public class PlanningAssignmentsController {
    private final PlanningAssignmentsService planningAssignmentsService;
    private final EmployeesService employeesService;

    @GetMapping(value = "/v1/planning-assignments", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public TableResponseEntity<PlanningAssignmentsResponse.SearchResult> searchData(EmployeesRequest.SearchForm dto) {
        return planningAssignmentsService.searchData(dto);
    }

    @GetMapping(value = "/v1/planning-assignments/export", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public ResponseEntity<Object> exportData(EmployeesRequest.SearchForm dto) throws Exception {
        return planningAssignmentsService.exportData(dto);
    }

    @PostMapping(value = "/v1/planning-assignments/{employeeId}", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.UPDATE, domainId = "employeeId")
    public ResponseEntity saveData(@RequestPart(value = "data") PlanningAssignmentsRequest.SubmitForm dto, @PathVariable Long employeeId,
                                   @RequestPart(value = "files", required = false) List<MultipartFile> files) throws BaseAppException {
        return planningAssignmentsService.saveData(dto, files, employeeId, null);
    }

    @PutMapping(value = "/v1/planning-assignments/{employeeId}/{id}", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.UPDATE, domainId = "employeeId")
    public ResponseEntity updateData(@RequestPart(value = "data") PlanningAssignmentsRequest.SubmitForm dto, @PathVariable Long employeeId, @PathVariable Long id,
                                     @RequestPart(value = "files", required = false) List<MultipartFile> files) throws BaseAppException {
        return planningAssignmentsService.saveData(dto, files, employeeId, id);
    }

    @DeleteMapping(value = "/v1/planning-assignments/{employeeId}/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.DELETE, domainId = "employeeId")
    public ResponseEntity deleteData(@PathVariable Long employeeId, @PathVariable Long id) throws BaseAppException {
        return planningAssignmentsService.deleteData(employeeId, id);
    }

    @GetMapping(value = "/v1/planning-assignments/pageable/{employeeId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW, domainId = "employeeId")
    public TableResponseEntity<EducationProcessResponse.DetailBean> getTableList(@PathVariable Long employeeId, BaseSearchRequest request) {
        return ResponseUtils.ok(planningAssignmentsService.getTableList(employeeId, request));
    }

    @GetMapping(value = "/v1/planning-assignments/pageable/personal", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW, resource = Constant.RESOURCES.PERSONAL_PLANNING_ASSIGNMENTS)
    public TableResponseEntity<EducationProcessResponse.DetailBean> getTableList(BaseSearchRequest request) {
        Long employeeId = employeesService.getEmployeeId(Utils.getUserEmpCode());
        return ResponseUtils.ok(planningAssignmentsService.getTableList(employeeId, request));
    }

    @GetMapping(value = "/v1/planning-assignments/{employeeId}/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW, domainId = "employeeId")
    public BaseResponseEntity<PlanningAssignmentsResponse.DetailBean> getDataById(@PathVariable Long employeeId, @PathVariable Long id) throws BaseAppException {
        return planningAssignmentsService.getDataById(employeeId, id);
    }

    @PostMapping(value = "/v1/planning-assignments/personal", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.UPDATE, resource = Constant.RESOURCES.PERSONAL_PLANNING_ASSIGNMENTS)
    public ResponseEntity saveData(@RequestPart(value = "data") PlanningAssignmentsRequest.SubmitForm dto, @RequestPart(value = "files", required = false) List<MultipartFile> files) throws BaseAppException {
        Long employeeId = employeesService.getEmployeeId(Utils.getUserEmpCode());
        return planningAssignmentsService.saveData(dto, files, employeeId, null);
    }

    @PutMapping(value = "/v1/planning-assignments/personal/{id}", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.UPDATE, resource = Constant.RESOURCES.PERSONAL_PLANNING_ASSIGNMENTS)
    public ResponseEntity updateData(@RequestPart(value = "data") PlanningAssignmentsRequest.SubmitForm dto, @PathVariable Long id,
                                     @RequestPart(value = "files", required = false) List<MultipartFile> files) throws BaseAppException {
        Long employeeId = employeesService.getEmployeeId(Utils.getUserEmpCode());
        return planningAssignmentsService.saveData(dto, files, employeeId, id);
    }

    @DeleteMapping(value = "/v1/planning-assignments/personal/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.DELETE, resource = Constant.RESOURCES.PERSONAL_PLANNING_ASSIGNMENTS)
    public ResponseEntity deleteData(@PathVariable Long id) throws BaseAppException {
        Long employeeId = employeesService.getEmployeeId(Utils.getUserEmpCode());
        return planningAssignmentsService.deleteData(employeeId, id);
    }

    @GetMapping(value = "/v1/planning-assignments/personal/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW, resource = Constant.RESOURCES.PERSONAL_PLANNING_ASSIGNMENTS)
    public BaseResponseEntity<PlanningAssignmentsResponse.DetailBean> getDataById(@PathVariable Long id) throws BaseAppException {
        Long employeeId = employeesService.getEmployeeId(Utils.getUserEmpCode());
        return planningAssignmentsService.getDataById(employeeId, id);
    }

    @PostMapping(value = "/v1/planning-assignments/import",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @HasPermission(scope = Scope.IMPORT, resource = Constant.RESOURCES.PLANNING_ASSIGNMENTS)
    public ResponseEntity<Object> processImport(@RequestPart MultipartFile file, @RequestParam(required = false) boolean isForceUpdate) throws Exception {
        return planningAssignmentsService.processImport(file, isForceUpdate);
    }

    @GetMapping(value = "/v1/planning-assignments/import-template", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.IMPORT, resource = Constant.RESOURCES.PLANNING_ASSIGNMENTS)
    public ResponseEntity<Object> downloadImportTemplate() throws Exception {
        return planningAssignmentsService.downloadImportTemplate();
    }

}
