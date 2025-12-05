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
import vn.hbtplus.annotations.Resource;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.constants.Constant;
import vn.hbtplus.constants.Scope;
import vn.hbtplus.models.dto.ContractTemplatesDTO;
import vn.hbtplus.models.response.BaseResponseEntity;
import vn.hbtplus.models.response.ContractTemplatesResponse;
import vn.hbtplus.models.response.ListResponseEntity;
import vn.hbtplus.models.response.TableResponseEntity;
import vn.hbtplus.repositories.entity.HrContractTypesEntity;
import vn.hbtplus.services.ContractTemplatesService;

import vn.hbtplus.utils.Utils;

import javax.validation.Valid;
import java.io.IOException;

@RestController
@RequestMapping(BaseConstants.REQUEST_MAPPING_PREFIX)
@RequiredArgsConstructor
@Resource(value= Constant.RESOURCE.PNS_CONTRACT_TEMPLATES)
public class ContractTemplatesController {
    private final ContractTemplatesService contractTemplatesService;

    @GetMapping(value = "/v1/contract-templates", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public TableResponseEntity<ContractTemplatesResponse> searchData(ContractTemplatesDTO dto) {
        return contractTemplatesService.searchData(dto);
    }

    @PostMapping(value = "/v1/contract-templates", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.CREATE)
    public BaseResponseEntity<Object> saveData(@RequestPart(value = "file", required = false) MultipartFile file,
                                               @Valid @RequestPart(value = "data") ContractTemplatesDTO dto) throws IOException {
        String userNameLogin = Utils.getUserNameLogin();
        dto.setFileTemplate(file);
        return contractTemplatesService.saveData(dto, userNameLogin);
    }

    @DeleteMapping(value = "/v1/contract-templates/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.DELETE)
    public BaseResponseEntity<Object> deleteData(@PathVariable Long id) {
        return contractTemplatesService.deleteData(id);
    }

    @GetMapping(value = "/v1/contract-templates/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public BaseResponseEntity<Object> getDataById(@PathVariable Long id) {
        return contractTemplatesService.getDataById(id);
    }

    @GetMapping(value = "/v1/contract-templates/type", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public ListResponseEntity<HrContractTypesEntity> getContractTemplatesType() {
        return contractTemplatesService.getContractTypes();
    }

    @GetMapping(value = "/v1/contract-templates/file/{contractTemplateId}")
    @HasPermission(scope = Scope.VIEW)
    public byte[] downloadPnsContractTemplates(@PathVariable Long contractTemplateId) {
        return contractTemplatesService.downloadContractTemplates(contractTemplateId);
    }

    @GetMapping(value = "/v1/contract-templates/export", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public ResponseEntity<Object> exportData(ContractTemplatesDTO dto) throws Exception {
        return contractTemplatesService.exportData(dto);
    }

}

