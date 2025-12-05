package vn.kpi.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import vn.kpi.configs.BaseCachingConfiguration;
import vn.kpi.feigns.PermissionFeignClient;
import vn.kpi.models.PermissionDataDto;
import vn.kpi.repositories.impl.UtilsRepository;
import vn.kpi.services.AuthorizationService;
import vn.kpi.services.RedisService;
import vn.kpi.utils.Utils;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthorizationServiceImpl implements AuthorizationService {
    private final PermissionFeignClient permissionFeignClient;
    private final HttpServletRequest httpServletRequest;
    private final UtilsRepository utilsRepository;
    private final RedisService redisService;

    @Override
    @Cacheable(cacheNames = BaseCachingConfiguration.AUTHORIZATION, key = "#userName + '#getOrgHasPermission' + #scope + #resource")
    public List<Long> getOrgHasPermission(String scope, String resource, String userName) {
        List<PermissionDataDto> permissionDataDtos = permissionFeignClient.getPermissionData(Utils.getRequestHeader(httpServletRequest), scope, resource, userName).getData();
        List<Long> orgPermissionIds = new ArrayList<>();
        if (permissionDataDtos != null) {
            permissionDataDtos.forEach(permissionDataDto -> {
                orgPermissionIds.addAll(permissionDataDto.getOrgIds());
            });
        }
        return new ArrayList<>(new HashSet<>(orgPermissionIds));
    }

    @Override
    public boolean hasPermissionWithOrg(Long orgId, String scope, String resource) {
        return hasPermissionWithOrg(orgId, scope, resource, Utils.getUserNameLogin());
    }

    @Override
    public boolean hasPermissionWithOrg(Long orgId, String scope, String resource, String userName) {
        List<Long> orgPermissionIds = getOrgHasPermission(scope, resource, userName);
        if (Utils.isNullOrEmpty(orgPermissionIds)) {
            return false;
        }
        return utilsRepository.checkOrgContains(orgPermissionIds, orgId);
    }

    @Override
    @Cacheable(cacheNames = BaseCachingConfiguration.AUTHORIZATION, key = "#userName + '#getPermissionData' + #scope + #resource")
    public List<PermissionDataDto> getPermissionData(String scope, String resource, String userName) {
        return permissionFeignClient.getPermissionData(Utils.getRequestHeader(httpServletRequest), scope, resource, userName).getData();
    }

    @Override
    @Cacheable(cacheNames = BaseCachingConfiguration.AUTHORIZATION, key = "#userName + '#checkPermission' + #scope + #resource")
    public boolean checkPermission(String scope, String resource, String userName) {
        return permissionFeignClient.checkPermission(Utils.getRequestHeader(httpServletRequest), scope, resource, userName).getData();
    }


}
