/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import vn.hbtplus.annotations.HasPermission;
import vn.hbtplus.models.dto.WorkCalendarsDTO;
import vn.hbtplus.models.response.*;
import vn.hbtplus.models.request.WorkCalendarsRequest;
import vn.hbtplus.services.WorkCalendarsService;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.constants.Scope;
import vn.hbtplus.constants.Constant;
import vn.hbtplus.annotations.Resource;
import vn.hbtplus.exceptions.RecordNotExistsException;
import vn.hbtplus.exceptions.BaseAppException;
import vn.hbtplus.utils.ResponseUtils;
import vn.hbtplus.utils.Utils;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping(BaseConstants.REQUEST_MAPPING_PREFIX)
@RequiredArgsConstructor
@Resource(value = Constant.RESOURCES.ABS_CALENDAR)
public class WorkCalendarsController {
    private final WorkCalendarsService workCalendarsService;

    @GetMapping(value = "/v1/work-calendars", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public TableResponseEntity<WorkCalendarsResponse> searchData(WorkCalendarsRequest.SearchForm dto) {
        return workCalendarsService.searchData(dto);
    }

    @PostMapping(value = "/v1/work-calendars", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.UPDATE)
    public BaseResponseEntity<Long> saveData(@RequestBody @Valid  WorkCalendarsRequest.SubmitForm dto) throws BaseAppException {
        return workCalendarsService.saveData(dto, null);
    }

    @PutMapping(value = "/v1/work-calendars/{id}", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.UPDATE)
    public BaseResponseEntity<Long> saveData(@RequestBody @Valid  WorkCalendarsRequest.SubmitForm dto,
                                             @PathVariable Long id) throws BaseAppException {
        return workCalendarsService.saveData(dto, id);
    }

    @DeleteMapping(value = "/v1/work-calendars/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.DELETE)
    public ResponseEntity deleteData(@PathVariable Long id) throws RecordNotExistsException {
        return workCalendarsService.deleteData(id);
    }

    @GetMapping(value = "/v1/work-calendars/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public BaseResponseEntity<WorkCalendarsResponse> getDataById(@PathVariable Long id)  throws RecordNotExistsException {
        return workCalendarsService.getDataById(id);
    }

    @GetMapping(value = "/v1/work-calendars/export", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public ResponseEntity<Object> exportData(WorkCalendarsRequest.SearchForm dto) throws Exception {
        return workCalendarsService.exportData(dto);
    }

    @GetMapping(value = "/v1/work-calendars/get-all-active", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public ListResponseEntity<WorkCalendarsDTO> getActiveWorkCalendars() {
        List<WorkCalendarsDTO> resultObj = workCalendarsService.getActiveWorkCalendars();
        return ResponseUtils.ok(resultObj);
    }

}
