package vn.kpi.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import vn.kpi.exceptions.BaseAppException;
import vn.kpi.models.PermissionDataDto;
import vn.kpi.models.dto.MenuDto;
import vn.kpi.models.request.UserRequest;
import vn.kpi.models.response.BaseResponseEntity;
import vn.kpi.models.response.ListResponseEntity;
import vn.kpi.models.response.UserResponse;
import vn.kpi.services.RedisService;
import vn.kpi.services.UserService;
import vn.kpi.utils.ResponseUtils;
import vn.kpi.utils.Utils;

import javax.validation.Valid;

@RestController
@RequestMapping("")
@RequiredArgsConstructor
@CrossOrigin
public class AuthenticateController {
    private final UserService userService;
    private final RedisService redisService;

    @PostMapping(value = "/v1/user/login", produces = MediaType.APPLICATION_JSON_VALUE)
    public BaseResponseEntity<UserResponse.TokenResponse> login(@Valid @RequestBody UserRequest.LoginForm loginForm) throws BaseAppException {
        //thuc hien clear cache
        userService.clearUserCache(loginForm.getLoginName());

        return ResponseUtils.ok(userService.login(loginForm));
    }

    @PostMapping(value = "/v1/guest/login", produces = MediaType.APPLICATION_JSON_VALUE)
    public BaseResponseEntity<UserResponse.TokenResponse> loginAsGuest() {
        return ResponseUtils.ok(userService.loginAsGuest());
    }

    @PostMapping(value = "/v1/user/refresh-token", produces = MediaType.APPLICATION_JSON_VALUE)
    public BaseResponseEntity<UserResponse.TokenResponse> refreshToken(@Valid @RequestBody UserRequest.RefreshTokenForm request) throws BaseAppException, ClassNotFoundException {
        return ResponseUtils.ok(userService.refreshToken(request.getToken()));
    }

    @PostMapping(value = "/v1/user/change-password", produces = MediaType.APPLICATION_JSON_VALUE)
    public BaseResponseEntity<Object> changePass(@Valid @RequestBody UserRequest.ChangePassForm changePassForm) throws BaseAppException {
        return ResponseUtils.ok(userService.changePassword(changePassForm));
    }

    @GetMapping(value = "/v1/user/menu", produces = MediaType.APPLICATION_JSON_VALUE)
    public ListResponseEntity<MenuDto> getMenus() {
        String loginName = Utils.getUserNameLogin();
//        redisService.evictCacheByPrefix(BaseCachingConfiguration.AUTHORIZATION, loginName);
        return ResponseUtils.ok(userService.getPermissions(loginName));
    }

    @GetMapping(value = "/v1/user/check-permission", produces = MediaType.APPLICATION_JSON_VALUE)
    public BaseResponseEntity<Boolean> checkPermission(@RequestParam String scope,
                                                       @RequestParam String resource,
                                                       @RequestParam String userName) {
        return ResponseUtils.ok(userService.hasPermission(userName, scope, resource));
    }
    @GetMapping(value = "/v1/user/has-role", produces = MediaType.APPLICATION_JSON_VALUE)
    public BaseResponseEntity<Boolean> hasRole(@RequestParam String roleCode,
                                                       @RequestParam String userName) {
        return ResponseUtils.ok(userService.hasRole(userName, roleCode));
    }

    @GetMapping(value = "/v1/user/avatar", produces = MediaType.APPLICATION_JSON_VALUE)
    public BaseResponseEntity<Object> getUserAvatar() {
        return ResponseUtils.ok(userService.getUserAvatar());
    }

    @GetMapping(value = "/v1/user/permission-data", produces = MediaType.APPLICATION_JSON_VALUE)
    public ListResponseEntity<PermissionDataDto> getDomain(@RequestParam String scope,
                                                           @RequestParam String resource,
                                                           @RequestParam String userName) {
        return ResponseUtils.ok(userService.getPermissionData(userName, scope, resource));
    }

    @GetMapping(value = "/v1/user/menu-tree", produces = MediaType.APPLICATION_JSON_VALUE)
    public ListResponseEntity<MenuDto> getMenuTree() {
        return ResponseUtils.ok(userService.getMenuTree());
    }

    @GetMapping(value = "/v1/permission/org-granted", produces = MediaType.APPLICATION_JSON_VALUE)
    public ListResponseEntity<Long> getGrantedDomain(@RequestParam String scope,
                                                           @RequestParam String resource,
                                                           @RequestParam Long orgId) {
        return ResponseUtils.ok(userService.getGrantedDomain(scope, resource, orgId, "DON_VI"));
    }
}
