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
import vn.hbtplus.models.request.CourseTraineesRequest;
import vn.hbtplus.services.CourseTraineesService;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.constants.Scope;
import vn.hbtplus.constants.Constant;
import vn.hbtplus.annotations.Resource;
import vn.hbtplus.exceptions.RecordNotExistsException;
import vn.hbtplus.exceptions.BaseAppException;
import javax.validation.Valid;

@RestController
@RequestMapping(BaseConstants.REQUEST_MAPPING_PREFIX)
@RequiredArgsConstructor
@Resource(value = Constant.RESOURCES.EMPLOYEE)
public class CourseTraineesController {
    private final CourseTraineesService courseTraineesService;

    @GetMapping(value = "/v1/course-trainees", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public TableResponseEntity<CourseTraineesResponse> searchData(CourseTraineesRequest.SearchForm dto) {
        return courseTraineesService.searchData(dto);
    }

    @PostMapping(value = "/v1/course-trainees", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.UPDATE)
    public ResponseEntity saveData(@Valid CourseTraineesRequest.SubmitForm dto) throws BaseAppException {
        return courseTraineesService.saveData(dto, null);
    }

    @PutMapping(value = "/v1/course-trainees/{id}", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.UPDATE)
    public ResponseEntity saveData(@Valid CourseTraineesRequest.SubmitForm dto, @PathVariable Long id) throws BaseAppException {
        return courseTraineesService.saveData(dto, id);
    }

    @DeleteMapping(value = "/v1/course-trainees/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.DELETE)
    public ResponseEntity deleteData(@PathVariable Long id) throws RecordNotExistsException {
        return courseTraineesService.deleteData(id);
    }

    @GetMapping(value = "/v1/course-trainees/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public BaseResponseEntity<CourseTraineesResponse> getDataById(@PathVariable Long id)  throws RecordNotExistsException {
        return courseTraineesService.getDataById(id);
    }

    @GetMapping(value = "/v1/course-trainees/export", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public ResponseEntity<Object> exportData(CourseTraineesRequest.SearchForm dto) throws Exception {
        return courseTraineesService.exportData(dto);
    }

}
