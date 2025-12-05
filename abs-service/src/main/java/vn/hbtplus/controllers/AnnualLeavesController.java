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
import vn.hbtplus.models.response.*;
import vn.hbtplus.models.request.AnnualLeavesRequest;
import vn.hbtplus.services.AnnualLeavesService;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.constants.Scope;
import vn.hbtplus.constants.Constant;
import vn.hbtplus.annotations.Resource;
import vn.hbtplus.exceptions.RecordNotExistsException;
import vn.hbtplus.exceptions.BaseAppException;
import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping(BaseConstants.REQUEST_MAPPING_PREFIX)
@RequiredArgsConstructor
@Resource(value = Constant.RESOURCES.ABS_ANNUAL_LEAVES)
public class AnnualLeavesController {
    private final AnnualLeavesService annualLeavesService;

    @GetMapping(value = "/v1/annual-leaves", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public TableResponseEntity<AnnualLeavesResponse.SearchResult> searchData(AnnualLeavesRequest.SearchForm dto) {
        return annualLeavesService.searchData(dto);
    }

    @PostMapping(value = "/v1/annual-leaves", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.UPDATE)
    public ResponseEntity saveData(@Valid @RequestBody AnnualLeavesRequest.SubmitForm dto) throws BaseAppException {
        return annualLeavesService.saveData(dto);
    }

    @PostMapping(value = "/v1/annual-leaves/calculate/{year}", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.UPDATE)
    public ResponseEntity calculate(@Valid @RequestBody AnnualLeavesRequest.CalculateForm dto, @PathVariable Integer year) throws BaseAppException {
        List<Long> listEmpIds = new ArrayList<>();
        return annualLeavesService.calculate(year, listEmpIds);
    }

    @DeleteMapping(value = "/v1/annual-leaves/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.DELETE)
    public ResponseEntity deleteData(@PathVariable Long id) throws RecordNotExistsException {
        return annualLeavesService.deleteData(id);
    }

    @GetMapping(value = "/v1/annual-leaves/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public BaseResponseEntity<AnnualLeavesResponse> getDataById(@PathVariable Long id)  throws RecordNotExistsException {
        return annualLeavesService.getDataById(id);
    }

    @GetMapping(value = "/v1/annual-leaves/export", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public ResponseEntity<Object> exportData(AnnualLeavesRequest.SearchForm dto) throws Exception {
        return annualLeavesService.exportData(dto);
    }

}
