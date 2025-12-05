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
import vn.hbtplus.models.request.CourseLessonsRequest;
import vn.hbtplus.repositories.entity.CourseLessonsEntity;
import vn.hbtplus.services.CourseLessonsService;
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
public class CourseLessonsController {
    private final CourseLessonsService courseLessonsService;

    @GetMapping(value = "/v1/course-lessons", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public TableResponseEntity<CourseLessonsResponse.SearchResult> searchData(CourseLessonsRequest.SearchForm dto) {
        return courseLessonsService.searchData(dto);
    }

    @PostMapping(value = "/v1/course-lessons", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.UPDATE)
    public ResponseEntity saveData(@Valid CourseLessonsRequest.SubmitForm dto) throws BaseAppException {
        return courseLessonsService.saveData(dto, null);
    }

    @PutMapping(value = "/v1/course-lessons/{id}", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.UPDATE)
    public ResponseEntity saveData(@Valid CourseLessonsRequest.SubmitForm dto, @PathVariable Long id) throws BaseAppException {
        return courseLessonsService.saveData(dto, id);
    }

    @DeleteMapping(value = "/v1/course-lessons/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.DELETE)
    public ResponseEntity deleteData(@PathVariable Long id) throws RecordNotExistsException {
        return courseLessonsService.deleteData(id);
    }

    @GetMapping(value = "/v1/course-lessons/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public BaseResponseEntity<CourseLessonsResponse.SearchResult> getDataById(@PathVariable Long id)  throws RecordNotExistsException {
        return courseLessonsService.getDataById(id);
    }

    @GetMapping(value = "/v1/course-lessons/course/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public ListResponseEntity<CourseLessonsEntity> getDataByCourseId(@PathVariable Long id)  throws RecordNotExistsException {
        return courseLessonsService.getDataByCourseId(id);
    }

    @GetMapping(value = "/v1/course-lessons/course-list/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public ListResponseEntity<CourseLessonsResponse.Selected> getDataByCourseListId(@PathVariable Long id)  throws RecordNotExistsException {
        return courseLessonsService.getDataByCourseListId(id);
    }

    @GetMapping(value = "/v1/course-lessons/export", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public ResponseEntity<Object> exportData(CourseLessonsRequest.SearchForm dto) throws Exception {
        return courseLessonsService.exportData(dto);
    }

}
