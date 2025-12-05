package vn.hbtplus.insurance.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.hbtplus.annotations.HasPermission;
import vn.hbtplus.insurance.constants.Constant;
import vn.hbtplus.constants.Scope;
import vn.hbtplus.exceptions.BaseAppException;
import vn.hbtplus.exceptions.RecordNotExistsException;
import vn.hbtplus.insurance.models.request.ContributionRateRequest;
import vn.hbtplus.models.response.BaseResponseEntity;
import vn.hbtplus.insurance.models.response.ContributionRateResponse;
import vn.hbtplus.models.response.TableResponseEntity;
import vn.hbtplus.insurance.services.ContributionRateService;
import vn.hbtplus.utils.ResponseUtils;

import javax.validation.Valid;

@RestController
@RequestMapping(Constant.REQUEST_MAPPING_PREFIX)
@RequiredArgsConstructor
public class ContributionRateController {
    private final ContributionRateService contributionRateService;
    @GetMapping(value = "/v1/contribution-rate", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public TableResponseEntity<ContributionRateResponse> search(ContributionRateRequest.SearchForm request) {
        return ResponseUtils.ok(contributionRateService.search(request));
    }

    @GetMapping(value = "/v1/contribution-rate/export", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.EXPORT)
    public ResponseEntity exportData(ContributionRateRequest.SearchForm request) {
        return ResponseUtils.ok(contributionRateService.exportData(request));
    }

    @PostMapping(value = "/v1/contribution-rate", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.CREATE)
    public ResponseEntity save(@Valid ContributionRateRequest.SubmitForm request) throws BaseAppException {
        return ResponseUtils.ok(contributionRateService.saveData(request, null));
    }

    @PutMapping(value = "/v1/contribution-rate/{id}", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.UPDATE)
    public ResponseEntity update(@Valid ContributionRateRequest.SubmitForm request, @PathVariable Long id) throws BaseAppException {
        return ResponseUtils.ok(contributionRateService.saveData(request, id));
    }

    @DeleteMapping(value = "/v1/contribution-rate/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.DELETE)
    public ResponseEntity deleteById(@PathVariable Long id) throws RecordNotExistsException {
        return ResponseUtils.ok(contributionRateService.deleteById(id));
    }
    @GetMapping(value = "/v1/contribution-rate/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public BaseResponseEntity<ContributionRateResponse> getById(@PathVariable Long id) throws RecordNotExistsException {
        return ResponseUtils.ok(contributionRateService.getById(id));
    }
}
