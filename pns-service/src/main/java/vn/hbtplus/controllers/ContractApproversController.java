/*
 * Copyright (C) 2022 HRPLUS. All rights reserved.
 * EcoIT. Use is subject to license terms.
 */
package vn.hbtplus.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import vn.hbtplus.annotations.HasPermission;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.constants.Scope;
import vn.hbtplus.models.dto.ContractApproversDTO;
import vn.hbtplus.models.dto.ContractEvaluationsDTO;
import vn.hbtplus.models.dto.RejectDTO;
import vn.hbtplus.models.response.BaseResponseEntity;
import vn.hbtplus.models.response.ContractApproversResponse;
import vn.hbtplus.models.response.TableResponseEntity;
import vn.hbtplus.services.CommonUtilsService;
import vn.hbtplus.services.ContractApproversService;

import javax.validation.Valid;
import java.security.SignatureException;
import java.util.List;

@RestController
@RequestMapping(BaseConstants.REQUEST_MAPPING_PREFIX)
@RequiredArgsConstructor
public class ContractApproversController {
    private final ContractApproversService contractApproversService;
    private final CommonUtilsService commonUtilsService;

    @GetMapping(value = "/v1/contract-approvers", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public TableResponseEntity<ContractApproversResponse> searchData(ContractApproversDTO dto) {
        dto.setEmployeeId(commonUtilsService.getEmpIdLogin());
        return contractApproversService.searchData(dto);
    }

    @PostMapping(value = "/v1/contract-approvers", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.UPDATE)
    public BaseResponseEntity<Object> saveData(
           @RequestPart(value = "files", required = false) List<MultipartFile> files,
           @Valid @RequestPart(value = "data") ContractApproversDTO dto
    ) {
        return contractApproversService.saveData(dto, files);
    }

    @GetMapping(value = "/v1/contract-approvers/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public BaseResponseEntity<Object> getDataById(@PathVariable Long id) throws SignatureException {
        return contractApproversService.getDataById(id);
    }

    @PostMapping(value = "/v1/contract-approvers/keep-signing", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.APPROVE)
    public BaseResponseEntity<Object> keepSigningByList(@RequestParam List<Long> listId) {
        return contractApproversService.keepSigningByList(listId);
    }

    @PostMapping(value = "/v1/contract-approvers/keep-signing-all", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.APPROVE)
    public BaseResponseEntity<Object> keepSigningAll(@RequestBody ContractApproversDTO dto) {
        return contractApproversService.keepSigningAll(dto);
    }

    @GetMapping(value = "/v1/contract-approvers/count-approval-record", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.APPROVE)
    public BaseResponseEntity<Object> countValidRecord(ContractApproversDTO dto) {
        return contractApproversService.countValidRecord(dto);
    }

    @PostMapping(value = "/v1/contract-approvers/liquidation-by-list", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.APPROVE)
    public BaseResponseEntity<Object> liquidationByList(@RequestBody RejectDTO dto) {
        return contractApproversService.liquidationByList(dto);
    }

    @PostMapping(value = "/v1/contract-approvers/save-evaluation", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.UPDATE)
    public BaseResponseEntity<Object> saveAssess(
            @RequestPart(value = "files", required = false) List<MultipartFile> files,
            @Valid @RequestPart(value = "data") ContractEvaluationsDTO dto) {
        return contractApproversService.saveEvaluation(dto, files);
    }

}

