package vn.hbtplus.services;

import org.springframework.http.ResponseEntity;
import vn.hbtplus.models.BaseDataTableDto;
import vn.hbtplus.models.BaseSearchRequest;
import vn.hbtplus.models.TreeDto;
import vn.hbtplus.models.dto.OrgLevelDTO;
import vn.hbtplus.models.request.OrganizationsRequest;
import vn.hbtplus.models.response.OrganizationsResponse;

import java.util.List;

public interface OrganizationTreeService {
    List<TreeDto> getRootNodes(String scope, String functionCode);

    List<TreeDto> getChildrenNodes(Long parentId);

    BaseDataTableDto search(OrganizationsRequest.SearchForm request);

    List<TreeDto> initTree();

    List<TreeDto> getByListNodeId(List<String> listNodeId);

    List<OrgLevelDTO> getOrgByLevel(OrgLevelDTO dto);
}
