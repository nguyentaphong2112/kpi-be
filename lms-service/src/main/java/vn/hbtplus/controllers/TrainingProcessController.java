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
import vn.hbtplus.models.response.*;
import vn.hbtplus.models.request.TrainingProcessRequest;
import vn.hbtplus.services.TrainingProcessService;
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
@Resource(value = Constant.RESOURCES.TRAINING_PROCESS)
public class TrainingProcessController {
    private final TrainingProcessService trainingProcessService;

    @GetMapping(value = "/v1/training-process", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public TableResponseEntity<TrainingProcessResponse.SearchResult> searchData(TrainingProcessRequest.SearchForm dto) {
        return trainingProcessService.searchData(dto);
    }

    @PostMapping(value = "/v1/training-process", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.UPDATE)
    public ResponseEntity saveData(@Valid @RequestPart(value = "data") TrainingProcessRequest.SubmitForm dto,
                                   @RequestPart(value = "files", required = false) List<MultipartFile> files) throws BaseAppException {
        return trainingProcessService.saveData(dto, files, null);
    }

    @PutMapping(value = "/v1/training-process/{id}", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.UPDATE)
    public ResponseEntity updateData(@Valid @RequestPart(value = "data") TrainingProcessRequest.SubmitForm dto,
                                     @RequestPart(value = "files", required = false) List<MultipartFile> files,
                                     @PathVariable Long id) throws BaseAppException {
        return trainingProcessService.saveData(dto, files, id);
    }

    @DeleteMapping(value = "/v1/training-process/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.DELETE)
    public ResponseEntity deleteData(@PathVariable Long id) throws RecordNotExistsException {
        return trainingProcessService.deleteData(id);
    }

    @GetMapping(value = "/v1/training-process/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public BaseResponseEntity<TrainingProcessResponse.Detail> getDataById(@PathVariable Long id)  throws RecordNotExistsException {
        return trainingProcessService.getDataById(id);
    }

    @GetMapping(value = "/v1/training-process/export", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public ResponseEntity<Object> exportData(TrainingProcessRequest.SearchForm dto) throws Exception {
        return trainingProcessService.exportData(dto);
    }

    @GetMapping(value = "/v1/training-process/download-template", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public ResponseEntity<Object> getTemplateImport() throws Exception {
        return ResponseUtils.getResponseFileEntity(trainingProcessService.getImportTemplate() , true);
    }

    @PostMapping(value = "/v1/training-process/import", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.IMPORT)
    public ResponseEntity importData(@RequestPart(value = "file") MultipartFile file) throws Exception {
        return ResponseUtils.ok(trainingProcessService.importData(file));
    }

}
