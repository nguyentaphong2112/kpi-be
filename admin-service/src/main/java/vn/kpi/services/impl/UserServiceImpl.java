package vn.kpi.services.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.kpi.configs.BaseCachingConfiguration;
import vn.kpi.configs.MdcForkJoinPool;
import vn.kpi.constants.BaseConstants;
import vn.kpi.exceptions.BaseAppException;
import vn.kpi.exceptions.RecordNotExistsException;
import vn.kpi.feigns.ACSFeignClient;
import vn.kpi.feigns.SsoFeignClient;
import vn.kpi.models.BaseDataTableDto;
import vn.kpi.models.PermissionDataDto;
import vn.kpi.models.UserTokenDto;
import vn.kpi.models.dto.MenuDto;
import vn.kpi.models.dto.UserDto;
import vn.kpi.models.dto.UserLogActivityDto;
import vn.kpi.models.dto.UserRoleDomainDto;
import vn.kpi.models.request.UserRequest;
import vn.kpi.models.response.ACSChangePasswordResponse;
import vn.kpi.models.response.ACSLoginResponse;
import vn.kpi.models.response.UserResponse;
import vn.kpi.repositories.entity.UserEntity;
import vn.kpi.repositories.entity.UserLogActivityEntity;
import vn.kpi.repositories.entity.UserLoginHistoryEntity;
import vn.kpi.repositories.impl.UserRepository;
import vn.kpi.repositories.jpa.UserLogActivityRepositoryJPA;
import vn.kpi.repositories.jpa.UserLoginHistoryRepositoryJPA;
import vn.kpi.repositories.jpa.UserRepositoryJPA;
import vn.kpi.services.RedisService;
import vn.kpi.services.UserRoleService;
import vn.kpi.services.UserService;
import vn.kpi.utils.JwtTokenUtils;
import vn.kpi.utils.PasswordGenerator;
import vn.kpi.utils.PlainTextEncoder;
import vn.kpi.utils.Utils;

