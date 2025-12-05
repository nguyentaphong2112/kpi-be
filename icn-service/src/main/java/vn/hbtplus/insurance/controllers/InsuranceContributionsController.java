/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.insurance.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import vn.hbtplus.annotations.HasPermission;
import vn.hbtplus.annotations.Resource;
import vn.hbtplus.insurance.constants.Constant;
import vn.hbtplus.constants.Scope;
import vn.hbtplus.exceptions.BaseAppException;
import vn.hbtplus.exceptions.RecordNotExistsException;
import vn.hbtplus.models.BaseApproveRequest;
import vn.hbtplus.insurance.models.request.InsuranceContributionsRequest;
import vn.hbtplus.models.response.BaseResponseEntity;
import vn.hbtplus.insurance.models.response.InsuranceContributionsResponse;
import vn.hbtplus.models.response.ListResponseEntity;
import vn.hbtplus.models.response.TableResponseEntity;
import vn.hbtplus.insurance.repositories.entity.InsuranceContributionsEntity;
import vn.hbtplus.insurance.services.InsuranceContributionsService;
import vn.hbtplus.utils.ResponseUtils;
import vn.hbtplus.utils.Utils;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping(Constant.REQUEST_MAPPING_PREFIX)
@RequiredArgsConstructor
@Resource(value = Constant.RESOURCE.ICN_INSURANCE_CONTRIBUTIONS)
public class InsuranceContributionsController {
    private final InsuranceContributionsService insuranceContributionsService;

    @GetMapping(value = "/v1/insurance-contributions", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public TableResponseEntity<InsuranceContributionsResponse> searchData(InsuranceContributionsRequest.SearchForm dto) {
        return insuranceContributionsService.searchData(dto);
    }

    @PostMapping(value = "/v1/insurance-contributions", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.UPDATE)
    public ResponseEntity saveData(@Valid InsuranceContributionsRequest.SubmitForm dto) throws BaseAppException {
        return insuranceContributionsService.saveData(dto);
    }

    @PostMapping(value = "/v1/insurance-contributions/calculate", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.CREATE)
    public ResponseEntity calculate(@RequestParam(required = false) List<String> empCodes, @RequestParam String periodDate) throws Exception {
        insuranceContributionsService.validateBeforeCalculate(Utils.getLastDay(Utils.stringToDate(periodDate)));
        return ResponseUtils.ok(insuranceContributionsService.calculate(empCodes, Utils.getLastDay(Utils.stringToDate(periodDate))));
    }

    @DeleteMapping(value = "/v1/insurance-contributions/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.DELETE)
    public ResponseEntity deleteData(@PathVariable Long id) throws BaseAppException {
        return insuranceContributionsService.deleteData(id);
    }

    @GetMapping(value = "/v1/insurance-contributions/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public BaseResponseEntity<InsuranceContributionsResponse> getDataById(@PathVariable Long id) throws RecordNotExistsException {
        return insuranceContributionsService.getDataById(id);
    }

    @GetMapping(value = "/v1/insurance-contributions/export", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public ResponseEntity<Object> exportData(InsuranceContributionsRequest.SearchForm dto) throws Exception {
        return insuranceContributionsService.exportData(dto);
    }

    @PutMapping(value = "/v1/insurance-contributions/approve-by-id", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.APPROVE)
    public ListResponseEntity<Long> approveByList(@RequestBody BaseApproveRequest dto) throws BaseAppException {
        return ResponseUtils.ok(insuranceContributionsService.updateStatusById(dto, InsuranceContributionsEntity.STATUS.PHE_DUYET));
    }

    @PutMapping(value = "/v1/insurance-contributions/undo-approve-by-id", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.APPROVE)
    public ListResponseEntity<Long> unApproveById(@RequestBody BaseApproveRequest dto) throws BaseAppException {
        return ResponseUtils.ok(insuranceContributionsService.updateStatusById(dto, InsuranceContributionsEntity.STATUS.DU_THAO));
    }

    @PutMapping(value = "/v1/insurance-contributions/approve-all", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.APPROVE)
    public ListResponseEntity<Long> approveAll(@RequestBody InsuranceContributionsRequest.SearchForm dto) throws BaseAppException {
        dto.setStatus(List.of(InsuranceContributionsEntity.STATUS.DU_THAO));
        return ResponseUtils.ok(insuranceContributionsService.updateStatus(dto, InsuranceContributionsEntity.STATUS.PHE_DUYET));
    }

    @PutMapping(value = "/v1/insurance-contributions/undo-approve-all", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.APPROVE)
    public ListResponseEntity<Long> undoApproveAll(@RequestBody InsuranceContributionsRequest.SearchForm dto) throws BaseAppException {
        dto.setStatus(List.of(InsuranceContributionsEntity.STATUS.PHE_DUYET));
        return ResponseUtils.ok(insuranceContributionsService.updateStatus(dto, InsuranceContributionsEntity.STATUS.DU_THAO));
    }

    @PostMapping(value = "/v1/insurance-contributions/retro", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.CREATE)
    public BaseResponseEntity<Integer> retroByIds(@RequestParam String periodDate, @RequestParam List<Long> ids) throws BaseAppException {
        return ResponseUtils.ok(insuranceContributionsService.retroByIds(Utils.getLastDay(Utils.stringToDate(periodDate)), ids));
    }

    @PostMapping(value = "/v1/insurance-contributions/retro-all", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.CREATE)
    public BaseResponseEntity<Integer> retroAll(@RequestParam String periodDate) throws BaseAppException {
        return null;
    }

    @PutMapping(value = "/v1/insurance-contributions/switch-type/{type}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.CREATE)
    public BaseResponseEntity<Integer> switchType(@PathVariable String type, @RequestParam List<Long> ids, @RequestParam(required = false) String reason) throws Exception {
        if (!type.equalsIgnoreCase(InsuranceContributionsEntity.TYPES.KO_THU)
                && !type.equalsIgnoreCase(InsuranceContributionsEntity.TYPES.THAI_SAN)
                && !type.equalsIgnoreCase(InsuranceContributionsEntity.TYPES.THU_BHXH)
        ) {
            throw new BaseAppException("Giá trị tham số type không hợp lệ!");
        }
        return ResponseUtils.ok(insuranceContributionsService.switchType(type, ids, reason));
    }

    @PostMapping(value = "/v1/insurance-contributions/retro-medical", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.CREATE)
    public ResponseEntity retroMedical(@RequestParam String periodDate, @Valid @RequestBody InsuranceContributionsRequest.RetroMedicalForm retroMedicalForm) throws BaseAppException, InstantiationException, IllegalAccessException {
        insuranceContributionsService.retroMedical(Utils.getLastDay(Utils.stringToDate(periodDate)), retroMedicalForm);

        return ResponseUtils.ok();
    }

    @PostMapping(value = "/v1/insurance-contributions/retro-medical/import", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.CREATE)
    public ResponseEntity importRetroMedical(@RequestPart MultipartFile file,
                                             @RequestParam String periodDate) throws Exception {
        insuranceContributionsService.importRetroMedical(Utils.getLastDay(Utils.stringToDate(periodDate)), file);

        return ResponseUtils.ok();
    }

    @GetMapping(value = "/v1/insurance-contributions/retro-medical/download-template", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public ResponseEntity<Object> downloadTemplate(@RequestParam String periodDate) throws Exception {
        return insuranceContributionsService.downloadTemplateRetroMedical(Utils.getLastDay(Utils.stringToDate(periodDate)));
    }
}
