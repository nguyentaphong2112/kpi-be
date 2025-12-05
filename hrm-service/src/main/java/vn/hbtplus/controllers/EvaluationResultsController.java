/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import vn.hbtplus.annotations.HasPermission;
import vn.hbtplus.annotations.Resource;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.constants.Constant;
import vn.hbtplus.constants.Scope;
import vn.hbtplus.exceptions.BaseAppException;
import vn.hbtplus.exceptions.RecordNotExistsException;
import vn.hbtplus.models.BaseSearchRequest;
import vn.hbtplus.models.request.EmployeesRequest;
import vn.hbtplus.models.request.EvaluationResultsRequest;
import vn.hbtplus.models.response.BaseResponseEntity;
import vn.hbtplus.models.response.EvaluationResultsResponse;
import vn.hbtplus.models.response.ListResponseEntity;
import vn.hbtplus.models.response.TableResponseEntity;
import vn.hbtplus.services.EmployeesService;
import vn.hbtplus.services.EvaluationResultsService;
import vn.hbtplus.utils.ResponseUtils;
import vn.hbtplus.utils.Utils;

import javax.validation.Valid;

@RestController
@RequestMapping(BaseConstants.REQUEST_MAPPING_PREFIX)
@RequiredArgsConstructor
@Resource(value = Constant.RESOURCES.EVALUATION_RESULTS)
public class EvaluationResultsController {
    private final EvaluationResultsService evaluationResultsService;
    private final EmployeesService employeesService;

    @GetMapping(value = "/v1/evaluation-results", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public TableResponseEntity<EvaluationResultsResponse.SearchResult> searchData(EmployeesRequest.SearchForm dto) {
        return evaluationResultsService.searchData(dto);
    }

    @GetMapping(value = "/v1/evaluation-results/export", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public ResponseEntity<Object> exportData(EmployeesRequest.SearchForm dto) throws Exception {
        return evaluationResultsService.exportData(dto);
    }

    @PostMapping(value = "/v1/evaluation-results/{employeeId}", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.CREATE, domainId = "employeeId")
    public ResponseEntity saveData(@RequestBody @Valid EvaluationResultsRequest.SubmitForm dto, @PathVariable Long employeeId) throws BaseAppException {
        return evaluationResultsService.saveData(dto, employeeId, null);
    }

    @PutMapping(value = "/v1/evaluation-results/{employeeId}/{id}", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.UPDATE, domainId = "employeeId")
    public ResponseEntity updateData(@RequestBody @Valid EvaluationResultsRequest.SubmitForm dto,
                                     @PathVariable Long employeeId, @PathVariable Long id
    ) throws BaseAppException {
        return evaluationResultsService.saveData(dto, employeeId, id);
    }


    @DeleteMapping(value = "/v1/evaluation-results/{employeeId}/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.DELETE, domainId = "employeeId")
    public ResponseEntity deleteData(@PathVariable Long employeeId, @PathVariable Long id) throws BaseAppException {
        return evaluationResultsService.deleteData(employeeId, id);
    }

    @GetMapping(value = "/v1/evaluation-results/{employeeId}/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW, domainId = "employeeId")
    public BaseResponseEntity<EvaluationResultsResponse.DetailBean> getDataById(@PathVariable Long employeeId, @PathVariable Long id) throws BaseAppException {
        return evaluationResultsService.getDataById(employeeId, id);
    }

    @GetMapping(value = "/v1/evaluation-results/pageable/{employeeId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW, domainId = "employeeId")
    public TableResponseEntity<EvaluationResultsResponse.SearchResult> getTableList(@PathVariable Long employeeId, BaseSearchRequest request) throws RecordNotExistsException {
        return ResponseUtils.ok(evaluationResultsService.getTableList(employeeId, request));
    }


    @GetMapping(value = "/v1/evaluation-results/evaluation_periods", produces = MediaType.APPLICATION_JSON_VALUE)
    public ListResponseEntity<EvaluationResultsResponse.EvaluationPeriods> getListEvaluationPeriods(@RequestParam(required = false) Integer year, @RequestParam(required = false) String evaluationType) {
        return ResponseUtils.ok(evaluationResultsService.getListEvaluationPeriods(year, evaluationType));
    }

    @PostMapping(value = "/v1/evaluation-results/personal", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.CREATE, resource = Constant.RESOURCES.PERSONAL_EVALUATION_RESULTS)
    public ResponseEntity saveData(@RequestBody @Valid EvaluationResultsRequest.SubmitForm dto) throws BaseAppException {
        Long employeeId = employeesService.getEmployeeId(Utils.getUserEmpCode());
        return evaluationResultsService.saveData(dto, employeeId, null);
    }

    @PutMapping(value = "/v1/evaluation-results/personal/{id}", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.UPDATE, resource = Constant.RESOURCES.PERSONAL_EVALUATION_RESULTS)
    public ResponseEntity updateData(@RequestBody @Valid EvaluationResultsRequest.SubmitForm dto,
                                     @PathVariable Long id
    ) throws BaseAppException {
        Long employeeId = employeesService.getEmployeeId(Utils.getUserEmpCode());
        return evaluationResultsService.saveData(dto, employeeId, id);
    }


    @DeleteMapping(value = "/v1/evaluation-results/personal/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.DELETE, resource = Constant.RESOURCES.PERSONAL_EVALUATION_RESULTS)
    public ResponseEntity deleteData(@PathVariable Long id) throws BaseAppException {
        Long employeeId = employeesService.getEmployeeId(Utils.getUserEmpCode());
        return evaluationResultsService.deleteData(employeeId, id);
    }

    @GetMapping(value = "/v1/evaluation-results/personal/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW, resource = Constant.RESOURCES.PERSONAL_EVALUATION_RESULTS)
    public BaseResponseEntity<EvaluationResultsResponse.DetailBean> getDataById(@PathVariable Long id) throws BaseAppException {
        Long employeeId = employeesService.getEmployeeId(Utils.getUserEmpCode());
        return evaluationResultsService.getDataById(employeeId, id);
    }

    @GetMapping(value = "/v1/evaluation-results/pageable/personal", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW, resource = Constant.RESOURCES.PERSONAL_EVALUATION_RESULTS)
    public TableResponseEntity<EvaluationResultsResponse.SearchResult> getTableList(BaseSearchRequest request) throws RecordNotExistsException {
        Long employeeId = employeesService.getEmployeeId(Utils.getUserEmpCode());
        return ResponseUtils.ok(evaluationResultsService.getTableList(employeeId, request));
    }

    @PostMapping(value = "/v1/evaluation-results/import", produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @HasPermission(scope = Scope.IMPORT, resource = Constant.RESOURCES.EVALUATION_RESULTS)
    public ResponseEntity<Object> processImport(@RequestPart MultipartFile file,
                                                @RequestParam Long periodId,
                                                @RequestParam(required = false) boolean isForceUpdate) throws Exception {
        return evaluationResultsService.processImport(file, periodId, isForceUpdate);
    }

    @GetMapping(value = "/v1/evaluation-results/import-template", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW, resource = Constant.RESOURCES.EVALUATION_RESULTS)
    public ResponseEntity<Object> downloadImportTemplate(@RequestParam Long periodId) throws Exception {
        return evaluationResultsService.downloadImportTemplate(periodId);
    }
}
