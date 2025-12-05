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
import vn.hbtplus.models.request.SalaryRanksRequest;
import vn.hbtplus.models.response.BaseResponseEntity;
import vn.hbtplus.models.response.ListResponseEntity;
import vn.hbtplus.models.response.SalaryRanksResponse;
import vn.hbtplus.models.response.TableResponseEntity;
import vn.hbtplus.services.SalaryRanksService;
import vn.hbtplus.utils.ResponseUtils;

import javax.validation.Valid;
import java.sql.Date;
import java.util.List;

@RestController
@RequestMapping(BaseConstants.REQUEST_MAPPING_PREFIX)
@RequiredArgsConstructor
@Resource(value = Constant.RESOURCES.SALARY_RANKS)
public class SalaryRanksController {
    private final SalaryRanksService salaryRanksService;

    @GetMapping(value = "/v1/salary-ranks", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public TableResponseEntity<SalaryRanksResponse.SearchResult> searchData(SalaryRanksRequest.SearchForm dto) {
        return salaryRanksService.searchData(dto);
    }

    @PostMapping(value = "/v1/salary-ranks", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.CREATE)
    public ResponseEntity saveData(@Valid @RequestBody SalaryRanksRequest.SubmitForm dto) throws BaseAppException {
        return salaryRanksService.saveData(dto, null);
    }

    @PutMapping(value = "/v1/salary-ranks/{id}", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.UPDATE)
    public ResponseEntity updateData(@Valid @RequestBody SalaryRanksRequest.SubmitForm dto, @PathVariable Long id) throws BaseAppException {
        return salaryRanksService.saveData(dto, id);
    }

    @DeleteMapping(value = "/v1/salary-ranks/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.DELETE)
    public ResponseEntity deleteData(@PathVariable Long id) throws RecordNotExistsException {
        return salaryRanksService.deleteData(id);
    }

    @GetMapping(value = "/v1/salary-ranks/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public BaseResponseEntity<SalaryRanksResponse.DetailBean> getDataById(@PathVariable Long id) throws RecordNotExistsException {
        return salaryRanksService.getDataById(id);
    }

    @GetMapping(value = "/v1/salary-ranks/list/{salaryType}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ListResponseEntity<SalaryRanksResponse> getSalaryRanks(@PathVariable String salaryType,
                                                                  @RequestParam(required = false) Date startDate,
                                                                  @RequestParam(required = false) Long empTypeId,
                                                                  @RequestParam(required = false) boolean isGetAttributes) throws RecordNotExistsException {
        return ResponseUtils.ok(salaryRanksService.getSalaryRanks(salaryType, startDate, empTypeId, isGetAttributes));
    }

    @GetMapping(value = "/v1/salary-ranks/by-list-type", produces = MediaType.APPLICATION_JSON_VALUE)
    public ListResponseEntity<SalaryRanksResponse> getSalaryRanksByListType(@RequestParam List<String> listSalaryType,
                                                                            @RequestParam(required = false) boolean isGetAttributes) throws RecordNotExistsException {
        return ResponseUtils.ok(salaryRanksService.getSalaryRanksByListType(listSalaryType, isGetAttributes));
    }

    @GetMapping(value = "/v1/salary-ranks/grades/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ListResponseEntity<SalaryRanksResponse.SalaryGradeDto> getSalaryRanks(@PathVariable Long id) throws RecordNotExistsException {
        return ResponseUtils.ok(salaryRanksService.getSalaryGrades(id));
    }

    @GetMapping(value = "/v1/salary-ranks/export", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public ResponseEntity<Object> exportData(SalaryRanksRequest.SearchForm dto) throws Exception {
        return salaryRanksService.exportData(dto);
    }
}
