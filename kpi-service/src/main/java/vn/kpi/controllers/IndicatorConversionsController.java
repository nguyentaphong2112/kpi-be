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
import vn.kpi.annotations.Resource;
import vn.kpi.constants.BaseConstants;
import vn.kpi.constants.Constant;
import vn.kpi.constants.Scope;
import vn.kpi.exceptions.BaseAppException;
import vn.kpi.exceptions.RecordNotExistsException;
import vn.kpi.models.dto.OrgDto;
import vn.kpi.models.request.IndicatorConversionsRequest;
import vn.kpi.models.response.BaseResponseEntity;
import vn.kpi.models.response.IndicatorConversionsResponse;
import vn.kpi.models.response.ListResponseEntity;
import vn.kpi.models.response.TableResponseEntity;
import vn.kpi.services.IndicatorConversionsService;
import vn.kpi.utils.ResponseUtils;

import javax.validation.Valid;

@RestController
@RequestMapping(BaseConstants.REQUEST_MAPPING_PREFIX)
@RequiredArgsConstructor
@Resource(value = Constant.RESOURCES.INDICATOR_CONVERSION)
public class IndicatorConversionsController {
    private final IndicatorConversionsService indicatorConversionsService;

    @GetMapping(value = "/v1/indicator-conversions", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public TableResponseEntity<IndicatorConversionsResponse.SearchResult> searchData(IndicatorConversionsRequest.SearchForm dto) {
        return indicatorConversionsService.searchData(dto);
    }

    @PostMapping(value = "/v1/indicator-conversions", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.CREATE)
    public ResponseEntity saveData(@Valid @RequestBody IndicatorConversionsRequest.SubmitForm dto) throws BaseAppException {
        return indicatorConversionsService.saveData(dto, null);
    }

    @PutMapping(value = "/v1/indicator-conversions/{id}", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.UPDATE)
    public ResponseEntity updateData(@Valid @RequestBody IndicatorConversionsRequest.SubmitForm dto, @PathVariable Long id) throws BaseAppException {
        return indicatorConversionsService.saveData(dto, id);
    }

    @DeleteMapping(value = "/v1/indicator-conversions/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.DELETE)
    public ResponseEntity deleteData(@PathVariable Long id) throws RecordNotExistsException {
        return indicatorConversionsService.deleteData(id);
    }

    @GetMapping(value = "/v1/indicator-conversions/indicators", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public ResponseEntity getListConversion(@RequestParam Long indicatorMasterId
    ) throws RecordNotExistsException {
        return ResponseUtils.ok(indicatorConversionsService.getListConversion(indicatorMasterId));
    }

    @GetMapping(value = "/v1/indicator-conversions/indicators/get-table", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public TableResponseEntity<IndicatorConversionsResponse.Indicator> getListConversionTable(IndicatorConversionsRequest.SearchForm dto) throws RecordNotExistsException {
        return indicatorConversionsService.getListConversionTable(dto);
    }

    @GetMapping(value = "/v1/indicator-conversions/indicators/search", produces = MediaType.APPLICATION_JSON_VALUE)
    public TableResponseEntity<IndicatorConversionsResponse.Indicator> getListIndicatorConversion(IndicatorConversionsRequest.SearchForm dto) throws RecordNotExistsException {
        return indicatorConversionsService.getListIndicatorConversion(dto);
    }

    @GetMapping(value = "/v1/indicator-conversions/organization", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity getListOrganization(@RequestParam(required = false) Long orgTypeId,
                                              @RequestParam(required = false) Long organizationId) throws RecordNotExistsException {
        return ResponseUtils.ok(indicatorConversionsService.getListOrganization(organizationId, orgTypeId));
    }

    @GetMapping(value = "/v1/indicator-conversions/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public BaseResponseEntity<IndicatorConversionsResponse.DetailBean> getDataById(@PathVariable Long id) throws RecordNotExistsException {
        return indicatorConversionsService.getDataById(id);
    }

//    @PostMapping(value = "/v1/indicator-conversions/status", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
//    @HasPermission(scope = Scope.UPDATE)
//    public ResponseEntity updateStatusByOrg(@Valid @RequestBody IndicatorConversionsRequest.SubmitForm dto) throws BaseAppException {
//        return indicatorConversionsService.updateStatusByOrg(dto);
//    }

    @PostMapping(value = "/v1/indicator-conversions/status/{id}", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.UPDATE)
    public ResponseEntity updateStatusById(@PathVariable Long id, @Valid @RequestBody IndicatorConversionsRequest.SubmitForm dto) throws BaseAppException {
        return indicatorConversionsService.updateStatusById(id, dto);
    }

//    @DeleteMapping(value = {"/v1/indicator-conversions/orgType/{orgTypeId}/{organizationId}", "/v1/indicator-conversions/orgType/{orgTypeId}/{organizationId}/{jobId}"}, produces = MediaType.APPLICATION_JSON_VALUE)
//    @HasPermission(scope = Scope.DELETE)
//    public ResponseEntity deleteByOrg(@PathVariable Long orgTypeId, @PathVariable Long organizationId, @PathVariable(required = false) Long jobId) throws RecordNotExistsException {
//        if (jobId != null) {
//            return indicatorConversionsService.deleteByOrg(orgTypeId, organizationId, jobId);
//        } else {
//            return indicatorConversionsService.deleteByOrg(orgTypeId, organizationId, null);
//        }
//    }

    @GetMapping(value = "/v1/indicator-conversions/download-template/{indicatorMasterId}/{orgId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public ResponseEntity<Object> getTemplateImportWorkScheduleDetail(@PathVariable Long indicatorMasterId, @PathVariable Long orgId) throws Exception {
        return ResponseUtils.getResponseFileEntity(indicatorConversionsService.getTemplateIndicator(indicatorMasterId, orgId), true);
    }

    @PostMapping(value = "/v1/indicator-conversions/import/{indicatorMasterId}", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.IMPORT)
    public ResponseEntity importData(@RequestPart(value = "file") MultipartFile file, @PathVariable Long indicatorMasterId) throws Exception {
        return ResponseUtils.ok(indicatorConversionsService.importData(file, indicatorMasterId));
    }

    @GetMapping(value = "/v1/indicator-conversions/point/{indicatorConversionId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public BaseResponseEntity<Integer> getPoint(@PathVariable Long indicatorConversionId, @RequestParam String value) throws Exception {
        return ResponseUtils.ok(indicatorConversionsService.getPoint(indicatorConversionId, value));
    }

    @GetMapping(value = "/v1/indicator-conversions/export", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public ResponseEntity<Object> exportData(IndicatorConversionsRequest.SearchForm dto) throws Exception {
        return indicatorConversionsService.exportData(dto);
    }

    @GetMapping(value = "/v1/indicator-conversions/get-org-list/{employeeId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ListResponseEntity<OrgDto> getOrgList(@PathVariable Long employeeId) {
        return indicatorConversionsService.getOrgList(employeeId);
    }
}
