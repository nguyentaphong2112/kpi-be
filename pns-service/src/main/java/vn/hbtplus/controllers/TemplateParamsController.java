/*
 * Copyright (C) 2022 HRPLUS. All rights reserved.
 * EcoIT. Use is subject to license terms.
 */
package vn.hbtplus.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.hbtplus.annotations.HasPermission;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.constants.Scope;
import vn.hbtplus.models.dto.TemplateParamsDTO;
import vn.hbtplus.models.response.BaseResponseEntity;
import vn.hbtplus.models.response.ListResponseEntity;
import vn.hbtplus.repositories.entity.TemplateParamsEntity;
import vn.hbtplus.services.TemplateParamsService;


@RestController
@RequestMapping(BaseConstants.REQUEST_MAPPING_PREFIX)
@RequiredArgsConstructor
public class TemplateParamsController {
    private final TemplateParamsService templateParamsService;

    @GetMapping(value = "/v1/template-params", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public ListResponseEntity<TemplateParamsEntity> searchData(TemplateParamsDTO dto) {
        return templateParamsService.searchData(dto);
    }

    @PostMapping(value = "/v1/template-params", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.CREATE)
    public BaseResponseEntity<Object> saveData(@RequestBody TemplateParamsDTO dto) {
        return templateParamsService.saveData(dto);
    }

    @DeleteMapping(value = "/v1/template-params/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.DELETE)
    public BaseResponseEntity<Object> deleteData(@PathVariable Long id) {
        return templateParamsService.deleteData(id);
    }

    @GetMapping(value = "/v1/template-params/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public BaseResponseEntity<Object> getDataById(@PathVariable Long id) {
        return templateParamsService.getDataById(id);
    }

}

