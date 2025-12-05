package vn.hbtplus.services;

import org.springframework.http.ResponseEntity;
import vn.hbtplus.exceptions.BaseAppException;
import vn.hbtplus.exceptions.RecordNotExistsException;
import vn.hbtplus.models.request.ConfigObjectAttributeRequest;
import vn.hbtplus.models.request.ConfigPageRequest;
import vn.hbtplus.models.response.BaseResponseEntity;
import vn.hbtplus.models.response.ConfigPageResponse;
import vn.hbtplus.models.response.TableResponseEntity;

import java.util.List;

public interface ConfigPageService {
    ConfigPageResponse.ExtractBean getConfigByUrl(String url);

    TableResponseEntity<ConfigPageResponse.SearchResult> searchData(ConfigPageRequest.SearchForm dto) throws BaseAppException;

    ResponseEntity saveData(ConfigPageRequest.SubmitForm dto, Long id)throws BaseAppException;

    ResponseEntity deleteData(Long id) throws RecordNotExistsException;

    BaseResponseEntity<ConfigPageResponse.Detail> getDataById(Long id) throws RecordNotExistsException;

    ResponseEntity<Object> exportData(ConfigPageRequest.SearchForm dto) throws Exception;

}
