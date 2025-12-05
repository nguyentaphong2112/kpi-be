package vn.kpi.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import vn.kpi.models.BaseDataTableDto;
import vn.kpi.models.request.DomainRequest;
import vn.kpi.models.response.DomainResponse;
import vn.kpi.repositories.impl.DomainRepository;
import vn.kpi.services.DomainService;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DomainServiceImpl implements DomainService {
    private final DomainRepository domainRepository;

    @Override
    public List<DomainResponse.DefaultDto> getDefaultList(String type) {
        return domainRepository.getCategories(type);
    }

    @Override
    public List<DomainResponse.DomainDto> getRootNodes(String type) {
        return domainRepository.getRootNodes(type);
    }

    @Override
    public List<DomainResponse.DomainDto> getChildrenNodes(String type,String parentKey) {
        return domainRepository.getChildrenNodes(type, parentKey);
    }

    @Override
    public BaseDataTableDto<DomainResponse.DomainDto> search(String type, DomainRequest.SearchForm request) {
        return domainRepository.search(type, request);
    }

    @Override
    public List<DomainResponse.DomainDto> getDomains(String type) {
        return domainRepository.getDomains(type);
    }
}
