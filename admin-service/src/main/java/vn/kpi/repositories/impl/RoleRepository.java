/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.kpi.repositories.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import vn.kpi.constants.BaseConstants;
import vn.kpi.models.BaseDataTableDto;
import vn.kpi.models.request.RoleRequest;
import vn.kpi.models.response.RoleResponse;
import vn.kpi.repositories.BaseRepository;
import vn.kpi.utils.QueryUtils;
import vn.kpi.utils.Utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Lop repository Impl ung voi bang sys_roles
 *
 * @author tudd
 * @version 1.0
 * @since 1.0
 */

@Repository
@RequiredArgsConstructor
public class RoleRepository extends BaseRepository {

    public BaseDataTableDto searchData(RoleRequest.SearchForm dto) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    a.role_id,
                    a.code,
                    a.name,
                    a.default_domain_type,
                    a.default_domain_value,
                    a.is_deleted,
                    a.created_by,
                    a.created_time,
                    a.modified_by,
                    a.modified_time,
                    a.last_update_time
                """);
        HashMap<String, Object> params = new HashMap<>();
        addCondition(sql, params, dto);
        return getListPagination(sql.toString(), params, dto, RoleResponse.SearchResult.class);
    }

    public List<Map<String, Object>> getListExport(RoleRequest.SearchForm dto) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    a.role_id,
                    a.code,
                    a.name,
                    a.domain_level,
                    a.is_deleted,
                    a.created_by,
                    a.created_time,
                    a.modified_by,
                    a.modified_time,
                    a.last_update_time
                """);
        Map<String, Object> params = new HashMap<>();
        addCondition(sql, params, dto);
        return getListData(sql.toString(), params);
    }

    private void addCondition(StringBuilder sql, Map<String, Object> params, RoleRequest.SearchForm dto) {
        sql.append("""
                    FROM sys_roles a
                    WHERE IFNULL(a.is_deleted, :activeStatus) = :activeStatus
                """);
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        QueryUtils.filter(dto.getKeySearch(), sql, params, "a.code", "a.name");
        sql.append(" ORDER BY a.name, a.role_id");
    }

    public void insertRolePermissions(Long roleId, List<Long> permissionIds) {
        String sqlUpdate = """
                    update sys_role_permissions sp 
                    set sp.is_deleted = 'N', modified_by = :userName, modified_time = now()
                    where sp.role_id = :roleId
                    and sp.permission_id in (:permissionIds)
                    and sp.is_deleted = 'Y'
                """;
        String sqlInsert = """
                insert into sys_role_permissions(role_permission_id, role_id, permission_id, is_deleted, created_by, created_time)
                select null, :roleId, p.permission_id, 'N', :userName, now()
                from sys_permissions p
                where p.permission_id in (:permissionIds)  
                and not exists (
                	select 1 from sys_role_permissions sp
                	where sp.role_id = :roleId
                	and sp.permission_id = p.permission_id
                )
                """;
        Map map = new HashMap();
        map.put("roleId", roleId);
        map.put("userName", Utils.getUserNameLogin());
        map.put("permissionIds", permissionIds);
        executeSqlDatabase(new String[]{sqlUpdate, sqlInsert}, map);
    }

    public List<RoleResponse.TreeDto> getListResources() {
        String sql = "select " +
                     "       concat('R_', rc.resource_id) as nodeId," +
                     "       rc.code as code," +
                     "       rc.name as name," +
                     "       rc.is_menu as isMenu," +
                     "       case " +
                     "           when rc.parent_id is null then null " +
                     "           else concat('R_', rc.parent_id) " +
                     "       end parent_id" +
                     "   from sys_resources rc " +
                     " where rc.is_deleted = 'N'" +
                     " and not exists (" +
                     "      select 1 from sys_resources rc1" +
                     "      where rc.path_id like concat(rc1.path_id, '%')" +
                     "      and rc1.status = 'INACTIVE'" +
                     " )" +
                     "   order by rc.path_order";
        Map map = new HashMap();
        return getListData(sql, map, RoleResponse.TreeDto.class);

    }

    public List<RoleResponse.TreeDto> getListPermissions() {
        String sql = "select " +
                     "       concat('P_', rc.permission_id) as nodeId," +
                     "       concat(sp.code, '_', sr.code) as code," +
                     "       rc.name as name," +
                     "       concat('R_', rc.resource_id) as parentId" +
                     "   from sys_permissions rc, sys_scopes sp, sys_resources sr " +
                     " where rc.is_deleted = 'N'" +
                     "   and rc.resource_id = sr.resource_id" +
                     "   and sp.scope_id = rc.scope_id" +
                     "   order by rc.name, sp.order_number";
        Map map = new HashMap();
        return getListData(sql, map, RoleResponse.TreeDto.class);
    }

    public List<String> getSelectedResources(Long roleId) {
        String sql = """
                select concat('R_', r.resource_id) as nodeId
                from sys_resources r 
                where r.is_deleted = 'N'
                and exists (
                    select 1 from sys_role_permissions rp, sys_permissions p
                    where p.permission_id = rp.permission_id
                    and rp.role_id = :roleId
                    and r.resource_id = p.resource_id
                    and rp.is_deleted = 'N'
                    and p.is_deleted = 'N'
                )
                """;
        Map map = new HashMap();
        map.put("roleId", roleId);

        return getListData(sql, map, String.class);
    }

    public List<String> getSelectedPermissions(Long roleId) {
        String sql = """
                    select concat('P_', rp.permission_id) as nodeId from sys_role_permissions rp
                    where rp.role_id = :roleId
                    and rp.is_deleted = 'N'
                """;
        Map map = new HashMap();
        map.put("roleId", roleId);

        return getListData(sql, map, String.class);
    }

    public void inactivePermissionNotIn(Long roleId, List<Long> permissionIds) {
        if (permissionIds.isEmpty()) {
            permissionIds.add(-1L);
        }
        String sql = """
                update sys_role_permissions sp
                set sp.is_deleted = 'Y', sp.modified_by = :userName, sp.modified_time = now()
                where sp.is_deleted = 'N'
                and sp.role_id = :roleId
                and sp.permission_id not in (:permissionIds) 
                """;
        Map map = new HashMap();
        map.put("roleId", roleId);
        map.put("userName", Utils.getUserNameLogin());
        map.put("permissionIds", permissionIds);
        executeSqlDatabase(sql, map);
    }

    public List<RoleResponse.DetailBean> getListRoles() {
        String sql = """
                SELECT
                    a.role_id,
                    a.code,
                    a.name from sys_roles a 
                    where a.is_deleted = 'N'
                    order by a.name
                """;
        return getListData(sql, new HashMap<>(), RoleResponse.DetailBean.class);
    }
}
