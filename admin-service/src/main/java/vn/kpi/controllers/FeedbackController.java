package vn.kpi.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.kpi.annotations.HasPermission;
import vn.kpi.constants.BaseConstants;
import vn.kpi.constants.Scope;
import vn.kpi.exceptions.BaseAppException;
import vn.kpi.exceptions.RecordNotExistsException;
import vn.kpi.models.request.FeedbackRequest;
import vn.kpi.models.response.BaseResponseEntity;
import vn.kpi.models.response.FeedbackResponse;
import vn.kpi.models.response.TableResponseEntity;
import vn.kpi.services.FeedbackService;
import vn.kpi.utils.ResponseUtils;

import javax.validation.Valid;

@RestController
@RequiredArgsConstructor
@RequestMapping(BaseConstants.REQUEST_MAPPING_PREFIX)
public class FeedbackController {
    private final FeedbackService feedbackService;

    @GetMapping(value = "/v1/user/feedbacks", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public TableResponseEntity<FeedbackResponse.SearchResult> searchData(FeedbackRequest.SearchForm dto) {
        return ResponseUtils.ok(feedbackService.searchData(dto));
    }

    @PostMapping(value = "/v1/user/feedbacks", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.CREATE)
    public ResponseEntity saveData(@ModelAttribute @Valid FeedbackRequest.SubmitForm dto) throws BaseAppException {
        return ResponseUtils.ok(feedbackService.saveData(null, dto));
    }

    @PostMapping(value = "/v1/user/feedbacks/comments/{id}", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.CREATE)
    public ResponseEntity saveComment(@ModelAttribute @Valid FeedbackRequest.SubmitForm dto, @PathVariable Long id) throws BaseAppException {
        return ResponseUtils.ok(feedbackService.saveComment(id, dto));
    }

    @PutMapping(value = "/v1/user/feedbacks/{id}", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.UPDATE)
    public ResponseEntity updateData(@ModelAttribute @Valid FeedbackRequest.SubmitForm dto, @PathVariable Long id) throws BaseAppException {
        return ResponseUtils.ok(feedbackService.saveData(id, dto));
    }

    @DeleteMapping(value = "/v1/user/feedbacks/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.DELETE)
    public ResponseEntity deleteData(@PathVariable Long id) throws RecordNotExistsException {
        return ResponseUtils.ok(feedbackService.deleteData(id));
    }

    @GetMapping(value = "/v1/user/feedbacks/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public BaseResponseEntity<FeedbackResponse.DetailBean> getDataById(@PathVariable Long id) throws RecordNotExistsException {

        return ResponseUtils.ok(feedbackService.getDataById(id));
    }

    @GetMapping(value = "/v1/admin/feedbacks", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public TableResponseEntity<FeedbackResponse.SearchResult> adminSearchData(FeedbackRequest.SearchForm dto) {
        return ResponseUtils.ok(feedbackService.adminSearchData(dto));
    }

    @PutMapping(value = "/v1/admin/feedbacks/{id}", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.UPDATE)
    public ResponseEntity processFeedBack(@RequestBody FeedbackRequest.SubmitForm dto, @PathVariable Long id) throws BaseAppException {
        return ResponseUtils.ok(feedbackService.processFeedBack(id, dto));
    }

    @GetMapping(value = "/v1/admin/feedbacks/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public BaseResponseEntity<FeedbackResponse.DetailBean> adminGetDataById(@PathVariable Long id) throws RecordNotExistsException {

        return ResponseUtils.ok(feedbackService.adminGetDataById(id));
    }

    @GetMapping(value = "/v1/admin/feedbacks/export", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public ResponseEntity<Object> exportData(FeedbackRequest.SearchForm dto) throws Exception {
        return feedbackService.exportData(dto);
    }

}
