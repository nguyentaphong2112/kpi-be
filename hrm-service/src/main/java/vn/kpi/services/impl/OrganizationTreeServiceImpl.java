package vn.kpi.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import vn.kpi.models.BaseDataTableDto;
import vn.kpi.models.TreeDto;
import vn.kpi.models.dto.OrgLevelDTO;
import vn.kpi.models.request.OrganizationsRequest;
import vn.kpi.repositories.impl.OrganizationTreeRepository;
import vn.kpi.services.OrganizationTreeService;

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
