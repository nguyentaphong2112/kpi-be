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
import vn.kpi.constants.BaseConstants;
import vn.kpi.constants.Constant;
import vn.kpi.constants.Scope;
import vn.kpi.exceptions.BaseAppException;
import vn.kpi.models.BaseSearchRequest;
import vn.kpi.models.request.AwardProcessRequest;
import vn.kpi.models.request.EmployeesRequest;
import vn.kpi.models.response.AwardProcessResponse;
import vn.kpi.models.response.BaseResponseEntity;
import vn.kpi.models.response.EducationProcessResponse;
import vn.kpi.models.response.TableResponseEntity;
import vn.kpi.services.AwardProcessService;
import vn.kpi.services.EmployeesService;
import vn.kpi.utils.ResponseUtils;
import vn.kpi.utils.Utils;

import javax.validation.Valid;

@RestController
@RequestMapping(BaseConstants.REQUEST_MAPPING_PREFIX)
@RequiredArgsConstructor
@Resource(value = Constant.RESOURCES.AWARD_PROCESS)
public class AwardProcessController {
    private final AwardProcessService awardProcessService;
    private final EmployeesService employeesService;

    @GetMapping(value = "/v1/award-process", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public TableResponseEntity<AwardProcessResponse.SearchResult> searchData(EmployeesRequest.SearchForm dto) {
        return awardProcessService.searchData(dto);
    }

    @GetMapping(value = "/v1/award-process/export", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public ResponseEntity<Object> exportData(EmployeesRequest.SearchForm dto) throws Exception {
        return awardProcessService.exportData(dto);
    }

    @PostMapping(value = "/v1/award-process/{employeeId}", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.CREATE, domainId = "employeeId")
    public BaseResponseEntity<Long> saveData(@PathVariable Long employeeId, @RequestBody @Valid AwardProcessRequest.SubmitForm dto) throws BaseAppException {
        return awardProcessService.saveData(dto, employeeId, null);
    }

    @PutMapping(value = "/v1/award-process/{employeeId}/{id}", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.UPDATE, domainId = "employeeId")
    public BaseResponseEntity<Long> updateData(@RequestBody @Valid AwardProcessRequest.SubmitForm dto, @PathVariable Long employeeId, @PathVariable Long id) throws BaseAppException {
        return awardProcessService.saveData(dto, employeeId, id);
    }

    @DeleteMapping(value = "/v1/award-process/{employeeId}/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.DELETE, domainId = "employeeId")
    public BaseResponseEntity<Long> deleteData(@PathVariable Long employeeId, @PathVariable Long id) throws BaseAppException {
        return awardProcessService.deleteData(employeeId, id);
    }

    @GetMapping(value = "/v1/award-process/{employeeId}/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW, domainId = "employeeId")
    public BaseResponseEntity<AwardProcessResponse.DetailBean> getDataById(@PathVariable Long employeeId, @PathVariable Long id) throws BaseAppException {
        return awardProcessService.getDataById(employeeId, id);
    }

    @GetMapping(value = "/v1/award-process/pageable/{employeeId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW, domainId = "employeeId")
    public TableResponseEntity<EducationProcessResponse.DetailBean> getTableList(@PathVariable Long employeeId, BaseSearchRequest request) {
        return ResponseUtils.ok(awardProcessService.getTableList(employeeId, request));
    }


    /**
     * ************
     * Hiển thị trong thong tin cá nhân
     */
    @PostMapping(value = "/v1/award-process/personal", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.CREATE, resource = Constant.RESOURCES.PERSONAL_AWARD_PROCESS)
    public BaseResponseEntity<Long> saveData(@RequestBody @Valid AwardProcessRequest.SubmitForm dto) throws BaseAppException {
        Long employeeId = employeesService.getEmployeeId(Utils.getUserEmpCode());
        return awardProcessService.saveData(dto, employeeId, null);
    }

    @PutMapping(value = "/v1/award-process/personal/{id}", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.UPDATE, resource = Constant.RESOURCES.PERSONAL_AWARD_PROCESS)
    public BaseResponseEntity<Long> updateData(@RequestBody @Valid AwardProcessRequest.SubmitForm dto, @PathVariable Long id) throws BaseAppException {
        Long employeeId = employeesService.getEmployeeId(Utils.getUserEmpCode());
        return awardProcessService.saveData(dto, employeeId, id);
    }

    @DeleteMapping(value = "/v1/award-process/personal/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.DELETE, resource = Constant.RESOURCES.PERSONAL_AWARD_PROCESS)
    public BaseResponseEntity<Long> deleteData(@PathVariable Long id) throws BaseAppException {
        Long employeeId = employeesService.getEmployeeId(Utils.getUserEmpCode());
        return awardProcessService.deleteData(employeeId, id);
    }

    @GetMapping(value = "/v1/award-process/personal/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW, resource = Constant.RESOURCES.PERSONAL_AWARD_PROCESS)
    public BaseResponseEntity<AwardProcessResponse.DetailBean> getDataById(@PathVariable Long id) throws BaseAppException {
        Long employeeId = employeesService.getEmployeeId(Utils.getUserEmpCode());
        return awardProcessService.getDataById(employeeId, id);
    }

    @GetMapping(value = "/v1/award-process/pageable/personal", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW, resource = Constant.RESOURCES.PERSONAL_AWARD_PROCESS)
    public TableResponseEntity<EducationProcessResponse.DetailBean> getTableList(BaseSearchRequest request) {
        Long employeeId = employeesService.getEmployeeId(Utils.getUserEmpCode());
        return ResponseUtils.ok(awardProcessService.getTableList(employeeId, request));
    }

    @PostMapping(value = "/v1/award-process/import", produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @HasPermission(scope = Scope.IMPORT, resource = Constant.RESOURCES.AWARD_PROCESS)
    public ResponseEntity<Object> processImport(@RequestPart MultipartFile file, @RequestParam(required = false) boolean isForceUpdate) throws Exception {
        return awardProcessService.processImport(file, isForceUpdate);
    }

    @GetMapping(value = "/v1/award-process/import-template", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW, resource = Constant.RESOURCES.AWARD_PROCESS)
    public ResponseEntity<Object> downloadImportTemplate() throws Exception {
        return awardProcessService.downloadImportTemplate();
    }

}
