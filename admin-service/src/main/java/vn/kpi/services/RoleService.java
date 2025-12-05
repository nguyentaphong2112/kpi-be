/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.kpi.services;

import org.springframework.http.ResponseEntity;
import vn.kpi.exceptions.BaseAppException;
import vn.kpi.exceptions.RecordNotExistsException;
import vn.kpi.models.BaseDataTableDto;
import vn.kpi.models.request.RoleRequest;
import vn.kpi.models.response.RoleResponse;

import java.util.List;

/**
 * Lop interface service ung voi bang sys_roles
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

public interface RoleService {

    BaseDataTableDto<RoleResponse.SearchResult> searchData(RoleRequest.SearchForm dto);

    Long saveData(RoleRequest.SubmitForm dto, Long id) throws BaseAppException;

    boolean deleteData(Long id) throws RecordNotExistsException;

    RoleResponse.DetailBean getDataById(Long id) throws RecordNotExistsException;

    ResponseEntity<Object> exportData(RoleRequest.SearchForm dto) throws Exception;

    boolean grantPermissions(RoleRequest.GrantPermissionForm dto, Long roleId) throws RecordNotExistsException;

    List<RoleResponse.TreeDto> initTreePermissions();

    List<String> getSelectedPermissions(Long roleId);

    List<RoleResponse.DetailBean> getListRoles();
}
