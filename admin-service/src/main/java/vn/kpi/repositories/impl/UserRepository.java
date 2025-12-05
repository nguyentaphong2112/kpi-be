package vn.kpi.repositories.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import vn.kpi.constants.BaseConstants;
import vn.kpi.feigns.HrmFeignClient;
import vn.kpi.models.BaseDataTableDto;
import vn.kpi.models.dto.MenuDto;
import vn.kpi.models.dto.UserDto;
import vn.kpi.models.dto.UserRoleDomainDto;
import vn.kpi.models.request.UserRequest;
import vn.kpi.models.response.UserResponse;
import vn.kpi.repositories.BaseRepository;
import vn.kpi.repositories.entity.UserEntity;
import vn.kpi.utils.QueryUtils;
import vn.kpi.utils.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Repository
public class UserRepository extends BaseRepository {
    private final HrmFeignClient hrmFeignClient;

    public UserRepository(HrmFeignClient hrmFeignClient) {
        super();
        this.hrmFeignClient = hrmFeignClient;
    }

    public UserDto getUser(String loginName) {
        String sql = """
                select su.* from sys_users su
                where (lower(su.login_name) like :loginName
                    or lower(su.id_no) like :loginName
                )
                and su.is_deleted = :noDeleted
                """;
        Map params = new HashMap<>();
        params.put("loginName", loginName.toLowerCase());
        params.put("noDeleted", BaseConstants.STATUS.NOT_DELETED);
        return getFirstData(sql, params, UserDto.class);
    }

    public UserEntity getUserEntity(String loginName) {
        String sql = """
                select su.* from sys_users su
                where lower(su.login_name) like :loginName
                and su.is_deleted = :noDeleted
                """;
        Map params = new HashMap<>();
        params.put("loginName", loginName.toLowerCase());
        params.put("noDeleted", BaseConstants.STATUS.NOT_DELETED);
        return getFirstData(sql, params, UserEntity.class);
    }

    public List<MenuDto> getMenus(String userNameLogin) {
        String sql;
        if ("guest".equalsIgnoreCase(userNameLogin)) {
            //neu la user khach thi chi hien thi menu khach
            sql = """
                    select a.resource_id menuId, a.code menuCode, a.name menuName, a.url menuUri,
                        a.parent_id parentId, a.icon 
                        from sys_resources a
                        where a.is_menu = 'Y'
                        and exists (
                            select 1 from sys_role_permissions rp, sys_permissions p, sys_roles sr
                            	where rp.permission_id = p.permission_id
                            	and p.resource_id = a.resource_id
                            	and rp.role_id = sr.role_id
                            	and sr.code = 'GUEST'
                            	and p.is_deleted = 'N'
                            	and rp.is_deleted = 'N'
                        )
                        order by path_order
                    """;
        } else {
            sql = """
                    select a.resource_id menuId, a.code menuCode, a.name menuName, a.url menuUri,
                        a.parent_id parentId, a.icon 
                        from sys_resources a
                        where a.is_menu = 'Y'
                        and exists (
                            select 1 from sys_users su, sys_user_roles ur,
                            	sys_role_permissions rp, sys_permissions p
                            	where lower(su.login_name) like lower(:userName)
                            	and su.user_id = ur.user_id
                            	and ur.role_id = rp.role_id
                            	and rp.permission_id = p.permission_id
                            	and p.resource_id = a.resource_id
                            	and p.is_deleted = 'N'
                            	and rp.is_deleted = 'N'
                            	and ur.is_deleted = 'N'
                            	and su.is_deleted = 'N'
                        )
                        order by path_order
                    """;
        }
        Map params = new HashMap();
        params.put("userName", userNameLogin);
        return getListData(sql, params, MenuDto.class);
    }

