package vn.kpi.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import vn.kpi.constants.BaseConstants;
import vn.kpi.models.TreeDto;
import vn.kpi.models.dto.OrgLevelDTO;
import vn.kpi.models.request.OrganizationsRequest;
import vn.kpi.models.response.ListResponseEntity;
import vn.kpi.models.response.OrganizationsResponse;
import vn.kpi.models.response.TableResponseEntity;
import vn.kpi.services.OrganizationTreeService;
import vn.kpi.utils.ResponseUtils;

import java.util.List;

@RestController
@RequestMapping(BaseConstants.REQUEST_MAPPING_PREFIX)
@RequiredArgsConstructor
public class OrganizationTreeController {
    private final OrganizationTreeService organizationTreeService;

    @GetMapping(value = "/v1/organization-tree/root-nodes", produces = MediaType.APPLICATION_JSON_VALUE)
    public ListResponseEntity<TreeDto> getResourceRootNodes(@RequestParam(value = "scope", required = false) String scope,
                                                            @RequestParam(value = "functionCode", required = false) String functionCode)  {
        return ResponseUtils.ok(organizationTreeService.getRootNodes(scope, functionCode));
    }

    @GetMapping(value = "/v1/organization-tree/children-nodes/{nodeId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ListResponseEntity<TreeDto> getResourceChildNodes(@PathVariable String nodeId)  {
        return ResponseUtils.ok(organizationTreeService.getChildrenNodes(Long.valueOf(nodeId)));
    }

    @GetMapping(value = "/v1/organization-tree/search", produces = MediaType.APPLICATION_JSON_VALUE)
    public TableResponseEntity<OrganizationsResponse.SearchTreeChooseResult> search(OrganizationsRequest.SearchForm request)  {
        return ResponseUtils.ok(organizationTreeService.search(request));
    }

    @GetMapping(value = "/v1/organization-tree/init-tree", produces = MediaType.APPLICATION_JSON_VALUE)
    public ListResponseEntity<TreeDto> initTree()  {
        return ResponseUtils.ok(organizationTreeService.initTree());
    }

    @GetMapping(value = "/v1/organization-tree/by-list-id", produces = MediaType.APPLICATION_JSON_VALUE)
    public ListResponseEntity<TreeDto> getByListNodeId(@RequestParam() List<String> listId)  {
        return ResponseUtils.ok(organizationTreeService.getByListNodeId(listId));
    }

    @GetMapping(value = "/v1/organization-tree/get-org-by-level", produces = MediaType.APPLICATION_JSON_VALUE)
    public ListResponseEntity<OrgLevelDTO> getOrgByLevel(OrgLevelDTO dto) {
        return ResponseUtils.ok(organizationTreeService.getOrgByLevel(dto));
    }
}
