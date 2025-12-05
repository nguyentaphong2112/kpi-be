/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.kpi.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import vn.kpi.annotations.HasPermission;
import vn.kpi.annotations.Resource;
import vn.kpi.annotations.UserLogActivity;
import vn.kpi.constants.BaseConstants;
import vn.kpi.constants.Constant;
import vn.kpi.constants.Scope;
import vn.kpi.exceptions.BaseAppException;
import vn.kpi.exceptions.RecordNotExistsException;
import vn.kpi.models.BaseSearchRequest;
import vn.kpi.models.request.EmployeesRequest;
import vn.kpi.models.request.WorkProcessRequest;
import vn.kpi.models.response.BaseResponseEntity;
import vn.kpi.models.response.TableResponseEntity;
import vn.kpi.models.response.WorkProcessResponse;
import vn.kpi.services.EmployeesService;
import vn.kpi.services.WorkProcessService;
import vn.kpi.utils.ResponseUtils;
import vn.kpi.utils.Utils;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping(BaseConstants.REQUEST_MAPPING_PREFIX)
@RequiredArgsConstructor
@Resource(value = Constant.RESOURCES.WORK_PROCESS)
public class WorkProcessController {
    private final WorkProcessService workProcessService;
    private final EmployeesService employeesService;

    @GetMapping(value = "/v1/work-process", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public TableResponseEntity<WorkProcessResponse.SearchResult> searchData(EmployeesRequest.SearchForm dto) {
        return workProcessService.searchData(dto);
    }

    @GetMapping(value = "/v1/work-process/export", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public ResponseEntity<Object> exportData(EmployeesRequest.SearchForm dto) throws Exception {
        return workProcessService.exportData(dto);
    }

    @PostMapping(value = "/v1/work-process/{employeeId}", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.CREATE, domainId = "employeeId")
    public BaseResponseEntity<Long> saveData(@RequestPart(value = "data") @Valid WorkProcessRequest.SubmitForm dto,
                                             @RequestPart(value = "files", required = false) List<MultipartFile> files,
                                             @PathVariable Long employeeId) throws BaseAppException {
        return workProcessService.saveData(dto, files, employeeId, null);
    }

    @PutMapping(value = "/v1/work-process/{employeeId}/{id}", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.UPDATE, domainId = "employeeId")
    public BaseResponseEntity<Long> updateData(@Valid @RequestPart(value = "data") WorkProcessRequest.SubmitForm dto,
                                               @RequestPart(value = "files", required = false) List<MultipartFile> files,
                                               @PathVariable Long employeeId, @PathVariable Long id) throws BaseAppException {
        return workProcessService.saveData(dto, files, employeeId, id);
    }

    @PostMapping(value = "/v2/work-process/{employeeId}", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.CREATE, domainId = "employeeId")
    public BaseResponseEntity<Long> saveData(@RequestPart(value = "data") @Valid WorkProcessRequest.SubmitFormV2 dto,
                                             @RequestPart(value = "files", required = false) List<MultipartFile> files,
                                             @PathVariable Long employeeId) throws BaseAppException {
        return ResponseUtils.ok(workProcessService.saveData(dto, files, employeeId, null));
    }

    @PutMapping(value = "/v2/work-process/{employeeId}/{id}", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.UPDATE, domainId = "employeeId")
    public BaseResponseEntity<Long> updateData(@Valid @RequestPart(value = "data") WorkProcessRequest.SubmitFormV2 dto,
                                               @RequestPart(value = "files", required = false) List<MultipartFile> files,
                                               @PathVariable Long employeeId, @PathVariable Long id) throws BaseAppException {
        return ResponseUtils.ok(workProcessService.saveData(dto, files, employeeId, id));
    }


    @DeleteMapping(value = "/v1/work-process/{employeeId}/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.DELETE, domainId = "employeeId")
    public ResponseEntity deleteData(@PathVariable Long id, @PathVariable Long employeeId) throws BaseAppException {
        return workProcessService.deleteData(employeeId, id);
    }

    @GetMapping(value = "/v1/work-process/{employeeId}/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW, domainId = "employeeId")
    public BaseResponseEntity<WorkProcessResponse.DetailBean> getDataById(@PathVariable Long id, @PathVariable Long employeeId) throws BaseAppException {
        return workProcessService.getDataById(employeeId, id);
    }

    @GetMapping(value = "/v1/work-process/pageable/{employeeId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW, domainId = "employeeId")
    public TableResponseEntity<WorkProcessResponse.DetailBean> getTableList(@PathVariable Long employeeId, BaseSearchRequest request) throws RecordNotExistsException {
        return ResponseUtils.ok(workProcessService.getTableList(employeeId, request));
    }

    @PostMapping(value = "/v1/work-process/personal", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.CREATE, resource = Constant.RESOURCES.PERSONAL_WORK_PROCESS)
    public BaseResponseEntity<Long> saveData(@Valid @RequestPart(value = "data") WorkProcessRequest.SubmitForm dto,
                                             @RequestPart(value = "files", required = false) List<MultipartFile> files) throws BaseAppException {
        Long employeeId = employeesService.getEmployeeId(Utils.getUserEmpCode());
        return workProcessService.saveData(dto, files, employeeId, null);
    }

    @PutMapping(value = "/v1/work-process/personal/{id}", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.UPDATE, resource = Constant.RESOURCES.PERSONAL_WORK_PROCESS)
    @UserLogActivity
    public BaseResponseEntity<Long> updateData(@Valid @RequestPart(value = "data") WorkProcessRequest.SubmitForm dto,
                                               @RequestPart(value = "files", required = false) List<MultipartFile> files,
                                               @PathVariable Long id) throws BaseAppException {
        Long employeeId = employeesService.getEmployeeId(Utils.getUserEmpCode());
        return workProcessService.saveData(dto, files, employeeId, id);
    }

    @DeleteMapping(value = "/v1/work-process/personal/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.DELETE, resource = Constant.RESOURCES.PERSONAL_WORK_PROCESS)
    public ResponseEntity deleteData(@PathVariable Long id) throws BaseAppException {
        Long employeeId = employeesService.getEmployeeId(Utils.getUserEmpCode());
        return workProcessService.deleteData(employeeId, id);
    }

    @GetMapping(value = "/v1/work-process/personal/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW, resource = Constant.RESOURCES.PERSONAL_WORK_PROCESS)
    public BaseResponseEntity<WorkProcessResponse.DetailBean> getDataById(@PathVariable Long id) throws BaseAppException {
        Long employeeId = employeesService.getEmployeeId(Utils.getUserEmpCode());
        return workProcessService.getDataById(employeeId, id);
    }

    @GetMapping(value = "/v1/work-process/pageable/personal", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW, resource = Constant.RESOURCES.PERSONAL_WORK_PROCESS)
    public TableResponseEntity<WorkProcessResponse.DetailBean> getTableList(BaseSearchRequest request) throws RecordNotExistsException {
        Long employeeId = employeesService.getEmployeeId(Utils.getUserEmpCode());
        return ResponseUtils.ok(workProcessService.getTableList(employeeId, request));
    }

    @GetMapping(value = "/v1/work-process/auto-update", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public ResponseEntity autoUpdate(@RequestParam String startDate) {
        return ResponseUtils.ok(workProcessService.autoUpdateWorkProcess(startDate, true));
    }

    @PostMapping(value = "/v2/work-process/personal", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.CREATE, resource = Constant.RESOURCES.PERSONAL_WORK_PROCESS)
    @UserLogActivity
    public BaseResponseEntity<Long> saveDataV2(@RequestPart(value = "data") @Valid WorkProcessRequest.SubmitFormV2 dto,
                                             @RequestPart(value = "files", required = false) List<MultipartFile> files) throws BaseAppException {
        Long employeeId = employeesService.getEmployeeId(Utils.getUserEmpCode());
        return ResponseUtils.ok(workProcessService.saveData(dto, files, employeeId, null));
    }

    @PutMapping(value = "/v2/work-process/personal/{id}", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.UPDATE, resource = Constant.RESOURCES.PERSONAL_WORK_PROCESS)
    @UserLogActivity
    public BaseResponseEntity<Long> updateDataV2(@Valid @RequestPart(value = "data") WorkProcessRequest.SubmitFormV2 dto,
                                               @RequestPart(value = "files", required = false) List<MultipartFile> files,
                                               @PathVariable Long id) throws BaseAppException {
        Long employeeId = employeesService.getEmployeeId(Utils.getUserEmpCode());
        return ResponseUtils.ok(workProcessService.saveData(dto, files, employeeId, id));
    }
}
