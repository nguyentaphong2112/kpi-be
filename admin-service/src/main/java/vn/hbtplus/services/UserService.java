package vn.hbtplus.services;

import vn.hbtplus.models.dto.UserDto;
import vn.hbtplus.models.dto.UserLogActivityDto;
import vn.hbtplus.exceptions.BaseAppException;
import vn.hbtplus.exceptions.RecordNotExistsException;
import vn.hbtplus.models.BaseDataTableDto;
import vn.hbtplus.models.PermissionDataDto;
import vn.hbtplus.models.dto.MenuDto;
import vn.hbtplus.models.request.UserRequest;
import vn.hbtplus.models.response.UserResponse;
import vn.hbtplus.models.response.UserResponse.TokenResponse;

import javax.validation.constraints.NotBlank;
import java.util.List;

public interface UserService {
    TokenResponse login(UserRequest.LoginForm loginForm) throws BaseAppException;

    boolean changePassword(UserRequest.ChangePassForm changePassForm) throws BaseAppException;

    List<MenuDto> getMenus();

    List<MenuDto> getMenuTree();

    TokenResponse refreshToken(String token) throws ClassNotFoundException, BaseAppException;

    BaseDataTableDto<UserResponse.SearchResult> searchData(UserRequest.SearchForm dto);

    UserDto getUserInfo(String loginName);

    UserRequest.SubmitForm saveData(UserRequest.SubmitForm dto, Long userId) throws BaseAppException;

    void importListUser(List<UserRequest.SubmitForm> userList) throws BaseAppException;

    boolean deleteData(Long id) throws RecordNotExistsException;

    UserResponse.DetailBean getDataById(Long id) throws RecordNotExistsException;

    String resetPassword(Long id, String password) throws RecordNotExistsException;

    boolean lockById(Long id) throws BaseAppException;

    boolean unlockById(Long id) throws BaseAppException;

    TokenResponse loginAsGuest();

    List<MenuDto> getPermissions(String loginName);

    boolean hasPermission(String userName, String scope, String resource);

    List<PermissionDataDto> getPermissionData(String userName, String scope, String resource);

    boolean resetPasswordAll(String password);

    Long saveUserLogActivity(UserLogActivityDto dto);

    List<Long> getGrantedDomain(String scope, String resource, Long orgId, String domainType);

    Boolean hasRole(String userName, String roleCode);

    Object getUserAvatar();

    void clearUserCache(@NotBlank String loginName);
}
