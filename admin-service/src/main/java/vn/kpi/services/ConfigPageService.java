package vn.kpi.services;

import org.springframework.http.ResponseEntity;
import vn.kpi.exceptions.BaseAppException;
import vn.kpi.exceptions.RecordNotExistsException;
import vn.kpi.models.request.ConfigPageRequest;
import vn.kpi.models.response.BaseResponseEntity;
import vn.kpi.models.response.ConfigPageResponse;
import vn.kpi.models.response.TableResponseEntity;

public interface ConfigPageService {
    ConfigPageResponse.ExtractBean getConfigByUrl(String url);

    TableResponseEntity<ConfigPageResponse.SearchResult> searchData(ConfigPageRequest.SearchForm dto) throws BaseAppException;

    ResponseEntity saveData(ConfigPageRequest.SubmitForm dto, Long id)throws BaseAppException;

    ResponseEntity deleteData(Long id) throws RecordNotExistsException;

    BaseResponseEntity<ConfigPageResponse.Detail> getDataById(Long id) throws RecordNotExistsException;

    ResponseEntity<Object> exportData(ConfigPageRequest.SearchForm dto) throws Exception;

}
