/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.hbtplus.annotations.HasPermission;
import vn.hbtplus.models.response.*;
import vn.hbtplus.models.request.AttendanceHistoriesRequest;
import vn.hbtplus.services.AttendanceHistoriesService;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.constants.Scope;
import vn.hbtplus.constants.Constant;
import vn.hbtplus.annotations.Resource;
import vn.hbtplus.exceptions.RecordNotExistsException;
import vn.hbtplus.exceptions.BaseAppException;
import vn.hbtplus.services.EmployeesService;
import vn.hbtplus.utils.Utils;

import javax.validation.Valid;

@RestController
@RequestMapping(BaseConstants.REQUEST_MAPPING_PREFIX)
@RequiredArgsConstructor
@Resource(value = Constant.RESOURCES.ABS_ATTENDANCE_HISTORIES)
public class AttendanceHistoriesController {
    private final AttendanceHistoriesService attendanceHistoriesService;
    private final EmployeesService employeesService;

    @GetMapping(value = "/v1/attendance-histories", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public TableResponseEntity<AttendanceHistoriesResponse> searchData(AttendanceHistoriesRequest.SearchForm dto) {
        dto.setEmployeeId(employeesService.getEmployeeId(Utils.getUserEmpCode()));
        return attendanceHistoriesService.searchData(dto);
    }

    @GetMapping(value = "/v1/attendance-histories/current-user", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public TableResponseEntity<AttendanceHistoriesResponse> searchDataByCurrentUser(AttendanceHistoriesRequest.SearchForm dto) {
        return attendanceHistoriesService.searchDataByCurrentUser(dto);
    }

    @PostMapping(value = "/v1/attendance-histories", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.UPDATE)
    public ResponseEntity saveData(@Valid @RequestBody AttendanceHistoriesRequest.SubmitForm dto) throws BaseAppException {
        return attendanceHistoriesService.saveData(dto,null);
    }

    @PutMapping(value = "/v1/attendance-histories/{id}", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.UPDATE)
    public ResponseEntity saveData(@Valid @RequestBody AttendanceHistoriesRequest.SubmitForm dto,@PathVariable Long id) throws BaseAppException {
        return attendanceHistoriesService.saveData(dto,id);
    }

    @DeleteMapping(value = "/v1/attendance-histories/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.DELETE)
    public ResponseEntity deleteData(@PathVariable Long id) throws RecordNotExistsException {
        return attendanceHistoriesService.deleteData(id);
    }

    @GetMapping(value = "/v1/attendance-histories/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public BaseResponseEntity<AttendanceHistoriesResponse> getDataById(@PathVariable Long id)  throws RecordNotExistsException {
        return attendanceHistoriesService.getDataById(id);
    }

    @GetMapping(value = "/v1/attendance-histories/export", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public ResponseEntity<Object> exportData(AttendanceHistoriesRequest.SearchForm dto) throws Exception {
        return attendanceHistoriesService.exportData(dto);
    }

    @GetMapping(value = "/v1/attendance-histories/get-log", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public TableResponseEntity<AttendanceHistoriesResponse.AttendanceLogResponse> getLogData(AttendanceHistoriesRequest.SearchForm dto) {
        return attendanceHistoriesService.getLogData(dto);
    }

    @PostMapping(value = "/v1/attendance-histories/status/{id}", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.APPROVE)
    public ResponseEntity updateStatusById(@Valid @RequestBody AttendanceHistoriesRequest.SubmitForm dto, @PathVariable Long id) throws BaseAppException {
        return attendanceHistoriesService.updateStatusById(dto, id);
    }

}
