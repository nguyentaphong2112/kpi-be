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
import vn.kpi.exceptions.RecordNotExistsException;
import vn.kpi.models.BaseSearchRequest;
import vn.kpi.models.request.EducationDegreesRequest;
import vn.kpi.models.request.EmployeesRequest;
import vn.kpi.models.response.BaseResponseEntity;
import vn.kpi.models.response.EducationDegreesResponse;
import vn.kpi.models.response.ListResponseEntity;
import vn.kpi.models.response.TableResponseEntity;
import vn.kpi.services.EducationDegreesService;
import vn.kpi.services.EmployeesService;
import vn.kpi.utils.ResponseUtils;
import vn.kpi.utils.Utils;

import java.util.List;

@RestController
@RequestMapping(BaseConstants.REQUEST_MAPPING_PREFIX)
@RequiredArgsConstructor
@Resource(value = Constant.RESOURCES.EDUCATION_DEGREES)
public class EducationDegreesController {
    private final EducationDegreesService educationDegreesService;
    private final EmployeesService employeesService;

    @GetMapping(value = "/v1/education-degrees", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public TableResponseEntity<EducationDegreesResponse.SearchResult> searchData(EmployeesRequest.SearchForm dto) {
        return educationDegreesService.searchData(dto);
    }

    @GetMapping(value = "/v1/education-degrees/export", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public ResponseEntity<Object> exportData(EmployeesRequest.SearchForm dto) throws Exception {
        return educationDegreesService.exportData(dto);
    }

    @GetMapping(value = "/v1/education-degrees/download-template", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public ResponseEntity<Object> exportTemplate() throws Exception {
        return educationDegreesService.downloadTemplate();

    }

    @PostMapping(value = "/v1/education-degrees/import", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.VIEW)
    public ResponseEntity<Object> importProcess(@RequestPart(value = "file") MultipartFile file,EducationDegreesRequest.SubmitForm dto) throws Exception {
        return educationDegreesService.importProcess(file,dto);
    }

    @PostMapping(value = "/v1/education-degrees/{employeeId}", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.CREATE, domainId = "employeeId")
    public BaseResponseEntity<Long> saveData(@RequestPart(value = "data") EducationDegreesRequest.SubmitForm dto,
                                             @PathVariable Long employeeId,
                                             @RequestPart(value = "files", required = false) List<MultipartFile> files) throws BaseAppException {
        return educationDegreesService.saveData(dto, files, employeeId, null);
    }

    @PutMapping(value = "/v1/education-degrees/{employeeId}/{id}", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.UPDATE, domainId = "employeeId")
    public BaseResponseEntity<Long> updateData(@RequestPart(value = "data") EducationDegreesRequest.SubmitForm dto,
                                               @RequestPart(value = "files", required = false) List<MultipartFile> files,
                                               @PathVariable Long employeeId,
                                               @PathVariable Long id) throws BaseAppException {
        return educationDegreesService.saveData(dto, files, employeeId, id);
    }

    @DeleteMapping(value = "/v1/education-degrees/{employeeId}/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.DELETE, domainId = "employeeId")
    public BaseResponseEntity<Long> deleteData(@PathVariable Long employeeId, @PathVariable Long id) throws BaseAppException {
        return educationDegreesService.deleteData(employeeId, id);
    }

    @GetMapping(value = "/v1/education-degrees/{employeeId}/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW, domainId = "employeeId")
    public BaseResponseEntity<EducationDegreesResponse.DetailBean> getDataById(@PathVariable Long employeeId, @PathVariable Long id) throws BaseAppException {
        return educationDegreesService.getDataById(employeeId, id);
    }

    @GetMapping(value = "/v1/education-degrees/pageable/{employeeId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW, domainId = "employeeId")
    public TableResponseEntity<EducationDegreesResponse.DetailBean> getTableList(@PathVariable Long employeeId, BaseSearchRequest request) throws RecordNotExistsException {
        return ResponseUtils.ok(educationDegreesService.getTableList(employeeId, request));
    }


    @PostMapping(value = "/v1/education-degrees/personal", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.CREATE, resource = Constant.RESOURCES.PERSONAL_EDUCATION_DEGREES)
    public BaseResponseEntity<Long> saveData(@RequestPart(value = "data") EducationDegreesRequest.SubmitForm dto,
                                             @RequestPart(value = "files", required = false) List<MultipartFile> files) throws BaseAppException {
        Long employeeId = employeesService.getEmployeeId(Utils.getUserEmpCode());
        return educationDegreesService.saveData(dto, files, employeeId, null);
    }

    @PutMapping(value = "/v1/education-degrees/personal/{id}", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.UPDATE, resource = Constant.RESOURCES.PERSONAL_EDUCATION_DEGREES)
    public BaseResponseEntity<Long> updateData(@RequestPart(value = "data") EducationDegreesRequest.SubmitForm dto,
                                               @RequestPart(value = "files", required = false) List<MultipartFile> files,
                                               @PathVariable Long id) throws BaseAppException {
        Long employeeId = employeesService.getEmployeeId(Utils.getUserEmpCode());
        return educationDegreesService.saveData(dto, files, employeeId, id);
    }

    @DeleteMapping(value = "/v1/education-degrees/personal/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.DELETE, resource = Constant.RESOURCES.PERSONAL_EDUCATION_DEGREES)
    public BaseResponseEntity<Long> deleteData(@PathVariable Long id) throws BaseAppException {
        Long employeeId = employeesService.getEmployeeId(Utils.getUserEmpCode());
        return educationDegreesService.deleteData(employeeId, id);
    }

    @GetMapping(value = "/v1/education-degrees/personal/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW, resource = Constant.RESOURCES.PERSONAL_EDUCATION_DEGREES)
    public BaseResponseEntity<EducationDegreesResponse.DetailBean> getDataById(@PathVariable Long id) throws BaseAppException {
        Long employeeId = employeesService.getEmployeeId(Utils.getUserEmpCode());
        return educationDegreesService.getDataById(employeeId, id);
    }

    @GetMapping(value = "/v1/education-degrees/pageable/personal", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW, resource = Constant.RESOURCES.PERSONAL_EDUCATION_DEGREES)
    public TableResponseEntity<EducationDegreesResponse.DetailBean> getTableList(BaseSearchRequest request) throws RecordNotExistsException {
        Long employeeId = employeesService.getEmployeeId(Utils.getUserEmpCode());
        return ResponseUtils.ok(educationDegreesService.getTableList(employeeId, request));
    }

    @GetMapping(value = "/v1/education-degrees/{type}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ListResponseEntity<EducationDegreesResponse.DetailBean> searchDataByType(
            @PathVariable("type") String type) {
        return ResponseUtils.ok(educationDegreesService.searchDataByType(type));
    }

}
