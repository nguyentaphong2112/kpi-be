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
import vn.hbtplus.models.request.EvaluationPeriodsRequest;
import vn.hbtplus.models.response.BaseResponseEntity;
import vn.hbtplus.models.response.EvaluationPeriodsResponse;
import vn.hbtplus.models.response.TableResponseEntity;
import vn.hbtplus.services.EvaluationPeriodsService;
import vn.hbtplus.utils.ResponseUtils;

import javax.validation.Valid;

@RestController
@RequestMapping(BaseConstants.REQUEST_MAPPING_PREFIX)
@RequiredArgsConstructor
@Resource(value = Constant.RESOURCES.EVALUATION_PERIODS)
public class EvaluationPeriodsController {
    private final EvaluationPeriodsService evaluationPeriodsService;

    @GetMapping(value = "/v1/evaluation-periods", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public TableResponseEntity<EvaluationPeriodsResponse.SearchResult> searchData(EvaluationPeriodsRequest.SearchForm dto) {
        return evaluationPeriodsService.searchData(dto);
    }

    @PostMapping(value = "/v1/evaluation-periods", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.CREATE)
    public ResponseEntity saveData(@Valid EvaluationPeriodsRequest.SubmitForm dto) throws BaseAppException {
        return evaluationPeriodsService.saveData(dto, null);
    }

    @PutMapping(value = "/v1/evaluation-periods/{id}", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.UPDATE)
    public ResponseEntity saveData(@Valid EvaluationPeriodsRequest.SubmitForm dto, @PathVariable Long id) throws BaseAppException {
        return evaluationPeriodsService.saveData(dto, id);
    }

    @PostMapping(value = "/v1/evaluation-periods/init-data/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.CREATE)
    public ResponseEntity initData(@PathVariable Long id) throws BaseAppException {
        return ResponseUtils.ok(evaluationPeriodsService.initData(id));
    }


    @DeleteMapping(value = "/v1/evaluation-periods/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.DELETE)
    public ResponseEntity deleteData(@PathVariable Long id) throws RecordNotExistsException {
        return evaluationPeriodsService.deleteData(id);
    }

    @GetMapping(value = "/v1/evaluation-periods/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public BaseResponseEntity<EvaluationPeriodsResponse.DetailBean> getDataById(@PathVariable Long id) throws RecordNotExistsException {
        return evaluationPeriodsService.getDataById(id);
    }

    @GetMapping(value = "/v1/evaluation-periods/export", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public ResponseEntity<Object> exportData(EvaluationPeriodsRequest.SearchForm dto) throws Exception {
        return evaluationPeriodsService.exportData(dto);
    }

    @GetMapping(value = "/v1/evaluation-periods/max-year", produces = MediaType.APPLICATION_JSON_VALUE)
    public BaseResponseEntity<EvaluationPeriodsResponse.MaxYear> getDataMaxYear() throws RecordNotExistsException {
        return evaluationPeriodsService.getDataByMaxYear();
    }

    @PutMapping(value = "/v1/evaluation-periods/status/{evaluationPeriodId}", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.UPDATE)
    public ResponseEntity saveStatus(@Valid EvaluationPeriodsRequest.Status dto, @PathVariable Long evaluationPeriodId) throws BaseAppException {
        return evaluationPeriodsService.updateStatusById(dto, evaluationPeriodId);
    }

}
