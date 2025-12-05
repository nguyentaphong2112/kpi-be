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
import vn.hbtplus.models.request.CourseLessonResultsRequest;
import vn.hbtplus.services.CourseLessonResultsService;
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
public class CourseLessonResultsController {
    private final CourseLessonResultsService courseLessonResultsService;

    @GetMapping(value = "/v1/course-lesson-results", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public TableResponseEntity<CourseLessonResultsResponse> searchData(CourseLessonResultsRequest.SearchForm dto) {
        return courseLessonResultsService.searchData(dto);
    }

    @PostMapping(value = "/v1/course-lesson-results", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.CREATE)
    public ResponseEntity saveData(@Valid CourseLessonResultsRequest.SubmitForm dto) throws BaseAppException {
        return courseLessonResultsService.saveData(dto, null);
    }

    @PutMapping(value = "/v1/course-lesson-results/{id}", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.UPDATE)
    public ResponseEntity saveData(@Valid CourseLessonResultsRequest.SubmitForm dto, @PathVariable Long id) throws BaseAppException {
        return courseLessonResultsService.saveData(dto, id);
    }

    @DeleteMapping(value = "/v1/course-lesson-results/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.DELETE)
    public ResponseEntity deleteData(@PathVariable Long id) throws RecordNotExistsException {
        return courseLessonResultsService.deleteData(id);
    }

    @GetMapping(value = "/v1/course-lesson-results/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public BaseResponseEntity<CourseLessonResultsResponse> getDataById(@PathVariable Long id)  throws RecordNotExistsException {
        return courseLessonResultsService.getDataById(id);
    }

    @GetMapping(value = "/v1/course-lesson-results/export", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public ResponseEntity<Object> exportData(CourseLessonResultsRequest.SearchForm dto) throws Exception {
        return courseLessonResultsService.exportData(dto);
    }

}
