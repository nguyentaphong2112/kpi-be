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
import vn.hbtplus.models.request.TrainingProgramsRequest;
import vn.hbtplus.services.TrainingProgramsService;
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
@Resource(value = Constant.RESOURCES.CRM_TRAINING_PROGRAMS)
public class TrainingProgramsController {
    private final TrainingProgramsService trainingProgramsService;

    @GetMapping(value = "/v1/training-programs", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public TableResponseEntity<TrainingProgramsResponse.SearchResult> searchData(TrainingProgramsRequest.SearchForm dto) {
        return trainingProgramsService.searchData(dto);
    }

    @PostMapping(value = "/v1/training-programs", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.CREATE)
    public ResponseEntity saveData(@Valid TrainingProgramsRequest.SubmitForm dto) throws BaseAppException {
        return trainingProgramsService.saveData(dto, null);
    }

    @PutMapping(value = "/v1/training-programs/{id}", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.UPDATE)
    public ResponseEntity saveData(@Valid TrainingProgramsRequest.SubmitForm dto, @PathVariable Long id) throws BaseAppException {
        return trainingProgramsService.saveData(dto, id);
    }

    @DeleteMapping(value = "/v1/training-programs/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.DELETE)
    public ResponseEntity deleteData(@PathVariable Long id) throws RecordNotExistsException {
        return trainingProgramsService.deleteData(id);
    }

    @GetMapping(value = "/v1/training-programs/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public BaseResponseEntity<TrainingProgramsResponse.DetailBean> getDataById(@PathVariable Long id) throws RecordNotExistsException {
        return trainingProgramsService.getDataById(id);
    }

    @GetMapping(value = "/v1/training-programs/export", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public ResponseEntity<Object> exportData(TrainingProgramsRequest.SearchForm dto) throws Exception {
        return trainingProgramsService.exportData(dto);
    }


    @GetMapping(value = "/v1/training-programs/list", produces = MediaType.APPLICATION_JSON_VALUE)
//    @HasPermission(scope = Scope.VIEW)
    public ListResponseEntity<TrainingProgramsResponse.DataSelected> getListData() throws Exception {
        return trainingProgramsService.getListData();
    }



}
