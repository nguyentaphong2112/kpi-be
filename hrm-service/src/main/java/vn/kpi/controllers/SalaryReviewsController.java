/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.kpi.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import vn.kpi.annotations.HasPermission;
import vn.kpi.models.response.*;
import vn.kpi.models.request.SalaryReviewsRequest;
import vn.kpi.services.SalaryReviewsService;
import vn.kpi.constants.BaseConstants;
import vn.kpi.constants.Scope;
import vn.kpi.constants.Constant;
import vn.kpi.annotations.Resource;
import vn.kpi.exceptions.RecordNotExistsException;
import vn.kpi.exceptions.BaseAppException;
import vn.kpi.utils.ResponseUtils;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping(BaseConstants.REQUEST_MAPPING_PREFIX)
@RequiredArgsConstructor
@Resource(value = Constant.RESOURCES.EMPLOYEE)
public class SalaryReviewsController {
    private final SalaryReviewsService salaryReviewsService;

    @GetMapping(value = "/v1/salary-reviews", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public TableResponseEntity<SalaryReviewsResponse.SearchResult> searchData(SalaryReviewsRequest.SearchForm dto) {
        return salaryReviewsService.searchData(dto);
    }

    @PostMapping(value = "/v1/salary-reviews/make-list", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.UPDATE)
    public ResponseEntity makeList(@Valid @RequestBody SalaryReviewsRequest.MakeListForm dto) throws BaseAppException, IllegalAccessException {
        return ResponseUtils.ok(salaryReviewsService.makeList(dto.getPeriodId()));
    }

    @PostMapping(value = "/v1/salary-reviews", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.UPDATE)
    public ResponseEntity saveData(@Valid @RequestBody SalaryReviewsRequest.SubmitForm dto) throws BaseAppException {
        return salaryReviewsService.saveData(dto, null);
    }


    @PostMapping(value = "/v1/salary-reviews/import/{periodId}", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.VIEW)
    public ResponseEntity importData(@PathVariable String periodId,
                                     @RequestPart(value = "file") MultipartFile fileImport,
                                     @RequestPart(value = "fileExtends", required = false) List<MultipartFile> fileExtends) throws Exception {
        return ResponseUtils.ok(salaryReviewsService.importData(periodId, fileImport, fileExtends));
    }

    @PutMapping(value = "/v1/salary-reviews/{id}", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.UPDATE)
    public ResponseEntity updateData(@Valid @RequestBody SalaryReviewsRequest.SubmitForm dto, @PathVariable Long id) throws BaseAppException {
        return salaryReviewsService.saveData(dto, id);
    }

    @DeleteMapping(value = "/v1/salary-reviews/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.DELETE)
    public ResponseEntity deleteData(@PathVariable Long id) throws RecordNotExistsException {
        return salaryReviewsService.deleteData(id);
    }

    @GetMapping(value = "/v1/salary-reviews/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public BaseResponseEntity<SalaryReviewsResponse.SearchResult> getDataById(@PathVariable Long id) throws RecordNotExistsException {
        return salaryReviewsService.getDataById(id);
    }

    @GetMapping(value = "/v1/salary-reviews/download-template/{periodId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public ResponseEntity<Object> downloadTemplate(@PathVariable String periodId) throws Exception {
        return salaryReviewsService.downloadTemplate(periodId);
    }
    @GetMapping(value = "/v1/salary-reviews/export", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public ResponseEntity<Object> exportData(SalaryReviewsRequest.SearchForm dto) throws Exception {
        return salaryReviewsService.exportData(dto);
    }

    @GetMapping(value = "/v1/salary-reviews/export/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public ResponseEntity<Object> exportDataById(@PathVariable Long id) throws Exception {
        return ResponseUtils.ok(salaryReviewsService.exportDataById(id), "Trich_sao_quyet_dinh_nang_luong.docx");
    }

}
