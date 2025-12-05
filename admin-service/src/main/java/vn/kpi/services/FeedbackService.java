package vn.kpi.services;

import org.springframework.http.ResponseEntity;
import vn.kpi.models.BaseDataTableDto;
import vn.kpi.models.request.FeedbackRequest;
import vn.kpi.models.response.FeedbackResponse;

public interface FeedbackService {
    BaseDataTableDto<FeedbackResponse.SearchResult> searchData(FeedbackRequest.SearchForm dto);
    
    BaseDataTableDto<FeedbackResponse.SearchResult> adminSearchData(FeedbackRequest.SearchForm dto);

    boolean saveData(Long id, FeedbackRequest.SubmitForm dto);

    boolean deleteData(Long id);

    FeedbackResponse.DetailBean getDataById(Long id);
    
    FeedbackResponse.DetailBean adminGetDataById(Long id);

    boolean saveComment(Long id, FeedbackRequest.SubmitForm dto);

    ResponseEntity<Object> processFeedBack(Long id, FeedbackRequest.SubmitForm dto);

    ResponseEntity<Object> exportData(FeedbackRequest.SearchForm dto) throws Exception;
}
