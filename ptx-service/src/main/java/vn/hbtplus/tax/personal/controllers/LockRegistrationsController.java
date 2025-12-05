/*
 * Copyright (C) 2022 HRPLUS. All rights reserved.
 * EcoIT. Use is subject to license terms.
 */
package vn.hbtplus.tax.personal.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import vn.hbtplus.annotations.HasPermission;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.constants.Scope;
import vn.hbtplus.tax.personal.constants.Constant;
import vn.hbtplus.tax.personal.models.request.LockRegistrationsDTO;
import vn.hbtplus.models.response.BaseResponseEntity;
import vn.hbtplus.models.response.ListResponseEntity;
import vn.hbtplus.tax.personal.models.response.LockRegistrationsResponse;
import vn.hbtplus.tax.personal.services.LockRegistrationsService;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping(Constant.REQ_EMP_MAPPING_PREFIX)
@RequiredArgsConstructor
public class LockRegistrationsController {
    private final LockRegistrationsService lockRegistrationsService;

    @GetMapping(value = "/v1/lock-registrations", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = vn.hbtplus.constants.Scope.VIEW)
    public ListResponseEntity<LockRegistrationsResponse> searchData(LockRegistrationsDTO dto) {
        return lockRegistrationsService.searchData(dto);
    }

    @PostMapping(value = "/v1/lock-registrations", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.UPDATE)
    public ResponseEntity<Object> saveData(
            @RequestPart(value = "files", required = false) List<MultipartFile> files,
            @Valid @RequestPart(value = "data") LockRegistrationsDTO dto
    ) {
        return lockRegistrationsService.saveData(dto, files);
    }

    @DeleteMapping(value = "/v1/lock-registrations/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.DELETE)
    public ResponseEntity<Object> deleteData(@PathVariable Long id) {
        return lockRegistrationsService.deleteData(id);
    }

    @GetMapping(value = "/v1`/lock-registrations/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public BaseResponseEntity<LockRegistrationsResponse> getDataById(@PathVariable Long id) {
        return lockRegistrationsService.getDataById(id);
    }

}

