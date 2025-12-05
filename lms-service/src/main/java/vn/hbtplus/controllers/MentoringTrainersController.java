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
import vn.hbtplus.annotations.Resource;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.constants.Constant;
import vn.hbtplus.constants.Scope;
import vn.hbtplus.exceptions.BaseAppException;
import vn.hbtplus.exceptions.RecordNotExistsException;
import vn.hbtplus.models.request.MentoringTrainersRequest;
import vn.hbtplus.models.response.BaseResponseEntity;
import vn.hbtplus.models.response.MentoringTrainersResponse;
import vn.hbtplus.models.response.TableResponseEntity;
import vn.hbtplus.services.MentoringTrainersService;

import javax.validation.Valid;

@RestController
@RequestMapping(BaseConstants.REQUEST_MAPPING_PREFIX)
@RequiredArgsConstructor
@Resource(value = Constant.RESOURCES.MENTORING_TRAINERS)
public class MentoringTrainersController {
    private final MentoringTrainersService mentoringTrainersService;

    @GetMapping(value = "/v1/mentoring-trainers", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public TableResponseEntity<MentoringTrainersResponse> searchData(MentoringTrainersRequest.SearchForm dto) {
        return mentoringTrainersService.searchData(dto);
    }

    @PostMapping(value = "/v1/mentoring-trainers", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.UPDATE)
    public ResponseEntity saveData(@Valid @RequestBody MentoringTrainersRequest.SubmitForm dto) throws BaseAppException {
        return mentoringTrainersService.saveData(dto, null);
    }

    @PutMapping(value = "/v1/mentoring-trainers/{id}", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.UPDATE)
    public ResponseEntity saveData(@Valid @RequestBody MentoringTrainersRequest.SubmitForm dto, @PathVariable Long id) throws BaseAppException {
        return mentoringTrainersService.saveData(dto, id);
    }

    @DeleteMapping(value = "/v1/mentoring-trainers/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.DELETE)
    public ResponseEntity deleteData(@PathVariable Long id) throws RecordNotExistsException {
        return mentoringTrainersService.deleteData(id);
    }

    @GetMapping(value = "/v1/mentoring-trainers/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public BaseResponseEntity<MentoringTrainersResponse> getDataById(@PathVariable Long id)  throws RecordNotExistsException {
        return mentoringTrainersService.getDataById(id);
    }

    @GetMapping(value = "/v1/mentoring-trainers/export", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public ResponseEntity<Object> exportData(MentoringTrainersRequest.SearchForm dto) throws Exception {
        return mentoringTrainersService.exportData(dto);
    }

    @GetMapping(value = "/v1/mentoring-trainers/download-template", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public ResponseEntity<Object> exportTemplate() throws Exception {
        return mentoringTrainersService.downloadTemplate();
    }

    @PostMapping(value = "/v1/mentoring-trainers/import-process", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.VIEW)
    public ResponseEntity<Object> importProcess(@RequestPart(value = "file") MultipartFile file) throws Exception {
        return mentoringTrainersService.importProcess(file);
    }

}
