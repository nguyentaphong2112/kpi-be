/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.kpi.repositories.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import vn.kpi.constants.BaseConstants;
import vn.kpi.models.BaseDataTableDto;
import vn.kpi.models.dto.UserRoleDomainDto;
import vn.kpi.models.request.UserRoleRequest;
import vn.kpi.models.response.UserRoleResponse;
import vn.kpi.repositories.BaseRepository;
import vn.kpi.utils.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Lop repository Impl ung voi bang sys_user_roles
 *
 * @author tudd
 * @version 1.0
 * @since 1.0
 */

@Repository
@RequiredArgsConstructor
public class UserRoleRepository extends BaseRepository {

    public BaseDataTableDto searchData(UserRoleRequest.SearchForm dto) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    a.user_role_id,
                    a.user_id,
                    a.role_id,
                    a.is_deleted,
                    a.created_by,
                    a.created_time,
                    a.modified_by,
                    a.modified_time,
                    a.last_update_time
                """);
        HashMap<String, Object> params = new HashMap<>();
        addCondition(sql, params, dto);
        return getListPagination(sql.toString(), params, dto, UserRoleResponse.class);
    }

    public List<Map<String, Object>> getListExport(UserRoleRequest.SearchForm dto) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    a.user_role_id,
                    a.user_id,
                    a.role_id,
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

    private void addCondition(StringBuilder sql, Map<String, Object> params, UserRoleRequest.SearchForm dto) {
        sql.append("""
                    FROM sys_user_roles a
                    
                    
                    WHERE IFNULL(a.is_deleted, :activeStatus) = :activeStatus
                """);
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        //QueryUtils.filterGe(dto.getFromDate(), sql, params, "a.from_date", "fromDate");
        //QueryUtils.filterLe(dto.getToDate(), sql, params, "a.from_date", "toDate");
        sql.append(" ORDER BY a.name");
    }

    public void inactiveRoleNotIn(Long userId, List<Long> roleIds) {
        if (roleIds.isEmpty()) {
            roleIds.add(-1L);
        }
        String sql = """
                update sys_user_roles a
                set a.is_deleted = 'Y', a.modified_by = :userName, a.modified_time = now()
                where a.is_deleted = 'N'
                and a.user_id = :userId
                and a.role_id not in (:roleIds) 
                """;
        Map map = new HashMap();
        map.put("userId", userId);
        map.put("roleIds", roleIds);
        map.put("userName", Utils.getUserNameLogin());
        executeSqlDatabase(sql, map);
    }
    public void inactiveRoleIn(Long userId, List<Long> roleIds) {
        String sql = """
                update sys_user_roles a
                set a.is_deleted = 'Y', a.modified_by = :userName, a.modified_time = now()
                where a.is_deleted = 'N'
                and a.user_id = :userId
                and a.role_id in (:roleIds) 
                """;
        Map map = new HashMap();
        map.put("userId", userId);
        map.put("roleIds", roleIds);
        map.put("userName", Utils.getUserNameLogin());
        executeSqlDatabase(sql, map);
    }

    public void grantRoles(Long userId, List<Long> roleIds) {
        String[] sql = new String[]{"""
                update sys_user_roles a
                set a.is_deleted = 'N', a.modified_by = :userName, a.modified_time = now()
                where a.is_deleted = 'Y'
                and a.user_id = :userId
                and a.role_id in (:roleIds) 
                """,
                """
                insert into sys_user_roles(user_role_id, user_id, role_id,
                    is_deleted, created_by, created_time
                ) select null, :userId, a.role_Id, 'N', :userName, now()
                from sys_roles a
                where a.is_deleted = 'N'
                and a.role_id in (:roleIds)
                and not exists (
                    select 1 from sys_user_roles b
                    where b.user_id = :userId
                    and b.role_id = a.role_id
                )
                """};
        Map map = new HashMap();
        map.put("userId", userId);
        map.put("roleIds", roleIds);
        map.put("userName", Utils.getUserNameLogin());
        executeSqlDatabase(sql, map);
    }

    public void grantRolesToUser(List<Long> userIds, Long roleId) {
        String[] sql = new String[]{"""
                update sys_user_roles a 
                set a.is_deleted = 'N', a.modified_by = :userName, a.modified_time = now()
                where a.is_deleted = 'Y'
                and a.user_id IN (:userIds)
                and a.role_id = :roleId 
                """,
                """
                insert into sys_user_roles(user_role_id, user_id, role_id,
                    is_deleted, created_by, created_time
                ) select null, a.user_id, :roleId, 'N', :userName, now()
                from sys_users a
                where a.is_deleted = 'N'
                and a.user_id IN (:userIds)
                and not exists (
                    select 1 from sys_user_roles b
                    where b.role_id = :roleId
                    and b.user_id = a.user_id
                )
                """};
        Map map = new HashMap();
        map.put("userIds", userIds);
        map.put("roleId", roleId);
        map.put("userName", Utils.getUserNameLogin());
        executeSqlDatabase(sql, map);
    }

    public void inactiveDomainNotIn(Long userId, Long roleId, List<List<UserRoleRequest.DomainDataBean>> domains) {
        Map map = new HashMap();
        map.put("userId", userId);
        map.put("roleId", roleId);
        map.put("userName", Utils.getUserNameLogin());

        StringBuilder sql = new StringBuilder("""
                update sys_user_role_domains a
                set a.is_deleted = 'Y', a.modified_by = :userName, a.modified_time = now()
                where a.user_role_id in (
                    select user_role_id from sys_user_roles b
                    where b.user_id = :userId
                        and b.role_id = :roleId
                )     
                and a.is_deleted = 'N'                            
                """);
        List<Object[]> keyList = getKeyList(domains);
        if (!keyList.isEmpty()) {
            sql.append(" and (domain_type, domain_id, key_order) not in (:keyList)");
            map.put("keyList", keyList);
        }
        executeSqlDatabase(sql.toString(), map);
    }

    public void activeDomainIn(Long userId, Long roleId, List<List<UserRoleRequest.DomainDataBean>> domains) {
        Map map = new HashMap();
        map.put("userId", userId);
        map.put("roleId", roleId);
        map.put("userName", Utils.getUserNameLogin());

        StringBuilder sql = new StringBuilder("""
                update sys_user_role_domains a
                set a.is_deleted = 'N', a.modified_by = :userName, a.modified_time = now()
                where a.user_role_id in (
                    select user_role_id from sys_user_roles b
                    where b.user_id = :userId
                        and b.role_id = :roleId
                )     
                and a.is_deleted = 'Y'                            
                """);
        List<Object[]> keyList = getKeyList(domains);
        if (!keyList.isEmpty()) {
            sql.append(" and (domain_type, domain_id, key_order) in (:keyList)");
            map.put("keyList", keyList);
        }
        executeSqlDatabase(sql.toString(), map);
    }

    private static List<Object[]> getKeyList(List<List<UserRoleRequest.DomainDataBean>> domains) {
        List<Object[]> keyList = new ArrayList<>();
        if(Utils.isNullOrEmpty(domains)){
            return keyList;
        }
        int key = 0;
        for (List<UserRoleRequest.DomainDataBean> row : domains) {
            if (!Utils.isNullOrEmpty(row)) {
                final int temp = key;
                row.forEach(item -> {
                    if (item.getDomainIds() != null) {
                        item.getDomainIds().forEach(id -> {
                            keyList.add(new Object[]{ item.getDomainType(), id, String.format("%02d", temp)});
                        });
                    }
                });
                key++;
            }
        }
        return keyList;
    }

    public void insertRoleDomains(Long userId, Long roleId, List<List<UserRoleRequest.DomainDataBean>> domains) {

        String sql = """
                insert into sys_user_role_domains(user_role_domain_id, user_role_id, domain_type,
                    domain_id, key_order, is_deleted, created_by, created_time)
                select null, a.user_role_id, :domainType, :domainId, :key, 'N',
                    :userName, now()
                from sys_user_roles a
                where a.user_id = :userId
                    and a.role_id = :roleId
                    and not exists (
                        select 1 from sys_user_role_domains b
                        where b.user_role_id = a.user_role_id
                        and b.domain_type = :domainType
                        and b.domain_id = :domainId
                        and b.key_order = :key
                    )
                """;
        List<Map> listMapParams = new ArrayList<>();
        int key = 0;
        for (List<UserRoleRequest.DomainDataBean> row : domains) {
            if (!Utils.isNullOrEmpty(row)) {
                final int temp = key;
                row.forEach(item -> {
                    if (item.getDomainIds() != null) {
                        item.getDomainIds().forEach(id -> {
                            Map mapParams = new HashMap();
                            mapParams.put("userId", userId);
                            mapParams.put("roleId", roleId);
                            mapParams.put("userName", Utils.getUserNameLogin());
                            mapParams.put("key", String.format("%02d", temp));
                            mapParams.put("domainType", item.getDomainType());
                            mapParams.put("domainId", id);
                            listMapParams.add(mapParams);
                        });
                    }
                });
                key++;
            }
        }
        if (!listMapParams.isEmpty()) {
            executeBatch(sql, listMapParams);
        }
    }

    public List<UserRoleDomainDto> getUserRoleDomains(Long userId) {
        String sql = """
                select
                    sr.role_id, sr.name as roleName,
                    a.domain_type, a.domain_id, a.key_order,
                    (select name from sys_domains where domain_type = a.domain_type and domain_id = a.domain_id) as domain_name 
                from sys_roles sr,sys_user_roles ur 
                left join sys_user_role_domains a on ur.user_role_id = a.user_role_id and a.is_deleted = 'N'
                where ur.user_id = :userId
                and sr.role_id = ur.role_id
                and sr.is_deleted = 'N'
                and ur.is_deleted = 'N'
                order by sr.name, a.key_order
                """;
        Map map = new HashMap<>();
        map.put("userId", userId);
        return getListData(sql, map, UserRoleDomainDto.class);
    }
}
