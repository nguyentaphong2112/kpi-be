package vn.hbtplus.services;

import vn.hbtplus.models.PermissionDataDto;

import java.util.List;

public interface AuthorizationService {
    List<Long> getOrgHasPermission(String scope, String resource, String userName);

    boolean hasPermissionWithOrg(Long orgId, String scope, String resource);

    boolean hasPermissionWithOrg(Long orgId, String scope, String resource, String userName);

    List<PermissionDataDto> getPermissionData(String scope, String resource, String userName);
    boolean checkPermission(String scope, String resource, String userName) ;
}
