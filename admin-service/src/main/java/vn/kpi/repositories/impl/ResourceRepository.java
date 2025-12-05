/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.kpi.repositories.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import vn.kpi.constants.BaseConstants;
import vn.kpi.constants.Scope;
import vn.kpi.models.BaseDataTableDto;
import vn.kpi.models.TreeDto;
import vn.kpi.models.request.ResourceRequest;
import vn.kpi.models.response.ResourceResponse;
import vn.kpi.repositories.BaseRepository;
import vn.kpi.repositories.entity.ResourceEntity;
import vn.kpi.utils.QueryUtils;
import vn.kpi.utils.Utils;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Lop repository Impl ung voi bang sys_resources
 *
 * @author tudd
 * @version 1.0
 * @since 1.0
 */

@Repository
@RequiredArgsConstructor
public class ResourceRepository extends BaseRepository {
    private final ScopeRepository scopeRepository;

    public BaseDataTableDto searchData(ResourceRequest.SearchForm dto) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    a.resource_id,
                    a.parent_id,
                    (select name from sys_resources p where p.resource_id = a.parent_id) as parentName,
                    ifnull((select 'Y' from sys_role_permissions rp, sys_permissions p
                                 where rp.is_deleted = 'N'
                                  and p.is_deleted = 'N'
                                  and p.resource_id = a.resource_id
                                  limit 1
                    ),'N') as usedStatus,
                    a.name,
                    a.code,
                    a.url,
                    a.icon,
                    a.status,
                    a.is_menu,
                    a.order_number,
                    a.is_deleted,
                    a.created_by,
                    a.created_time,
                    a.modified_by,
                    a.modified_time,
                    a.last_update_time
                """);
        HashMap<String, Object> params = new HashMap<>();
        addCondition(sql, params, dto);
        return getListPagination(sql.toString(), params, dto, ResourceResponse.SearchResult.class);
    }

    public List<Map<String, Object>> getListExport(ResourceRequest.SearchForm dto) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    a.resource_id,
                    a.parent_id,
                    a.name,
                    a.code,
                    a.url,
                    a.is_menu,
                    a.order_number,
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

    private void addCondition(StringBuilder sql, Map<String, Object> params, ResourceRequest.SearchForm dto) {
        sql.append("""
                    FROM sys_resources a
                    WHERE IFNULL(a.is_deleted, :activeStatus) = :activeStatus
                """);
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        if (!Utils.isNullOrEmpty(dto.getKeySearch())) {
            sql.append(" and (lower(a.code) like :keySearch or lower(a.name) like :keySearch)");
            params.put("keySearch", "%" + dto.getKeySearch().toLowerCase() + "%");
        }
        if (!Utils.isNullOrEmpty(dto.getStatus())) {
            sql.append(" and a.status = :status");
            params.put("status", dto.getStatus());
        }
        if (dto.getParentId() != null) {
            sql.append(" and a.path_id like :pathId");
            params.put("pathId", "%/" + dto.getParentId() + "/%");
        }
        if (!Utils.isNullOrEmpty(dto.getUsedStatus())) {
            sql.append(MessageFormat.format(" and {0} (select 1 from sys_role_permissions rp, sys_permissions p" +
                            "  where rp.is_deleted = :activeStatus" +
                            "   and p.is_deleted = :activeStatus" +
                            "   and p.resource_id = a.resource_id)",
                    "Y".equals(dto.getUsedStatus()) ? "EXISTS" : "NOT EXISTS"));
        }
        QueryUtils.filter(dto.getUrl(), sql, params, "a.url");

        sql.append(" ORDER BY a.path_order");
    }

    public void inActiveScopeNotIn(Long resourceId, List<Long> scopeIds) {
        String sql = "update sys_permissions p " +
                " set p.is_deleted = 'Y'," +
                "   p.modified_by = :userName," +
                "   p.modified_time = now()" +
                " where p.is_deleted = 'N'" +
                "   and p.resource_id = :resourceId" +
                "   and p.scope_id not in (:scopeIds)";
        Map<String, Object> params = new HashMap<>();
        params.put("resourceId", resourceId);
        params.put("scopeIds", scopeIds);
        params.put("userName", Utils.getUserNameLogin());
        executeSqlDatabase(sql, params);
    }

    public void activeScopeIn(Long resourceId, List<Long> scopeIds) {
        String sql = "update sys_permissions p " +
                " set p.is_deleted = 'N'," +
                "   p.modified_by = :userName," +
                "   p.modified_time = now()" +
                " where p.is_deleted = 'Y'" +
                "   and p.resource_id = :resourceId" +
                "   and p.scope_id in (:scopeIds)";
        Map<String, Object> params = new HashMap<>();
        params.put("resourceId", resourceId);
        params.put("scopeIds", scopeIds);
        params.put("userName", Utils.getUserNameLogin());
        executeSqlDatabase(sql, params);
    }

    public void insertPermissions(Long resourceId, List<Long> scopeIds) {
        String sql = """
                insert into sys_permissions(permission_id, scope_id, resource_id, name, is_deleted, created_by, created_time)
                select
                	null, sp.scope_id, sc.resource_id, CONCAT(sp.`name`, ' ',sc.`name`) as name, 'N', :userName, NOW()
                from sys_scopes sp, sys_resources sc
                where sc.resource_id = :resourceId
                and sp.scope_id in (:scopeIds)
                and not exists (
                	select 1 from sys_permissions p
                	where p.resource_id = sc.resource_id
                	and p.scope_id = sp.scope_id
                )
                """;
        Map<String, Object> params = new HashMap<>();
        params.put("resourceId", resourceId);
        params.put("scopeIds", scopeIds);
        params.put("userName", Utils.getUserNameLogin());
        executeSqlDatabase(sql, params);
    }

    public List<Long> getScopeIdsByResource(Long id) {
        String sql = "select scope_id from sys_permissions sp " +
                " where sp.is_deleted = 'N'" +
                " and sp.resource_id = :id";
        Map<String, Object> params = new HashMap<>();
        params.put("id", id);
        return getListData(sql, params, Long.class);
    }

    public void updatePathInfo(ResourceEntity entity) {
        String[] sql = new String[]{
                "call proc_update_menu_path(:id)",
                "call proc_reset_menu_order()"
        };
        Map<String, Object> params = new HashMap<>();
        params.put("id", entity.getParentId());
        executeSqlDatabase(sql, params);
    }

    public List<TreeDto> getResourceRootNodes() {
        String sql = """
                SELECT
                    rc.resource_id nodeId,
                    rc.name,
                    rc.code,
                    rc.parent_id parentId,
                    rc.path_id
                FROM sys_resources rc 
                    WHERE rc.parent_id IS NULL
                    and rc.is_deleted = 'N'
                    order by rc.path_order
                """;
        return getListData(sql, new HashMap<>(), TreeDto.class);
    }

    public List<TreeDto> getAllResources() {
        String sql = """
                SELECT
                    rc.resource_id nodeId,
                    rc.name,
                    rc.code,
                    rc.parent_id parentId,
                    rc.path_id
                FROM sys_resources rc 
                    WHERE rc.is_deleted = 'N'
                    order by rc.path_order
                """;
        return getListData(sql, new HashMap<>(), TreeDto.class);
    }

    public List<TreeDto> getChildren(Long nodeId) {
        String sql = """
                SELECT
                    rc.resource_id nodeId,
                    rc.name,
                    rc.code,
                    rc.parent_id parentId,
                    rc.path_id
                FROM sys_resources rc 
                    WHERE rc.is_deleted = 'N'
                    and rc.parent_id = :nodeId
                    order by rc.path_order
                """;
        Map params = new HashMap();
        params.put("nodeId", nodeId);
        return getListData(sql, params, TreeDto.class);

    }

    public BaseDataTableDto<ResourceResponse.SearchTreeChooseResult> searchTreeChooser(ResourceRequest.TreeSearchRequest request) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    a.resource_id,
                    a.parent_id,
                    (select name from sys_resources p where p.resource_id = a.parent_id) as parentName,
                    a.name,
                    a.code
                from sys_resources a
                where a.is_deleted = 'N'
                """);
        HashMap<String, Object> params = new HashMap<>();
        if (request.getParentId() != null) {
            sql.append(" and a.parent_id = :parentId");
            params.put("parentId", request.getParentId());
        }
        if (!Utils.isNullOrEmpty(request.getKeySearch())) {
            sql.append(" and (lower(a.code) like :keySearch or lower(a.name) like :keySearch)");
            params.put("keySearch", request.getKeySearch().toLowerCase());
        }
        return getListPagination(sql.toString(), params, request, ResourceResponse.SearchTreeChooseResult.class);
    }

    public List<Long> getPermissionViewIds(List<Long> resourceIds, List<Long> permissionIds) {
        StringBuilder sql = new StringBuilder("""
                select permission_id from sys_permissions p
                where p.resource_id in (:resourceIds)
                and p.scope_id in (:scopeViewId)
                """);
        HashMap<String, Object> params = new HashMap<>();
        params.put("scopeViewId", scopeRepository.getScopeId(Scope.VIEW));
        params.put("resourceIds", resourceIds);
        if (!permissionIds.isEmpty()) {
            sql.append(" and p.resource_id not in (" +
                    "   select p1.resource_id from sys_permissions p1" +
                    "   where p1.permission_id in (:permissionIds)" +
                    ")");
            params.put("permissionIds", permissionIds);
        }
        return getListData(sql.toString(), params, Long.class);
    }

    public boolean checkResourceUsed(Long resourceId) {
        String sql = """
                    SELECT COUNT(1)
                    FROM sys_role_permissions rp, sys_permissions p
                    WHERE rp.is_deleted = 'N'
                    AND p.is_deleted = 'N'
                    AND rp.permission_id = p.permission_id
                    AND p.resource_id = :resourceId
                """;
        HashMap<String, Object> params = new HashMap<>();
        params.put("resourceId", resourceId);
        return queryForObject(sql, params, Integer.class) > 0;
    }
}
