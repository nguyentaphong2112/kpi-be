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
import vn.hbtplus.models.request.QuestionsRequest;
import vn.hbtplus.services.QuestionsService;
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
public class QuestionsController {
    private final QuestionsService questionsService;

    @GetMapping(value = "/v1/questions", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public TableResponseEntity<QuestionsResponse> searchData(QuestionsRequest.SearchForm dto) {
        return questionsService.searchData(dto);
    }

    @PostMapping(value = "/v1/questions", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.UPDATE)
    public ResponseEntity saveData(@Valid @RequestBody QuestionsRequest.SubmitForm dto) throws BaseAppException {
        return questionsService.saveData(dto);
    }

    @DeleteMapping(value = "/v1/questions/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.DELETE)
    public ResponseEntity deleteData(@PathVariable Long id) throws RecordNotExistsException {
        return questionsService.deleteData(id);
    }

    @GetMapping(value = "/v1/questions/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public BaseResponseEntity<QuestionsResponse> getDataById(@PathVariable Long id)  throws RecordNotExistsException {
        return questionsService.getDataById(id);
    }

    @GetMapping(value = "/v1/questions/export", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public ResponseEntity<Object> exportData(QuestionsRequest.SearchForm dto) throws Exception {
        return questionsService.exportData(dto);
    }

}
