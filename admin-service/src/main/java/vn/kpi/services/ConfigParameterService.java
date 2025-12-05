package vn.kpi.services;


import org.springframework.http.ResponseEntity;
import vn.kpi.exceptions.BaseAppException;
import vn.kpi.exceptions.RecordNotExistsException;
import vn.kpi.models.BaseDataTableDto;
import vn.kpi.models.request.ConfigParameterRequest;
import vn.kpi.models.request.ParameterRequest;
import vn.kpi.models.response.ConfigParameterResponse;
import vn.kpi.models.response.ParameterResponse;

import java.util.Date;
import java.util.List;
import java.util.Map;

public interface ConfigParameterService {
    BaseDataTableDto<ParameterResponse.SearchResult> search(ParameterRequest.SearchForm request, String configGroup);

    Object exportData(ParameterRequest.SearchForm request, String configGroup);

    Object saveData(ParameterRequest.SubmitForm request, String configGroup, Date key) throws BaseAppException;

    Object getById(Long id);

    List<ConfigParameterResponse> getConfigGroups(String moduleCode);

    ResponseEntity updateConfigGroup(ConfigParameterRequest.SubmitForm config) throws BaseAppException;

    Object deleteById(String configGroup, Date startDate);

    ParameterResponse.SearchResult getById(String configGroup, Date startDate) throws RecordNotExistsException;

    Map<String, String> getParameters(List<String> configCodes);

    List<ConfigParameterResponse> getListConfigByCodes(String attributeValue);



}
