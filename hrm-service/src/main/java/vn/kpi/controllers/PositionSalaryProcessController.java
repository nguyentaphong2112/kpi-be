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
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import vn.kpi.annotations.HasPermission;
import vn.kpi.annotations.Resource;
import vn.kpi.constants.BaseConstants;
import vn.kpi.constants.Constant;
import vn.kpi.constants.Scope;
import vn.kpi.exceptions.BaseAppException;
import vn.kpi.exceptions.RecordNotExistsException;
import vn.kpi.models.BaseSearchRequest;
import vn.kpi.models.request.EmployeesRequest;
import vn.kpi.models.request.PositionSalaryProcessRequest;
import vn.kpi.models.response.BaseResponseEntity;
import vn.kpi.models.response.PositionSalaryProcessResponse;
import vn.kpi.models.response.TableResponseEntity;
import vn.kpi.services.EmployeesService;
import vn.kpi.services.PositionSalaryProcessService;
import vn.kpi.utils.ResponseUtils;
import vn.kpi.utils.Utils;

import java.util.List;

@RestController
@RequestMapping(BaseConstants.REQUEST_MAPPING_PREFIX)
@RequiredArgsConstructor
@Resource(value = Constant.RESOURCES.POSITION_SALARY_PROCESS)
public class PositionSalaryProcessController {
    private final PositionSalaryProcessService positionSalaryProcessService;
    private final EmployeesService employeesService;

