/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import vn.hbtplus.annotations.HasPermission;
import vn.hbtplus.annotations.Resource;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.constants.Constant;
import vn.hbtplus.constants.Scope;
import vn.hbtplus.exceptions.BaseAppException;
import vn.hbtplus.exceptions.RecordNotExistsException;
import vn.hbtplus.models.BaseSearchRequest;
import vn.hbtplus.models.request.EducationCertificatesRequest;
import vn.hbtplus.models.request.EmployeesRequest;
import vn.hbtplus.models.response.BaseResponseEntity;
import vn.hbtplus.models.response.EducationCertificatesResponse;
import vn.hbtplus.models.response.TableResponseEntity;
import vn.hbtplus.services.EducationCertificatesService;
import vn.hbtplus.services.EmployeesService;
import vn.hbtplus.utils.ResponseUtils;
import vn.hbtplus.utils.Utils;

import java.util.List;

@RestController
@RequestMapping(BaseConstants.REQUEST_MAPPING_PREFIX)
@RequiredArgsConstructor
@Resource(value = Constant.RESOURCES.EDUCATION_CERTIFICATES)
public class EducationCertificatesController {
    private final EducationCertificatesService educationCertificatesService;
    private final EmployeesService employeesService;

    @GetMapping(value = "/v1/education-certificates", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public TableResponseEntity<EducationCertificatesResponse.SearchResult> searchData(EmployeesRequest.SearchForm dto) {
        return educationCertificatesService.searchData(dto);
    }

    @GetMapping(value = "/v1/education-certificates/export", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public ResponseEntity<Object> exportData(EmployeesRequest.SearchForm dto) throws Exception {
        return educationCertificatesService.exportData(dto);
    }

    @GetMapping(value = "/v1/education-certificates/download-template", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public ResponseEntity<Object> exportTemplate(EducationCertificatesRequest.SubmitForm dto) throws Exception {
       return educationCertificatesService.downloadTemplate(dto);

    }

    @PostMapping(value = "/v1/education-certificates/import", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.VIEW)
    public ResponseEntity<Object> importProcess(@RequestPart(value = "file") MultipartFile file,EducationCertificatesRequest.SubmitForm dto) throws Exception {
        return educationCertificatesService.importProcess(file,dto);
    }

    @PostMapping(value = "/v1/education-certificates/{employeeId}", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.CREATE, domainId = "employeeId")
    public BaseResponseEntity<Long> saveData(@RequestPart(value = "data") EducationCertificatesRequest.SubmitForm dto,
                                             @PathVariable Long employeeId,
                                             @RequestPart(value = "files", required = false) List<MultipartFile> files) throws BaseAppException {
        return educationCertificatesService.saveData(dto, files, employeeId, null);
    }

    @PutMapping(value = "/v1/education-certificates/{employeeId}/{id}", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.UPDATE, domainId = "employeeId")
    public BaseResponseEntity<Long> updateData(@RequestPart(value = "data") EducationCertificatesRequest.SubmitForm dto,
                                               @RequestPart(value = "files", required = false) List<MultipartFile> files,
                                               @PathVariable Long employeeId,
                                               @PathVariable Long id) throws BaseAppException {
        return educationCertificatesService.saveData(dto, files, employeeId, id);
    }

    @DeleteMapping(value = "/v1/education-certificates/{employeeId}/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.DELETE, domainId = "employeeId")
    public BaseResponseEntity<Long> deleteData(@PathVariable Long employeeId, @PathVariable Long id) throws BaseAppException {
        return educationCertificatesService.deleteData(employeeId, id);
    }

    @GetMapping(value = "/v1/education-certificates/{employeeId}/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW, domainId = "employeeId")
    public BaseResponseEntity<EducationCertificatesResponse.DetailBean> getDataById(@PathVariable Long employeeId, @PathVariable Long id) throws BaseAppException {
        return educationCertificatesService.getDataById(employeeId, id);
    }

    @GetMapping(value = "/v1/education-certificates/pageable/{employeeId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW, domainId = "employeeId")
    public TableResponseEntity<EducationCertificatesResponse.DetailBean> getTableList(@PathVariable Long employeeId, BaseSearchRequest request) throws RecordNotExistsException {
        return ResponseUtils.ok(educationCertificatesService.getTableList(employeeId, request));
    }

    @PostMapping(value = "/v1/education-certificates/personal", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.CREATE, resource = Constant.RESOURCES.PERSONAL_EDUCATION_CERTIFICATES)
    public BaseResponseEntity<Long> saveData(@RequestPart(value = "data") EducationCertificatesRequest.SubmitForm dto,
                                             @RequestPart(value = "files", required = false) List<MultipartFile> files) throws BaseAppException {
        Long employeeId = employeesService.getEmployeeId(Utils.getUserEmpCode());
        return educationCertificatesService.saveData(dto, files, employeeId, null);
    }

    @PutMapping(value = "/v1/education-certificates/personal/{id}", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.UPDATE, resource = Constant.RESOURCES.PERSONAL_EDUCATION_CERTIFICATES)
    public BaseResponseEntity<Long> updateData(@RequestPart(value = "data") EducationCertificatesRequest.SubmitForm dto,
                                               @RequestPart(value = "files", required = false) List<MultipartFile> files,
                                               @PathVariable Long id) throws BaseAppException {
        Long employeeId = employeesService.getEmployeeId(Utils.getUserEmpCode());
        return educationCertificatesService.saveData(dto, files, employeeId, id);
    }

    @DeleteMapping(value = "/v1/education-certificates/personal/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.DELETE, resource = Constant.RESOURCES.PERSONAL_EDUCATION_CERTIFICATES)
    public BaseResponseEntity<Long> deleteData(@PathVariable Long id) throws BaseAppException {
        Long employeeId = employeesService.getEmployeeId(Utils.getUserEmpCode());
        return educationCertificatesService.deleteData(employeeId, id);
    }

    @GetMapping(value = "/v1/education-certificates/personal/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW, resource = Constant.RESOURCES.PERSONAL_EDUCATION_CERTIFICATES)
    public BaseResponseEntity<EducationCertificatesResponse.DetailBean> getDataById(@PathVariable Long id) throws BaseAppException {
        Long employeeId = employeesService.getEmployeeId(Utils.getUserEmpCode());
        return educationCertificatesService.getDataById(employeeId, id);
    }

    @GetMapping(value = "/v1/education-certificates/pageable/personal", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW, resource = Constant.RESOURCES.PERSONAL_EDUCATION_CERTIFICATES)
    public TableResponseEntity<EducationCertificatesResponse.DetailBean> getTableList(BaseSearchRequest request) throws RecordNotExistsException {
        Long employeeId = employeesService.getEmployeeId(Utils.getUserEmpCode());
        return ResponseUtils.ok(educationCertificatesService.getTableList(employeeId, request));
    }


}
