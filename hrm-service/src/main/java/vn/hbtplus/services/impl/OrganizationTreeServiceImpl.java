package vn.hbtplus.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import vn.hbtplus.models.BaseDataTableDto;
import vn.hbtplus.models.BaseSearchRequest;
import vn.hbtplus.models.TreeDto;
import vn.hbtplus.models.dto.OrgLevelDTO;
import vn.hbtplus.models.request.OrganizationsRequest;
import vn.hbtplus.models.response.OrganizationsResponse;
import vn.hbtplus.repositories.impl.OrganizationTreeRepository;
import vn.hbtplus.services.OrganizationTreeService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class OrganizationTreeServiceImpl implements OrganizationTreeService {
    private final OrganizationTreeRepository organizationTreeRepository;

    @Override
    public List<TreeDto> getRootNodes(String scope, String functionCode) {
        return organizationTreeRepository.getRootNodes(scope, functionCode);
    }

    @Override
    public List<TreeDto> getChildrenNodes(Long parentId) {
        return organizationTreeRepository.getChildrenNodes(parentId);
    }

    @Override
    public BaseDataTableDto search(OrganizationsRequest.SearchForm request) {
        return organizationTreeRepository.search(request);
    }

    @Override
    public List<TreeDto> initTree() {
        List<TreeDto> lstMenu = organizationTreeRepository.getAllOrganizations();
        List<TreeDto> results = new ArrayList<>();
        Map<String, TreeDto> mapOrganizations = new HashMap<>();
        lstMenu.stream().forEach(item -> {
            mapOrganizations.put(item.getNodeId(), item);
        });
        lstMenu.stream().forEach(item -> {
            if (item.getParentId() == null || mapOrganizations.get(item.getParentId()) == null) {
                results.add(item);
            } else {
                TreeDto parent = mapOrganizations.get(item.getParentId());
                parent.addChild(item);
            }
        });

        return results;
    }

    @Override
    public List<TreeDto> getByListNodeId(List<String> listNodeId) {
        return organizationTreeRepository.getByListNodeId(listNodeId);
    }

    @Override
    public List<OrgLevelDTO> getOrgByLevel(OrgLevelDTO dto) {
        return organizationTreeRepository.getOrgByLevel(dto);
    }
}
