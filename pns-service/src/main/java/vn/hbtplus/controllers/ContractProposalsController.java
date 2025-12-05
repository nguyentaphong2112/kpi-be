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
import vn.hbtplus.models.dto.ContractProposalsDTO;
import vn.hbtplus.models.dto.ContractProposalsFormDTO;
import vn.hbtplus.models.response.BaseResponseEntity;
import vn.hbtplus.models.response.ContractProposalsResponse;
import vn.hbtplus.models.response.TableResponseEntity;
import vn.hbtplus.services.ContractProposalsService;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping(BaseConstants.REQUEST_MAPPING_PREFIX)
@RequiredArgsConstructor
public class ContractProposalsController {

    private final ContractProposalsService contractProposalsService;
//    private final MailWarningServiceImpl mailWarningService;

    @GetMapping(value = "/v1/contract-proposals", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public TableResponseEntity<ContractProposalsResponse> searchData(ContractProposalsDTO dto) {
        return contractProposalsService.searchData(dto);
    }

    @PostMapping(value = "/v1/contract-proposals", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.CREATE)
    public ResponseEntity<Object> saveData(@RequestBody ContractProposalsFormDTO dto
    ) {
        return contractProposalsService.saveData(dto);
    }

    @GetMapping(value = "/v1/contract-proposals/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public ResponseEntity<Object> getDataById(@PathVariable Long id) {
        return contractProposalsService.getDataById(id);
    }

    @GetMapping(value = "/v1/contract-proposals/{id}/export-contract", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public ResponseEntity<Object> exportContract(@PathVariable Long id) throws Exception {
        return contractProposalsService.exportContract(id);
    }

    @GetMapping(value = "/v1/contract-proposals/export-contract", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public ResponseEntity<Object> exportContractByForm(ContractProposalsDTO dto) throws Exception {
        return contractProposalsService.exportContractByForm(dto);
    }

    @GetMapping(value = "/v1/contract-proposals/export-contract-by-ids", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public ResponseEntity<Object> exportContractByListId(@RequestParam List<Long> listId) throws Exception {
        return contractProposalsService.exportContractByListId(listId);
    }

    @GetMapping(value = "/v1/contract-proposals/export", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public ResponseEntity<Object> exportData(ContractProposalsDTO dto) {
        return contractProposalsService.exportData(dto);
    }

    @PostMapping(value = "/v1/contract-proposals/upload-file-signed", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public ResponseEntity<Object> uploadFileSigned(@RequestPart(value = "files", required = false) List<MultipartFile> files,
            @RequestPart(value = "type") Integer type) throws IOException {
        return contractProposalsService.uploadFileSigned(files, type);
    }

    @PostMapping(value = "/v1/contract-proposals/update-status-file-signed", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.UPDATE)
    public ResponseEntity<Object> updateStatusFileSigned(@RequestBody List<ContractProposalsDTO> listDTO) {
        return contractProposalsService.updateStatusFileSigned(listDTO);
    }

    @DeleteMapping(value = "/v1/contract-proposals/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.DELETE)
    public ResponseEntity<Object> deleteDataById(@PathVariable Long id) {
        return contractProposalsService.deleteDataById(id);
    }

    @DeleteMapping(value = "/v1/contract-proposals/delete-by-form", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.DELETE)
    public ResponseEntity<Object> deleteByForm(ContractProposalsDTO dto) {
        return contractProposalsService.deleteByForm(dto);
    }

    @PostMapping(value = "/v1/contract-proposals/send-mail-by-list", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public ResponseEntity<Object> sendMailByList(@RequestBody ContractProposalsDTO dto) throws Exception {
        return contractProposalsService.sendMailByList(dto);
    }
	
	@PostMapping(value = "/v1/contract-proposals/send-mail-all", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public ResponseEntity<Object> sendMailAll(ContractProposalsDTO dto) throws Exception {
        return contractProposalsService.sendMailAll(dto);
    }

	@GetMapping(value = "/v1/contract-proposals/contract-fee/import-template", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public ResponseEntity<Object> importTemplateContractFee() throws Exception {
        return contractProposalsService.importTemplateContractFee();
    }

    @PostMapping(value = "/v1/contract-proposals/contract-fee/import-process", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.UPDATE)
    public ResponseEntity<Object> importProcessContractFee(HttpServletRequest req, @RequestPart(value = "fileImport", required = false) MultipartFile fileImport) throws IOException {
        return contractProposalsService.importProcessContractFee(fileImport, req);
    }

    @GetMapping(value = "/v1/contract-proposals/suggest-contract", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public ResponseEntity<Object> suggestContract(ContractProposalsDTO dto) throws Exception {
//
//        List<Integer> listDuration = new ArrayList<>();
//        if(!Utils.isNullOrEmpty(dto.getDurations())){
//            String[] arr = dto.getDurations().split(",");
//            for (String duration : arr){
//                if(!Utils.isNullOrEmpty(duration)){
//                    listDuration.add(Integer.valueOf(duration.trim()));
//                }
//            }
//        }

        if(dto.getType() == 1){
            contractProposalsService.autoFindNewContract();
        } else if(dto.getType() == 2){
            contractProposalsService.autoFindExpiredContract(dto.getFromDate(), dto.getToDate());
        } else if(dto.getType() == 3) {
            contractProposalsService.autoFindAppendixContract();
        }
//        else if(dto.getType() == 4) {
//            mailWarningService.sendManagerWarningBeforeExpiredContract(listDuration);
//        } else if(dto.getType() == 5) {
//            mailWarningService.sendManagerWarningAfterExpiredContract(listDuration);
//        } else if(dto.getType() == 6) {
//            mailWarningService.sendEmpWarningBeforeExpiredContract(listDuration);
//        } else if(dto.getType() == 7) {
//            mailWarningService.sendHRWarningBeforeExpiredContract(listDuration);
//        } else if(dto.getType() == 8) {
//            mailWarningService.sendHRWarningAfterExpiredContract(listDuration);
//        } else if(dto.getType() == 9) {
//            mailWarningService.sendHRNewContract(listDuration);
//        } else if(dto.getType() == 10) {
//            mailWarningService.sendEmpNewContract(listDuration);
//        } else if(dto.getType() == 11) {
//            mailWarningService.sendHRAppendixContract(listDuration);
//        } else if(dto.getType() == 12) {
//            mailWarningService.sendEmpAppendixContract(listDuration);
//        }
        return BaseResponseEntity.ok(null);
    }
}
