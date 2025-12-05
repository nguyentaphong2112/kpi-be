/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import vn.hbtplus.annotations.HasPermission;
import vn.hbtplus.annotations.Resource;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.constants.Constant;
import vn.hbtplus.constants.Scope;
import vn.hbtplus.exceptions.BaseAppException;
import vn.hbtplus.exceptions.RecordNotExistsException;
import vn.hbtplus.models.request.EmployeesRequest;
import vn.hbtplus.models.response.BaseResponseEntity;
import vn.hbtplus.models.response.EmployeesResponse;
import vn.hbtplus.models.response.ListResponseEntity;
import vn.hbtplus.models.response.TableResponseEntity;
import vn.hbtplus.services.EmployeesService;
import vn.hbtplus.utils.ResponseUtils;

import javax.validation.Valid;

@RestController
@RequestMapping(BaseConstants.REQUEST_MAPPING_PREFIX)
@RequiredArgsConstructor
@Resource(value = Constant.RESOURCES.EMPLOYEE)
public class EmployeesController {
    private final EmployeesService employeesService;

    @GetMapping(value = "/v1/employees", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public TableResponseEntity<EmployeesResponse.SearchResult> searchData(EmployeesRequest.SearchForm dto) {
        return employeesService.searchData(dto);
    }

    @PostMapping(value = "/v1/employees", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.UPDATE)
    public ResponseEntity saveData(@Valid @ModelAttribute EmployeesRequest.SubmitForm dto) throws BaseAppException {
        return employeesService.saveData(dto, null);
    }

    @PutMapping(value = "/v1/employees/{id}", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.UPDATE)
    public ResponseEntity updateData(@Valid @ModelAttribute EmployeesRequest.SubmitForm dto, @PathVariable Long id) throws BaseAppException {
        return employeesService.saveData(dto, id);
    }

    @DeleteMapping(value = "/v1/employees/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.DELETE)
    public ResponseEntity deleteData(@PathVariable Long id) throws RecordNotExistsException {
        return employeesService.deleteData(id);
    }

    @GetMapping(value = "/v1/employees/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public BaseResponseEntity<EmployeesResponse.DetailBean> getDataById(@PathVariable Long id)  throws RecordNotExistsException {
        return employeesService.getDataById(id);
    }

    @GetMapping(value = "/v1/employees/export", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public ResponseEntity<Object> exportData(EmployeesRequest.SearchForm dto) throws Exception {
        return employeesService.exportData(dto);
    }

    @GetMapping(value = "/v1/employees/list", produces = MediaType.APPLICATION_JSON_VALUE)
//    @HasPermission(scope = Scope.VIEW)
    public ListResponseEntity<EmployeesResponse.SearchResult> getListEmployee(@RequestParam String keySearch) {
        return ResponseUtils.ok(employeesService.getListEmployee(keySearch));
    }

}
