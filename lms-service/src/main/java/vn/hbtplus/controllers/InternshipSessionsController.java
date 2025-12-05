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
import vn.hbtplus.models.request.InternshipSessionsRequest;
import vn.hbtplus.services.InternshipSessionsService;
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
@Resource(value = Constant.RESOURCES.INTERNSHIP_SESSION)
public class InternshipSessionsController {
    private final InternshipSessionsService internshipSessionsService;

    @GetMapping(value = "/v1/internship-sessions", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public TableResponseEntity<InternshipSessionsResponse.SearchResult> searchData(InternshipSessionsRequest.SearchForm dto) {
        return internshipSessionsService.searchData(dto);
    }

    @PostMapping(value = "/v1/internship-sessions", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.UPDATE)
    public ResponseEntity saveData(@Valid @RequestPart(value = "data") InternshipSessionsRequest.SubmitForm dto,
                                   @RequestPart(value = "files", required = false) List<MultipartFile> files) throws BaseAppException {
        return internshipSessionsService.saveData(dto,files, null);
    }

    @PutMapping(value = "/v1/internship-sessions/{id}", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.UPDATE)
    public ResponseEntity updateData(@Valid @RequestPart(value = "data") InternshipSessionsRequest.SubmitForm dto,
                                     @RequestPart(value = "files", required = false) List<MultipartFile> files,
                                     @PathVariable Long id) throws BaseAppException {
        return internshipSessionsService.saveData(dto, files, id);
    }

    @DeleteMapping(value = "/v1/internship-sessions/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.DELETE)
    public ResponseEntity deleteData(@PathVariable Long id) throws RecordNotExistsException {
        return internshipSessionsService.deleteData(id);
    }

    @GetMapping(value = "/v1/internship-sessions/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public BaseResponseEntity<InternshipSessionsResponse.Detail> getDataById(@PathVariable Long id) throws RecordNotExistsException {
        return internshipSessionsService.getDataById(id);
    }

    @GetMapping(value = "/v1/internship-sessions/export", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public ResponseEntity<Object> exportData(InternshipSessionsRequest.SearchForm dto) throws Exception {
        return internshipSessionsService.exportData(dto);
    }

    @GetMapping(value = "/v1/internship-sessions/download-template", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public ResponseEntity<Object> getTemplateImportWorkScheduleDetail() throws Exception {
        return ResponseUtils.getResponseFileEntity(internshipSessionsService.getTemplateIndicator() , true);
    }

    @PostMapping(value = "/v1/internship-sessions/import", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.IMPORT)
    public ResponseEntity importData(@RequestPart(value = "file") MultipartFile file) throws Exception {
        return ResponseUtils.ok(internshipSessionsService.importData(file));
    }

}
