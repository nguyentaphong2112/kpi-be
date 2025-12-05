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
import vn.hbtplus.models.request.ReasonTypesRequest;
import vn.hbtplus.models.response.*;
import vn.hbtplus.models.request.WorkdayTypesRequest;
import vn.hbtplus.services.WorkdayTypesService;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.constants.Scope;
import vn.hbtplus.constants.Constant;
import vn.hbtplus.annotations.Resource;
import vn.hbtplus.exceptions.RecordNotExistsException;
import vn.hbtplus.exceptions.BaseAppException;
import vn.hbtplus.utils.ResponseUtils;

import javax.validation.Valid;

@RestController
@RequestMapping(BaseConstants.REQUEST_MAPPING_PREFIX)
@RequiredArgsConstructor
@Resource(value = Constant.RESOURCES.ABS_WORKDAY_TYPES)
public class WorkdayTypesController {
    private final WorkdayTypesService workdayTypesService;

    @GetMapping(value = "/v1/workday-types", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public TableResponseEntity<WorkdayTypesResponse> searchData(WorkdayTypesRequest.SearchForm dto) {
        return workdayTypesService.searchData(dto);
    }

    @PostMapping(value = "/v1/workday-types", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.UPDATE)
    public BaseResponseEntity saveData(@RequestBody @Valid  WorkdayTypesRequest.SubmitForm dto) throws BaseAppException {
        return workdayTypesService.saveData(dto , null);
    }

    @PutMapping(value = "/v1/workday-types/{id}", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.UPDATE)
    public BaseResponseEntity<Long> saveData(@RequestBody @Valid WorkdayTypesRequest.SubmitForm dto,
                                             @PathVariable Long id) throws BaseAppException {
        return workdayTypesService.saveData(dto , id);
    }

    @DeleteMapping(value = "/v1/workday-types/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.DELETE)
    public ResponseEntity deleteData(@PathVariable Long id) throws RecordNotExistsException {
        return workdayTypesService.deleteData(id);
    }

    @GetMapping(value = "/v1/workday-types/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public BaseResponseEntity<WorkdayTypesResponse> getDataById(@PathVariable Long id)  throws RecordNotExistsException {
        return workdayTypesService.getDataById(id);
    }

    @GetMapping(value = "/v1/workday-types/export", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public ResponseEntity<Object> exportData(WorkdayTypesRequest.SearchForm dto) throws Exception {
        return workdayTypesService.exportData(dto);
    }

    @GetMapping(value = "/v1/workday-types/list", produces = MediaType.APPLICATION_JSON_VALUE)
    public ListResponseEntity<WorkdayTypesResponse> getList() {
        return ResponseUtils.ok(workdayTypesService.getList());
    }
}
