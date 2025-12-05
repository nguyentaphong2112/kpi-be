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
import vn.hbtplus.models.request.ExamPapersRequest;
import vn.hbtplus.services.ExamPapersService;
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
@Resource(value = Constant.RESOURCES.QUESTION)
public class ExamPapersController {
    private final ExamPapersService examPapersService;

    @GetMapping(value = "/v1/exam-papers", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public TableResponseEntity<ExamPapersResponse> searchData(ExamPapersRequest.SearchForm dto) {
        return examPapersService.searchData(dto);
    }

    @PostMapping(value = "/v1/exam-papers", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.UPDATE)
    public ResponseEntity saveData(@Valid @RequestBody ExamPapersRequest.SubmitForm dto) throws BaseAppException {
        return examPapersService.saveData(dto);
    }

    @DeleteMapping(value = "/v1/exam-papers/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.DELETE)
    public ResponseEntity deleteData(@PathVariable Long id) throws RecordNotExistsException {
        return examPapersService.deleteData(id);
    }

    @GetMapping(value = "/v1/exam-papers/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public BaseResponseEntity<ExamPapersResponse> getDataById(@PathVariable Long id)  throws RecordNotExistsException {
        return examPapersService.getDataById(id);
    }

    @GetMapping(value = "/v1/exam-papers/export", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public ResponseEntity<Object> exportData(ExamPapersRequest.SearchForm dto) throws Exception {
        return examPapersService.exportData(dto);
    }

}
