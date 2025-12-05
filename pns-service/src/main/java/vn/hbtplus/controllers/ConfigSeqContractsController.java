/*
 * Copyright (C) 2022 HRPLUS. All rights reserved.
 * EcoIT. Use is subject to license terms.
 */
package vn.hbtplus.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.hbtplus.annotations.HasPermission;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.constants.Scope;
import vn.hbtplus.models.dto.ConfigSeqContractsDTO;
import vn.hbtplus.models.response.BaseResponseEntity;
import vn.hbtplus.models.response.ConfigSeqContractsResponse;
import vn.hbtplus.models.response.TableResponseEntity;
import vn.hbtplus.services.ConfigSeqContractsService;

import javax.validation.Valid;

@RestController
@RequestMapping(BaseConstants.REQUEST_MAPPING_PREFIX)
@RequiredArgsConstructor
public class ConfigSeqContractsController {
    private final ConfigSeqContractsService configSeqContractsService;

    @GetMapping(value = "/v1/config-seq-contracts", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public TableResponseEntity<ConfigSeqContractsResponse> searchData(ConfigSeqContractsDTO dto) {
        return configSeqContractsService.searchData(dto);
    }

    @PostMapping(value = "/v1/config-seq-contracts", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.UPDATE)
    public BaseResponseEntity<Object> saveData(@Valid @RequestBody ConfigSeqContractsDTO dto) {
        return configSeqContractsService.saveData(dto);
    }

    @DeleteMapping(value = "/v1/config-seq-contracts/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.DELETE)
    public BaseResponseEntity<Object> deleteData(@PathVariable Long id) {
        return configSeqContractsService.deleteData(id);
    }

    @GetMapping(value = "/v1/config-seq-contracts/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public BaseResponseEntity<Object> getDataById(@PathVariable Long id) {
        return configSeqContractsService.getDataById(id);
    }

    @GetMapping(value = "/v1/config-seq-contracts/export", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public ResponseEntity<Object> exportData(ConfigSeqContractsDTO dto) throws Exception {
        return configSeqContractsService.exportData(dto);
    }

}
