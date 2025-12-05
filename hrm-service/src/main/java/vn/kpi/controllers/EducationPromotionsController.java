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
import vn.kpi.annotations.Resource;
import vn.kpi.constants.BaseConstants;
import vn.kpi.constants.Constant;
import vn.kpi.constants.Scope;
import vn.kpi.exceptions.BaseAppException;
import vn.kpi.exceptions.RecordNotExistsException;
import vn.kpi.models.BaseSearchRequest;
import vn.kpi.models.request.EducationPromotionsRequest;
import vn.kpi.models.request.EmployeesRequest;
import vn.kpi.models.response.BaseResponseEntity;
import vn.kpi.models.response.EducationPromotionsResponse;
import vn.kpi.models.response.TableResponseEntity;
import vn.kpi.services.EducationPromotionsService;
import vn.kpi.services.EmployeesService;
import vn.kpi.utils.ResponseUtils;
import vn.kpi.utils.Utils;

import javax.validation.Valid;

@RestController
@RequestMapping(BaseConstants.REQUEST_MAPPING_PREFIX)
@RequiredArgsConstructor
@Resource(value = Constant.RESOURCES.EDUCATION_PROMOTIONS)
public class EducationPromotionsController {
    private final EducationPromotionsService educationPromotionsService;
    private final EmployeesService employeesService;

    @GetMapping(value = "/v1/education-promotions", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public TableResponseEntity<EducationPromotionsResponse.SearchResult> searchData(EmployeesRequest.SearchForm dto) {
        return educationPromotionsService.searchData(dto);
    }

    @GetMapping(value = "/v1/education-promotions/export", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public ResponseEntity<Object> exportData(EmployeesRequest.SearchForm dto) throws Exception {
        return educationPromotionsService.exportData(dto);
    }

    @PostMapping(value = "/v1/education-promotions/{employeeId}", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.CREATE, domainId = "employeeId")
    public ResponseEntity saveData(@RequestBody @Valid EducationPromotionsRequest.SubmitForm dto, @PathVariable Long employeeId) throws BaseAppException {
        return educationPromotionsService.saveData(dto, employeeId, null);
    }

    @PutMapping(value = "/v1/education-promotions/{employeeId}/{id}", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.UPDATE, domainId = "employeeId")
    public ResponseEntity updateData(@RequestBody @Valid EducationPromotionsRequest.SubmitForm dto,
                                     @PathVariable Long employeeId,
                                     @PathVariable Long id
    ) throws BaseAppException {
        return educationPromotionsService.saveData(dto, employeeId, id);
    }

    @DeleteMapping(value = "/v1/education-promotions/{employeeId}/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.DELETE, domainId = "employeeId")
    public ResponseEntity deleteData(@PathVariable Long id, @PathVariable Long employeeId) throws BaseAppException {
        return educationPromotionsService.deleteData(employeeId, id);
    }

    @GetMapping(value = "/v1/education-promotions/{employeeId}/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW, domainId = "employeeId")
    public BaseResponseEntity<EducationPromotionsResponse.DetailBean> getDataById(@PathVariable Long id, @PathVariable Long employeeId) throws BaseAppException {
        return educationPromotionsService.getDataById(employeeId, id);
    }

    @GetMapping(value = "/v1/education-promotions/pageable/{employeeId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW, domainId = "employeeId")
    public TableResponseEntity<EducationPromotionsResponse.DetailBean> getTableList(@PathVariable Long employeeId, BaseSearchRequest request) throws RecordNotExistsException {
        return ResponseUtils.ok(educationPromotionsService.getTableList(employeeId, request));
    }

    @PostMapping(value = "/v1/education-promotions/personal", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.CREATE, resource = Constant.RESOURCES.PERSONAL_EDUCATION_PROMOTIONS)
    public ResponseEntity saveData(@RequestBody @Valid EducationPromotionsRequest.SubmitForm dto) throws BaseAppException {
        Long employeeId = employeesService.getEmployeeId(Utils.getUserEmpCode());
        return educationPromotionsService.saveData(dto, employeeId, null);
    }

    @PutMapping(value = "/v1/education-promotions/personal/{id}", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.UPDATE, resource = Constant.RESOURCES.PERSONAL_EDUCATION_PROMOTIONS)
    public ResponseEntity updateData(@RequestBody @Valid EducationPromotionsRequest.SubmitForm dto,
                                     @PathVariable Long id
    ) throws BaseAppException {
        Long employeeId = employeesService.getEmployeeId(Utils.getUserEmpCode());
        return educationPromotionsService.saveData(dto, employeeId, id);
    }

    @DeleteMapping(value = "/v1/education-promotions/personal/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.DELETE, resource = Constant.RESOURCES.PERSONAL_EDUCATION_PROMOTIONS)
    public ResponseEntity deleteData(@PathVariable Long id) throws BaseAppException {
        Long employeeId = employeesService.getEmployeeId(Utils.getUserEmpCode());
        return educationPromotionsService.deleteData(employeeId, id);
    }

    @GetMapping(value = "/v1/education-promotions/personal/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW, resource = Constant.RESOURCES.PERSONAL_EDUCATION_PROMOTIONS)
    public BaseResponseEntity<EducationPromotionsResponse.DetailBean> getDataById(@PathVariable Long id) throws BaseAppException {
        Long employeeId = employeesService.getEmployeeId(Utils.getUserEmpCode());
        return educationPromotionsService.getDataById(employeeId, id);
    }

    @GetMapping(value = "/v1/education-promotions/pageable/personal", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW, resource = Constant.RESOURCES.PERSONAL_EDUCATION_PROMOTIONS)
    public TableResponseEntity<EducationPromotionsResponse.DetailBean> getTableList(BaseSearchRequest request) throws RecordNotExistsException {
        Long employeeId = employeesService.getEmployeeId(Utils.getUserEmpCode());
        return ResponseUtils.ok(educationPromotionsService.getTableList(employeeId, request));
    }

}
