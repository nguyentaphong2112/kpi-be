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
import vn.hbtplus.annotations.Resource;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.constants.Constant;
import vn.hbtplus.constants.Scope;
import vn.hbtplus.exceptions.BaseAppException;
import vn.hbtplus.exceptions.RecordNotExistsException;
import vn.hbtplus.models.request.ResearchProjectsRequest;
import vn.hbtplus.models.response.BaseResponseEntity;
import vn.hbtplus.models.response.ResearchProjectsResponse;
import vn.hbtplus.models.response.TableResponseEntity;
import vn.hbtplus.services.ResearchProjectsService;
import vn.hbtplus.utils.ResponseUtils;

import javax.validation.Valid;

@RestController
@RequestMapping(BaseConstants.REQUEST_MAPPING_PREFIX)
@RequiredArgsConstructor
@Resource(value = Constant.RESOURCES.RESEARCH)
public class ResearchProjectsController {
    private final ResearchProjectsService researchProjectsService;

    @GetMapping(value = "/v1/research-projects", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public TableResponseEntity<ResearchProjectsResponse.SearchResult> searchData(ResearchProjectsRequest.SearchForm dto) {
        return researchProjectsService.searchData(dto);
    }

    @PostMapping(value = "/v1/research-projects", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.UPDATE)
    public ResponseEntity saveData(@Valid @ModelAttribute ResearchProjectsRequest.SubmitForm dto) throws BaseAppException {
        return researchProjectsService.saveData(dto, null);
    }
    @PutMapping(value = "/v1/research-projects/{id}", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.UPDATE)
    public ResponseEntity updateData(@Valid @ModelAttribute ResearchProjectsRequest.SubmitForm dto, @PathVariable Long id) throws BaseAppException {
        return researchProjectsService.saveData(dto, id);
    }

    @DeleteMapping(value = "/v1/research-projects/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.DELETE)
    public ResponseEntity deleteData(@PathVariable Long id) throws RecordNotExistsException {
        return researchProjectsService.deleteData(id);
    }

    @GetMapping(value = "/v1/research-projects/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public BaseResponseEntity<ResearchProjectsResponse.DetailBean> getDataById(@PathVariable Long id)  throws RecordNotExistsException {
        return ResponseUtils.ok(researchProjectsService.getDataById(id));
    }

    @GetMapping(value = "/v1/research-projects/export", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public ResponseEntity<Object> exportData(ResearchProjectsRequest.SearchForm dto) throws Exception {
        return researchProjectsService.exportData(dto);
    }

}
