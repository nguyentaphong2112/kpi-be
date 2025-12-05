/*
 * Copyright (C) 2022 HRPLUS. All rights reserved.
 * EcoIT. Use is subject to license terms.
 */
package vn.hbtplus.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import vn.hbtplus.annotations.HasPermission;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.constants.Scope;
import vn.hbtplus.models.dto.ConfigApprovalsDTO;
import vn.hbtplus.models.response.BaseResponseEntity;
import vn.hbtplus.models.response.ConfigApprovalsResponse;
import vn.hbtplus.models.response.TableResponseEntity;
import vn.hbtplus.services.ConfigApprovalsService;

import javax.validation.Valid;
import java.security.SignatureException;

@RestController
@RequestMapping(BaseConstants.REQUEST_MAPPING_PREFIX)
@RequiredArgsConstructor
public class ConfigApprovalsController {
    private final ConfigApprovalsService configApprovalsService;

    @GetMapping(value = "/v1/config-approvals", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public TableResponseEntity<ConfigApprovalsResponse> searchData(ConfigApprovalsDTO dto) {
        return configApprovalsService.searchData(dto);
    }

    @PostMapping(value = "/v1/config-approvals", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.UPDATE)
    public BaseResponseEntity<Object> saveData(
           @RequestPart(value = "file", required = false) MultipartFile file,
           @Valid @RequestPart(value = "data") ConfigApprovalsDTO dto) throws SignatureException {
        return configApprovalsService.saveData(dto, file);
    }

    @DeleteMapping(value = "/v1/config-approvals/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.DELETE)
    public BaseResponseEntity<Object> deleteData(@PathVariable Long id) {
        return configApprovalsService.deleteData(id);
    }

    @GetMapping(value = "/v1/config-approvals/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public BaseResponseEntity<Object> getDataById(@PathVariable Long id) throws SignatureException {
        return configApprovalsService.getDataById(id);
    }

    @GetMapping(value = "/v1/config-approvals/export", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public ResponseEntity<Object> exportData(ConfigApprovalsDTO dto) throws Exception {
        return configApprovalsService.exportData(dto);
    }

}

