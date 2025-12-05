package vn.hbtplus.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.hbtplus.annotations.HasPermission;
import vn.hbtplus.annotations.Resource;
import vn.hbtplus.annotations.UserLogActivity;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.constants.Constant;
import vn.hbtplus.constants.Scope;
import vn.hbtplus.exceptions.BaseAppException;
import vn.hbtplus.exceptions.RecordNotExistsException;
import vn.hbtplus.models.request.LogTaskRequest;
import vn.hbtplus.models.response.BaseResponseEntity;
import vn.hbtplus.models.response.LogTasKResponse;
import vn.hbtplus.models.response.TableResponseEntity;
import vn.hbtplus.services.LogTaskService;
import vn.hbtplus.services.impl.TaskReminderService;

import javax.validation.Valid;


@RestController
@RequestMapping(BaseConstants.REQUEST_MAPPING_PREFIX)
@RequiredArgsConstructor
@Resource(value = Constant.RESOURCES.LOG_TASK)
public class LogTaskController {
    private final LogTaskService logTaskService;
    private final TaskReminderService taskReminderService;

    @GetMapping(value = "/v1/log-task", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public TableResponseEntity<LogTasKResponse.SearchResult> searchData(LogTaskRequest.SearchForm dto) {
        return logTaskService.searchData(dto);
    }

    @PostMapping(value = "/v1/log-task", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.CREATE)
    public BaseResponseEntity<Long> saveData(@RequestBody @Valid LogTaskRequest.SubmitForm dto) throws BaseAppException {
        return logTaskService.saveData(dto, null);
    }

    @PutMapping(value = "/v1/log-task/{id}", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.UPDATE)
    @UserLogActivity
    public BaseResponseEntity<Long> updateData(@RequestBody @Valid  LogTaskRequest.SubmitForm dto, @PathVariable Long id) throws BaseAppException {
        return logTaskService.saveData(dto, id);
    }

    @DeleteMapping(value = "/v1/log-task/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.DELETE)
    public BaseResponseEntity<Long> deleteData(@PathVariable Long id) throws BaseAppException {
        return logTaskService.deleteData(id);
    }

    @GetMapping(value = "/v1/log-task/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public BaseResponseEntity<LogTasKResponse.DetailBean> getDataById(@PathVariable Long id)  throws RecordNotExistsException {
        return logTaskService.getDataById(id);
    }

    @GetMapping(value = "/v1/log-task/export", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public ResponseEntity<Object> exportData(LogTaskRequest.SearchForm dto) throws Exception {
        return logTaskService.exportData(dto);
    }

    @GetMapping(value = "/v1/log-task/resendRemindersIfStillPending", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public void resendRemindersIfStillPending() throws Exception {
        taskReminderService.resendRemindersIfStillPending();
    }
}
