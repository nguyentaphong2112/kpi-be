package vn.kpi.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.kpi.annotations.HasPermission;
import vn.kpi.annotations.Resource;
import vn.kpi.constants.Constant;
import vn.kpi.constants.Scope;
import vn.kpi.models.dto.UserDto;
import vn.kpi.models.dto.UserLogActivityDto;
import vn.kpi.exceptions.BaseAppException;
import vn.kpi.exceptions.RecordNotExistsException;
import vn.kpi.models.request.UserRequest;
import vn.kpi.models.response.BaseResponseEntity;
import vn.kpi.models.response.TableResponseEntity;
import vn.kpi.models.response.UserResponse;
import vn.kpi.services.UserService;
import vn.kpi.utils.ResponseUtils;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("")
@RequiredArgsConstructor
@Resource(value = Constant.RESOURCES.USER)
@CrossOrigin
public class UserController {
    private final UserService userService;

    @GetMapping(value = "/v1/user", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public TableResponseEntity<UserResponse.SearchResult> searchData(UserRequest.SearchForm dto) {
        return ResponseUtils.ok(userService.searchData(dto));
    }

    @GetMapping(value = "/v1/user/info", produces = MediaType.APPLICATION_JSON_VALUE)
    public UserDto getUserInfo(@RequestParam("loginName") String loginName) {
        return userService.getUserInfo(loginName);
    }

    @PostMapping(value = "/v1/user", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.CREATE)
    public ResponseEntity saveData(@Valid @RequestBody UserRequest.SubmitForm dto) throws BaseAppException {
        return ResponseUtils.ok(userService.saveData(dto, null));
    }
    @PostMapping(value = "/v1/user/list", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.CREATE)
    public ResponseEntity importListUser(@RequestBody List<UserRequest.SubmitForm> userList) throws BaseAppException {
        userService.importListUser(userList);
        return ResponseUtils.ok();
    }

    @PutMapping(value = "/v1/user/{id}", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.UPDATE)
    public ResponseEntity updateData(@Valid  @RequestBody UserRequest.SubmitForm dto, @PathVariable Long id) throws BaseAppException {
        return ResponseUtils.ok(userService.saveData(dto, id));
    }

    @DeleteMapping(value = "/v1/user/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.DELETE)
    public ResponseEntity deleteData(@PathVariable Long id) throws RecordNotExistsException {
        return ResponseUtils.ok(userService.deleteData(id));
    }

    @GetMapping(value = "/v1/user/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public BaseResponseEntity<UserResponse.DetailBean> getDataById(@PathVariable Long id)  throws RecordNotExistsException {
        return ResponseUtils.ok(userService.getDataById(id));
    }

    @PutMapping(value = "/v1/user/reset-password/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.UPDATE)
    public BaseResponseEntity<String> resetPassword(@PathVariable Long id,@RequestParam(required = false) String password)  throws RecordNotExistsException {
        return ResponseUtils.ok(userService.resetPassword(id, password));
    }
    @PutMapping(value = "/v1/user/reset-password/all", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.UPDATE)
    public BaseResponseEntity<Boolean> resetPasswordAll(@RequestParam(required = false) String password)  throws RecordNotExistsException {
        return ResponseUtils.ok(userService.resetPasswordAll(password));
    }

    @PutMapping(value = "/v1/user/lock-by-id/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.UPDATE)
    public BaseResponseEntity lockById(@PathVariable Long id) throws BaseAppException {
        return ResponseUtils.ok(userService.lockById(id));
    }
    @PutMapping(value = "/v1/user/unlock-by-id/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.UPDATE)
    public BaseResponseEntity unlockById(@PathVariable Long id) throws BaseAppException {
        return ResponseUtils.ok(userService.unlockById(id));
    }

    @PostMapping(value = "/v1/user/save-log-activity", produces = MediaType.APPLICATION_JSON_VALUE)
    public BaseResponseEntity saveUserLogActivity(@RequestBody UserLogActivityDto dto) {
        return ResponseUtils.ok(userService.saveUserLogActivity(dto));
    }
}
