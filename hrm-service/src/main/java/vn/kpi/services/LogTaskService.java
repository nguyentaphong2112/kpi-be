package vn.kpi.services;

import org.springframework.http.ResponseEntity;
import vn.kpi.models.request.LogTaskRequest;
import vn.kpi.models.response.BaseResponseEntity;
import vn.kpi.models.response.LogTasKResponse;
import vn.kpi.models.response.TableResponseEntity;
import javax.validation.Valid;

public interface LogTaskService {
    TableResponseEntity<LogTasKResponse.SearchResult> searchData(LogTaskRequest.SearchForm dto);

    BaseResponseEntity<Long> saveData(LogTaskRequest.@Valid SubmitForm dto, Long logTaskId);

    BaseResponseEntity<Long> deleteData(Long id);

    BaseResponseEntity<LogTasKResponse.DetailBean> getDataById(Long id);

    ResponseEntity<Object> exportData(LogTaskRequest.SearchForm dto);
}
