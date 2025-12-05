/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.kpi.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.kpi.annotations.HasPermission;
import vn.kpi.annotations.Resource;
import vn.kpi.constants.BaseConstants;
import vn.kpi.constants.Constant;
import vn.kpi.constants.Scope;
import vn.kpi.exceptions.BaseAppException;
import vn.kpi.models.request.UserRoleRequest;
import vn.kpi.models.response.ListResponseEntity;
import vn.kpi.models.response.UserRoleResponse;
import vn.kpi.services.UserRoleService;
import vn.kpi.utils.ResponseUtils;

import javax.validation.Valid;

@RestController
@RequestMapping(BaseConstants.REQUEST_MAPPING_PREFIX)
@RequiredArgsConstructor
@Resource(value = Constant.RESOURCES.USER_ROLE)
public class UserRoleController {
    private final UserRoleService userRolesService;

    @GetMapping(value = "/v1/user-role/{userId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public ListResponseEntity<UserRoleResponse> viewUserRole(@PathVariable Long userId) {
        return ResponseUtils.ok(userRolesService.viewUserRole(userId));
    }

    @PostMapping(value = "/v1/user-role/grant-domain", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.CREATE)
    public ResponseEntity grantDomains(@Valid @RequestBody UserRoleRequest.SubmitForm dto) throws BaseAppException {
        return ResponseUtils.ok(userRolesService.grantDomains(dto));
    }

    @PostMapping(value = "/v1/user-role/{userId}", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.CREATE)
    public ResponseEntity grantRole(Long roleId, @PathVariable Long userId) throws BaseAppException {
        return ResponseUtils.ok(userRolesService.grantRole(userId, roleId));
    }

    @DeleteMapping(value = "/v1/user-role/{userId}/{roleId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    @HasPermission(scope = Scope.DELETE)
    public ResponseEntity deleteRole(@PathVariable Long roleId, @PathVariable Long userId) throws BaseAppException {
        return ResponseUtils.ok(userRolesService.deleteRole(userId, roleId));
    }

}
