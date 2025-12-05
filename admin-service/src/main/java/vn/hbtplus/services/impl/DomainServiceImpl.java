package vn.hbtplus.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import vn.hbtplus.models.BaseDataTableDto;
import vn.hbtplus.models.request.DomainRequest;
import vn.hbtplus.models.response.DomainResponse;
import vn.hbtplus.repositories.impl.DomainRepository;
import vn.hbtplus.services.DomainService;

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
