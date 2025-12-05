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
import vn.hbtplus.models.TreeDto;
import vn.hbtplus.models.request.GenresRequest;
import vn.hbtplus.models.response.BaseResponseEntity;
import vn.hbtplus.models.response.GenresResponse;
import vn.hbtplus.models.response.ListResponseEntity;
import vn.hbtplus.models.response.TableResponseEntity;
import vn.hbtplus.services.GenresService;
import vn.hbtplus.utils.ResponseUtils;

import javax.validation.Valid;

@RestController
@RequestMapping(BaseConstants.REQUEST_MAPPING_PREFIX)
@RequiredArgsConstructor
@Resource(value = Constant.RESOURCES.ICN_CONFIG_PARAMETER)
public class GenresController {
    private final GenresService genresService;

    @GetMapping(value = "/v1/genres", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public TableResponseEntity<GenresResponse> searchData(GenresRequest.SearchForm dto) {
        return genresService.searchData(dto);
    }

    @PostMapping(value = "/v1/genres", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.UPDATE)
    public ResponseEntity saveData(@Valid  GenresRequest.SubmitForm dto) throws BaseAppException {
        return genresService.saveData(dto);
    }

    @DeleteMapping(value = "/v1/genres/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.DELETE)
    public ResponseEntity deleteData(@PathVariable Long id) throws RecordNotExistsException {
        return genresService.deleteData(id);
    }

    @GetMapping(value = "/v1/genres/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public BaseResponseEntity<GenresResponse> getDataById(@PathVariable Long id)  throws RecordNotExistsException {
        return genresService.getDataById(id);
    }

    @GetMapping(value = "/v1/genres/export", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public ResponseEntity<Object> exportData(GenresRequest.SearchForm dto) throws Exception {
        return genresService.exportData(dto);
    }
    @GetMapping(value = "/v1/genres-tree/init-tree", produces = MediaType.APPLICATION_JSON_VALUE)
    public ListResponseEntity<TreeDto> initTree()  {
        return ResponseUtils.ok(genresService.initTree());
    }
}
