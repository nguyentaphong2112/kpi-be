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
import vn.hbtplus.annotations.Resource;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.constants.Constant;
import vn.hbtplus.constants.Scope;
import vn.hbtplus.exceptions.BaseAppException;
import vn.hbtplus.exceptions.RecordNotExistsException;
import vn.hbtplus.models.request.DutySchedulesRequest;
import vn.hbtplus.models.response.BaseResponseEntity;
import vn.hbtplus.models.response.DutySchedulesResponse;
import vn.hbtplus.models.response.ListResponseEntity;
import vn.hbtplus.models.response.TableResponseEntity;
import vn.hbtplus.services.DutySchedulesService;
import vn.hbtplus.utils.ResponseUtils;

import javax.validation.Valid;

@RestController
@RequestMapping(BaseConstants.REQUEST_MAPPING_PREFIX)
@RequiredArgsConstructor
@Resource(value = Constant.RESOURCES.ABS_DUTY_SCHEDULES)
public class DutySchedulesController {
    private final DutySchedulesService dutySchedulesService;

    @GetMapping(value = "/v1/duty-schedules", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public TableResponseEntity<DutySchedulesResponse.SearchResult> searchData(DutySchedulesRequest.SearchForm dto) {
        return dutySchedulesService.searchData(dto);
    }

    @PostMapping(value = "/v1/duty-schedules", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.UPDATE)
    public ResponseEntity saveData(@Valid @RequestBody DutySchedulesRequest.SubmitForm dto) throws BaseAppException {
        return dutySchedulesService.saveData(dto);
    }

    @DeleteMapping(value = "/v1/duty-schedules/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.DELETE)
    public ResponseEntity deleteData(@PathVariable Long id) throws RecordNotExistsException {
        return dutySchedulesService.deleteData(id);
    }

    @GetMapping(value = "/v1/duty-schedules/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public BaseResponseEntity<DutySchedulesResponse.SearchResult> getDataById(@PathVariable Long id)  throws RecordNotExistsException {
        return dutySchedulesService.getDataById(id);
    }

    @GetMapping(value = "/v1/duty-schedules/list-data", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public ListResponseEntity<DutySchedulesResponse.DetailBean> getListData(DutySchedulesRequest.SearchForm dto)  throws RecordNotExistsException {
        return ResponseUtils.ok(dutySchedulesService.getListData(dto));
    }

    @GetMapping(value = "/v1/duty-schedules/copy-data", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public ListResponseEntity<DutySchedulesResponse.DetailBean> getListCopyData(DutySchedulesRequest.SearchForm dto)  throws RecordNotExistsException {
        return ResponseUtils.ok(dutySchedulesService.getListCopyData(dto));
    }

    @GetMapping(value = "/v1/duty-schedules/export", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public ResponseEntity<Object> exportData(DutySchedulesRequest.ReportForm dto) throws Exception {
        return dutySchedulesService.exportData(dto);
    }

    @GetMapping(value = "/v1/duty-schedules/export-total", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public ResponseEntity<Object> exportDataTotal(DutySchedulesRequest.ReportForm dto) throws Exception {
        return dutySchedulesService.exportDataTotal(dto);
    }


    @GetMapping(value = "/v1/duty-schedules/list-week", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public ListResponseEntity<DutySchedulesResponse.ListBoxBean> getListWeek() throws Exception {
        return  ResponseUtils.ok(dutySchedulesService.getWeeks());
    }

}
