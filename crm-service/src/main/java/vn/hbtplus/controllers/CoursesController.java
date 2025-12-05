/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import vn.hbtplus.annotations.HasPermission;
import vn.hbtplus.models.request.CourseLessonResultsRequest;
import vn.hbtplus.models.response.*;
import vn.hbtplus.models.request.CoursesRequest;
import vn.hbtplus.services.CoursesService;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.constants.Scope;
import vn.hbtplus.constants.Constant;
import vn.hbtplus.annotations.Resource;
import vn.hbtplus.exceptions.RecordNotExistsException;
import vn.hbtplus.exceptions.BaseAppException;
import vn.hbtplus.utils.ResponseUtils;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping(BaseConstants.REQUEST_MAPPING_PREFIX)
@RequiredArgsConstructor
@Resource(value = Constant.RESOURCES.CRM_COURSES)
public class CoursesController {
    private final CoursesService coursesService;

    @GetMapping(value = "/v1/courses", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public TableResponseEntity<CoursesResponse.SearchResult> searchData(CoursesRequest.SearchForm dto) {
        return coursesService.searchData(dto);
    }

    @PostMapping(value = "/v1/courses", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.CREATE)
    public ResponseEntity saveData(@RequestBody @Valid CoursesRequest.SubmitForm dto) throws BaseAppException {
        return coursesService.saveData(dto, null);
    }

    @PutMapping(value = "/v1/courses/{id}", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.UPDATE)
    public ResponseEntity saveData(@RequestBody @Valid CoursesRequest.SubmitForm dto, @PathVariable Long id) throws BaseAppException {
        return coursesService.saveData(dto, id);
    }

    @DeleteMapping(value = "/v1/courses/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.DELETE)
    public ResponseEntity deleteData(@PathVariable Long id) throws RecordNotExistsException {
        return coursesService.deleteData(id);
    }

    @GetMapping(value = "/v1/courses/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public BaseResponseEntity<CoursesResponse.DetailBean> getDataById(@PathVariable Long id)  throws RecordNotExistsException {
        return coursesService.getDataById(id);
    }

    @GetMapping(value = "/v1/courses/export", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public ResponseEntity<Object> exportData(CoursesRequest.SearchForm dto) throws Exception {
        return coursesService.exportData(dto);
    }

    @GetMapping(value = "/v1/courses/user-list", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public ListResponseEntity<CoursesResponse.UserDataSelected> getListUserData() throws Exception {
        return coursesService.getListUserData();
    }

    @GetMapping(value = "/v1/courses/course-list", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public ListResponseEntity<CoursesResponse.DataSelected> getListData(CoursesRequest.SearchForm dto) throws Exception {
        return coursesService.getListData(dto);
    }


    @PostMapping(value = "/v1/courses/lesson-result", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.CREATE)
    public ResponseEntity saveLessonResult(@Valid @RequestBody CoursesRequest.SubmitLessonResult dto) throws BaseAppException {
        return coursesService.saveLessonResult(dto);
    }

    @GetMapping(value = "/v1/courses/lesson-result/{traineeId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public ListResponseEntity<CourseLessonResultsRequest.SubmitForm> getListLessonResult(@RequestParam List<Long> listCourseLessonId, @PathVariable Long traineeId) throws Exception {
        return coursesService.getListLessonResult(listCourseLessonId, traineeId);
    }

    @GetMapping(value = "/v1/courses/trainee/download-template", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public ResponseEntity<Object> getTemplateImportTrainee() throws Exception {
        return ResponseUtils.getResponseFileEntity(coursesService.getTemplateTrainee() , true);
    }

    @GetMapping(value = "/v1/courses/download-template", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public ResponseEntity<Object> getTemplateImport(CoursesRequest.ImportRequest dto) throws Exception {
        return ResponseUtils.getResponseFileEntity(coursesService.getTemplate(dto) , true);
    }

    @PostMapping(value = "/v1/courses/import", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.IMPORT)
    public ResponseEntity importData(@ModelAttribute @Valid CoursesRequest.ImportRequest dto) throws Exception {
        return ResponseUtils.ok(coursesService.importData(dto));
    }


    @PostMapping(value = "/v1/courses/trainee/import", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.IMPORT)
    public ListResponseEntity importDataTrainee(@RequestPart(value = "file") MultipartFile file) throws Exception {
        return ResponseUtils.ok(coursesService.importDataTrainee(file));
    }

}
