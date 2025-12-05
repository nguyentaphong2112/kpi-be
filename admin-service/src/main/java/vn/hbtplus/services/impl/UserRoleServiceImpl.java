/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.hbtplus.configs.BaseCachingConfiguration;
import vn.hbtplus.exceptions.BaseAppException;
import vn.hbtplus.models.dto.UserRoleDomainDto;
import vn.hbtplus.models.request.UserRoleRequest;
import vn.hbtplus.models.response.UserRoleResponse;
import vn.hbtplus.repositories.impl.UserRoleRepository;
import vn.hbtplus.repositories.jpa.RoleRepositoryJPA;
import vn.hbtplus.repositories.jpa.UserRoleRepositoryJPA;
import vn.hbtplus.services.UserRoleService;
import vn.hbtplus.utils.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Lop impl service ung voi bang sys_user_roles
 *
 * @author tudd
 * @version 1.0
 * @since 1.0
 */

@Service
@RequiredArgsConstructor
public class UserRoleServiceImpl implements UserRoleService {

    private final UserRoleRepository userRolesRepository;
    private final UserRoleRepositoryJPA userRolesRepositoryJPA;
    private final RoleRepositoryJPA roleRepositoryJPA;

    private final CacheManager cacheManager;
    private final RedisServiceImpl redisServiceImpl;


    @Override
    @Transactional
    public boolean grantDomains(UserRoleRequest.SubmitForm dto) throws BaseAppException {
        //luu du lieu phan quyen nguoi dung va user
        List<Long> roleIds = new ArrayList<>();
        if (!Utils.isNullOrEmpty(dto.getRoleData())) {
            dto.getRoleData().forEach(item -> {
                roleIds.add(item.getRoleId());
            });
        }
        //inactive role cu di
//        userRolesRepository.inactiveRoleNotIn(dto.getUserId(), roleIds);
        //insert or active role moi
        if (!roleIds.isEmpty()) {
            userRolesRepository.grantRoles(dto.getUserId(), roleIds);
        }

        //luu du lieu phan quyen
        if (!Utils.isNullOrEmpty(dto.getRoleData())) {
            dto.getRoleData().forEach(item -> {
                Long roleId = item.getRoleId();
                saveUserRoleDomains(dto.getUserId(), roleId, item.getGroupDomains());
            });
        }
        redisServiceImpl.clearCache(BaseCachingConfiguration.ADMIN_USER_ROLE);
        return true;
    }

    private void saveUserRoleDomains(Long userId, Long roleId, List<List<UserRoleRequest.DomainDataBean>> domains) {
        //inactive gia tri khong duoc chon
        userRolesRepository.inactiveDomainNotIn(userId, roleId, domains);
        //insert or active gia tri duoc chon
        if (!Utils.isNullOrEmpty(domains)) {
            userRolesRepositoryJPA.flush();
            userRolesRepository.activeDomainIn(userId, roleId, domains);
            userRolesRepositoryJPA.flush();
            userRolesRepository.insertRoleDomains(userId, roleId, domains);
        }
    }


    @Override
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = BaseCachingConfiguration.ADMIN_USER_ROLE, key = "#userId")
    public List<UserRoleResponse> viewUserRole(Long userId) {
        List<UserRoleResponse> results = new ArrayList<>();
        List<UserRoleDomainDto> domainDtos = userRolesRepository.getUserRoleDomains(userId);

        Long roleId = 0L;
        UserRoleResponse userRoleResponse = null;
        String key = "";
        for (UserRoleDomainDto dto : domainDtos) {
            if (!roleId.equals(dto.getRoleId())) {
                userRoleResponse = new UserRoleResponse();
                userRoleResponse.setRoleId(dto.getRoleId());
                userRoleResponse.setRoleName(dto.getRoleName());
                results.add(userRoleResponse);
                key = "";
            }
            if (!Utils.isNullOrEmpty(dto.getDomainType())) {
                if (!key.equalsIgnoreCase(dto.getKeyOrder())) {
                    userRoleResponse.getGroupDomains().add(new ArrayList<>());
                }
                userRoleResponse.addDomain(dto);
                key = dto.getKeyOrder();
            }
            roleId = dto.getRoleId();
        }
        return results;
    }

    @Override
    @Transactional
    @CacheEvict(cacheNames = BaseCachingConfiguration.ADMIN_USER_ROLE, key = "#userId")
    public boolean grantRole(Long userId, Long roleId) {
        userRolesRepository.grantRoles(userId, Arrays.asList(roleId));
        return true;
    }

    @Override
    public boolean grantRole(Long userId, List<String> roleCodes) {
        roleCodes.forEach(roleCode -> {
            Long roleId = roleRepositoryJPA.getRoleId(roleCode.toUpperCase());
            if (roleCode != null) {
                grantRole(userId, roleId);
            }
        });
        return true;
    }

    @Override
    @Transactional
    @CacheEvict(cacheNames = BaseCachingConfiguration.ADMIN_USER_ROLE, key = "#userId")
    public boolean deleteRole(Long userId, Long roleId) {
        userRolesRepository.inactiveRoleIn(userId, Arrays.asList(roleId));
        return true;
    }

    @Override
    @Transactional
    public boolean grantRolesToUser(List<Long> userIds, List<String> roleCodes) {
        roleCodes.forEach(roleCode -> {
            Long roleId = roleRepositoryJPA.getRoleId(roleCode.toUpperCase());
            if (roleCode != null) {
                userRolesRepository.grantRolesToUser(userIds, roleId);
            }
        });
        return true;
    }
}