import javax.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {
    private final JwtTokenUtils jwtTokenUtils;
    private final UserRepository userRepository;
    private final UserRepositoryJPA userRepositoryJPA;
    private final UserRoleService userRoleService;

    private final UserLogActivityRepositoryJPA userLogActivityRepositoryJPA;

    private final UserLoginHistoryRepositoryJPA userLoginHistoryRepositoryJPA;

    private final HttpServletRequest request;
    private final ACSFeignClient acsFeignClient;
    private final RedisService redisService;
    private final SsoFeignClient ssoFeignClient;
    @Value("${service.properties.acs-auth-url:default}")
    private String urlAcs;

    private final MdcForkJoinPool forkJoinPool;

    @Override
    public UserResponse.TokenResponse login(UserRequest.LoginForm loginForm) throws BaseAppException {
        UserDto userDto;
        if ("default".equalsIgnoreCase(urlAcs)) {
            userDto = userRepository.getUser(loginForm.getLoginName());
            if (userDto == null) {
                throw new BaseAppException("USER_PASSWORD_INVALID", "error.login.userOrPasswordInvalid");
            }
            String encryptedPassword = PlainTextEncoder.encode(loginForm.getPassword(), getSalt(userDto.getUserId()));
            if (!encryptedPassword.equals(userDto.getPassword())) {
                throw new BaseAppException("USER_PASSWORD_INVALID", "error.login.userOrPasswordInvalid");
            }
            if (userDto.getStatus().equals(UserEntity.STATUS.INACTIVE)) {
                throw new BaseAppException("USER_LOCKED", "error.login.userLocked");
            }
        } else {
            ACSLoginResponse acsLoginResponse = loginResponse(loginForm.getLoginName(), loginForm.getPassword());
            userDto = userRepository.getUser(loginForm.getLoginName());
            if (userDto == null) {
                //nếu user chua tồn tại --> thuc hien them moi user
                UserEntity userEntity = Utils.copyProperties(acsLoginResponse.getData().getUser(), new UserEntity());
                userEntity.setStatus(UserEntity.STATUS.ACTIVE);
                userRepositoryJPA.save(userEntity);
                userDto = Utils.copyProperties(userEntity, new UserDto());
            }
        }
        if (userDto != null) {
            userDto.setRoleCodeList(userRepository.getRoleCodeList(userDto.getLoginName()));
        }

        UserResponse.TokenResponse response = new UserResponse.TokenResponse();
        response.setUserInfo(Utils.copyProperties(userDto, new UserResponse.UserInfo()));
        response.setAccessToken(jwtTokenUtils.generateToken(userDto, true));
        response.setRefreshToken(jwtTokenUtils.generateToken(userDto, false));

        UserLoginHistoryEntity historyEntity = UserLoginHistoryEntity.builder().loginName(userDto.getLoginName()).
                loginTime(new Date()).ipAddress(request.getRemoteAddr()).build();
        historyEntity.setCreatedBy(userDto.getLoginName());
        historyEntity.setCreatedTime(new Date());
        userLoginHistoryRepositoryJPA.save(historyEntity);
        return response;
    }

    private String getSalt(Long userId) {
        return String.format("@#%d56", userId);
    }

    @Override
    @Transactional
    public boolean changePassword(UserRequest.ChangePassForm changePassForm) throws BaseAppException {
        UserEntity userEntity = userRepository.getUserEntity(changePassForm.getLoginName());
        if (userEntity == null) {
            throw new BaseAppException("USER_PASSWORD_INVALID", "error.login.userOrPasswordInvalid");
        }
        if (!changePassForm.getPassword().equals(changePassForm.getRetypePassword())) {
            throw new BaseAppException("RETYPE_PASSWORD_INVALID", "error.changePass.passwordAndRetypePassNotMatch");
        }
        if ("default".equalsIgnoreCase(urlAcs)) {
            String encryptedPassword = PlainTextEncoder.encode(changePassForm.getOldPassword(), getSalt(userEntity.getUserId()));
            if (!encryptedPassword.equals(userEntity.getPassword())) {
                throw new BaseAppException("USER_PASSWORD_INVALID", "error.login.userOrPasswordInvalid");
            }

            userEntity.setPassword(PlainTextEncoder.encode(changePassForm.getPassword(), getSalt(userEntity.getUserId())));
            userEntity.setModifiedTime(new Date());
            userEntity.setModifiedBy(changePassForm.getLoginName());
            userRepositoryJPA.save(userEntity);
            UserRequest.SubmitFormToSso form = new UserRequest.SubmitFormToSso();
            BeanUtils.copyProperties(userEntity, form);
            try {
                ssoFeignClient.updatePassword(Utils.getRequestHeader(request), form);
            } catch (Exception ex) {
                log.error("[Login] error", ex);
                throw new BaseAppException("ERROR_CONNECT_ACS_SYSTEM", "error.login.errorConnectToAcsSystem");
            }
        } else {
            ACSLoginResponse loginResponse = loginResponse(changePassForm.getLoginName(), changePassForm.getOldPassword());
            HttpHeaders httpHeaders = new HttpHeaders();
            if (loginResponse == null || loginResponse.getData() == null) {
                throw new BaseAppException("USER_PASSWORD_INVALID", "error.login.userOrPasswordInvalid");
            }
            httpHeaders.set("TokenCode", loginResponse.getData().getTokenCode());
            String password = String.format("%s:%s", changePassForm.getOldPassword(), changePassForm.getPassword());
            httpHeaders.set("Password", Base64.getEncoder().encodeToString(password.getBytes()));
            try {
                Utils.disableSslVerification();
                Response response = acsFeignClient.changePass(httpHeaders, "{}");
                String dataResponse = new String(response.body().asInputStream().readAllBytes(), StandardCharsets.UTF_8);
                ObjectMapper objectMapper = new ObjectMapper();
                objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                Map<String, Object> responseMap = objectMapper.readValue(dataResponse, new TypeReference<>() {
                });
                ACSChangePasswordResponse changePasswordResponse = objectMapper.convertValue(responseMap, ACSChangePasswordResponse.class);
                if (changePasswordResponse == null || (changePasswordResponse.getIsSuccess() != null && !changePasswordResponse.getIsSuccess())) {
                    throw new BaseAppException("USER_PASSWORD_INVALID", StringUtils.join(changePasswordResponse.getParamResponse().getMessages(), "\n"));
                }
            } catch (Exception ex) {
                log.error("[Login] error", ex);
                throw new BaseAppException("ERROR_CONNECT_ACS_SYSTEM", "error.login.errorConnectToAcsSystem");
            }
        }
        return true;
    }

    @Override
    @Transactional(readOnly = true)
    public List<MenuDto> getMenus() {
        List<MenuDto> lstMenu = userRepository.getMenus(Utils.getUserNameLogin());
        return lstMenu;
    }

    @Override
    public List<MenuDto> getMenuTree() {
        List<MenuDto> lstMenu = userRepository.getMenus(Utils.getUserNameLogin());
        List<MenuDto> results = new ArrayList<>();
        Map<Long, MenuDto> mapMenus = new HashMap<>();
        lstMenu.stream().forEach(item -> {
            mapMenus.put(item.getMenuId(), item);
        });
        lstMenu.stream().forEach(item -> {
            if (item.getParentId() == null || mapMenus.get(item.getParentId()) == null) {
                results.add(item);
            } else {
                MenuDto parent = mapMenus.get(item.getParentId());
                parent.addChild(item);
            }
        });

        return results;
    }

    @Override
    public UserResponse.TokenResponse refreshToken(String token) throws ClassNotFoundException, BaseAppException {
        UserTokenDto userTokenDto = jwtTokenUtils.getUerNameFromToken(token, null);

        UserDto userDto = userRepository.getUser(userTokenDto.getLoginName());
        if (userDto == null) {
            throw new BaseAppException("USER_INVALID", "error.refreshToken.userInvalid");
        }

        if (userDto.getStatus().equals(UserEntity.STATUS.INACTIVE)) {
            throw new BaseAppException("USER_LOCKED", "error.refreshToken.userLocked");
        }

        UserResponse.TokenResponse response = new UserResponse.TokenResponse();
        response.setUserInfo(Utils.copyProperties(userDto, new UserResponse.UserInfo()));
        response.setAccessToken(jwtTokenUtils.generateToken(userDto, true));
        response.setRefreshToken(jwtTokenUtils.generateToken(userDto, false));
        return response;
    }

    @Override
    public BaseDataTableDto<UserResponse.SearchResult> searchData(UserRequest.SearchForm dto) {
        return userRepository.searchData(dto);
    }

    @Override
    public UserDto getUserInfo(String loginName) {
        return userRepository.getUser(loginName);
    }

    @Override
    @Transactional
    public UserRequest.SubmitForm saveData(UserRequest.SubmitForm dto, Long userId) throws BaseAppException {
        if (userRepository.duplicate(UserEntity.class, userId, "login_name", dto.getLoginName())) {
            throw new BaseAppException("ERROR_LOGIN_NAME_DUPLICATE", "error.user.duplicateLoginName");
        }
        UserEntity entity;
        if (userId != null && userId > 0L) {
            entity = userRepositoryJPA.getById(userId);
            entity.setModifiedTime(new Date());
            entity.setModifiedBy(Utils.getUserNameLogin());
        } else {
            entity = new UserEntity();
            entity.setCreatedTime(new Date());
            entity.setCreatedBy(Utils.getUserNameLogin());
        }
        Utils.copyProperties(dto, entity);
        entity.setIsDeleted(BaseConstants.STATUS.NOT_DELETED);
        entity.setStatus(UserEntity.STATUS.ACTIVE);
        userRepositoryJPA.save(entity);
        if (StringUtils.isBlank(dto.getPassword())) {
            dto.setPassword("12345");
        }
        if (!Utils.isNullOrEmpty(dto.getPassword()) && (userId == null || userId <= 0)) {
            entity.setPassword(PlainTextEncoder.encode(dto.getPassword(), getSalt(entity.getUserId())));
        }
        //xu ly insert default role
        if (!Utils.isNullOrEmpty(dto.getDefaultRoles())) {
            userRoleService.grantRole(entity.getUserId(), dto.getDefaultRoles());
        }

        // Tạo acc bên sso
        log.info("Gọi sang SSO để tạo account");
        Long finalUserId = entity.getUserId();
        HttpHeaders headers = Utils.getRequestHeader(request);
        forkJoinPool.execute(() -> createSsoUser(headers, dto, finalUserId));
        return dto;
    }

    private void createSsoUser(HttpHeaders headers, UserRequest.SubmitForm dto, Long userId) {
        UserRequest.SubmitFormToSso ssoForm = new UserRequest.SubmitFormToSso();
        Utils.copyProperties(dto, ssoForm);
        ssoForm.setUserId(userId);
        try (Response response = ssoFeignClient.updatePassword(headers, ssoForm)){
            log.info("[createSsoUser] response: {}", response);
        }
    }

    @Override
    @Transactional
    public void importListUser(List<UserRequest.SubmitForm> userList) throws BaseAppException {
        if (Utils.isNullOrEmpty(userList)) {
            return;
        }
        List<String> loginNameList = new ArrayList<>();
        userList.forEach(item -> loginNameList.add(item.getLoginName()));
        Map<String, Long> loginNameMap = userRepository.getMapLoginNameByList(loginNameList);

        List<UserEntity> entityList = new ArrayList<>();
        List<UserRequest.SubmitForm> prepareUserList = new ArrayList<>();
        for (UserRequest.SubmitForm form : userList) {
            if (loginNameMap.get(form.getLoginName()) == null) {
                prepareUserList.add(form);
            }
        }
        Long nextUserId = userRepository.getNextId(UserEntity.class, prepareUserList.size());
        List<Long> userIds = new ArrayList<>();
        int index = 1;
        for (UserRequest.SubmitForm form : prepareUserList) {
            UserEntity entity = new UserEntity();
            entity.setUserId(nextUserId + (index++));
            entity.setCreatedTime(new Date());
            entity.setCreatedBy(Utils.getUserNameLogin());
            Utils.copyProperties(form, entity);
            entity.setIsDeleted(BaseConstants.STATUS.NOT_DELETED);
            entity.setStatus(UserEntity.STATUS.ACTIVE);

            if (!Utils.isNullOrEmpty(form.getPassword())) {
                entity.setPassword(PlainTextEncoder.encode(form.getPassword(), getSalt(entity.getUserId())));
            }

            entityList.add(entity);
            userIds.add(entity.getUserId());
        }

        userRepository.insertBatch(UserEntity.class, entityList, Utils.getUserNameLogin());
        userRoleService.grantRolesToUser(userIds, userList.get(0).getDefaultRoles());
    }

    @Override
    @Transactional
    public boolean deleteData(Long id) throws RecordNotExistsException {
        Optional<UserEntity> optional = userRepositoryJPA.findById(id);
        if (optional.isEmpty() || BaseConstants.STATUS.DELETED.equals(optional.get().getIsDeleted())) {
            throw new RecordNotExistsException(id, UserEntity.class);
        }
        userRepository.deActiveObject(UserEntity.class, id);
        return true;
    }

    @Override
    public UserResponse.DetailBean getDataById(Long id) throws RecordNotExistsException {
        Optional<UserEntity> optional = userRepositoryJPA.findById(id);
        if (optional.isEmpty() || BaseConstants.STATUS.DELETED.equals(optional.get().getIsDeleted())) {
            throw new RecordNotExistsException(id, UserEntity.class);
        }
        UserResponse.DetailBean dto = new UserResponse.DetailBean();
        Utils.copyProperties(optional.get(), dto);
        return dto;
    }

    @Override
    @Transactional
    public String resetPassword(Long id, String password) throws RecordNotExistsException {
        Optional<UserEntity> optional = userRepositoryJPA.findById(id);
        if (optional.isEmpty() || BaseConstants.STATUS.DELETED.equals(optional.get().getIsDeleted())) {
            throw new RecordNotExistsException(id, UserEntity.class);
        }
        if (Utils.isNullOrEmpty(password)) {
            password = PasswordGenerator.generatePassword(8);
        }
        UserEntity userEntity = optional.get();
        userEntity.setPassword(PlainTextEncoder.encode(password, getSalt(userEntity.getUserId())));
        userEntity.setModifiedTime(new Date());
        userEntity.setModifiedBy(Utils.getUserNameLogin());
        userRepositoryJPA.save(userEntity);
        UserRequest.SubmitFormToSso form = new UserRequest.SubmitFormToSso();
        BeanUtils.copyProperties(userEntity, form);
        try {
            ssoFeignClient.updatePassword(Utils.getRequestHeader(request), form);
        } catch (Exception ex) {
            log.error("[Login] error", ex);
            throw new BaseAppException("ERROR_CONNECT_ACS_SYSTEM", "error.login.errorConnectToAcsSystem");
        }
        return password;
    }

    @Override
    @Transactional
    public boolean lockById(Long id) throws BaseAppException {
        Optional<UserEntity> optional = userRepositoryJPA.findById(id);
        if (optional.isEmpty() || BaseConstants.STATUS.DELETED.equals(optional.get().getIsDeleted())) {
            throw new RecordNotExistsException(id, UserEntity.class);
        } else if (UserEntity.STATUS.INACTIVE.equals(optional.get().getStatus())) {
            throw new BaseAppException("RECORD_LOCKED", "error.record.locked");
        }
        UserEntity entity = optional.get();
        entity.setModifiedTime(new Date());
        entity.setModifiedBy(Utils.getUserNameLogin());
        entity.setStatus(UserEntity.STATUS.INACTIVE);
        userRepositoryJPA.save(entity);
        return true;
    }

    @Override
    @Transactional
    public boolean unlockById(Long id) throws BaseAppException {
        Optional<UserEntity> optional = userRepositoryJPA.findById(id);
        if (optional.isEmpty() || BaseConstants.STATUS.DELETED.equals(optional.get().getIsDeleted())) {
            throw new RecordNotExistsException(id, UserEntity.class);
        } else if (UserEntity.STATUS.ACTIVE.equals(optional.get().getStatus())) {
            throw new BaseAppException("RECORD_UNLOCKED", "error.record.unlocked");
        }
        UserEntity entity = optional.get();
        entity.setModifiedTime(new Date());
        entity.setModifiedBy(Utils.getUserNameLogin());
        entity.setStatus(UserEntity.STATUS.ACTIVE);
        userRepositoryJPA.save(entity);
        return true;
    }

    @Override
    public UserResponse.TokenResponse loginAsGuest() {
        UserDto userDto = new UserDto();
        userDto.setUserId(0L);
        userDto.setEmail("guest");
        userDto.setLoginName("guest");
        userDto.setFullName("Guest");
        UserResponse.TokenResponse response = new UserResponse.TokenResponse();
        response.setUserInfo(Utils.copyProperties(userDto, new UserResponse.UserInfo()));
        response.setAccessToken(jwtTokenUtils.generateToken(userDto, true));
        response.setRefreshToken(jwtTokenUtils.generateToken(userDto, false));
        return response;
    }

    @Override
    @Transactional(readOnly = true)
//    @Cacheable(cacheNames = BaseCachingConfiguration.ADMIN_MENU, key = "#loginName", unless = "#result == null or #result.isEmpty()")
    public List<MenuDto> getPermissions(String loginName) {
        List<MenuDto> listPermissions = userRepository.getListPermissions(loginName);
        Map<Long, MenuDto> mapResults = new HashMap<>();
        List<MenuDto> listResults = new ArrayList<>();
        listPermissions.forEach(item -> {
            if (mapResults.get(item.getMenuId()) == null) {
                listResults.add(item);
                mapResults.put(item.getMenuId(), item);
                item.getScopes().add(item.getScope());
            } else {
                mapResults.get(item.getMenuId()).getScopes().add(item.getScope());
            }
        });
        return listResults;
    }

    @Override
    public boolean hasPermission(String userName, String scope, String resource) {
        //check phan quyen theo user
        return userRepository.hasPermission(userName, scope, resource);
    }

    @Override
    public List<PermissionDataDto> getPermissionData(String userName, String scope, String resource) {
        List<UserRoleDomainDto> listDomains = userRepository.getPermissionData(userName, scope, resource);
        List<PermissionDataDto> results = new ArrayList<>();
        Map<String, PermissionDataDto> mapPermissionDatas = new HashMap<>();
        listDomains.forEach(item -> {
            if (mapPermissionDatas.get(item.getKeyOrder()) == null) {
                PermissionDataDto permissionDataDto = new PermissionDataDto();
                mapPermissionDatas.put(item.getKeyOrder(), permissionDataDto);
            }
            if (item.getDomainType().equalsIgnoreCase("DON_VI")) {
                mapPermissionDatas.get(item.getKeyOrder()).getOrgIds().add(Long.valueOf(item.getDomainId()));
            } else if (item.getDomainType().equalsIgnoreCase("DOI_TUONG")) {
                mapPermissionDatas.get(item.getKeyOrder()).getEmpTypeIds().add(Long.valueOf(item.getDomainId()));
            }
        });
        List<String> keyExisted = new ArrayList<>();
        mapPermissionDatas.values().forEach(item -> {
            if (!keyExisted.contains(item.getKey())) {
                keyExisted.add(item.getKey());
                results.add(item);
            }
        });
        return results;
    }

    @Override
    public boolean resetPasswordAll(String password) {
        String userName = Utils.getUserNameLogin();
        List<UserEntity> userEntities = userRepositoryJPA.findToResetPassword();
        userEntities.forEach(userEntity -> {
            if ("cccd".equalsIgnoreCase(password)) {
                if (Utils.isNullOrEmpty(userEntity.getIdNo())) {
                    throw new BaseAppException(String.format("User %s không có số định danh!", userEntity.getLoginName()));
                }
                userEntity.setPassword(PlainTextEncoder.encode(userEntity.getIdNo(), getSalt(userEntity.getUserId())));
            } else {
                userEntity.setPassword(PlainTextEncoder.encode(Utils.isNullOrEmpty(password) ? "123456" : password, getSalt(userEntity.getUserId())));
            }
            userEntity.setModifiedTime(new Date());
            userEntity.setModifiedBy(userName);
        });
        userRepository.updateBatch(UserEntity.class, userEntities, true);
        return true;
    }

    @Override
    @Transactional
    public Long saveUserLogActivity(UserLogActivityDto dto) {
        UserLogActivityEntity entity = new UserLogActivityEntity();
        Utils.copyProperties(dto, entity);
        entity.setCreatedBy(Utils.getUserNameLogin());
        entity.setCreatedTime(new Date());

        entity = userLogActivityRepositoryJPA.save(entity);

        return entity.getUserLogActivityId();
    }

    @Override
    public List<Long> getGrantedDomain(String scope, String resource, Long orgId,
                                       String domainType) {
        return userRepository.getGrantedDomain(scope, resource, orgId, domainType);
    }

    @Override
    public Boolean hasRole(String userName, String roleCode) {
        return userRepository.hasRole(userName, roleCode);
    }

    @Override
    public Object getUserAvatar() {
        String userName = Utils.getUserNameLogin();
        return null;
    }

    @Override
    public void clearUserCache(String userName) {
        String[] cacheNames = new String[]{BaseCachingConfiguration.AUTHORIZATION}; // đúng vùng cache
        for (String cacheName : cacheNames) {
            redisService.evictCacheByPrefix(cacheName, userName + "#");
        }
    }

    private ACSLoginResponse loginResponse(String loginName, String password) {
        //check dang nhap qua he thong ACS
        HttpHeaders httpHeaders = new HttpHeaders();
        String key = String.format("%s:%s:%s", "HRM", loginName, password);
        httpHeaders.put("Authorization", Arrays.asList(String.format("Basic %s", Base64.getEncoder().encodeToString(key.getBytes()))));
        ACSLoginResponse acsLoginResponse = null;
        try {
            Utils.disableSslVerification();
            Response response = acsFeignClient.login(httpHeaders);
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            Map<String, Object> responseMap = objectMapper.readValue(response.body().asInputStream(), new TypeReference<>() {
            });
            acsLoginResponse = objectMapper.convertValue(responseMap, ACSLoginResponse.class);
            if (acsLoginResponse == null || acsLoginResponse.getData() == null || acsLoginResponse.getData().getUser() == null) {
                throw new BaseAppException("USER_PASSWORD_INVALID", "error.login.userOrPasswordInvalid");
            }
        } catch (Exception ex) {
            log.error("[Login] error", ex);
            throw new BaseAppException("ERROR_CONNECT_ACS_SYSTEM", "error.login.errorConnectToAcsSystem");
        }

        return acsLoginResponse;
    }
}