    public BaseDataTableDto<UserResponse.SearchResult> searchData(UserRequest.SearchForm dto) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    a.* from sys_users a
                    where a.is_deleted = 'N'
                """);
        HashMap<String, Object> params = new HashMap<>();
//        QueryUtils.filter(dto.getLoginName(), sql, params, "a.login_name");
//        QueryUtils.filter(dto.getFullName(), sql, params, "a.full_name");
        QueryUtils.filter(dto.getKeySearch(), sql, params, "a.login_name", "a.full_name");
        return getListPagination(sql.toString(), params, dto, UserResponse.SearchResult.class);
    }


    public BaseDataTableDto<UserResponse.SearchResult> getUserInfo(UserRequest.SearchForm dto) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    a.* from sys_users a
                    where a.is_deleted = 'N'
                    and a.login_name in (:loginName)
                """);
        HashMap<String, Object> params = new HashMap<>();
        params.put("loginName", dto.getLoginName());
        return getListPagination(sql.toString(), params, dto, UserResponse.SearchResult.class);
    }

    public List<MenuDto> getListPermissions(String userName) {
        if ("guest".equalsIgnoreCase(userName)) {
            String sql = """
                    select 
                    	sc.`code` scope,
                    	sr.`code` as menuCode,
                    	sr.resource_id menuId, sr.name menuName, sr.url menuUri,
                        sr.parent_id parentId, sr.icon,
                        sr.is_menu as isMenu
                    from sys_permissions p, sys_scopes sc, sys_resources sr 
                    where p.resource_id = sr.resource_id
                    and sc.scope_id = p.scope_id
                    and exists (
                    	select 1 from sys_role_permissions rp, sys_roles u
                    	where rp.permission_id = p.permission_id
                    	and rp.role_id = u.role_id
                    	and u.code = :roleGuest
                    	and rp.is_deleted = 'N'
                    )
                    and not exists (
                          select 1 from sys_resources rc1
                          where sr.path_id like concat(rc1.path_id, '%')
                          and rc1.status = 'INACTIVE'
                    )
                    and p.is_deleted = 'N' and sr.is_deleted = 'N'
                    order by sr.path_order, sr.`code`
                    """;
            HashMap<String, Object> params = new HashMap<>();
            params.put("roleGuest", "GUEST");
            return getListData(sql, params, MenuDto.class);
        } else {
            String sql = """
                    select 
                    	sc.`code` scope,
                    	sr.`code` as menuCode,
                    	sr.resource_id menuId, sr.name menuName, sr.url menuUri,
                        sr.parent_id parentId, sr.icon,
                        sr.is_menu as isMenu
                    from sys_permissions p, sys_scopes sc, sys_resources sr 
                    where p.resource_id = sr.resource_id
                    and sc.scope_id = p.scope_id
                    and exists (
                    	select 1 from sys_role_permissions rp,
                    		sys_user_roles ur, sys_users u
                    	where rp.permission_id = p.permission_id
                    	and rp.role_id = ur.role_id
                    	and ur.user_id = u.user_id
                    	and u.login_name = :userName
                    	and rp.is_deleted = 'N'
                    	and ur.is_deleted = 'N'
                    )
                    and not exists (
                          select 1 from sys_resources rc1
                          where sr.path_id like concat(rc1.path_id, '%')
                          and rc1.status = 'INACTIVE'
                    )
                    and p.is_deleted = 'N' and sr.is_deleted = 'N'
                    order by sr.path_order, sr.`code`
                    """;
            HashMap<String, Object> params = new HashMap<>();
            params.put("userName", userName);
            return getListData(sql, params, MenuDto.class);
        }
    }

    public boolean hasPermission(String userName, String scope, String resource) {
        String sql = """
                select 
                	p.permission_id
                from sys_permissions p, sys_role_permissions rp,
                	sys_user_roles ur, sys_resources rc, sys_scopes sc
                where ur.role_id = rp.role_id
                and rp.permission_id = p.permission_id
                and rp.is_deleted = 'N'
                and p.is_deleted = 'N'
                and ur.is_deleted = 'N'
                and p.resource_id = rc.resource_id
                and p.scope_id = sc.scope_id
                and ur.user_id in (
                	select user_id from sys_users su
                	where su.is_deleted = 'N'
                	and su.login_name = :userName
                )
                and sc.`code` = :scope
                and rc.`code` = :resource
                """;
        HashMap<String, Object> params = new HashMap<>();
        params.put("userName", userName);
        params.put("scope", scope);
        params.put("resource", resource);
        return getFirstData(sql, params, Long.class) != null;
    }

    public List<UserRoleDomainDto> getPermissionData(String userName, String scope, String resource) {
        UserEntity userEntity = getUserEntity(userName);
        String sql = """
                select 
                    concat(urd.key_order, '-', urd.user_role_id) key_order,
                    urd.domain_type,
                    urd.domain_id
                from sys_permissions p, sys_role_permissions rp,
                	sys_user_roles ur, sys_resources rc, sys_scopes sc,
                	sys_user_role_domains urd,
                	sys_domains dm
                where ur.role_id = rp.role_id
                and rp.permission_id = p.permission_id
                and rp.is_deleted = 'N'
                and p.is_deleted = 'N'
                and ur.is_deleted = 'N'
                and p.resource_id = rc.resource_id
                and p.scope_id = sc.scope_id
                and ur.user_id = :userId
                and dm.domain_type = urd.domain_type
                and dm.domain_id = urd.domain_id
                and sc.`code` = :scope
                and rc.`code` = :resource
                and urd.is_deleted = 'N'
                and urd.user_role_id = ur.user_role_id
                order by dm.path_order, dm.domain_id
                """;
        HashMap<String, Object> params = new HashMap<>();
        params.put("userId", userEntity.getUserId());
        params.put("scope", scope);
        params.put("resource", resource);
        List<UserRoleDomainDto> results = getListData(sql, params, UserRoleDomainDto.class);
        //Lay role mac dinh
        if (!Utils.isNullOrEmpty(userEntity.getEmployeeCode())) {
            results.addAll(getGrantedDomainDefault("DON_VI", userEntity, scope, resource));
        }

        return results;
    }

    private List<UserRoleDomainDto> getGrantedDomainDefault(String donVi, UserEntity userEntity, String scope, String resource) {
        String sql = """
                select min(sr.default_domain_value) as org_level_manage
                from sys_permissions p, sys_role_permissions rp,
                	sys_user_roles ur, sys_resources rc,
                	sys_scopes sc, sys_roles sr
                where p.permission_id = rp.permission_id
                and rp.role_id = sr.role_id
                and rp.role_id = ur.role_id
                and ur.user_id = :userId
                and p.scope_id = sc.scope_id
                and p.resource_id = rc.resource_id
                and rp.is_deleted = 'N'
                and ur.is_deleted = 'N'
                and sr.is_deleted = 'N'
                and sc.`code` = :scope
                and rc.`code` = :resource
                and sr.default_domain_type = 'CAP_DV_QUAN_LY';
                """;
        HashMap<String, Object> params = new HashMap<>();
        params.put("userId", userEntity.getUserId());
        params.put("scope", scope);
        params.put("resource", resource);
        String orgLevelManage = queryForObject(sql, params, String.class);
        List results = new ArrayList<>();
        if (!Utils.isNullOrEmpty(orgLevelManage)) {
            Long orgId = hrmFeignClient.getOrgByOrgLevelManage(Utils.getHeader(), userEntity.getEmployeeCode(), Long.valueOf(orgLevelManage)).getData();
            if (orgId != null) {
                UserRoleDomainDto urd = new UserRoleDomainDto();
                urd.setDomainType(donVi);
                urd.setDomainId(String.valueOf(orgId));
                urd.setKeyOrder("default_domain");
                results.add(urd);
            }
        }
        return results;
    }

    /**
     * Lấy các cấp được phân quyền cho đơn vị
     *
     * @param scope
     * @param resource
     * @param orgId
     * @param domainType
     * @return
     */
    public List<Long> getGrantedDomain(String scope, String resource, Long orgId, String domainType) {
        String sql = """
                select a.domain_id from sys_domains a
                where exists (
                	select 1 from sys_user_role_domains urd ,
                		sys_role_permissions rp , sys_user_roles ur, sys_permissions p, sys_scopes cp, sys_resources rc
                	where urd.domain_id = a.domain_id
                	and urd.domain_type = urd.domain_type
                	and urd.user_role_id = ur.user_role_id
                	and ur.role_id = rp.role_id
                	and rp.permission_id = p.permission_id
                	and urd.is_deleted = 'N'
                	and rp.is_deleted = 'N'
                	and ur.is_deleted = 'N'
                	and p.scope_id = cp.scope_id
                	and p.resource_id = rc.resource_id
                	and p.is_deleted = 'N'
                	and cp.`code` = :scope
                	and rc.`code` = :resourceCode
                )
                and a.domain_type = :domainType
                and (select path_id from sys_domains where domain_type = :domainType and domain_id = :orgId) like concat(a.path_id,'%')
                order by a.path_order desc
                """;
        HashMap<String, Object> params = new HashMap<>();
        params.put("scope", scope);
        params.put("domainType", domainType);
        params.put("resourceCode", resource);
        params.put("pathId", "%/" + orgId + "/%");
        params.put("orgId", orgId);
        return getListData(sql, params, Long.class);
    }

    public Map<String, Long> getMapLoginNameByList(List<String> loginNameList) {
        String sql = """
                select user_id, login_name
                from sys_users
                where is_deleted = 'N' and login_name in (:loginNames)
                """;
        Map<String, Object> params = new HashMap<>();
        params.put("loginNames", loginNameList);
        List<UserResponse.DetailBean> dataList = getListData(sql, params, UserResponse.DetailBean.class);

        return dataList.stream().collect(Collectors.toMap(UserResponse.DetailBean::getLoginName, UserResponse.DetailBean::getUserId));
    }

    public List<String> getRoleCodeList(String loginName) {
        String sql = """
                SELECT distinct r.code
                FROM sys_user_roles ur
                    JOIN sys_users u ON ur.user_id = u.user_id AND u.is_deleted = 'N'
                    JOIN sys_roles r ON ur.role_id = r.role_id and r.is_deleted = 'N'
                WHERE u.login_name = :loginName and ur.is_deleted = 'N'
                """;
        Map<String, Object> params = new HashMap<>();
        params.put("loginName", loginName);

        return getListData(sql, params, String.class);
    }

    public Boolean hasRole(String userName, String roleCode) {
        String sql = """
                select 
                	count(*)
                from sys_roles sr,
                	sys_user_roles ur
                where ur.role_id = sr.role_id
                and sr.is_deleted = 'N'
                and ur.is_deleted = 'N'
                and upper(sr.`code`) = :scope
                """;
        HashMap<String, Object> params = new HashMap<>();
        params.put("userName", userName);
        params.put("roleCode", roleCode.toUpperCase());
        return queryForObject(sql, params, Long.class) > 0;
    }
}