    @GetMapping(value = "/v1/position-salary-process", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public TableResponseEntity<PositionSalaryProcessResponse.SearchResult> searchData(EmployeesRequest.SearchForm dto) {
        return positionSalaryProcessService.searchData(dto);
    }

    @GetMapping(value = "/v1/position-salary-process/export", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public ResponseEntity<Object> exportData(EmployeesRequest.SearchForm dto) throws Exception {
        return positionSalaryProcessService.exportData(dto);
    }

    @PostMapping(value = "/v1/position-salary-process/{employeeId}", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.CREATE, domainId = "employeeId")
    public ResponseEntity saveData(@RequestPart(value = "data") PositionSalaryProcessRequest.SubmitForm dto, @PathVariable Long employeeId,
                                     @RequestPart(value = "files", required = false) List<MultipartFile> files
    ) throws BaseAppException {
        return positionSalaryProcessService.saveData(dto, files, employeeId, null);
    }

    @PutMapping(value = "/v1/position-salary-process/{employeeId}/{id}", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.UPDATE, domainId = "employeeId")
    public ResponseEntity updateData(@PathVariable Long employeeId, @RequestPart(value = "data") PositionSalaryProcessRequest.SubmitForm dto,
                                     @RequestPart(value = "files", required = false) List<MultipartFile> files,
                                     @PathVariable Long id) throws BaseAppException {
        return positionSalaryProcessService.saveData(dto, files, employeeId, id);
    }

    @PostMapping(value = "/v2/position-salary-process/{employeeId}", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.CREATE, domainId = "employeeId")
    public ResponseEntity saveData(@RequestPart(value = "data") PositionSalaryProcessRequest.SubmitFormV2 dto, @PathVariable Long employeeId,
                                   @RequestPart(value = "files", required = false) List<MultipartFile> files
    ) throws BaseAppException {
        return positionSalaryProcessService.saveData(dto, files, employeeId, null);
    }

    @PutMapping(value = "/v2/position-salary-process/{employeeId}/{id}", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.UPDATE, domainId = "employeeId")
    public ResponseEntity updateData(@PathVariable Long employeeId, @RequestPart(value = "data") PositionSalaryProcessRequest.SubmitFormV2 dto,
                                     @RequestPart(value = "files", required = false) List<MultipartFile> files,
                                     @PathVariable Long id) throws BaseAppException {
        return positionSalaryProcessService.saveData(dto, files, employeeId, id);
    }


    @DeleteMapping(value = "/v1/position-salary-process/{employeeId}/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.DELETE, domainId = "employeeId")
    public ResponseEntity deleteData(@PathVariable Long id, @PathVariable Long employeeId) throws BaseAppException {
        return positionSalaryProcessService.deleteData(employeeId, id);
    }

    @DeleteMapping(value = "/v2/position-salary-process/{employeeId}/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.DELETE, domainId = "employeeId")
    public ResponseEntity deleteDataV2(@PathVariable Long id, @PathVariable Long employeeId) throws BaseAppException {
        return positionSalaryProcessService.deleteDataV2(employeeId, id);
    }

    @GetMapping(value = "/v1/position-salary-process/{employeeId}/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW, domainId = "employeeId")
    public BaseResponseEntity<PositionSalaryProcessResponse.DetailBean> getDataById(@PathVariable Long id, @PathVariable Long employeeId) throws BaseAppException {
        return positionSalaryProcessService.getDataById(employeeId, id);
    }

    @GetMapping(value = "/v2/position-salary-process/{employeeId}/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW, domainId = "employeeId")
    public BaseResponseEntity<PositionSalaryProcessResponse.DetailBeanV2> getDataByIdV2(@PathVariable Long id, @PathVariable Long employeeId) throws BaseAppException {
        return positionSalaryProcessService.getDataByIdV2(employeeId, id);
    }

    @GetMapping(value = "/v1/position-salary-process/pageable/{employeeId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW, domainId = "employeeId")
    public TableResponseEntity<PositionSalaryProcessResponse.SearchResult> getTableList(@PathVariable Long employeeId, BaseSearchRequest request) throws RecordNotExistsException {
        return ResponseUtils.ok(positionSalaryProcessService.getTableList(employeeId, request));
    }

    @PostMapping(value = "/v1/position-salary-process/personal", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.CREATE, resource = Constant.RESOURCES.PERSONAL_POSITION_SALARY_PROCESS)
    public ResponseEntity saveData(@RequestPart(value = "data") PositionSalaryProcessRequest.SubmitForm dto,
                                     @RequestPart(value = "files", required = false) List<MultipartFile> files
    ) throws BaseAppException {
        Long employeeId = employeesService.getEmployeeId(Utils.getUserEmpCode());
        return positionSalaryProcessService.saveData(dto, files, employeeId, null);
    }

    @PutMapping(value = "/v1/position-salary-process/personal/{id}", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.UPDATE, resource = Constant.RESOURCES.PERSONAL_POSITION_SALARY_PROCESS)
    public ResponseEntity updateData(@RequestPart(value = "data") PositionSalaryProcessRequest.SubmitForm dto, @PathVariable Long id,
                                     @RequestPart(value = "files", required = false) List<MultipartFile> files
    ) throws BaseAppException {
        Long employeeId = employeesService.getEmployeeId(Utils.getUserEmpCode());
        return positionSalaryProcessService.saveData(dto, files, employeeId, id);
    }

    @PostMapping(value = "/v2/position-salary-process/personal", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.CREATE, resource = Constant.RESOURCES.PERSONAL_POSITION_SALARY_PROCESS)
    public ResponseEntity saveData(@RequestPart(value = "data") PositionSalaryProcessRequest.SubmitFormV2 dto,
                                   @RequestPart(value = "files", required = false) List<MultipartFile> files
    ) throws BaseAppException {
        Long employeeId = employeesService.getEmployeeId(Utils.getUserEmpCode());
        return positionSalaryProcessService.saveData(dto, files, employeeId, null);
    }

    @PutMapping(value = "/v2/position-salary-process/personal/{id}", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.UPDATE, resource = Constant.RESOURCES.PERSONAL_POSITION_SALARY_PROCESS)
    public ResponseEntity updateData(@RequestPart(value = "data") PositionSalaryProcessRequest.SubmitFormV2 dto, @PathVariable Long id,
                                     @RequestPart(value = "files", required = false) List<MultipartFile> files
    ) throws BaseAppException {
        Long employeeId = employeesService.getEmployeeId(Utils.getUserEmpCode());
        return positionSalaryProcessService.saveData(dto, files, employeeId, id);
    }

    @DeleteMapping(value = "/v1/position-salary-process/personal/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.DELETE, resource = Constant.RESOURCES.PERSONAL_POSITION_SALARY_PROCESS)
    public ResponseEntity deleteData(@PathVariable Long id) throws BaseAppException {
        Long employeeId = employeesService.getEmployeeId(Utils.getUserEmpCode());
        return positionSalaryProcessService.deleteData(employeeId, id);
    }

    @GetMapping(value = "/v1/position-salary-process/personal/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW, resource = Constant.RESOURCES.PERSONAL_POSITION_SALARY_PROCESS)
    public BaseResponseEntity<PositionSalaryProcessResponse.DetailBean> getDataById(@PathVariable Long id) throws BaseAppException {
        Long employeeId = employeesService.getEmployeeId(Utils.getUserEmpCode());
        return positionSalaryProcessService.getDataById(employeeId, id);
    }

    @DeleteMapping(value = "/v2/position-salary-process/personal/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.DELETE, resource = Constant.RESOURCES.PERSONAL_POSITION_SALARY_PROCESS)
    public ResponseEntity deleteDataV2(@PathVariable Long id) throws BaseAppException {
        Long employeeId = employeesService.getEmployeeId(Utils.getUserEmpCode());
        return positionSalaryProcessService.deleteDataV2(employeeId, id);
    }

    @GetMapping(value = "/v2/position-salary-process/personal/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW, resource = Constant.RESOURCES.PERSONAL_POSITION_SALARY_PROCESS)
    public BaseResponseEntity<PositionSalaryProcessResponse.DetailBeanV2> getDataByIdV2(@PathVariable Long id) throws BaseAppException {
        Long employeeId = employeesService.getEmployeeId(Utils.getUserEmpCode());
        return positionSalaryProcessService.getDataByIdV2(employeeId, id);
    }

    @GetMapping(value = "/v1/position-salary-process/pageable/personal", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW, resource = Constant.RESOURCES.PERSONAL_POSITION_SALARY_PROCESS)
    public TableResponseEntity<PositionSalaryProcessResponse.SearchResult> getTableList(BaseSearchRequest request) throws RecordNotExistsException {
        Long employeeId = employeesService.getEmployeeId(Utils.getUserEmpCode());
        return ResponseUtils.ok(positionSalaryProcessService.getTableList(employeeId, request));
    }
}
