/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.controllers;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.hbtplus.annotations.HasPermission;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.constants.Constant;
import vn.hbtplus.constants.Scope;
import vn.hbtplus.exceptions.BaseAppException;
import vn.hbtplus.exceptions.RecordNotExistsException;
import vn.hbtplus.models.request.TimekeepingsRequest;
import vn.hbtplus.models.response.BaseResponseEntity;
import vn.hbtplus.models.response.TableResponseEntity;
import vn.hbtplus.models.response.TimekeepingsResponse;
import vn.hbtplus.repositories.entity.WorkdayTypesEntity;
import vn.hbtplus.services.TimekeepingsService;
import vn.hbtplus.utils.ResponseUtils;

import javax.validation.Valid;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping(BaseConstants.REQUEST_MAPPING_PREFIX)
@RequiredArgsConstructor
public class TimekeepingsController {
    private final TimekeepingsService timekeepingsService;

    @GetMapping(value = "/v1/time-keeping/{type}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW, resource = {Constant.RESOURCES.ABS_TIMEKEEPING, Constant.RESOURCES.ABS_OVERTIME_TIMEKEEPING})
    public TableResponseEntity<TimekeepingsResponse.SearchResult> searchData(@Valid TimekeepingsRequest.SearchForm dto, @PathVariable String type) {
        return timekeepingsService.searchData(type, dto);
    }

    @PostMapping(value = "/v1/time-keeping/{type}", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.UPDATE, resource = {Constant.RESOURCES.ABS_TIMEKEEPING, Constant.RESOURCES.ABS_OVERTIME_TIMEKEEPING})
    public ResponseEntity saveData(@Valid @RequestBody TimekeepingsRequest.SubmitForm dto, @PathVariable String type) throws BaseAppException {
        return timekeepingsService.saveData(type, dto);
    }

    @DeleteMapping(value = "/v1/time-keeping/{type}/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.DELETE, resource = {Constant.RESOURCES.ABS_TIMEKEEPING, Constant.RESOURCES.ABS_OVERTIME_TIMEKEEPING})
    public ResponseEntity deleteData(@PathVariable Long id, @PathVariable String type) throws RecordNotExistsException {
        return timekeepingsService.deleteData(id);
    }

    @GetMapping(value = "/v1/time-keeping/{type}/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW, resource = {Constant.RESOURCES.ABS_TIMEKEEPING, Constant.RESOURCES.ABS_OVERTIME_TIMEKEEPING})
    public BaseResponseEntity<TimekeepingsResponse> getDataById(@PathVariable Long id, @PathVariable String type) throws RecordNotExistsException {
        return timekeepingsService.getDataById(id);
    }

    @GetMapping(value = "/v1/time-keeping/export/{type}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW, resource = {Constant.RESOURCES.ABS_TIMEKEEPING, Constant.RESOURCES.ABS_OVERTIME_TIMEKEEPING})
    public ResponseEntity<Object> exportData(TimekeepingsRequest.SearchForm dto, @PathVariable String type) throws Exception {
        return timekeepingsService.exportData(type, dto);
    }

    @PostMapping(value = "/v1/time-keeping/auto-set-timekeeping/{type}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW, resource = {Constant.RESOURCES.ABS_TIMEKEEPING, Constant.RESOURCES.ABS_OVERTIME_TIMEKEEPING})
    public ResponseEntity<Object> autoCalculate(@RequestBody TimekeepingsRequest.SearchForm dto, @PathVariable String type) {
        Date timekeepingDate = dto.getStartDate();
        List<Long> empIds = null;
        while (!timekeepingDate.after(dto.getEndDate())) {
            if(WorkdayTypesEntity.TYPE.LAM_THEM.equals(type)){
                timekeepingsService.autoSetOverTimekeeping(timekeepingDate, empIds);
            } else {
                timekeepingsService.autoSetTimekeeping(timekeepingDate, empIds);
            }
            timekeepingDate = DateUtils.addDays(timekeepingDate, 1);
        }
        //huy bang cong nghi viec
        return ResponseUtils.ok();
    }
}
