/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.insurance.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.hbtplus.annotations.HasPermission;
import vn.hbtplus.annotations.Resource;
import vn.hbtplus.insurance.constants.Constant;
import vn.hbtplus.constants.Scope;
import vn.hbtplus.exceptions.BaseAppException;
import vn.hbtplus.insurance.models.request.InsuranceRetractionsRequest;
import vn.hbtplus.insurance.models.response.InsuranceRetractionsResponse;
import vn.hbtplus.models.response.TableResponseEntity;
import vn.hbtplus.insurance.services.InsuranceRetractionsService;
import vn.hbtplus.utils.ResponseUtils;
import vn.hbtplus.utils.Utils;

import java.util.List;

@RestController
@RequestMapping(Constant.REQUEST_MAPPING_PREFIX)
@RequiredArgsConstructor
@Resource(value = Constant.RESOURCE.ICN_INSURANCE_RETRACTIONS)
public class InsuranceRetractionsController {
    private final InsuranceRetractionsService insuranceRetractionsService;

    @PostMapping(value = "/v1/insurance-retractions/calculate", produces = {MediaType.APPLICATION_JSON_VALUE})
    @HasPermission(scope = Scope.CREATE)
    public ResponseEntity calculate(@RequestParam(required = false) List<String> empCodes, @RequestParam String periodDate) throws Exception {
        return ResponseUtils.ok(insuranceRetractionsService.calculate(empCodes, Utils.getLastDay(Utils.stringToDate(periodDate)), false));
    }

    @PostMapping(value = "/v1/insurance-retractions/calculate-by-list-period", produces = {MediaType.APPLICATION_JSON_VALUE})
    @HasPermission(scope = Scope.CREATE)
    public ResponseEntity calculateByListPeriod(@RequestParam(required = false) List<String> empCodes, @RequestParam List<String> listPeriodDate) throws Exception {
        return ResponseUtils.ok(insuranceRetractionsService.calculateByListPeriod(empCodes, listPeriodDate));
    }

    @GetMapping(value = "/v1/insurance-retractions", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public TableResponseEntity<InsuranceRetractionsResponse> searchData(InsuranceRetractionsRequest.SearchForm dto) {
        return insuranceRetractionsService.searchData(dto);
    }

    @DeleteMapping(value = "/v1/insurance-retractions", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.DELETE)
    public ResponseEntity deleteData(InsuranceRetractionsRequest.SearchForm dto) throws BaseAppException {
        return insuranceRetractionsService.deleteData(dto);
    }

    /**
     * tim kiem tai popup truy thu truy linh
     * @param dto tham so tim kiem
     * @return danh sach
     */
    @GetMapping(value = "/v1/insurance-retractions/retro", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public TableResponseEntity<InsuranceRetractionsResponse> searchDataPopup(InsuranceRetractionsRequest.SearchForm dto) {
        return insuranceRetractionsService.searchDataPopup(dto);
    }

//    @PostMapping(value = "/v1/insurance-retractions", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
//    @HasPermission(scope = Scope.UPDATE)
//    public ResponseEntity saveData(@Valid  InsuranceRetractionsRequest.SubmitForm dto) throws BaseAppException {
//        return insuranceRetractionsService.saveData(dto);
//    }
//
//    @DeleteMapping(value = "/v1/insurance-retractions/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
//    @HasPermission(scope = Scope.DELETE)
//    public ResponseEntity deleteData(@PathVariable Long id) throws RecordNotExistsException {
//        return insuranceRetractionsService.deleteData(id);
//    }
//
//    @GetMapping(value = "/v1/insurance-retractions/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
//    @HasPermission(scope = Scope.VIEW)
//    public BaseResponseEntity<InsuranceRetractionsResponse> getDataById(@PathVariable Long id)  throws RecordNotExistsException {
//        return insuranceRetractionsService.getDataById(id);
//    }
//
    @GetMapping(value = "/v1/insurance-retractions/export", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public ResponseEntity<Object> exportData(InsuranceRetractionsRequest.SearchForm dto) throws Exception {
        return insuranceRetractionsService.exportData(dto);
    }

}
