package vn.kpi.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.kpi.constants.BaseConstants;
import vn.kpi.models.request.DomainRequest;
import vn.kpi.models.response.DomainResponse;
import vn.kpi.models.response.ListResponseEntity;
import vn.kpi.models.response.TableResponseEntity;
import vn.kpi.services.DomainService;
import vn.kpi.utils.ResponseUtils;

@RestController
@RequestMapping(BaseConstants.REQUEST_MAPPING_PREFIX)
@RequiredArgsConstructor
public class DomainController {
    private final DomainService domainService;

    @GetMapping(value = "v1/domain/default-list/{type}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ListResponseEntity<DomainResponse.DefaultDto> getDefaultList(@PathVariable String type) {
        return ResponseUtils.ok(domainService.getDefaultList(type));
    }

    @GetMapping(value = "v1/domain/root-nodes/{type}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ListResponseEntity<DomainResponse.DomainDto> getRootNodes(@PathVariable String type) {
        return ResponseUtils.ok(domainService.getRootNodes(type));
    }

    @GetMapping(value = "v1/domain/children-nodes/{type}/{parentKey}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ListResponseEntity<DomainResponse.DomainDto> getChildrenNodes(@PathVariable String type, @PathVariable String parentKey) {
        return ResponseUtils.ok(domainService.getChildrenNodes(type, parentKey));
    }

    @GetMapping(value = "v1/domain/search/{type}", produces = MediaType.APPLICATION_JSON_VALUE)
    public TableResponseEntity<DomainResponse.DomainDto> search(@PathVariable String type, DomainRequest.SearchForm request) {
        return ResponseUtils.ok(domainService.search(type, request));
    }

    @GetMapping(value = "v1/domain/list/{type}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ListResponseEntity<DomainResponse.DomainDto> getListDomains(@PathVariable String type) {
        return ResponseUtils.ok(domainService.getDomains(type));
    }
}
