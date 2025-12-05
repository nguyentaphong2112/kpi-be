/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import vn.hbtplus.annotations.HasPermission;
import vn.hbtplus.models.response.*;
import vn.hbtplus.models.request.OvertimeRecordsRequest;
import vn.hbtplus.services.OvertimeRecordsService;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.constants.Scope;
import vn.hbtplus.constants.Constant;
import vn.hbtplus.annotations.Resource;
import vn.hbtplus.exceptions.RecordNotExistsException;
import vn.hbtplus.exceptions.BaseAppException;
import javax.validation.Valid;

@RestController
@RequestMapping(BaseConstants.REQUEST_MAPPING_PREFIX)
@RequiredArgsConstructor
@Resource(value = Constant.RESOURCES.ABS_OVERTIME_RECORDS)
public class OvertimeRecordsController {
    private final OvertimeRecordsService overtimeRecordsService;

    @GetMapping(value = "/v1/overtime-records", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public TableResponseEntity<OvertimeRecordsResponse> searchData(OvertimeRecordsRequest.SearchForm dto) {
        return overtimeRecordsService.searchData(dto);
    }

    @PostMapping(value = "/v1/overtime-records", consumes = {MediaType.APPLICATION_JSON_VALUE})
    @HasPermission(scope = Scope.UPDATE)
    public ResponseEntity saveData(@Valid @RequestBody OvertimeRecordsRequest.SubmitForm dto) throws BaseAppException {
        return overtimeRecordsService.saveData(dto,null);
    }

    @PutMapping(value = "/v1/overtime-records/{id}", consumes = {MediaType.APPLICATION_JSON_VALUE})
    @HasPermission(scope = Scope.UPDATE)
    public ResponseEntity saveData(@Valid @RequestBody OvertimeRecordsRequest.SubmitForm dto, @PathVariable Long id) throws BaseAppException {
        return overtimeRecordsService.saveData(dto,id);
    }

    @DeleteMapping(value = "/v1/overtime-records/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.DELETE)
    public ResponseEntity deleteData(@PathVariable Long id) throws RecordNotExistsException {
        return overtimeRecordsService.deleteData(id);
    }

    @GetMapping(value = "/v1/overtime-records/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public BaseResponseEntity<OvertimeRecordsResponse> getDataById(@PathVariable Long id)  throws RecordNotExistsException {
        return overtimeRecordsService.getDataById(id);
    }

    @GetMapping(value = "/v1/overtime-records/export", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public ResponseEntity<Object> exportData(OvertimeRecordsRequest.SearchForm dto) throws Exception {
        return overtimeRecordsService.exportData(dto);
    }

    @PostMapping(value = "/v1/overtime-records/import", produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public ResponseEntity<Object> processImport(@RequestPart MultipartFile file) throws Exception {
        return overtimeRecordsService.processImport(file);
    }

    @GetMapping(value = "/v1/overtime-records/import-template", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public ResponseEntity<Object> downloadImportTemplate() throws Exception {
        return overtimeRecordsService.downloadImportTemplate();
    }

}
