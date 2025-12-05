/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.services;

import vn.hbtplus.exceptions.BaseAppException;
import vn.hbtplus.models.request.UserRoleRequest;
import vn.hbtplus.models.response.UserRoleResponse;

import java.util.List;

/**
 * Lop interface service ung voi bang sys_user_roles
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

public interface UserRoleService {


    boolean grantDomains(UserRoleRequest.SubmitForm dto) throws BaseAppException;

    List<UserRoleResponse> viewUserRole(Long userId);

    boolean grantRole(Long userId, Long roleId);
    boolean grantRole(Long userId, List<String> roleIds);

    boolean deleteRole(Long userId, Long roleId);

    boolean grantRolesToUser(List<Long> userIds, List<String> roleCodes);
}
