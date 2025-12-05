package vn.kpi.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.kpi.constants.BaseConstants;
import vn.kpi.models.response.ListResponseEntity;
import vn.kpi.models.response.ScopeResponse;
import vn.kpi.services.ScopeService;
import vn.kpi.utils.ResponseUtils;

@RestController
@RequestMapping(BaseConstants.REQUEST_MAPPING_PREFIX)
@RequiredArgsConstructor
public class ScopeController {
    private final ScopeService scopeService;

    @GetMapping(value = "/v1/scope/list", produces = MediaType.APPLICATION_JSON_VALUE)
    public ListResponseEntity<ScopeResponse> getScopes() {
        return ResponseUtils.ok(scopeService.getScopes());
    }
}
