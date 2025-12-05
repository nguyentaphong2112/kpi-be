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
import vn.hbtplus.models.request.MentoringTraineesRequest;
import vn.hbtplus.models.response.BaseResponseEntity;
import vn.hbtplus.models.response.MentoringTraineesResponse;
import vn.hbtplus.models.response.TableResponseEntity;
import vn.hbtplus.services.MentoringTraineesService;
import vn.hbtplus.utils.ResponseUtils;

import java.util.List;

@RestController
@RequestMapping(BaseConstants.REQUEST_MAPPING_PREFIX)
@RequiredArgsConstructor
@Resource(value = Constant.RESOURCES.MENTORING_TRAINEES)
public class MentoringTraineesController {
    private final MentoringTraineesService mentoringTraineesService;

    @GetMapping(value = "/v1/mentoring-trainees", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public TableResponseEntity<MentoringTraineesResponse.SearchResult> searchData(MentoringTraineesRequest.SearchForm dto) {
        return mentoringTraineesService.searchData(dto);
    }

    @PostMapping(value = "/v1/mentoring-trainees", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.CREATE)
    public ResponseEntity saveData(@RequestPart(value = "data") MentoringTraineesRequest.SubmitForm dto, @RequestPart(value = "files", required = false) List<MultipartFile> files) throws BaseAppException {
        return mentoringTraineesService.saveData(dto, files, null);
    }

    @PutMapping(value = "/v1/mentoring-trainees/{id}", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.UPDATE)
    public ResponseEntity saveData(@RequestPart(value = "data") MentoringTraineesRequest.SubmitForm dto, @RequestPart(value = "files", required = false) List<MultipartFile> files, @PathVariable Long id) throws BaseAppException {
        return mentoringTraineesService.saveData(dto, files, id);
    }

    @DeleteMapping(value = "/v1/mentoring-trainees/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.DELETE)
    public ResponseEntity deleteData(@PathVariable Long id) throws RecordNotExistsException {
        return mentoringTraineesService.deleteData(id);
    }

    @GetMapping(value = "/v1/mentoring-trainees/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public BaseResponseEntity<MentoringTraineesResponse.DetailBean> getDataById(@PathVariable Long id) throws RecordNotExistsException {
        return mentoringTraineesService.getDataById(id);
    }

    @GetMapping(value = "/v1/mentoring-trainees/export", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public ResponseEntity<Object> exportData(MentoringTraineesRequest.SearchForm dto) throws Exception {
        return mentoringTraineesService.exportData(dto);
    }

    @GetMapping(value = "/v1/mentoring-trainees/download-template", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public ResponseEntity<Object> getTemplateImportWorkScheduleDetail() throws Exception {
        return ResponseUtils.getResponseFileEntity(mentoringTraineesService.getTemplateIndicator() , true);
    }

    @PostMapping(value = "/v1/mentoring-trainees/import", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.IMPORT)
    public ResponseEntity importData(@RequestPart(value = "file") MultipartFile file) throws Exception {
        return ResponseUtils.ok(mentoringTraineesService.importData(file));
    }

}
