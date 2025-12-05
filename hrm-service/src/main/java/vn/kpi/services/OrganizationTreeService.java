package vn.kpi.services;

import vn.kpi.models.BaseDataTableDto;
import vn.kpi.models.TreeDto;
import vn.kpi.models.dto.OrgLevelDTO;
import vn.kpi.models.request.OrganizationsRequest;

import java.util.List;

public interface OrganizationTreeService {
    List<TreeDto> getRootNodes(String scope, String functionCode);

    List<TreeDto> getChildrenNodes(Long parentId);

    BaseDataTableDto search(OrganizationsRequest.SearchForm request);

    List<TreeDto> initTree();

    List<TreeDto> getByListNodeId(List<String> listNodeId);

    List<OrgLevelDTO> getOrgByLevel(OrgLevelDTO dto);
}
