package vn.hbtplus.services;

import org.springframework.http.ResponseEntity;
import vn.hbtplus.models.request.LogTaskRequest;
import vn.hbtplus.models.response.BaseResponseEntity;
import vn.hbtplus.models.response.LogTasKResponse;
import vn.hbtplus.models.response.TableResponseEntity;
import javax.validation.Valid;

public interface LogTaskService {
    TableResponseEntity<LogTasKResponse.SearchResult> searchData(LogTaskRequest.SearchForm dto);

    BaseResponseEntity<Long> saveData(LogTaskRequest.@Valid SubmitForm dto, Long logTaskId);

    BaseResponseEntity<Long> deleteData(Long id);

    BaseResponseEntity<LogTasKResponse.DetailBean> getDataById(Long id);

    ResponseEntity<Object> exportData(LogTaskRequest.SearchForm dto);
}
