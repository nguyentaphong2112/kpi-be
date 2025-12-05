package vn.hbtplus.tax.income.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import vn.hbtplus.annotations.HasPermission;
import vn.hbtplus.annotations.Resource;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.constants.Scope;
import vn.hbtplus.models.response.BaseResponseEntity;
import vn.hbtplus.models.response.TableResponseEntity;
import vn.hbtplus.tax.income.constants.Constant;
import vn.hbtplus.tax.income.models.request.TaxCommitmentRequest;
import vn.hbtplus.tax.income.models.response.TaxCommitmentResponse;
import vn.hbtplus.tax.income.services.TaxCommitmentService;
import vn.hbtplus.utils.ResponseUtils;

import javax.validation.Valid;

@RestController
@RequestMapping(Constant.REQUEST_MAPPING_PREFIX)
@RequiredArgsConstructor
@Resource(value = Constant.RESOURCE.PIT_TAX_COMMITMENTS)
public class TaxCommitmentController {

    private final TaxCommitmentService taxCommitmentService;

    @GetMapping(value = "/v1/tax-commitment", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public TableResponseEntity<TaxCommitmentResponse> searchData(TaxCommitmentRequest.SearchForm dto) {
        return ResponseUtils.ok(taxCommitmentService.searchData(dto));
    }

    @DeleteMapping(value = "/v1/tax-commitment/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public BaseResponseEntity<Long> deleteById(@PathVariable Long id) throws Exception {
        return ResponseUtils.ok(taxCommitmentService.deleteById(id));
    }

    @PostMapping(value = "/v1/tax-commitment", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.CREATE)
    public BaseResponseEntity<Long> saveTaxCommitment(@ModelAttribute @Valid TaxCommitmentRequest.UpdateForm form) throws Exception {
        return ResponseUtils.ok(taxCommitmentService.saveTaxCommitment(form));
    }

    @PutMapping(value = "/v1/tax-commitment/{id}", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.UPDATE)
    public BaseResponseEntity<Long> updateTaxCommitment(@ModelAttribute @Valid TaxCommitmentRequest.UpdateForm form) throws Exception {
        return ResponseUtils.ok(taxCommitmentService.saveTaxCommitment(form));
    }

    @GetMapping(value = "/v1/tax-commitment/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public BaseResponseEntity<TaxCommitmentResponse> getTaxCommitmentById(@PathVariable Long id) throws Exception {
        return ResponseUtils.ok(taxCommitmentService.getTaxCommitmentById(id));
    }

    @PostMapping(value = "/v1/tax-commitment/import", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.IMPORT)
    public ResponseEntity importTaxCommitment(@RequestPart MultipartFile file) throws Exception {
        taxCommitmentService.importTaxCommitment(file);
        return ResponseUtils.ok();
    }

    @GetMapping(value = "/v1/tax-commitment/export", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public ResponseEntity<Object> exportData(TaxCommitmentRequest.SearchForm dto) throws Exception {
        return taxCommitmentService.exportData(dto);
    }

    @GetMapping(value = "/v1/tax-commitment/download-template", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public ResponseEntity<Object> downloadTemplate() throws Exception {
        return taxCommitmentService.downloadTemplate();
    }
}
