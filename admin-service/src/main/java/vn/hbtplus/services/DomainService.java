package vn.hbtplus.services;

import vn.hbtplus.models.BaseDataTableDto;
import vn.hbtplus.models.request.DomainRequest;
import vn.hbtplus.models.response.DomainResponse;

import java.util.List;

public interface DomainService {
    List<DomainResponse.DefaultDto> getDefaultList(String type);

    List<DomainResponse.DomainDto> getRootNodes(String type);

    List<DomainResponse.DomainDto> getChildrenNodes(String type, String parentKey);

    BaseDataTableDto<DomainResponse.DomainDto> search(String type, DomainRequest.SearchForm request);

    List<DomainResponse.DomainDto> getDomains(String type);
}
