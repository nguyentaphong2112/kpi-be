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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import vn.hbtplus.annotations.HasPermission;
import vn.hbtplus.annotations.Resource;
import vn.hbtplus.annotations.UserLogActivity;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.constants.Constant;
import vn.hbtplus.constants.Scope;
import vn.hbtplus.exceptions.BaseAppException;
import vn.hbtplus.exceptions.RecordNotExistsException;
import vn.hbtplus.models.dto.EmployeeInfoDto;
import vn.hbtplus.models.request.EmployeesRequest;
import vn.hbtplus.models.response.BaseResponseEntity;
import vn.hbtplus.models.response.EmployeesResponse;
import vn.hbtplus.models.response.TableResponseEntity;
import vn.hbtplus.services.EmployeesService;
import vn.hbtplus.services.ImportEmployeeService;
import vn.hbtplus.utils.ResponseUtils;
import vn.hbtplus.utils.Utils;

import javax.validation.Valid;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping(BaseConstants.REQUEST_MAPPING_PREFIX)
@RequiredArgsConstructor
@Resource(value = Constant.RESOURCES.EMPLOYEE)
public class EmployeesController {
    private final EmployeesService employeesService;
    private final ImportEmployeeService importEmployeeService;

    @GetMapping(value = "/v1/employees/basic-information", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    @UserLogActivity
    public TableResponseEntity<EmployeesResponse.SearchResult> searchBasicInfoEmployee(EmployeesRequest.SearchForm dto) {
        return ResponseUtils.ok(employeesService.searchBasicInfoEmployee(dto));
    }

    @GetMapping(value = "/v1/employees/export", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public ResponseEntity<Object> exportData(EmployeesRequest.SearchForm dto) throws Exception {
        return employeesService.exportData(dto);
    }

    @GetMapping(value = "/v1/employees/data-picker", produces = MediaType.APPLICATION_JSON_VALUE)
    public TableResponseEntity<EmployeesResponse.SearchResult> getEmpDataPicker(EmployeesRequest.SearchForm dto) {
        return ResponseUtils.ok(employeesService.getEmpDataPicker(dto));
    }

    @PostMapping(value = "/v1/employees", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.CREATE)
    public ResponseEntity saveData(@Valid EmployeesRequest.SubmitForm dto) throws BaseAppException, InstantiationException, IllegalAccessException {
        return employeesService.saveData(dto);
    }

    @DeleteMapping(value = "/v1/employees/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.DELETE, domainId = "id")
    public ResponseEntity deleteData(@PathVariable Long id) throws RecordNotExistsException {
        return employeesService.deleteData(id);
    }

    @GetMapping(value = "/v1/employees/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW, domainId = "id")
    public BaseResponseEntity<EmployeesResponse.PersonalInfo> getDataById(@PathVariable Long id) throws RecordNotExistsException, ExecutionException, InterruptedException {
        return ResponseUtils.ok(employeesService.getDataById(id));
    }

    @GetMapping(value = "/v1/employees/personal", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW, resource = Constant.RESOURCES.PERSONAL_INFO)
    public BaseResponseEntity<EmployeesResponse.PersonalInfo> getDataById() throws RecordNotExistsException, ExecutionException, InterruptedException {
        Long employeeId = employeesService.getEmployeeId(Utils.getUserEmpCode());
        return ResponseUtils.ok(employeesService.getDataById(employeeId));
    }

    @GetMapping(value = "/v1/employees/basic-information/{employeeId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW, domainId = "employeeId")
    public BaseResponseEntity<EmployeeInfoDto> getBasicInfo(@PathVariable Long employeeId) throws RecordNotExistsException {
        return employeesService.getBasicInfo(employeeId);
    }

    @PutMapping(value = "/v1/employees/basic-information/{employeeId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.UPDATE, domainId = "employeeId")
    public BaseResponseEntity<Long> saveBasicInfo(@RequestBody() @Valid EmployeesRequest.PersonalInfoSubmitForm dto, @PathVariable Long employeeId) throws BaseAppException {
        return employeesService.saveBasicInfo(dto, employeeId);
    }

    @GetMapping(value = "/v1/employees/basic-information/personal", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW, resource = Constant.RESOURCES.PERSONAL_INFO)
    public BaseResponseEntity<EmployeeInfoDto> getBasicInfo() throws RecordNotExistsException {
        Long employeeId = employeesService.getEmployeeId(Utils.getUserEmpCode());
        if (employeeId != null) {
            return employeesService.getBasicInfo(employeeId);
        } else {
            return null;
        }
    }

    @PutMapping(value = "/v1/employees/basic-information/personal", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.UPDATE, resource = Constant.RESOURCES.PERSONAL_INFO)
    public BaseResponseEntity<Long> saveBasicInfo(@RequestBody() @Valid EmployeesRequest.PersonalInfoSubmitForm dto) throws BaseAppException {
        Long employeeId = employeesService.getEmployeeId(Utils.getUserEmpCode());
        return employeesService.saveBasicInfo(dto, employeeId);
    }


    @GetMapping(value = "/v1/employees/avatar/{employeeId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public BaseResponseEntity<Object> getAvatar(@PathVariable Long employeeId) throws IOException {
        return employeesService.getAvatar(employeeId);
    }

    @PostMapping(value = "/v1/employees/avatar/{employeeId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.UPDATE, domainId = "employeeId")
    public ResponseEntity<Object> uploadAvatar(
            @PathVariable Long employeeId,
            @RequestBody MultipartFile fileAvatar) throws IOException {
        return employeesService.uploadAvatar(employeeId, fileAvatar);
    }

    @DeleteMapping(value = "/v1/employees/avatar/{employeeId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.UPDATE, domainId = "employeeId")
    public ResponseEntity<Object> deleteAvatar(@PathVariable Long employeeId) {
        return employeesService.deleteAvatar(employeeId);
    }

    @GetMapping(value = "/v1/employees/avatar/personal", produces = MediaType.APPLICATION_JSON_VALUE)
    public BaseResponseEntity<Object> getAvatar() throws IOException {
        Long employeeId = employeesService.getEmployeeId(Utils.getUserEmpCode());
        return employeesService.getAvatar(employeeId);
    }

    @PostMapping(value = "/v1/employees/avatar/personal", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW, resource = Constant.RESOURCES.PERSONAL_INFO)
    public ResponseEntity<Object> uploadAvatar(
            @RequestBody MultipartFile fileAvatar) throws IOException {
        Long employeeId = employeesService.getEmployeeId(Utils.getUserEmpCode());
        return employeesService.uploadAvatar(employeeId, fileAvatar);
    }

    @DeleteMapping(value = "/v1/employees/avatar/personal", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW, resource = Constant.RESOURCES.PERSONAL_INFO)
    public ResponseEntity<Object> deleteAvatar() {
        Long employeeId = employeesService.getEmployeeId(Utils.getUserEmpCode());
        return employeesService.deleteAvatar(employeeId);
    }

    @PutMapping(value = "/v1/employees/political-info/{employeeId}", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.UPDATE, resource = Constant.RESOURCES.POLITICAL_INFO, domainId = "employeeId")
    public ResponseEntity savePoliticalInfo(@Valid EmployeesRequest.PoliticalInfo dto, @PathVariable Long employeeId) throws BaseAppException {
        return ResponseUtils.ok(employeesService.updatePoliticalInfo(dto, employeeId));
    }

    @GetMapping(value = "/v1/employees/political-info/{employeeId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    @HasPermission(scope = Scope.UPDATE, resource = Constant.RESOURCES.POLITICAL_INFO, domainId = "employeeId")
    public BaseResponseEntity<EmployeesResponse.PoliticalInfo> getPoliticalInfo(@PathVariable Long employeeId) throws BaseAppException {
        return ResponseUtils.ok(employeesService.getPoliticalInfo(employeeId));
    }

    @PutMapping(value = "/v1/employees/political-info/personal", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.UPDATE, resource = Constant.RESOURCES.PERSONAL_POLITICAL_INFO)
    public ResponseEntity savePoliticalInfo(@Valid EmployeesRequest.PoliticalInfo dto) throws BaseAppException {
        Long employeeId = employeesService.getEmployeeId(Utils.getUserEmpCode());
        return ResponseUtils.ok(employeesService.updatePoliticalInfo(dto, employeeId));
    }

    @GetMapping(value = "/v1/employees/political-info/personal", produces = {MediaType.APPLICATION_JSON_VALUE})
    @HasPermission(scope = Scope.UPDATE, resource = Constant.RESOURCES.PERSONAL_POLITICAL_INFO)
    public BaseResponseEntity<EmployeesResponse.PoliticalInfo> getPoliticalInfo() throws BaseAppException {
        Long employeeId = employeesService.getEmployeeId(Utils.getUserEmpCode());
        return ResponseUtils.ok(employeesService.getPoliticalInfo(employeeId));
    }

    @GetMapping(value = "/v1/employees/export/{employeeId}")
    @HasPermission(scope = Scope.VIEW)
    public ResponseEntity exportCV(@PathVariable Long employeeId) throws Exception {
        return ResponseUtils.ok(employeesService.exportWord(employeeId), "so_yeu_ly_lich.docx");
    }

    @GetMapping(value = "/v1/employees/export/personal")
    @HasPermission(scope = Scope.VIEW, resource = Constant.RESOURCES.PERSONAL_INFO)
    public ResponseEntity exportCV() throws Exception {
        Long employeeId = employeesService.getEmployeeId(Utils.getUserEmpCode());
        return ResponseUtils.ok(employeesService.exportWord(employeeId), "so_yeu_ly_lich.docx");
    }

    @GetMapping(value = "/v1/employees/directory", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW, resource = Constant.RESOURCES.EMPLOYEE_DIRECTORY)
    public TableResponseEntity<EmployeesResponse.SearchResult> searchEmployeeDirectory(EmployeesRequest.SearchForm dto) {
        return ResponseUtils.ok(employeesService.searchEmployeeDirectory(dto));
    }

    @PostMapping(value = "/v1/employees/import", produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @HasPermission(scope = Scope.IMPORT)
    public ResponseEntity<Object> processImport(@RequestPart MultipartFile file, @RequestParam(required = false) boolean isForceUpdate) throws Exception {
        if (isForceUpdate) {
            return importEmployeeService.processImportUpdate(file);
        } else {
            return importEmployeeService.processImport(file);
        }
    }

    @GetMapping(value = "/v1/employees/import-template", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public ResponseEntity<Object> downloadImportTemplate(
            @RequestParam(required = false) boolean isForceUpdate
    ) throws Exception {
        return importEmployeeService.downloadImportTemplate(isForceUpdate);
    }

    @GetMapping(value = "/v1/employee/org-by-level-manage", produces = MediaType.APPLICATION_JSON_VALUE)
    public BaseResponseEntity<Long> getOrgByOrgLevelManage(@RequestParam String employeeCode, @RequestParam Long orgLevelManage) throws Exception {
        return ResponseUtils.ok(employeesService.getOrgByOrgLevelManage(employeeCode, orgLevelManage));
    }
}
