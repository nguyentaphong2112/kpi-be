/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.kpi.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.kpi.annotations.HasPermission;
import vn.kpi.models.response.*;
import vn.kpi.models.request.IndicatorUsingScopesRequest;
import vn.kpi.services.IndicatorUsingScopesService;
import vn.kpi.constants.BaseConstants;
import vn.kpi.constants.Scope;
import vn.kpi.constants.Constant;
import vn.kpi.annotations.Resource;
import vn.kpi.exceptions.RecordNotExistsException;
import vn.kpi.exceptions.BaseAppException;
import javax.validation.Valid;

@RestController
@RequestMapping(BaseConstants.REQUEST_MAPPING_PREFIX)
@RequiredArgsConstructor
@Resource(value = Constant.RESOURCES.INDICATOR_SCOPES)
public class IndicatorUsingScopesController {
    private final IndicatorUsingScopesService indicatorUsingScopesService;

    @GetMapping(value = "/v1/indicator-using-scopes", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public TableResponseEntity<IndicatorUsingScopesResponse> searchData(IndicatorUsingScopesRequest.SearchForm dto) {
        return indicatorUsingScopesService.searchData(dto);
    }

    @PostMapping(value = "/v1/indicator-using-scopes", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.UPDATE)
    public ResponseEntity saveData(@Valid  IndicatorUsingScopesRequest.SubmitForm dto) throws BaseAppException {
        return indicatorUsingScopesService.saveData(dto);
    }

    @DeleteMapping(value = "/v1/indicator-using-scopes/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.DELETE)
    public ResponseEntity deleteData(@PathVariable Long id) throws RecordNotExistsException {
        return indicatorUsingScopesService.deleteData(id);
    }

    @GetMapping(value = "/v1/indicator-using-scopes/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public BaseResponseEntity<IndicatorUsingScopesResponse> getDataById(@PathVariable Long id)  throws RecordNotExistsException {
        return indicatorUsingScopesService.getDataById(id);
    }

    @GetMapping(value = "/v1/indicator-using-scopes/export", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public ResponseEntity<Object> exportData(IndicatorUsingScopesRequest.SearchForm dto) throws Exception {
        return indicatorUsingScopesService.exportData(dto);
    }

}
